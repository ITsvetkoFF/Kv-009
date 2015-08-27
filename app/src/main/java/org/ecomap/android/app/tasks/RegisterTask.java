package org.ecomap.android.app.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.User;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.fragments.RegistrationFragment;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.utils.SharedPreferencesHelper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class RegisterTask extends AsyncTask<String, Void, Void> {

    private final RegistrationFragment registrationFragment;
    private final Context mContext;
    private String resMessage;
    private ProgressDialog progressBar;

    public RegisterTask(RegistrationFragment registrationFragment, Context context) {
        this.registrationFragment = registrationFragment;
        this.mContext = context;
        resMessage = null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressBar = new ProgressDialog(mContext);
        progressBar.setMessage(registrationFragment.getString(R.string.connecting_server));
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.show();
    }

    @Override
    protected Void doInBackground(String[] params) {
        URL url;
        HttpURLConnection connection = null;

        try {
            url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/register");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.connect();

            //validation step
            if (!params[0].isEmpty() && !params[1].isEmpty()
                    && !params[3].isEmpty() &&
                    params[3].equals(params[4])
                    && MainActivity.isEmailValid(params[2])) {

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

                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line).append("\n");
                    }
                    reader.close();

                    JSONObject data = new JSONObject(responseBody.toString());

                    Set<String> set = User.getSetFromJSONArray(data.getJSONArray("user_perms"));

                    SharedPreferencesHelper.onLogInSavePref(mContext, data.get("first_name").toString(),
                            data.get("last_name").toString(),
                            params[2], params[3], data.get("user_roles").toString(), data.get("user_id").toString(),
                            set);

                    MainActivity.setUserId(MainActivity.cookieManager.getCookieStore().getCookies().toString());

                    String fileNamePref = registrationFragment.getResources().getString(R.string.fileNamePreferences);

                    User.getInstance(data.get("first_name").toString(), data.get("last_name").toString(),
                            params[2], params[3], data.get("user_roles").toString(), data.get("user_id").toString(),
                            set);

                    resMessage = "Hello " + SharedPreferencesHelper.getStringPref(mContext, fileNamePref, MainActivity.FIRST_NAME_KEY, "")
                            + " " + SharedPreferencesHelper.getStringPref(mContext, fileNamePref, MainActivity.LAST_NAME_KEY, "") + "!";

                } else {
                    StringBuilder responseBody = new StringBuilder();
                    BufferedReader reader
                            = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line).append("\n");
                    }
                    reader.close();

                    JSONObject data = new JSONObject(responseBody.toString());
                    resMessage = data.get("message").toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        progressBar.dismiss();

        Toast.makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();

        if (MainActivity.isUserIsAuthorized()) {
            registrationFragment.dismiss();
        }
    }
}
