package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.ui.components.NonScrollableListView;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class AddProblemFragment extends DialogFragment{

    private Context mContext;
    private View view;

    private EditText problemTitle;
    private EditText problemDescription;
    private EditText problemSolution;
    private Spinner spinner;

    ProgressDialog progressBar;

    private NonScrollableListView nonScrollableListView;
    public ImageAdapter imgAdapter;
    public static ArrayList<String> selectedPhotos = new ArrayList<>();

    private Button cancelButton;
    private Button sendProblemButton;
    private Button addPhotoButton;

    public static final int REQUEST_CODE = 1;
    private int problemType;
    private String[] params;
    int problemID;

    private String resultMessage = null;

    public int responseCode;

    public static AddProblemFragment newInstance(){

        AddProblemFragment fragment = new AddProblemFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_problem_layout, container, false);
        getDialog().setTitle("Add Problem Description");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        problemTitle = (EditText) view.findViewById(R.id.problemTitle);
        problemDescription = (EditText) view.findViewById(R.id.problemDescription);
        problemSolution = (EditText) view.findViewById(R.id.problemSolution);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        cancelButton = (Button) view.findViewById(R.id.cancel);
        sendProblemButton = (Button) view.findViewById(R.id.send_problem);
        addPhotoButton = (Button) view.findViewById(R.id.add_photo);

        nonScrollableListView = (NonScrollableListView) view.findViewById(R.id.nonScrollableListView);
        imgAdapter = new ImageAdapter(mContext, selectedPhotos);
        nonScrollableListView.setAdapter(imgAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                problemType = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send intent to library for picking photos
                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                intent.setPhotoCount(8);
                intent.setShowCamera(true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        sendProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                params = new String[9];

                params[0] = "UNSOLVED";
                params[1] = "3";
                params[2] = problemTitle.getText().toString();
                params[3] = String.valueOf(problemType);
                params[4] = problemDescription.getText().toString();
                params[5] = problemSolution.getText().toString();
                params[6] = "1";
                params[7] = String.valueOf(EcoMapFragment.getMarkerPosition().latitude);
                params[8] = String.valueOf(EcoMapFragment.getMarkerPosition().longitude);

                new AddProblemTask().execute(params);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Getting photo paths from lib
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                selectedPhotos.clear();
                selectedPhotos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                imgAdapter.updateDataSet(selectedPhotos);
            }

        }
    }

    private class AddProblemTask extends AsyncTask<String, Void, Void> {

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
                    view = nonScrollableListView.getChildAt(i);
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

    private class UploadPhotoTask extends AsyncTask {

        String resMessage;
        Context mContext;
        int problemID;
        String imagePath;
        String comment;
        int serverResponseCode;
        String serverResponseMessage;
        ProgressDialog photoProgressBar;

        public UploadPhotoTask(Context context, int problemID, String imagePath, String comment) {
            this.mContext = context;
            this.problemID = problemID;
            this.imagePath = imagePath;
            this.comment = comment;
            resMessage = null;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            Log.e("MYLOG", "Inside background");

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(imagePath);

            if (!MainActivity.isUserIsAuthorized()) {
                Log.e("MYLOG", "Not auth");
                return null;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL("http://176.36.11.25:8000/api/problems/" + problemID + "/photos");
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
                    dos.writeBytes("Content-Disposition: form-data; name=\"photos\";filename=\"" + imagePath +"\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0)
                    {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    Log.e("MYLOG", "File is written");

                    serverResponseCode = conn.getResponseCode();
                    serverResponseMessage = conn.getResponseMessage();

                    Log.i("MYLOG", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {
                        resMessage = "Photos uploaded";
                    }
                    else {
                        resMessage = "Upload error";
                    }
                    // close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    Log.e("MYLOG", "error: " + ex.getMessage(), ex);

                } catch (final Exception e) {
                    e.printStackTrace();
                    Log.e("MYLOG", "Exception : " + e.getMessage());
                }
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            photoProgressBar = new ProgressDialog(mContext);
            photoProgressBar.setMessage("Uploading photos");
            photoProgressBar.setIndeterminate(true);
            photoProgressBar.setCancelable(true);
            photoProgressBar.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            photoProgressBar.dismiss();

            new Toast(mContext).makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();

        }
    }

    private static class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private List<String> mImagesURLArray;
        private LayoutInflater inflater;
        private DisplayImageOptions options;

        public ImageAdapter(Context c, List<String> titledPhotos) {

            this.mImagesURLArray = titledPhotos;
            this.mContext = c;
            this.inflater = LayoutInflater.from(mContext);

            this.options = new DisplayImageOptions.Builder()
                    //.showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                            //.showImageOnFail(R.drawable.ic_action_refresh)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        /**
         * Update adapter data set
         */
        public void updateDataSet(List<String> data) {
            mImagesURLArray = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mImagesURLArray.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            View view = convertView;

            if (view == null) {

                view = inflater.inflate(R.layout.add_problem_photo_item, parent, false);
                holder = new ViewHolder();

                assert view != null;

                holder.imageView = (ImageView) view.findViewById(R.id.iv_photo);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                holder.closeButton = (ImageView) view.findViewById(R.id.iv_error);
                holder.comment = (EditText) view.findViewById(R.id.add_photo_edit_text);

                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            //Clearing array of photos and updating adapter
            holder.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPhotos.remove(position);
                    updateDataSet(selectedPhotos);
                }
            });

            if (mImagesURLArray != null && mImagesURLArray.size() > 0) {

                final String imgURL = "file://"+mImagesURLArray.get(position);


                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(
                        imgURL,
                        holder.imageView,
                        options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.progressBar.setProgress(0);
                                holder.progressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                holder.progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                holder.progressBar.setVisibility(View.GONE);
                            }
                        },
                        new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                holder.progressBar.setProgress(Math.round(100.0f * current / total));
                            }
                        });
            }
            //We're need to clear each edittext after changing photos
            holder.comment.setText("");

            return view;
        }

        static class ViewHolder {
            ImageView imageView;
            ImageView closeButton;
            ProgressBar progressBar;
            EditText comment;
        }

    }
}


