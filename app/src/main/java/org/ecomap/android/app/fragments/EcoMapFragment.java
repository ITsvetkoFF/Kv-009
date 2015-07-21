package org.ecomap.android.app.fragments;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wunderlist.slidinglayer.SlidingLayer;

import org.ecomap.android.app.MyIconRendered;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.activities.ViewPhotosActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.EcoMapAPIContract;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.ui.components.ExpandableHeightGridView;
import org.ecomap.android.app.ui.fragments.CommentsFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class EcoMapFragment extends Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private UiSettings UISettings;

    private ClusterManager<Problem> mClusterManager;
    Context mContext;
    private ArrayList<Problem> values;
    private ArrayList<LatLng> points;
    private ArrayList<Marker> markers;
    Cursor cursor;
    EcoMapReceiver receiver;

    // initializing static variables of position for map saving after rotation and backstack
    private static double longitude = 30.417397;
    private static double latitude = 50.461166;
    private static float zoomlevel = 5;




    Button cancelButton;
    MapView mapView;
    // Might be null if Google Play services APK is not available.

    private static final String tag="EcoMapFragment";
    private static int markerClickType;
    private View v;
    private SlidingLayer slidingLayer;
    private SlidingLayer addProblemSliding;
    private ImageView showTypeImage, showLike;
    private TextView showTitle, showByTime, showType, showContent, showProposal, showNumOfLikes, showStatus;
    private LinearLayout showHead;
    private static String filterCondition;

    private FloatingActionButton floatingActionButton;
    private Marker marker;

    public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

    public ImageAdapter imgAdapter;
    private List<String> mImagesURLArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate");

    }
    public static void setFilterCondition(String s){
        filterCondition=s;
        Log.i(tag, "condition is set:"+ filterCondition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.i(tag, "onCreateView");


        v = inflater.inflate(R.layout.map_layout_main, container, false);
        mapView = (MapView) v.findViewById(R.id.mapview);

        //Temporary is to initialize mapView by null to get rotation works without exceptions.
        mapView.onCreate(null);
        Log.i(tag, "OnCreateView mapview");

        mMap = mapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        UISettings = mMap.getUiSettings();
        UISettings.setMapToolbarEnabled(false);
        UISettings.setCompassEnabled(true);
        UISettings.setMyLocationButtonEnabled(true);


        MapsInitializer.initialize(this.getActivity());







        values = new ArrayList<>();
        points = new ArrayList<>();
        markers = new ArrayList<>();
        mContext = getActivity();

        addProblemSliding= (SlidingLayer) v.findViewById(R.id.slidingLayer1);
        addProblemSliding.setSlidingEnabled(false);

        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProblemSliding.openLayer(true);
                //call the wunderlist
                setMarkerClickType(2);
            }
        });

        slidingLayer = (SlidingLayer) v.findViewById(R.id.show_problem_sliding_layer);

        slidingLayer.setOnInteractListener(new SlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {

            }

            @Override
            public void onShowPreview() {

                floatingActionButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onClose() {

                floatingActionButton.setVisibility(View.VISIBLE);

            }

            @Override
            public void onOpened() {

            }

            @Override
            public void onPreviewShowed() {

            }

            @Override
            public void onClosed() {

            }
        });

        cancelButton = (Button)v.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProblemSliding.closeLayer(true);
            }
        });

        showTypeImage = (ImageView) v.findViewById(R.id.show_type_image);
        showLike = (ImageView) v.findViewById(R.id.show_like);
        showTitle = (TextView) v.findViewById(R.id.show_title);
        showType = (TextView) v.findViewById(R.id.show_type);
        //showByTime = (TextView) v.findViewById(R.id.show_by_time);
        showContent = (TextView) v.findViewById(R.id.show_content);
        showProposal = (TextView) v.findViewById(R.id.show_proposal);
        showNumOfLikes = (TextView) v.findViewById(R.id.show_numOfLikes);
        showHead = (LinearLayout) v.findViewById(R.id.show_head);
        showStatus = (TextView) v.findViewById(R.id.show_status);

        ExpandableHeightGridView gridview = (ExpandableHeightGridView) v.findViewById(R.id.gridview);
        gridview.setExpanded(true);

        imgAdapter = new ImageAdapter(getActivity(), new ArrayList<ProblemPhotoEntry>());
        gridview.setAdapter(imgAdapter);

        final ScrollView mScrollView = (ScrollView) v.findViewById(R.id.details_scrollview);
        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.scrollTo(0, 0);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        IntentFilter filter = new IntentFilter("Data");
        receiver = new EcoMapReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);

        setUpMap();
    }

    // saving map position for restoring after rotation or backstack
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("longitude", mMap.getCameraPosition().target.longitude);
        outState.putDouble("latitude", mMap.getCameraPosition().target.latitude);
        outState.putFloat("zoomlevel", mMap.getCameraPosition().zoom);
    }

    // restoring saved map position after rotation or backstack
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
            zoomlevel = savedInstanceState.getFloat("zoomlevel");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering receiver after pausing fragment
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy");

        mapView.onDestroy();
        // if current map fragment is last in backstack - kill activity
        if (getFragmentManager().getBackStackEntryCount() == 0){
            getActivity().finish();
        }
        Log.i(tag, "mapView onDestroy");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void setUpMap() {
        Log.i(tag, "set up map");

        if (isNetworkAvailable()) {
            //Start service to get a new number of revision and new data
            Intent intent = new Intent(this.getActivity(), EcoMapService.class);
            getActivity().startService(intent);
        }
        if(filterCondition==null)
            {
                fillMap();
            }

        else {
                fillMap(filterCondition);
            }
        }







    public void fillMap() {
        Log.i(tag, "fillmap");
        double latitude, longitude;
        String title;
        int type_id;

        values.clear();
        mMap.clear();

        cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Problem p = new Problem(cursor, getActivity());
            values.add(p);
        }

        cursor.close();
        setUpClusterer();
    }
    //����� ��� ��������� ����� �� problem_id, ���������� ������ fillmap � ���������� �������
    public void fillMap(String filterCondition) {
        Log.i(tag, "fillmap with condition"+filterCondition);
        double latitude, longitude;
        String title;
        int type_id;


        values.clear();
        mMap.clear();


        cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, filterCondition, null, null, null);

        while (cursor.moveToNext()) {

            Problem p = new Problem(cursor, getActivity());
            values.add(p);
        }

        cursor.close();
        setUpClusterer();
    }


    public void setUpClusterer() {
        //Position the map from static variables
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomlevel));

        //Initialize the manager with the mContext and the map.
        mClusterManager = new ClusterManager<>(mContext, mMap);

        //Point the map's listeners at the listeners implemented by the cluster.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerClickType == 1) {
                    Marker m = mMap.addMarker(new MarkerOptions().position(latLng));
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    markers.add(m);
                    points.add(latLng);
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (markerClickType == 1) {
                                if (points.get(0).equals(marker.getPosition())) {
                                    countPolygonPoints();
                                    for (Marker m : markers) {
                                        m.remove();
                                    }
                                }
                                return false;
                            }
                            return false;
                        }
                    });

                    //ADDING PROBLEM VIA FLOATING ACTION BUTTON

                } else if (markerClickType == 2) {
                    //TODO check if user is authorized
                    if (MainActivity.isUserIdSet()) {
                        if (marker != null) {
                            marker.remove();
                        }
                        marker = mMap.addMarker(new MarkerOptions().position(latLng));
                        marker.setTitle("Houston we have a problem here!");
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    } else {

                        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setMessage(R.string.action_sign_in);
                        alert.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new LoginFragment().show(getFragmentManager(), "login_layout");
                            }
                        });
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dialog.cancel();
                            }
                        });
                        //alert.setCancelable(true);
                       /* alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.cancel();
                            }
                        });*/
                        alert.show();
                    }
                }
            }
        });

        //Add cluster items to the cluster manager.
        mClusterManager.addItems(values);
        mClusterManager.setOnClusterClickListener
                (new ClusterManager.OnClusterClickListener<Problem>() {
                    @Override
                    public boolean onClusterClick(Cluster cluster) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),
                                mMap.getCameraPosition().zoom + 2));
                        return false;
                    }
                });

        //Set render for markers rendering
        mClusterManager.setRenderer(new MyIconRendered(mContext, mMap, mClusterManager));

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Problem>() {
            @Override
            public boolean onClusterItemClick(final Problem problem) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(problem.getPosition(),
                        /*mMap.getCameraPosition().zoom*/15.0f));

                //Set Problem object parameters to a view at show problem fragment
                showTypeImage.setImageResource(problem.getResBigImage());
                showType.setText(problem.getTypeString());
                showTitle.setText(problem.getTitle());
                //showByTime.setText(problem.getByTime());
                showContent.setText(problem.getContent());
                showProposal.setText(problem.getProposal());
                showNumOfLikes.setText(problem.getNumberOfLikes());

                //Check problem status and choose color fo text
                if (problem.getStatus().equalsIgnoreCase("UNSOLVED")) {
                    showStatus.setText(problem.getStatus());
                    showStatus.setTextColor(Color.RED);
                } else {
                    showStatus.setText(problem.getStatus());
                    showStatus.setTextColor(Color.GREEN);
                }

                //Mechanism for likes ++ when click on heart
                showLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!problem.isLiked()) {
                            problem.setNumberOfLikes(1);
                            problem.setLiked(true);

                            new AsyncAddVote().execute(problem.getId());

                            Toast.makeText(mContext, mContext.getString(R.string.message_isLiked), Toast.LENGTH_SHORT).show();

                        } else if (problem.isLiked()) {
                            //problem.setNumberOfLikes(-1);
                            //problem.setLiked(false);
                            Toast.makeText(mContext, mContext.getString(R.string.message_isAlreadyLiked), Toast.LENGTH_SHORT).show();
                        }
                        showNumOfLikes.setText(problem.getNumberOfLikes());
                    }
                });

                //comments
                FragmentManager chFm = getChildFragmentManager();
                Fragment f = chFm.findFragmentByTag(CommentsFragment.TAG);
                //if (f == null) {
                f = CommentsFragment.newInstance(problem);
                //}
                chFm.beginTransaction().replace(R.id.fragment_comments, f, CommentsFragment.TAG).commit();

                //photos
                new AsyncGetPhotos().execute(problem.getId());

                //Set part of sliding layer visible
                slidingLayer.setPreviewOffsetDistance(showHead.getHeight());
                slidingLayer.openPreview(true);

                return false;
            }
        });
    }

    private void countPolygonPoints() {
        if (points.size() >= 3) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(points);
            polygonOptions.strokeColor(mContext.getResources().getColor(R.color.polygonEdges));
            polygonOptions.strokeWidth(7);
            polygonOptions.fillColor(mContext.getResources().getColor(R.color.polygon));
            mMap.addPolygon(polygonOptions);
            points.clear();
            markerClickType = 0;
            mMap.setOnMarkerClickListener(mClusterManager);
        }
    }

    public static void setMarkerClickType(int type) {
        markerClickType = type;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class EcoMapReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(filterCondition==null)
            {
                fillMap();
            }

            else {
                fillMap(filterCondition);
            }

        }
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

    private class AsyncGetPhotos extends AsyncTask<Integer, Integer, List<ProblemPhotoEntry>> {

        private final String LOG_TAG = AsyncGetPhotos.class.getSimpleName();

        String JSONStr = null;

        @Override
        protected List<ProblemPhotoEntry> doInBackground(Integer... params) {

            Integer numProblem = params[0];

            String ECOMAP_PHOTOS_URL = EcoMapAPIContract.ECOMAP_HTTP_BASE_URL + "/api/problems/" + numProblem + "/photos";
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

    private class AsyncAddVote extends AsyncTask<Integer, Void, Boolean> {

        private final String LOG_TAG = AsyncAddVote.class.getSimpleName();
        private Integer problem_id;

        @Override
        protected Boolean doInBackground(Integer... params) {
            URL url = null;
            Boolean result = Boolean.FALSE;
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
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setDoOutput(true);
                        connection.connect();

                        /**
                         * sending request
                         * request.toString() - translate our object into appropriate JSON text
                         * {
                         *      "content": "your comment"
                         * }
                         */
                        //OutputStream outputStream = connection.getOutputStream();
                        //outputStream.write(request.toString().getBytes("UTF-8"));

                        //handling result from server
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            result = Boolean.TRUE;
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }

            return result;
        }


    }

}

