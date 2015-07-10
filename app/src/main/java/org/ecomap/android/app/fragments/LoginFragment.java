package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.Fragment;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginFragment extends DialogFragment {

    AutoCompleteTextView email;
    EditText password;
    Button signIn;
    TextView signUpLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_layout, container, false);
        getDialog().setTitle("Sign In");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        email = (AutoCompleteTextView) getView().findViewById(R.id.email);
        password = (EditText) getView().findViewById(R.id.password);
        signIn = (Button) getView().findViewById(R.id.email_sign_in_button);
        signUpLink = (TextView) getView().findViewById(R.id.link_to_register);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask(getActivity()).execute();
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switch to Registration Fragment
                new RegistrationFragment().show(getFragmentManager(), "registration_layout");
                dismiss();
            }
        });
    }

    private class LoginTask extends AsyncTask {
        String resMessage;
        Context mContext;
        ProgressDialog progressBar;

        public LoginTask(Context context) {
            this.mContext = context;
            resMessage = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar = new ProgressDialog(mContext);
            progressBar.setMessage("Connecting to Ecomap Server");
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);
            progressBar.show();
        }

        @Override
        protected Void doInBackground(Object[] params) {
            URL url = null;
            HttpURLConnection connection = null;

            try {
                url = new URL(MainActivity.API_URL + "/login");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.connect();

                //validation
                if (MainActivity.isEmailValid(email.getText().toString()) && (! password.getText().toString().isEmpty())) {

                    //creating JSONObject for request
                    JSONObject request = new JSONObject();
                    request.put("email", email.getText());
                    request.put("password", password.getText());

                    //sending request
                    connection.getOutputStream().write(request.toString().getBytes("UTF-8"));

                    //handling result from server
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        StringBuilder responseBody = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line + "\n");
                        }
                        reader.close();

                        JSONObject data = new JSONObject(responseBody.toString());
                        MainActivity.setUserFirstName(data.get("first_name").toString());
                        MainActivity.setUserSecondName(data.get("last_name").toString());
                        MainActivity.setUserId(MainActivity.cookieManager.getCookieStore().getCookies().toString());
                        MainActivity.setUserIsAuthorized(true);

                        resMessage = "Hello " + MainActivity.getUserFirstName() + " " + MainActivity.getUserSecondName() + "!";

                    } else {

                        StringBuilder responseBody = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line + "\n");
                        }
                        reader.close();

                        JSONObject data = new JSONObject(responseBody.toString());
                        resMessage = data.get("message").toString();
                    }

                } else if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    resMessage = "Please fill all the fields for authorization";

                } else if (! MainActivity.isEmailValid(email.getText())){
                    resMessage = "Please enter correct email";
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressBar.dismiss();
            new Toast(mContext).makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();

            if (MainActivity.isUserIsAuthorized()){
                dismiss();
            }
        }
    }
}
