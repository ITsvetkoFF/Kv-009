package org.ecomap.android.app.sync;

/**
 * Created by yura on 8/7/15.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.User;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.fragments.LoginFragment;
import org.ecomap.android.app.utils.SharedPreferencesHelper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class SocialLoginTask extends AsyncTask<String, Void, Void> {

    LoginFragment loginFragment;
    String resMessage;
    Context mContext;
    ProgressDialog progressBar;

    public SocialLoginTask(LoginFragment loginFragment, Context context) {
        this.loginFragment = loginFragment;
        this.mContext = context;
        resMessage = null;
    }

    @Override
    protected Void doInBackground(String[] params) {

        String urlType = (String) params[0];
        String firstName = (String) params[1];
        String lastName = (String) params[2];
        String id = (String) params[3];
        String email = (String) params[4];
        URL url = null;
        HttpURLConnection connection = null;

        try {
            url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/auth/" + urlType);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.connect();

            //creating JSONObject for request
            JSONObject request = new JSONObject();
            request.put("first_name", firstName);
            request.put("last_name", lastName);
            request.put(urlType + "_id", id);
            request.put("email", email);

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

                Set<String> set = User.getSetFromJSONArray(data.getJSONArray("user_perms"));

                SharedPreferencesHelper.onLogInSavePref(
                        mContext,
                        data.get("first_name").toString(),
                        data.get("last_name").toString(),
                        email,
                        null,
                        data.get("user_roles").toString(),
                        data.get("user_id").toString(),
                        set);

                MainActivity.setUserId(MainActivity.cookieManager.getCookieStore().getCookies().toString());

                User.getInstance(
                        data.get("first_name").toString(),
                        data.get("last_name").toString(),
                        email,
                        null,
                        data.get("user_roles").toString(),
                        data.get("user_id").toString(),
                        set);

                String fileNamePref = loginFragment.getResources().getString(R.string.fileNamePreferences);

                resMessage = "Hello " + SharedPreferencesHelper.getStringPref(mContext, fileNamePref, MainActivity.FIRST_NAME_KEY, "")
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

            connection.disconnect();

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressBar = new ProgressDialog(mContext);
        progressBar.setMessage(loginFragment.getString(R.string.connecting_server));
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.show();
    }

    @Override
    protected void onPostExecute(Void o) {
        super.onPostExecute(o);
        MainActivity.changeAuthorizationState();

        progressBar.dismiss();
        loginFragment.dismiss();

        Toast.makeText(mContext, resMessage, Toast.LENGTH_LONG).show();
    }
}
