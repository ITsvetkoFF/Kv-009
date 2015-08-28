package org.ecomap.android.app.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.sync.EcoMapAPIContract;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadPhotoTask extends AsyncTask<Void, Integer, Void> {

    private String resMessage;
    private final Context mContext;
    private final int problemID;
    private final String imagePath;
    private final String comment;
    private static final String LOG_TAG = UploadPhotoTask.class.getSimpleName();


    public UploadPhotoTask(Context context, int problemID, String imagePath, String comment) {
        this.mContext = context;
        this.problemID = problemID;
        this.imagePath = imagePath;
        this.comment = comment;
        //this.notification = notification;
        resMessage = null;
        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.e(LOG_TAG, "Inside background");

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(imagePath);

        Bitmap b = BitmapFactory.decodeFile(imagePath);

        int width, height;

        if (b.getHeight() > b.getWidth() && b.getHeight() > 1600 && b.getWidth() > 1200){
            height = 1600;
            width = 1200;
        } else if (b.getHeight() > 1200 && b.getWidth() > 1600) {
            height = 1200;
            width = 1600;
        } else {
            width = b.getWidth();
            height = b.getHeight();
        }

        Bitmap sb = Bitmap.createScaledBitmap(b, width, height, true);

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(EcoMapAPIContract.ECOMAP_API_URL + "/problems/" + problemID + "/photos");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            //Adding Parameter comments
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"comments\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(comment); // mobile_no is String variable
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            //Adding Parameter image
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"photos\";filename=\"" + imagePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            long fileSize = sourceFile.length();
            long bytesWritten = 0;
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesWritten += bufferSize;

                double percentage = bytesWritten / fileSize * 100;
                publishProgress((int) percentage);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            Log.i(LOG_TAG, "File is written");

            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i(LOG_TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {
                resMessage = mContext.getString(R.string.photos_uploaded);
            } else {
                resMessage = mContext.getString(R.string.uploading_error);
            }
            // close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e(LOG_TAG, "error: " + ex.getMessage(), ex);

        } catch (final Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Exception : " + e.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void o) {

        Toast.makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();

    }
}

