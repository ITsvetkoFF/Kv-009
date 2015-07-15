package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class RegistrationFragment extends DialogFragment {

    EditText firstName;
    EditText secondName;
    AutoCompleteTextView email;
    EditText password;
    EditText confirmPassword;
    Button signUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_layout, container, false);
        getDialog().setTitle("Sign Up");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firstName = (EditText) getView().findViewById(R.id.first_name);
        secondName = (EditText) getView().findViewById(R.id.second_name);
        email = (AutoCompleteTextView) getView().findViewById(R.id.email);
        password = (EditText) getView().findViewById(R.id.password);
        confirmPassword = (EditText) getView().findViewById(R.id.confirm_password);
        signUp = (Button) getView().findViewById(R.id.email_sign_up_button);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegisterTask(getActivity()).execute();
            }
        });
    }

    private class RegisterTask extends AsyncTask{

        Context mContext;
        String resMessage;
        ProgressDialog progressBar;

        public RegisterTask(Context context){
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
        protected Object doInBackground(Object[] params) {
            URL url = null;
            HttpURLConnection connection = null;

            try{
                url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/register");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.connect();

                //validation step
                if(!firstName.getText().toString().isEmpty() && !secondName.getText().toString().isEmpty() && !password.getText().toString().isEmpty() &&
                        password.getText().toString().equals(confirmPassword.getText().toString()) && MainActivity.isEmailValid(email.getText().toString())){

                    JSONObject request = new JSONObject();
                    request.put("first_name", firstName.getText());
                    request.put("last_name", secondName.getText());
                    request.put("email", email.getText());
                    request.put("password", password.getText());

                    //sending request to server
                    connection.getOutputStream().write(request.toString().getBytes("UTF-8"));

                    //handling response
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
                } else if(firstName.getText().toString().isEmpty() || secondName.getText().toString().isEmpty() || password.getText().toString().isEmpty() || email.getText().toString().isEmpty()){
                    resMessage = "Please fill all the fields for registration";
                } else if (! password.getText().toString().equals(confirmPassword.getText().toString())){
                    resMessage = "Password does not match";
                } else if(! MainActivity.isEmailValid(email.getText().toString())) {
                    resMessage = "Please enter correct email";
                }

                return null;

            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
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
