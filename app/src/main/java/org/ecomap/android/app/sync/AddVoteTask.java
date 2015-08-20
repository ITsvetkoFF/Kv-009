package org.ecomap.android.app.sync;

import android.os.AsyncTask;
import android.util.Log;

import org.ecomap.android.app.activities.MainActivity;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Stanislav on 27.07.2015.
 */
public class AddVoteTask extends AsyncTask<Integer, Void, Boolean> {

    private final String LOG_TAG = AddVoteTask.class.getSimpleName();
    private Integer problem_id;

    @Override
    protected Boolean doInBackground(Integer... params) {
        URL url;
        Boolean result = Boolean.FALSE;
        problem_id = params[0];

        //validation
        if (MainActivity.isUserIsAuthorized()) {
            if (params.length > 0 && params[0] != null) {

                HttpURLConnection connection = null;

                try {

                    //creating JSONObject for request
                    JSONObject request = new JSONObject();
                    request.put("content", params[0]);

                    url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + problem_id + "/vote");

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);
                    connection.connect();

                    /**
                     * sending request
                     * request.toString() - translate our object into appropriate JSON text
                     * {
                     *      "content": "your comment"
                     * }
                     */
                    //OutputStream outputStream = connection.getOutputStream();
                    //outputStream.write(request.toString().getBytes("UTF-8"));

                    //handling result from server
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        result = Boolean.TRUE;
                    }

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }

        return result;
    }


}
