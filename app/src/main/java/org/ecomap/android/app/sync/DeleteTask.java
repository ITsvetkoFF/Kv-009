package org.ecomap.android.app.sync;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = DeleteTask.class.getSimpleName();
    private Context mContext;
    private int responseCode;

    public DeleteTask(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + Integer.valueOf(params[0]));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.connect();

            responseCode = urlConnection.getResponseCode();

        } catch (IOException e){
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            EcoMapService.firstStart = true;
            mContext.startService(new Intent(mContext, EcoMapService.class));
        }


    }
}
