package org.ecomap.android.app.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.ecomap.android.app.R;
import org.ecomap.android.app.sync.EcoMapAPIContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
        resMessage = null;
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

        BitmapFactory.Options bm = new BitmapFactory.Options();
        bm.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bm);

        int srcW = bm.outWidth;
        int srcH = bm.outHeight;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;

        if(srcH > srcW){
            double scC = srcH / 1600.0d;

            bitmapOptions.inSampleSize = (int) scC;
            bitmapOptions.inDensity = srcH;
            bitmapOptions.inTargetDensity = 1600 * bitmapOptions.inSampleSize;
        } else {
            double scC = srcW / 1600.0d;

            bitmapOptions.inSampleSize = (int) scC;
            bitmapOptions.inDensity = srcW;
            bitmapOptions.inTargetDensity = 1600 * bitmapOptions.inSampleSize;
        }

        Bitmap b = BitmapFactory.decodeFile(imagePath, bitmapOptions);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 75, bos);
            byte[] bitmapData = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);

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
            Log.d(LOG_TAG, "doInBackground " + comment);
            dos.writeUTF(comment); // mobile_no is String variable
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            //Adding Parameter image
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"photos\";filename=\"" + imagePath + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of maximum size
            bytesAvailable = bs.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = bs.read(buffer, 0, bufferSize);
            long fileSize = bitmapData.length;
            long bytesWritten = 0;
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesWritten += bufferSize;

                double percentage = bytesWritten / fileSize * 100;
                publishProgress((int) percentage);

                bytesAvailable = bs.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = bs.read(buffer, 0, bufferSize);

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
            bs.close();
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

