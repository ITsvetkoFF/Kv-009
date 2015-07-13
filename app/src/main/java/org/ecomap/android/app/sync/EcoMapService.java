package org.ecomap.android.app.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.ecomap.android.app.R;
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

public class EcoMapService extends IntentService {

    private final String LOG_TAG = EcoMapService.class.getSimpleName();
    private SharedPreferences sPref;
    private int numCurrentRevision;

    public EcoMapService() {
        super("EcoMapService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        HttpURLConnection urlConnection = null;

        BufferedReader reader = null;

        try {

            sPref = getSharedPreferences(getString(R.string.fileNamePreferences), MODE_PRIVATE);
            numCurrentRevision = sPref.getInt(getString(R.string.prefNumRevision), 0);

            Log.i(LOG_TAG, "numCurrentRevision is " + numCurrentRevision);

            final String REVISION_PARAM = "rev";

            Uri builtUri = Uri.parse(EcoMapAPIContract.ECOMAP_BASE_URL).buildUpon()
                    .appendQueryParameter(REVISION_PARAM, String.valueOf(numCurrentRevision)).build();

            URL url = new URL(builtUri.toString());

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

            String JSONStr = buffer.toString();

            // Starting method for parsing data from JSON and writing them to database
            boolean dataUpdated = getProblemsFromJSON(JSONStr);

            if (dataUpdated) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("Data"));
            }

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
    //return true, if data was added
    //false, if no data updated
    private boolean getProblemsFromJSON(String JSONStr) {

        try {
            JSONObject data = new JSONObject(JSONStr);

            //get number of new revision
            int numNewRevision = data.getInt("current_activity_revision");

            Log.i(LOG_TAG, "numNewRevision is " + numNewRevision);

            if (numCurrentRevision == numNewRevision) {
                Log.i(LOG_TAG, "revisions are equals!");
                return true;
            }

            JSONArray jArr = data.getJSONArray("data");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(JSONStr.length());

            for (int i = 0; i < jArr.length(); i++) {
                int problem_id;
                String title;
                double latitude, longitude;
                int type_id;
                String status;
                String first_name;
                String last_name;
                String severity;
                int number_of_votes;
                String date;
                String content;
                String proposal;
                int region_id;
                int number_of_comments;

                String action;

                JSONObject obj = jArr.getJSONObject(i);

                if (obj.has(EcoMapAPIContract.ACTION)) {
                    //ACTION
                    action = obj.getString(EcoMapAPIContract.ACTION);

                    if (EcoMapAPIContract.ACTION_DELETE.equals(action)) {
                        //ACTION DELETE
                        problem_id = obj.getInt(EcoMapAPIContract.ID);
                        this.getContentResolver().delete(EcoMapContract.ProblemsEntry.CONTENT_URI, "_id = " + problem_id, null);

                    } else if (EcoMapAPIContract.ACTION_VOTE.equals(action)) {
                        //ACTION VOTE
                        problem_id = obj.getInt(EcoMapAPIContract.ID);
                        number_of_votes = obj.getInt(EcoMapAPIContract.NUMBER_OF_VOTES_UPDATE);
                        ContentValues cv = new ContentValues();
                        cv.put(EcoMapAPIContract.NUMBER_OF_VOTES,number_of_votes);
                        this.getContentResolver().update(EcoMapContract.ProblemsEntry.CONTENT_URI,cv,"_id = " + problem_id, null);
                    }

                } else {
                    //NEW PROBLEM
                    problem_id         = obj.getInt(EcoMapAPIContract.ID);
                    title              = obj.getString(EcoMapAPIContract.TITLE);
                    latitude           = obj.getDouble(EcoMapAPIContract.LATITUDE);
                    longitude          = obj.getDouble(EcoMapAPIContract.LONGITUDE);
                    type_id            = obj.getInt(EcoMapAPIContract.PROBLEMS_TYPES_ID);
                    status             = obj.getString(EcoMapAPIContract.STATUS);
                    first_name         = obj.getString(EcoMapAPIContract.FIRST_NAME);
                    last_name          = obj.getString(EcoMapAPIContract.LAST_NAME);
                    severity           = obj.getString(EcoMapAPIContract.SEVERITY);
                    number_of_votes    = obj.getInt(EcoMapAPIContract.NUMBER_OF_VOTES);
                    date               = obj.getString(EcoMapAPIContract.DATE);
                    content            = obj.getString(EcoMapAPIContract.CONTENT);
                    proposal           = obj.getString(EcoMapAPIContract.PROPOSAL);
                    region_id          = obj.getInt(EcoMapAPIContract.REGION_ID);
                    number_of_comments = obj.getInt(EcoMapAPIContract.NUMBER_OF_COMMENTS);

                    ContentValues mapValues = new ContentValues();

                    mapValues.put(EcoMapContract.ProblemsEntry._ID, problem_id);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_TITLE, title);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE, latitude);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE, longitude);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID, type_id);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_STATUS, status);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_USER_FIRST_NAME, first_name);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_USER_LAST_NAME, last_name);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY, severity);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES, number_of_votes);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, date);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_CONTENT, content);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL, proposal);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID, region_id);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER, number_of_comments);

                    cVVector.add(mapValues);
                }
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(EcoMapContract.ProblemsEntry.CONTENT_URI, cvArray);
            }

            //update preferences
            sPref = getSharedPreferences(getString(R.string.fileNamePreferences), MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt(getString(R.string.prefNumRevision), numNewRevision);
            ed.commit();

            Log.i(LOG_TAG, "revision was updated!");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }
}