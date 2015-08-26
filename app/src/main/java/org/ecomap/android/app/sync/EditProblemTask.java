package org.ecomap.android.app.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

public class EditProblemTask extends AsyncTask<String, Void, Void> {

    private ProgressDialog progressBar;
    private final Context mContext;
    private int responseCode;

    public EditProblemTask(Context context){
        this.mContext = context;
        this.progressBar = null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressBar = new ProgressDialog(mContext);
        progressBar.setMessage("Connecting to Ecomap server");
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);
        progressBar.show();
    }

    @Override
    protected Void doInBackground(String... params) {

        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + Integer.valueOf(params[9]));
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestMethod("PUT");

            //creating JSONObject for request
            JSONObject request = new JSONObject();
            request.put("status", params[0]);
            request.put("severity", params[1]);
            request.put("title", params[2]);
            request.put("problem_type_id", Integer.valueOf(params[3]));
            request.put("content", params[4]);
            request.put("proposal", params[5]);
            request.put("region_id", Integer.valueOf(params[6]));
            request.put("latitude", Double.valueOf(params[7]));
            request.put("longitude", Double.valueOf(params[8]));

            //sending request
            urlConnection.getOutputStream().write(request.toString().getBytes("UTF-8"));

            //handling response
            responseCode = urlConnection.getResponseCode();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressBar.dismiss();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            EcoMapService.firstStart=true;
            Intent intent = new Intent(mContext, EcoMapService.class);
            mContext.startService(intent);

            Toast.makeText(mContext, mContext.getString(R.string.problem_edited), Toast.LENGTH_SHORT).show();

            mContext.startActivity(new Intent(mContext, MainActivity.class));

        }
    }
}
