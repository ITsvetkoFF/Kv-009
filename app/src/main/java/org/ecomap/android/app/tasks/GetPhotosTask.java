package org.ecomap.android.app.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetPhotosTask extends AsyncTask<Integer, Integer, List<ProblemPhotoEntry>> {

    private final EcoMapFragment ecoMapFragment;
    private final String LOG_TAG = GetPhotosTask.class.getSimpleName();

    private String JSONStr;

    public GetPhotosTask(EcoMapFragment ecoMapFragment) {
        this.ecoMapFragment = ecoMapFragment;
    }

    @Override
    protected List<ProblemPhotoEntry> doInBackground(Integer... params) {

        Integer numProblem = params[0];

        String ECOMAP_PHOTOS_URL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/api/problems/" + numProblem + "/photos";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        List<ProblemPhotoEntry> ret = new ArrayList<>();

        try {
            // Getting input stream from URL

            URL url = new URL(ECOMAP_PHOTOS_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return ret;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return ret;
            }

            JSONStr = buffer.toString();

            // Starting method for parsing data from JSON and writing them to database
            ret = getPhotosFromJSON();

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        return ret;
    }

    // parsing data from JSON and writing them to database
    private List<ProblemPhotoEntry> getPhotosFromJSON() {
        final String COMMENT = "comment";
        final String PHOTO_NAME = "name";

        try {
            JSONArray jArr = new JSONArray(JSONStr);

            List<ProblemPhotoEntry> syncedList = Collections.synchronizedList(new ArrayList<ProblemPhotoEntry>(JSONStr.length()));

            for (int i = 0; i < jArr.length(); i++) {
                String title;
                String image_name;

                JSONObject obj = jArr.getJSONObject(i);
                title = obj.getString(COMMENT);
                image_name = obj.getString(PHOTO_NAME);
                syncedList.add(new ProblemPhotoEntry(title, image_name));
            }

            return syncedList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


    @Override
    protected void onPostExecute(List<ProblemPhotoEntry> imgagesArray) {
        ecoMapFragment.imgAdapter.updateDataSet(imgagesArray);
    }
}
