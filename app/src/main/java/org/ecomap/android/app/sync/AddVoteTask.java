package org.ecomap.android.app.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.fragments.EcoMapFragment;
import org.ecomap.android.app.utils.MapClustering;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Stanislav on 27.07.2015.
 */
public class AddVoteTask extends AsyncTask<Integer, Void, Boolean> {

    private final String LOG_TAG = AddVoteTask.class.getSimpleName();
    private Integer problem_id;
    private String TAG = "myTAG";

    private Context mContext;

    public AddVoteTask(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        URL url = null;
        Boolean result = null;
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

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");



                    Log.e(TAG, "Ответ из сервера на запрос:"+String.valueOf(connection.getResponseCode()));

                    connection.connect();

                    //handling result from server
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.e(TAG, "Проблема " + problem_id + " лайкнута, "+"потому что "+connection.getResponseCode()+" и " + HttpURLConnection.HTTP_OK);

                        result = true;


                    } else {Log.e(TAG, "Проблему " + problem_id + " дважды лайкнуть не получиться"+"потому что "+connection.getResponseCode()+" и " + HttpURLConnection.HTTP_OK);


                    result = false;}

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }
        Log.e(TAG, "Возращаемый результат с бекенда " + String.valueOf(result));
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            //добавить +1 лайк в проблему в базе данных
            EcoMapFragment.lastOpenProblem.setNumberOfLikes(1);
            //в преференс что даная проблема этим пользователем тру
            EcoMapFragment.lastOpenProblem.setLiked(true);


            Log.e(TAG, "Нажата кнопка лайк выполнятся условия проблема не лайк");
            EcoMapFragment.showLike.setImageResource(R.drawable.heart_icon);

            EcoMapFragment.showNumOfLikes.setText(EcoMapFragment.lastOpenProblem.getNumberOfLikes());

            Toast.makeText(this.mContext, R.string.message_isLiked, Toast.LENGTH_SHORT).show();

        } else {
            EcoMapFragment.showLike.setImageResource(R.drawable.heart_icon);
            EcoMapFragment.lastOpenProblem.setLiked(true);
            Toast.makeText(this.mContext, R.string.message_isAlreadyLiked_in_the_web, Toast.LENGTH_SHORT).show();


        }

    }
}
