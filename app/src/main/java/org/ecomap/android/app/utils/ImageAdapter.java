package org.ecomap.android.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.ViewPhotosActivity;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;

import java.util.List;

/**
 * Created by Stanislav on 27.07.2015.
 */
public class ImageAdapter extends BaseAdapter {

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
