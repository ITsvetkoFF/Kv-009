package org.ecomap.android.app.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.AddProblemActivity;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.sync.UploadingServiceSession;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddProblemTask extends AsyncTask<String, Void, Void> {

    private ProgressDialog progressBar;
    private final WeakReference<Context> mContext;
    private int responseCode;
    private int problemID;
    private String resultMessage;
    private UploadingServiceSession mServiceSession;
    private AddProblemActivity addProblemActivity;

    public AddProblemTask(Context context, UploadingServiceSession serviceSession, AddProblemActivity mActivity) {
        this.mContext = new WeakReference<>(context);
        this.progressBar = null;
        this.responseCode = 0;
        this.problemID = 0;
        this.mServiceSession = serviceSession;
        this.addProblemActivity = mActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Context context = mContext.get();
        if (context != null) {
            progressBar = new ProgressDialog(context);
            progressBar.setMessage("Connecting to Ecomap server");
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);
            progressBar.show();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);

            //creating JSONObject for request
            JSONObject request = new JSONObject();
            request.put("status", params[0]);
            request.put("severity", params[1]);
            request.put("title", params[2]);
            request.put("problem_type_id", params[3]);
            request.put("content", params[4]);
            request.put("proposal", params[5]);
            request.put("region_id", params[6]);
            request.put("latitude", params[7]);
            request.put("longitude", params[8]);

            //sending request
            urlConnection.getOutputStream().write(request.toString().getBytes("UTF-8"));

            //handling response
            responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder responseBody = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append("\n");
                }
                reader.close();

                JSONObject data = new JSONObject(responseBody.toString());

                problemID = data.getInt("id");
                Context context = mContext.get();
                if (context != null) {
                    resultMessage = context.getString(R.string.problem_added);
                }

            } else {
                StringBuilder responseBody = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append("\n");
                }
                reader.close();

                JSONObject data = new JSONObject(responseBody.toString());
                resultMessage = data.get("message").toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        progressBar.dismiss();
        EcoMapService.firstStart = true;

        Context context = mContext.get();
        if (context != null) {
            Intent intent = new Intent(context, EcoMapService.class);
            context.startService(intent);

            Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                sendPhoto(problemID);
                EcoMapFragment.disableAddProblemMode();
                addProblemActivity.finish();
            }
        }
    }

    private void sendPhoto(int problemId) {
        View view;
        EditText editText;
        String path;
        String comment;

        //Checking selected photos
        if (!AddProblemActivity.selectedPhotos.isEmpty()) {
            if (AddProblemActivity.selectedPhotos.size() > 0 && mServiceSession.isBound()) {
                mServiceSession.doStartService();
            }
            for (int i = 0; i < AddProblemActivity.selectedPhotos.size(); i++) {
                //Get path for each photo
                final ProblemPhotoEntry photoEntry = AddProblemActivity.selectedPhotos.get(i);
                path = photoEntry.getImgURL();
                comment = photoEntry.getCaption();
                //Start new AsyncTask for each photo and comment (test problem ID is 361)
                if (mServiceSession.isBound()) {
                    mServiceSession.sendUploadRequest(problemId, path, comment);
                }
            }
            mServiceSession = null;
        }
    }
}
