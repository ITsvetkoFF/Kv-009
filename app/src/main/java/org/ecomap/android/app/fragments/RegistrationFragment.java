package org.ecomap.android.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;


public class RegistrationFragment extends Fragment {

    EditText firstName;
    EditText secondName;
    AutoCompleteTextView email;
    EditText password;
    EditText confirmPassword;
    Button signUp;
    CookieManager cookieManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.registration_layout, container, false);
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

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

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

        public RegisterTask(Context context){
            this.mContext = context;
            resMessage = null;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            URL url = null;
            HttpURLConnection connection = null;

            try{
                url = new URL("http://176.36.11.25:8000/api/register");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.connect();

                JSONObject request = new JSONObject();
                request.put("first_name", firstName.getText());
                request.put("last_name", secondName.getText());
                request.put("email", email.getText());
                request.put("password", password.getText());

                if(password.getText().equals(confirmPassword.getText())){
                    connection.getOutputStream().write(request.toString().getBytes("UTF-8"));

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        StringBuilder responseBody = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line + "\n");
                        }
                        reader.close();

                        MainActivity.setUserFirstName(firstName.getText().toString());
                        MainActivity.setUserSecondName(secondName.getText().toString());
                        MainActivity.setUserId(cookieManager.getCookieStore().getCookies().toString());

                        resMessage = "Hello " + MainActivity.getUserFirstName() + " " + MainActivity.getUserSecondName() + "!";

                    } else {
                        resMessage = "Something is wrong! Try again";//TODO response codes for registration
                    }
                } else {
                    resMessage = "Password does not match";
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
            new Toast(mContext).makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
