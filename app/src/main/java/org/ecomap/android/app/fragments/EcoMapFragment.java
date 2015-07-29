package org.ecomap.android.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.wunderlist.slidinglayer.SlidingLayer;

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.AddVoteTask;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.sync.GetPhotosTask;
import org.ecomap.android.app.utils.ImageAdapter;
import org.ecomap.android.app.utils.MapClustering;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.widget.ExpandableHeightGridView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class EcoMapFragment extends Fragment {
    private static final String tag = "EcoMapFragment";
    private static int markerClickType;
    private static String filterCondition;
    public ImageAdapter imgAdapter;
    private Context mContext;
    Cursor cursor;
    EcoMapReceiver receiver;
    MapView mapView;
    private GoogleMap mMap;
    private UiSettings UISettings;
    private ClusterManager<Problem> mClusterManager;
    private ArrayList<Problem> values;
    private View v;
    public SlidingLayer mSlidingLayer;
    private ImageView showTypeImage, showLike;
    private TextView showTitle, showByTime, showContent, showProposal, showNumOfLikes, showStatus;
    private ScrollView detailedScrollView;
    private LinearLayout showHead;
    private Marker marker;
    public static CameraPosition cameraPosition;

    private FloatingActionButton fabAddProblem, fabUkraine, fabToMe;

    private static LatLng markerPosition = null;
    private MapClustering mapClusterer;
    private CoordinatorLayout rootLayout;

    //for rotating screen - save last position of SlidingPanel
    private static boolean isOpenSlidingLayer = false, addProblemModeActivated;
    public static Problem lastOpenProblem;

    Snackbar addProblemSnackbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.i(tag, "onCreateView");

        v = inflater.inflate(R.layout.map_layout_main, container, false);
        mapView = (MapView) v.findViewById(R.id.mapview);

        //Temporary is to initialize mapView by null to get rotation works without exceptions.
        mapView.onCreate(null);
        mMap = mapView.getMap();
        mMap.setMyLocationEnabled(true);

        MapsInitializer.initialize(getActivity());

        mMap = mapView.getMap();
        mMap.setMyLocationEnabled(true);
        UISettings = mMap.getUiSettings();
        UISettings.setMapToolbarEnabled(false);
        UISettings.setMyLocationButtonEnabled(false);

        values = new ArrayList<>();
        mContext = getActivity();

        fabAddProblem = (FloatingActionButton) v.findViewById(R.id.fabAddProblem);
        rootLayout = (CoordinatorLayout) v.findViewById(R.id.rootLayout);

        fabAddProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isUserIdSet()) {

                    if (mapClusterer.getMarker() == null) {
                        setMarkerClickType(2);
                        fabAddProblem.setImageResource(R.drawable.ic_done_white_24dp);

                        addProblemSnackbar.setAction(getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setMarkerClickType(0);
                                mapClusterer.deleteMarker();
                                fabAddProblem.setImageResource(R.drawable.ic_add_white_24dp);
                            }
                        });

                        addProblemSnackbar.show();

                    } else {
                        new AddProblemFragment().show(getFragmentManager(), "add_problem_layout");
                    }
                } else {
                    signInAlertDialog();
                }
            }
        });

        fabUkraine = (FloatingActionButton) v.findViewById(R.id.fabUkraine);
        fabUkraine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5f));
            }
        });

        fabToMe = (FloatingActionButton) v.findViewById(R.id.fabToMe);
        fabToMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = mMap.getMyLocation();
                if (loc != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 14.0f));
                }
            }
        });

        showTypeImage = (ImageView) v.findViewById(R.id.show_type_image);
        showLike = (ImageView) v.findViewById(R.id.show_like);
        showTitle = (TextView) v.findViewById(R.id.show_title);
        showByTime = (TextView) v.findViewById(R.id.show_date_added);
        showContent = (TextView) v.findViewById(R.id.show_content);
        showProposal = (TextView) v.findViewById(R.id.show_proposal);
        showNumOfLikes = (TextView) v.findViewById(R.id.show_numOfLikes);
        showHead = (LinearLayout) v.findViewById(R.id.show_head);
        showStatus = (TextView) v.findViewById(R.id.show_status);
        detailedScrollView = (ScrollView) v.findViewById(R.id.details_scrollview);

        mSlidingLayer = (SlidingLayer) v.findViewById(R.id.show_problem_sliding_layer);

//        LayerTransformer transformer = new AlphaTransformer();
//        mSlidingLayer.setLayerTransformer(transformer);

        mSlidingLayer.setOnInteractListener(new SlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {
                //If onOpen, we show all lines of title
                showTitle.setMaxLines(99);
                showTitle.setEllipsize(null);

                //set scroll UP
                detailedScrollView.fullScroll(View.FOCUS_UP);//if you move at the end of the scroll
                detailedScrollView.pageScroll(View.FOCUS_UP);//if you move at the middle of the scroll

            }

            @Override
            public void onShowPreview() {

                //If onPreview, we show only 1 line of title
                showTitle.setMaxLines(1);
                showTitle.setEllipsize(TextUtils.TruncateAt.END);
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onOpened() {
                isOpenSlidingLayer = true;
            }

            @Override
            public void onPreviewShowed() {
            }

            @Override
            public void onClosed() {
                isOpenSlidingLayer = false;
            }
        });

        if (isOpenSlidingLayer) {
            mSlidingLayer.openPreview(true);
            fillSlidingPanel(lastOpenProblem);
        }

        ExpandableHeightGridView gridview = (ExpandableHeightGridView) v.findViewById(R.id.gridview);
        gridview.setExpanded(true);

        imgAdapter = new ImageAdapter(mContext, new ArrayList<ProblemPhotoEntry>());
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

        addProblemSnackbar = Snackbar.make(rootLayout, getString(R.string.choose_location), Snackbar.LENGTH_INDEFINITE);
        View snackBarView = addProblemSnackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.accent));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_action);

        textView.setTextColor(Color.WHITE);
    }

    // saving map position for restoring after rotation or backstack
    @Override
    public void onPause() {
        super.onPause();
        cameraPosition = mMap.getCameraPosition();
        // unregistering receiver after pausing fragment
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy");

        mapView.onDestroy();
        // if current map fragment is last in backstack - kill activity
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            getActivity().finish();
        }
        Log.i(tag, "mapView onDestroy");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private class EcoMapReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillMap(filterCondition);
        }
    }

    private void setUpMap() {
        Log.i(tag, "set up map");

        if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                .isNetworkAvailable()) {
            //Start service to get a new number of revision and new data
            Intent intent = new Intent(this.getActivity(), EcoMapService.class);
            getActivity().startService(intent);
        }
        else{
            fillMap(filterCondition);
        }
    }

    //if there is some filter condition, then this method will be called. It will search only needed points
    public void fillMap(String filterCondition) {
        if (filterCondition == null){
            filterCondition = "";
        }
        Log.i(tag, "fillmap with condition" + filterCondition);

        values.clear();
        mMap.clear();

        cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, filterCondition, null, null);

        while (cursor.moveToNext()) {

            Problem p = new Problem(cursor, getActivity());
            values.add(p);
        }

        cursor.close();
        mapClusterer = new MapClustering(cameraPosition, mMap, mContext, values, this);
        mapClusterer.setUpClusterer();
    }

    public static void setFilterCondition(String s) {
        filterCondition = s;
        Log.i(tag, "condition is set:" + filterCondition);
    }

    public static void setMarkerClickType(int type) {
        markerClickType = type;
    }

    public static int getMarkerClickType(){
        return markerClickType;
    }

    public static LatLng getMarkerPosition() {
        return markerPosition;
    }

    public static void setMarkerPosition(LatLng position) {
        markerPosition = position;
    }

    public void fillSlidingPanel(final Problem problem){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(problem.getPosition(), 11.0f));

        //Set Problem object parameters to a view at show problem fragment
        showTypeImage.setImageResource(problem.getResBigImage());
        showTitle.setText(problem.getTitle());
        showByTime.setText(problem.getUserDate());
        showContent.setText(problem.getContent());
        showProposal.setText(problem.getProposal());
        showNumOfLikes.setText(problem.getNumberOfLikes());

        if (problem.isLiked()){
            showLike.setImageResource(R.drawable.heart_icon);
        }else{
            showLike.setImageResource(R.drawable.heart_empty);
        }

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

                    new AddVoteTask().execute(problem.getId());

                    showLike.setImageResource(R.drawable.heart_icon);

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
        Fragment f = chFm.findFragmentByTag(CommentsFragment.LOG_TAG);
        //if (f == null) {
        f = CommentsFragment.newInstance(problem);
        //}
        chFm.beginTransaction().replace(R.id.fragment_comments, f, CommentsFragment.LOG_TAG).commit();

        //photos
        new GetPhotosTask(this).execute(problem.getId());
    }

    private void signInAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(R.string.error_need_to_sign_in);

        alert.setPositiveButton(getString(R.string.sign_in), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LoginFragment().show(getFragmentManager(), "login_layout");
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.show();
    }

    /*private void showTabLayout(){

        if (tabLayout != null){
            tabLayout.removeAllTabs();
            tabLayout.setVisibility(TabLayout.VISIBLE);
        }

        tabLayout = (TabLayout) v.findViewById(R.id.tabLayout);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.primary));
        tabLayout.setTabTextColors(getResources().getColor(R.color.white), getResources().getColor(R.color.secondary_text));
        tabLayout.addTab(tabLayout.newTab().setText("Choose Location"));
        tabLayout.addTab(tabLayout.newTab().setText("Add Description"));

        viewPager = (ViewPager) v.findViewById(R.id.pager);

        adapter = new PagerAdapter(getFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }*/
}

