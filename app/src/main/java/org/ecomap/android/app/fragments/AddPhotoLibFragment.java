package org.ecomap.android.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.iwf.photopicker.PhotoPagerActivity;
import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

/**
 * Created by yura on 7/18/15.
 */
public class AddPhotoLibFragment extends android.support.v4.app.Fragment{

    Button addPhoto;
    Button sendPhoto;
    private View view;
    Context mContext;

    public static final int REQUEST_CODE = 1;

    RecyclerView recyclerView;
    PhotoAdapter photoAdapter;
    ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.add_photo_layout, container, false);
        mContext = getActivity();

        addPhoto = (Button) view.findViewById(R.id.add_photo);
        sendPhoto = (Button) view.findViewById(R.id.send_photo);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);


        photoAdapter = new PhotoAdapter(mContext, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                intent.setPhotoCount(8);
                intent.setShowCamera(true);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        sendPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    View view;
                    EditText editText;
                    String path;
                    String comment;
                    for(int i = 0; i < selectedPhotos.size(); i++){
                        view = recyclerView.getChildAt(i);
                        editText = (EditText) view.findViewById(R.id.add_photo_edit_text);
                        comment = editText.getText().toString();
                        path = selectedPhotos.get(i);
                        new UploadPhotoTask(getActivity(), 363, path, comment).execute();
                    }
            }
        });

        return view;
    }

    public void previewPhoto(Intent intent) {
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<String> photos = null;
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();

            if (photos != null) {
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        }
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

        private ArrayList<String> photoPaths = new ArrayList<String>();
        private LayoutInflater inflater;

        private Context mContext;

        public PhotoAdapter(Context mContext, ArrayList<String> photoPaths) {
            this.photoPaths = photoPaths;
            this.mContext = mContext;
            inflater = LayoutInflater.from(mContext);

        }

        @Override public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.add_problem_photo_item, parent, false);
            return new PhotoViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

            Uri uri = Uri.fromFile(new File(photoPaths.get(position)));

            Glide.with(mContext)
                    .load(uri)
                    .centerCrop()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.ic_photo_black_48dp)
                    .error(R.drawable.ic_broken_image_black_48dp)
                    .into(holder.ivPhoto);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, PhotoPagerActivity.class);
                    intent.putExtra(PhotoPagerActivity.EXTRA_CURRENT_ITEM, position);
                    intent.putExtra(PhotoPagerActivity.EXTRA_PHOTOS, photoPaths);
                    if (mContext instanceof MainActivity) {
                        previewPhoto(intent);
                    }
                }
            });
        }

        @Override public int getItemCount() {
            return photoPaths.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivPhoto;

            public PhotoViewHolder(View itemView) {
                super(itemView);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            }
        }
    }

    private class UploadPhotoTask extends AsyncTask {

        String resMessage;
        Context mContext;
        ProgressDialog progressBar;
        int problemID;
        String imagePath;
        String comment;

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

                    Log.e("MYLOG","File is written");

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("MYLOG", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {
                        resMessage = "Photos uploaded";
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

            progressBar = new ProgressDialog(mContext);
            progressBar.setMessage("Uploading photos");
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);
            progressBar.show();
        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressBar.dismiss();
            new Toast(mContext).makeText(mContext, resMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
