package org.ecomap.android.app.utils;

import android.util.Log;

import org.ecomap.android.app.EcomapApplication;
import org.ecomap.android.app.R;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by y.ridkous@gmail.com on 30.08.2015.
 */
public class RESTRequestsHelper {

    public static class Response{

        public int responseCode = -1;
        public int problemID = -1;
        public String resultMessage;

        public Response(int responseCode, int problemID, String resultMessage) {
            this.responseCode = responseCode;
            this.problemID = problemID;
            this.resultMessage = resultMessage;
        }
    }

    public static Response sendProblem(String... params){

        int responseCode = -1;
        int problemID = -1;
        String resultMessage = "";

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
                resultMessage = EcomapApplication.getAppContext().getString(R.string.problem_added);

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
            Log.e("", e.getMessage());
        }

        return new Response(responseCode, problemID, resultMessage);
    }
}
