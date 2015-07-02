package org.ecomap.android.app.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.ecomap.android.app.EcoMapFragment;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.data.EcoMapContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by yura on 6/30/15.
 */
public class EcoMapService extends IntentService {

    private final String LOG_TAG = EcoMapService.class.getSimpleName();
    String JSONStr = null;

    public EcoMapService(){
        super("EcoMapService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            // Getting input stream from URL
            final String FORECAST_BASE_URL = "http://ecomap.org/api/problems";

            URL url = new URL(FORECAST_BASE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            JSONStr = buffer.toString();

            // Starting method for parsing data from JSON and writing them to database
            getProblemsFromJSON();

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
        return;
    }

    // parsing data from JSON and writing them to database
    private void getProblemsFromJSON(){
        final String TITLE = "Title";
        final String LATITUDE = "Latitude";
        final String LONGITUDE = "Longtitude";
        final String PROBLEMS_TYPES_ID = "ProblemTypes_Id";

        try{
            JSONArray jArr = new JSONArray(JSONStr);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(JSONStr.length());

            for (int i = 0; i < jArr.length(); i++){
                String title;
                double latitude, longitude;
                int type_id;

                JSONObject obj = jArr.getJSONObject(i);
                title = obj.getString(TITLE);
                latitude = obj.getDouble(LATITUDE);
                longitude = obj.getDouble(LONGITUDE);
                type_id = obj.getInt(PROBLEMS_TYPES_ID);

                ContentValues mapValues = new ContentValues();

                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_TITLE, title);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE, latitude);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE, longitude);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID, type_id);

                // start inserting dummy data
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_STATUS, "STATUS");
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_USER_NAME, "USER_NAME");
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY, 1);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_VOTES_NUMBER, 1);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, "DATE");
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_CONTENT, "CONTENT");
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL, "PROPOSAL");
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID, 1);
                mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER, 1);
                // finish inserting dummy data

                cVVector.add(mapValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(EcoMapContract.ProblemsEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "EcoMap Service Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}