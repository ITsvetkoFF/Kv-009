package org.ecomap.android.app.sync;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

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
import java.util.ArrayList;

public class GetResourcesService extends IntentService {

    private final String LOG_TAG = getClass().getSimpleName();

    public GetResourcesService() {
        super("GetResourcesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        fetchResources();
    }

    private void fetchResources() {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        ArrayList<Integer> resourcesListArray = new ArrayList<>();

        try {
            URL url = new URL(EcoMapAPIContract.ECOMAP_RESOURCES_URL);

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
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            String JSONStr = buffer.toString();
            JSONArray jArr = new JSONArray(JSONStr);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject id = jArr.getJSONObject(i);
                resourcesListArray.add(id.getInt("id"));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
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
        for (Integer res : resourcesListArray) {

            urlConnection = null;
            reader = null;

            try {
                URL url = new URL(EcoMapAPIContract.ECOMAP_RESOURCES_URL + "/" + res);

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
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return;
                }

                String JSONStr = buffer.toString();
                ContentValues resourcesValues = new ContentValues();
                JSONObject currentID = new JSONObject(JSONStr);

                resourcesValues.put(EcoMapContract.ResourcesEntry._ID, currentID.getInt("id"));
                resourcesValues.put(EcoMapContract.ResourcesEntry.COLUMN_TITLE, currentID.getString("title"));
                resourcesValues.put(EcoMapContract.ResourcesEntry.COLUMN_CONTENT, currentID.getString("content"));

                getContentResolver().insert(EcoMapContract.ResourcesEntry.CONTENT_URI, resourcesValues);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            } catch (Exception e) {
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
        }
    }
}