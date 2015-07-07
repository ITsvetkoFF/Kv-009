package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.Fragment;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LoginFragment extends Fragment {

    AutoCompleteTextView email;
    EditText password;
    Button signIn;
    TextView signUpLink;

    CookieManager cookieManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.login_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        email = (AutoCompleteTextView) getView().findViewById(R.id.email);
        password = (EditText) getView().findViewById(R.id.password);
        signIn = (Button) getView().findViewById(R.id.email_sign_in_button);
        signUpLink = (TextView) getView().findViewById(R.id.link_to_register);

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

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
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new RegistrationFragment()).commit();
            }
        });
    }
    private class LoginTask extends AsyncTask {
        String resMessage;
        Context mContext;

        public LoginTask(Context context) {
            this.mContext = context;
            resMessage = null;
        }

        @Override
        protected Void doInBackground(Object[] params) {
            URL url = null;
            HttpURLConnection connection = null;

            try {
                url = new URL("http://176.36.11.25:8000/api/login");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.connect();

                //creating JSONObject for reqiest
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
                    MainActivity.setUserId(cookieManager.getCookieStore().getCookies().toString());

                    resMessage = "Hello " + MainActivity.getUserFirstName() + " " + MainActivity.getUserSecondName() + "!";

                } else {
                    resMessage = "Error! Please check your data and try again";
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            new Toast(mContext.getApplicationContext()).makeText(getActivity().getApplicationContext(), resMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
