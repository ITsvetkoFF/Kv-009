package org.ecomap.android.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.ViewPhotosActivity;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.ui.components.ExpandableHeightGridView;
import org.ecomap.android.app.ui.fragments.CommentsFragment;
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
import java.util.Collections;
import java.util.List;

/**
 * Created by y.ridkous@gmail.com on 24.06.2015.
 */
public class ProblemDetailsFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";
    private static final int PROBLEM_NUMBER = 185;

    public ImageAdapter imgAdapter;
    private List<String> mImagesURLArray;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncGetPhotos().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.problem_details_layout, container, false);

        FragmentManager chFm = getChildFragmentManager();
        Fragment f = chFm.findFragmentByTag(CommentsFragment.TAG);
        if (f == null) {
            f = CommentsFragment.newInstance();
        }
        chFm.beginTransaction().replace(R.id.fragment_comments, f, CommentsFragment.TAG).commit();

        ExpandableHeightGridView gridview = (ExpandableHeightGridView) rootView.findViewById(R.id.gridview);
        gridview.setExpanded(true);

        imgAdapter = new ImageAdapter(getActivity(), new ArrayList<ProblemPhotoEntry>());
        gridview.setAdapter(imgAdapter);

        final ScrollView mScrollView = (ScrollView) rootView.findViewById(R.id.details_scrollview);
        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.scrollTo(0, 0);
            }
        });

//        TextView txt = (TextView) view.findViewById(R.id.tabNumber);
//        Bundle args = getArguments();
//        txt.setText(String.valueOf(args.getInt(ARG_SECTION_NUMBER)));
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private static class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private List<ProblemPhotoEntry> mImagesURLArray;
        private LayoutInflater inflater;
        private DisplayImageOptions options;


        public ImageAdapter(Context c, List<ProblemPhotoEntry> titledPhotos) {
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
        public void updateDataSet(List<ProblemPhotoEntry> data) {
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

                view = inflater.inflate(R.layout.item_image_grid, parent, false);
                holder = new ViewHolder();

                assert view != null;

                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);

                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (mImagesURLArray != null && mImagesURLArray.size() > 0) {
                final ProblemPhotoEntry problemPhotoEntry = mImagesURLArray.get(position);

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ViewPhotosActivity.class);
                        intent.putExtra(ViewPhotosActivity.IMAGE_POSITION, position);
                        intent.putExtra(ViewPhotosActivity.PHOTO_ENTRY, mImagesURLArray.toArray(new ProblemPhotoEntry[mImagesURLArray.size()]));
                        mContext.startActivity(intent);
                    }
                });


                /* On case they will change naming logic again
                String[] imgName = problemPhotoEntry.getImgURL().split("\\.");
                final String imgURL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/static/thumbnails/" + imgName[0] + "." + "thumbnail." + imgName[1];
                */

                final String imgURL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/static/thumbnails/" + problemPhotoEntry.getImgURL();

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
            return view;
        }

        static class ViewHolder {
            ImageView imageView;
            ProgressBar progressBar;
        }

    }

    private class AsyncGetPhotos extends AsyncTask<Void, Void, List<ProblemPhotoEntry>> {

        private static final String ECOMAP_PHOTOS_URL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/api/problems/" + PROBLEM_NUMBER + "/photos";
        private final String LOG_TAG = AsyncGetPhotos.class.getSimpleName();

        String JSONStr = null;

        @Override
        protected List<ProblemPhotoEntry> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<ProblemPhotoEntry> ret = new ArrayList<>();

            try {
                // Getting input stream from URL

                URL url = new URL(ECOMAP_PHOTOS_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return ret;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return ret;
                }

                JSONStr = buffer.toString();

                // Starting method for parsing data from JSON and writing them to database
                ret = getPhotosFromJSON();

            } catch (IOException e) {
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

            return ret;
        }

        // parsing data from JSON and writing them to database
        private List<ProblemPhotoEntry> getPhotosFromJSON() {
            final String COMMENT = "comment";
            final String PHOTO_NAME = "name";

            try {
                JSONArray jArr = new JSONArray(JSONStr);

                List<ProblemPhotoEntry> syncedList = Collections.synchronizedList(new ArrayList<ProblemPhotoEntry>(JSONStr.length()));

                for (int i = 0; i < jArr.length(); i++) {
                    String title;
                    String image_name;

                    JSONObject obj = jArr.getJSONObject(i);
                    title = obj.getString(COMMENT);
                    image_name = obj.getString(PHOTO_NAME);
                    syncedList.add(new ProblemPhotoEntry(title, image_name));
                }

                return syncedList;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new ArrayList<ProblemPhotoEntry>();
        }


        @Override
        protected void onPostExecute(List<ProblemPhotoEntry> imgagesArray) {
            imgAdapter.updateDataSet(imgagesArray);
        }
    }


}
