package org.ecomap.android.app.sync;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
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

    private String JSONStr = null;
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

            // Getting input stream from URL
            final String ECOMAP_BASE_URL = "http://176.36.11.25:8000/api/problems?";

            final String REVISION_PARAM = "rev";

            Uri builtUri = Uri.parse(ECOMAP_BASE_URL).buildUpon()
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

            JSONStr = buffer.toString();

            // Starting method for parsing data from JSON and writing them to database
            boolean dataUpdated = getProblemsFromJSON();

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
    private boolean getProblemsFromJSON() {
        final String TITLE = "title";
        final String LATITUDE = "latitude";
        final String LONGITUDE = "longitude";
        final String PROBLEMS_TYPES_ID = "problem_type_id";
        final String ACTION = "action";
        final String ACTION_DELETE = "DELETED";
        final String ACTION_VOTE = "VOTE";
        final String ID = "id";
        final String NUMBER_OF_VOTES = "number_of_votes";
        final String NUMBER_OF_VOTES_INJSON = "count";

        try {
            JSONObject data = new JSONObject(JSONStr);

            //get number of new revision
            int numNewRevision = data.getInt("current_activity_revision");

            Log.i(LOG_TAG, "numNewRevision is " + numNewRevision);

            if (numCurrentRevision == numNewRevision) {
                Log.i(LOG_TAG, "revisions are equals!");
                return false;
            }

            JSONArray jArr = data.getJSONArray("data");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(JSONStr.length());

            for (int i = 0; i < jArr.length(); i++) {
                String title;
                double latitude, longitude;
                int type_id;
                int id;
                int number_of_votes;
                String action;

                JSONObject obj = jArr.getJSONObject(i);

                if (obj.has(ACTION)) {
                    //ACTION
                    action = obj.getString(ACTION);

                    if (ACTION_DELETE.equals(action)) {
                        //ACTION DELETE
                        id = obj.getInt(ID);
                        this.getContentResolver().delete(EcoMapContract.ProblemsEntry.CONTENT_URI, "_id = " + id, null);

                    } else if (ACTION_VOTE.equals(action)) {
                        //ACTION VOTE
                        id = obj.getInt(ID);
                        number_of_votes = obj.getInt(NUMBER_OF_VOTES_INJSON);
                        ContentValues cv = new ContentValues();
                        cv.put(NUMBER_OF_VOTES,number_of_votes);
                        this.getContentResolver().update(EcoMapContract.ProblemsEntry.CONTENT_URI,cv,"_id = " + id, null);
                    }

                } else {
                    //NEW PROBLEM
                    title = obj.getString(TITLE);
                    latitude = obj.getDouble(LATITUDE);
                    longitude = obj.getDouble(LONGITUDE);
                    type_id = obj.getInt(PROBLEMS_TYPES_ID);
                    id = obj.getInt(ID);

                    ContentValues mapValues = new ContentValues();

                    mapValues.put(EcoMapContract.ProblemsEntry._ID, id);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_TITLE, title);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE, latitude);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE, longitude);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID, type_id);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_STATUS, "STATUS");
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_USER_NAME, "USER_NAME");
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY, 1);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES, 1);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, "DATE");
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_CONTENT, "CONTENT");
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL, "PROPOSAL");
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID, 1);
                    mapValues.put(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER, 1);

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