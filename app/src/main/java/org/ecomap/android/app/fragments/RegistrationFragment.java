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

    private EditText firstName;
    private EditText secondName;
    private AutoCompleteTextView email;
    private EditText password;
    private EditText confirmPassword;
    private Button signUp;

    private TextInputLayout tilFirstName, tilSecondName, tilEmail, tilPassword, tilConfirmPassword;

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

        tilFirstName = (TextInputLayout) getView().findViewById(R.id.til_first_name_reg);
        tilFirstName.setErrorEnabled(true);
        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (firstName.getText().toString().isEmpty()){
                        tilFirstName.setError("First name cannot be blank");
                        signUp.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilFirstName.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilSecondName = (TextInputLayout) getView().findViewById(R.id.til_second_name_reg);
        tilSecondName.setErrorEnabled(true);
        secondName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (secondName.getText().toString().isEmpty()){
                        tilSecondName.setError("Second name cannot be blank");
                        signUp.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilSecondName.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilEmail = (TextInputLayout) getView().findViewById(R.id.til_email_reg);
        tilEmail.setErrorEnabled(true);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!email.getText().toString().isEmpty()) {
                        if (!MainActivity.isEmailValid(email.getText().toString())) {
                            tilEmail.setError("Please enter correct email");
                            signUp.setClickable(false);
                            Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                        } else {
                            tilEmail.setErrorEnabled(false);
                            signUp.setClickable(true);
                        }
                    } else if (email.getText().toString().isEmpty()) {
                        tilEmail.setError("Email cannot be blank");
                        signUp.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilEmail.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilPassword = (TextInputLayout) getView().findViewById(R.id.til_password_reg);
        tilPassword.setErrorEnabled(true);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (password.getText().toString().isEmpty()){
                        tilPassword.setError("Password cannot be blank");
                        signUp.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilPassword.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilConfirmPassword = (TextInputLayout) getView().findViewById(R.id.til_confirm_password_reg);
        tilConfirmPassword.setErrorEnabled(true);
        confirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (confirmPassword.getText().toString().isEmpty()){
                        tilConfirmPassword.setError("Password cannot be blank");
                        signUp.setClickable(false);
                        Snackbar.make(v, "Fill all fields correctly, please", Snackbar.LENGTH_LONG).show();
                    } else {
                        tilConfirmPassword.setErrorEnabled(false);
                        confirmPassword.setClickable(true);
                    }
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    new RegisterTask(getActivity()).execute(firstName.getText().toString()
                            , secondName.getText().toString(), email.getText().toString()
                            , password.getText().toString(), confirmPassword.getText().toString());
                } else {
                    Snackbar.make(v, "Check internet connection please", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private class RegisterTask extends AsyncTask<String, Void, Void>{

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
        protected Void doInBackground(String[] params) {
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
                if(!params[0].isEmpty() && !params[1].isEmpty()
                        && !params[3].isEmpty() &&
                        params[3].equals(params[4])
                        && MainActivity.isEmailValid(params[2])){

                    JSONObject request = new JSONObject();
                    request.put("first_name", params[0]);
                    request.put("last_name", params[1]);
                    request.put("email", params[2]);
                    request.put("password", params[3]);

                    //sending request to server
                    connection.getOutputStream().write(request.toString().getBytes("UTF-8"));

                    //handling response
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        StringBuilder responseBody = new StringBuilder();
                        BufferedReader reader = new BufferedReader
                                (new InputStreamReader(connection.getInputStream()));

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line + "\n");
                        }
                        reader.close();

                        JSONObject data = new JSONObject(responseBody.toString());
                        MainActivity.setUserFirstName(data.get("first_name").toString());
                        MainActivity.setUserSecondName(data.get("last_name").toString());
                        MainActivity.setUserId(MainActivity.cookieManager.getCookieStore()
                                .getCookies().toString());
                        MainActivity.changeAuthorizationState();

                        resMessage = "Hello " + MainActivity.getUserFirstName() + " "
                                + MainActivity.getUserSecondName() + "!";
                    } else {
                        StringBuilder responseBody = new StringBuilder();
                        BufferedReader reader
                                = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line + "\n");
                        }
                        reader.close();

                        JSONObject data = new JSONObject(responseBody.toString());
                        resMessage = data.get("message").toString();
                    }
                } else if(params[0].isEmpty() || params[1].isEmpty() || params[3].isEmpty()
                        || params[2].isEmpty()){
                    resMessage = "Please fill all the fields for registration";
                } else if (! params[3].equals(params[4])){
                    resMessage = "Password does not match";
                } else if(! MainActivity.isEmailValid(params[2])) {
                    resMessage = "Please enter correct email";
                }
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
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            progressBar.dismiss();

            new Toast(mContext).makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();

            if (MainActivity.isUserIsAuthorized()){
                dismiss();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
