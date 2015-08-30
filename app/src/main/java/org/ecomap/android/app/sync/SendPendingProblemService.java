package org.ecomap.android.app.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.EcoMapDBHelper;
import org.ecomap.android.app.utils.RESTRequestsHelper;

import java.net.HttpURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class SendPendingProblemService extends IntentService {
    private static final String ACTION_UPLOAD_PROBLEM = "org.ecomap.android.app.sync.action.UploadProblem";

    private static final String ACTION_BAZ = "org.ecomap.android.app.sync.action.UploadPhotos";
    private static final String EXTRA_PARAM1 = "org.ecomap.android.app.sync.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "org.ecomap.android.app.sync.extra.PARAM2";

    public SendPendingProblemService() {
        super("SendPendingProblemService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUploadingProblems(Context context) {
        Intent intent = new Intent(context, SendPendingProblemService.class);
        intent.setAction(ACTION_UPLOAD_PROBLEM);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD_PROBLEM.equals(action)) {
                uploadPendingProblems();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     * request.put("status", params[0]);
     * request.put("severity", params[1]);
     * request.put("title", params[2]);
     * request.put("problem_type_id", params[3]);
     * request.put("content", params[4]);
     * request.put("proposal", params[5]);
     * request.put("region_id", params[6]);
     * request.put("latitude", params[7]);
     * request.put("longitude", params[8]);
     */
    //private void handleActionUploadingProblem(String title, String problem_type_id, String content, String proposal, String latitude, String longitude) {
    private boolean handleActionUploadingProblem(String[] params) {

        RESTRequestsHelper.Response resp = RESTRequestsHelper.sendProblem(params);
        if (resp.responseCode == HttpURLConnection.HTTP_OK) {
            return true;
        }

        return false;
    }


    private void uploadPendingProblems() {

        EcoMapDBHelper mOpenHelper = new EcoMapDBHelper(getApplicationContext());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final String MY_QUERY = "SELECT * FROM pending a LEFT JOIN problems b ON a.problem_id = b._id ORDER BY b._id";

        Cursor cursor = db.rawQuery(MY_QUERY, new String[]{});

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String[] params = new String[10];

                params[0] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_STATUS));
                params[1] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY));
                params[2] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
                params[3] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID));
                params[4] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_CONTENT));
                params[5] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL));
                params[6] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID));
                params[7] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE));
                params[8] = cursor.getString(cursor.getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE));
                params[9] = cursor.getString(cursor.getColumnIndex(EcoMapContract.PendingProblemsEntry.COLUMN_PHOTOS));

                if(handleActionUploadingProblem(params)){

                    //TODO: delete record from pending problems

                }

            }
        }

        if (cursor != null) {
            cursor.close();
        }


    }


}