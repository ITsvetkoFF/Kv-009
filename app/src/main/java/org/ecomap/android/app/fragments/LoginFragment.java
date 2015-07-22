package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.utils.SharedPreferencesHelper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginFragment extends DialogFragment {

    private AutoCompleteTextView email;
    private EditText password;
    private Button signIn;
    private TextView signUpLink;
    private TextInputLayout tilEmail, tilPass;

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

        tilEmail = (TextInputLayout) getView().findViewById(R.id.til_email);
        tilEmail.setErrorEnabled(true);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!email.getText().toString().isEmpty()) {
                        if (!MainActivity.isEmailValid(email.getText().toString())) {
                            tilEmail.setError("Please enter correct email");
                            signIn.setClickable(false);
                            Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                        } else {
                            tilEmail.setErrorEnabled(false);
                            signIn.setClickable(true);
                        }
                    } else if (email.getText().toString().isEmpty()) {
                        tilEmail.setError("Email cannot be blank");
                        signIn.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilEmail.setErrorEnabled(false);
                        signIn.setClickable(true);
                    }
                }
            }
        });

        tilPass = (TextInputLayout) getView().findViewById(R.id.til_password);
        tilPass.setErrorEnabled(true);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (password.getText().toString().isEmpty()) {
                        tilPass.setError("Password cannot be blank");
                        signIn.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilPass.setErrorEnabled(false);
                        signIn.setClickable(true);
                    }
                }
            }
        });



        signIn.setClickable(false);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    new LoginTask(getActivity()).execute(email.getText().toString()
                            , password.getText().toString());
                } else {
                    Snackbar.make(v, "Check internet connection please", Snackbar.LENGTH_LONG).show();
                }
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

    private class LoginTask extends AsyncTask<String, Void, Void> {
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
        protected Void doInBackground(String[] params) {
            URL url = null;
            HttpURLConnection connection = null;

            try {
                url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/login");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.connect();

                //validation
                if (MainActivity.isEmailValid(params[0]) && (! params[1].isEmpty())) {

                    //creating JSONObject for request
                    JSONObject request = new JSONObject();
                    request.put("email", params[0]);
                    request.put("password", params[1]);

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

                        SharedPreferencesHelper.onLogInSavePref(mContext, data.get("first_name").toString(),
                                data.get("last_name").toString(),
                                params[0],
                                params[1]);

                        MainActivity.setUserId(MainActivity.cookieManager.getCookieStore().getCookies().toString());

                        String fileNamePref = getResources().getString(R.string.fileNamePreferences);

                        resMessage = "Hello " + SharedPreferencesHelper.getStringPref(mContext,fileNamePref,MainActivity.FIRST_NAME_KEY, "")
                                + " " + SharedPreferencesHelper.getStringPref(mContext, fileNamePref, MainActivity.LAST_NAME_KEY, "") + "!";

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
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            MainActivity.changeAuthorizationState();

            progressBar.dismiss();
            dismiss();

            Toast.makeText(mContext, resMessage, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
