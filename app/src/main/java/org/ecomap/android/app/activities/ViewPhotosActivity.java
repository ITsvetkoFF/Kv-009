package org.ecomap.android.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.ecomap.android.app.R;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.widget.ZoomableImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewPhotosActivity extends AppCompatActivity {

    public static final String IMAGE_POSITION = "IMAGE_POSITION";
    public static final String PHOTO_ENTRY = "PHOTO_ENTRY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        int position = intent.getIntExtra(IMAGE_POSITION, 0);
        Parcelable[] photoEntries = intent.getParcelableArrayExtra(PHOTO_ENTRY);
        ArrayList<Parcelable> mImagesURLArray = new ArrayList<>(Arrays.asList(photoEntries));

        FragmentManager fm = getSupportFragmentManager();
        ImagePagerFragment fragment = (ImagePagerFragment) fm.findFragmentByTag(ImagePagerFragment.class.getSimpleName());

        if (fragment == null) {

            fragment = new ImagePagerFragment();

            Bundle args = new Bundle();
            args.putInt(IMAGE_POSITION, position);
            args.putParcelableArrayList(PHOTO_ENTRY, mImagesURLArray);
            fragment.setArguments(args);

        }

        fm.beginTransaction()
                .replace(R.id.content_frame, fragment, ImagePagerFragment.class.getSimpleName())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class ImagePagerFragment extends Fragment implements ZoomableImageView.OnSingleTouchListener {
        ActionBar mActionBar;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ViewPhotosActivity activity = (ViewPhotosActivity) getActivity();
            mActionBar = activity.getSupportActionBar();
            if (savedInstanceState != null) {
                boolean bIsActionBarVisible = savedInstanceState.getBoolean("ACTION_BAR_VISIBILITY", true);
                if (bIsActionBarVisible)
                    mActionBar.show();
                else
                    mActionBar.hide();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("ACTION_BAR_VISIBILITY", mActionBar.isShowing());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image_pager_view_photos, container, false);

            ArrayList<Parcelable> mImagesURLArray = getArguments().getParcelableArrayList(PHOTO_ENTRY);

            ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
            pager.setAdapter(new ImageAdapter(this, mImagesURLArray));
            pager.setCurrentItem(getArguments().getInt(IMAGE_POSITION, 0));
            pager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.space_between_photos)); ///---------- replace with dimension
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            //inflater.inflate(R.menu.menu_problem_details, menu);
        }

        @Override
        public void OnSingleTouch() {
            if (mActionBar.isShowing())
                mActionBar.hide();
            else
                mActionBar.show();
        }
    }

    private static class ImageAdapter extends PagerAdapter {

        private static final String LOG_TAG = ViewPhotosActivity.class.getSimpleName() + "/" + ImageAdapter.class.getSimpleName();
        private final DisplayImageOptions options;
        private final ImagePagerFragment fragment;
        private final List<Parcelable> mImagesURLArray;

        ImageAdapter(ImagePagerFragment fragment, List<Parcelable> titledPhotos) {

            this.options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    //.resetViewBeforeLoading(true)
                    .cacheOnDisk(true)
                    .cacheInMemory(false)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();

            this.fragment = fragment;
            this.mImagesURLArray = titledPhotos;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mImagesURLArray.size();
        }




        @Override
        public Object instantiateItem(ViewGroup view, int position) {

            class ViewHolder {
                ZoomableImageView imageView;
                ProgressBar spinner;
                TextView txtImgCaption;
            }

            final ViewHolder holder;
            final Context context = view.getContext();
            //if (view.getTag() == null) {

                View imageLayout = LayoutInflater.from(context).inflate(R.layout.item_pager_image, view, false);
                assert imageLayout != null;
                holder = new ViewHolder();
                holder.imageView = (ZoomableImageView) imageLayout.findViewById(R.id.image);
                holder.imageView.registerOnSingleTouchListener(fragment);
                holder.spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
                holder.txtImgCaption = (TextView) imageLayout.findViewById(R.id.photoDescription);

            //    view.setTag(holder);

            //} else {
            //    holder = (ViewHolder) view.getTag();
            //}

            SimpleImageLoadingListener MyImageLoadingListener = new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    holder.spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, message);
                    holder.spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.spinner.setVisibility(View.GONE);
                    holder.imageView.setImageBitmap(loadedImage);
                }
            };

            if (mImagesURLArray != null && mImagesURLArray.size() > 0) {

                final ProblemPhotoEntry problemPhotoEntry = (ProblemPhotoEntry) mImagesURLArray.get(position);
                final String imgURL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/static/photos/" + problemPhotoEntry.getImgURL();

                final String caption = problemPhotoEntry.getCaption() == null ? "" : problemPhotoEntry.getCaption();
                holder.txtImgCaption.setText(caption);

                ImageLoader.getInstance().loadImage(imgURL, options, MyImageLoadingListener);
            }

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}