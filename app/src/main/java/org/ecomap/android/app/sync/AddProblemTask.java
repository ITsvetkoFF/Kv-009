package org.ecomap.android.app.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.AddProblemFragment;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddProblemTask extends AsyncTask<String, Void, Void> {

    private ProgressDialog progressBar;
    private Context mContext;
    private int responseCode;
    private int problemID;
    private String resultMessage = null;

    public AddProblemTask(Context context){
        this.mContext = context;
        this.progressBar = null;
        this.responseCode = 0;
        this.problemID = 0;
        this.resultMessage = null;
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

        URL url = null;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);

            //TODO data validation here

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

                String line = null;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line + "\n");
                }
                reader.close();

                JSONObject data = new JSONObject(responseBody.toString());

                problemID = data.getInt("id");
                resultMessage = "Problem Successfully Added";

            } else {
                StringBuilder responseBody = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));

                String line = null;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line + "\n");
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

        //TODO change to SnackBar
        new Toast(mContext).makeText(mContext, resultMessage, Toast.LENGTH_LONG);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            sendPhoto(problemID);
            AddProblemFragment.getSendProblemButton().setText(mContext.getString(R.string.exit));
            EcoMapFragment.exitAddProblemMode();
        }
    }

    private void sendPhoto(int problemId){
        View view;
        EditText editText;
        String path;
        String comment;

        //Checking selected photos
        if (!AddProblemFragment.selectedPhotos.isEmpty()) {
            for(int i = 0; i < AddProblemFragment.selectedPhotos.size(); i++){
                //Get each ListView item
                view = AddProblemFragment.getNonScrollableListView().getChildAt(i);
                editText = (EditText) view.findViewById(R.id.add_photo_edit_text);
                //Get comment
                comment = editText.getText().toString();
                //Get path for each photo
                path = AddProblemFragment.selectedPhotos.get(i);
                //Start new AsyncTask for each photo and comment (test problem ID is 361)
                new UploadPhotoTask(mContext, problemId, path, comment).execute();
            }
        }
    }
}
