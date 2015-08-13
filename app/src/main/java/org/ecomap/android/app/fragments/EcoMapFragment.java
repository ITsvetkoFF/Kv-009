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
import android.widget.Button;
import android.widget.ImageView;
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

import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.CommentPhotoActivity;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.model.ProblemPhotoEntry;
import org.ecomap.android.app.sync.AddVoteTask;
import org.ecomap.android.app.sync.EcoMapService;
import org.ecomap.android.app.sync.GetPhotosTask;
import org.ecomap.android.app.sync.UploadPhotoTask;
import org.ecomap.android.app.ui.components.EcoMapSlidingLayer;
import org.ecomap.android.app.utils.ImageAdapter;
import org.ecomap.android.app.utils.MapClustering;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.ecomap.android.app.utils.SnackBarHelper;
import org.ecomap.android.app.widget.ExpandableHeightGridView;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

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
    private ArrayList<Problem> values;
    private View v;
    public EcoMapSlidingLayer mSlidingLayer;
    private ImageView showTypeImage, showLike;
    private TextView showTitle, showByTime, showContent, showProposal, showNumOfLikes, showStatus;
    private ScrollView detailedScrollView;
    public static CameraPosition cameraPosition;

    private FloatingActionButton fabUkraine, fabToMe, fabAddProblem;

    private LatLng markerPosition = null;
    private MapClustering mapClusterer;
    private CoordinatorLayout rootLayout;

    //for rotating screen - save last position of SlidingPanel
    private static boolean isOpenSlidingLayer = false;
    public static Problem lastOpenProblem;

    private Snackbar addProblemSnackbar;

    private boolean addproblemModeIsEnabled = false;

    private Button addPhotoButton;
    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_PHOTOS_ADDED = 2;
    public static ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "onCreate");
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.i(tag, "onCreateView");

        //getActivity().invalidateOptionsMenu();

        v = inflater.inflate(R.layout.map_layout_main, container, false);
        mapView = (MapView) v.findViewById(R.id.mapview);

        //Temporary is to initialize mapView by null to get rotation works without exceptions.
        mapView.onCreate(null);
        mMap = mapView.getMap();
        mMap.setMyLocationEnabled(true);

        MapsInitializer.initialize(getActivity());


        UISettings = mMap.getUiSettings();
        UISettings.setMapToolbarEnabled(false);
        UISettings.setMyLocationButtonEnabled(false);

        values = new ArrayList<>();
        mContext = getActivity();

        fabAddProblem = (FloatingActionButton) v.findViewById(R.id.fabAddProblem);
        rootLayout = (CoordinatorLayout) v.findViewById(R.id.rootLayout);

        fabUkraine = (FloatingActionButton) v.findViewById(R.id.fabUkraine);
        fabUkraine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingLayer.closeLayer(true);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5f));
            }
        });

        fabToMe = (FloatingActionButton) v.findViewById(R.id.fabToMe);
        fabToMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = mMap.getMyLocation();
                if (loc != null) {
                    mSlidingLayer.closeLayer(true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 14.0f));
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
        showStatus = (TextView) v.findViewById(R.id.show_status);
        detailedScrollView = (ScrollView) v.findViewById(R.id.details_scrollview);
        //deleteProblem = (ImageView) v.findViewById(R.id.action_delete_problem);

        mSlidingLayer = (EcoMapSlidingLayer) v.findViewById(R.id.show_problem_sliding_layer);

        mSlidingLayer.setOnInteractListener(new EcoMapSlidingLayer.OnInteractListener() {
            @Override
            public void onOpen() {
                //If onOpen, we show all lines of title
                showTitle.setMaxLines(5);
                showTitle.setEllipsize(TextUtils.TruncateAt.END);

                //set scroll UP
                detailedScrollView.fullScroll(View.FOCUS_UP);//if you move at the end of the scroll
                detailedScrollView.pageScroll(View.FOCUS_UP);//if you move at the middle of the scroll
                isOpenSlidingLayer = true;

            }

            @Override
            public void onShowPreview() {

                //If onPreview, we show only 1 line of title
                showTitle.setMaxLines(1);
                showTitle.setEllipsize(TextUtils.TruncateAt.END);
                isOpenSlidingLayer = true;
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
                MainActivity.currentProblem = null;
                getActivity().invalidateOptionsMenu();
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

        addPhotoButton = (Button) v.findViewById(R.id.add_photo);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send intent to library for picking photos
                if (MainActivity.isUserIdSet()) {
                    PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                    intent.setPhotoCount(8);
                    intent.setShowCamera(true);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    signInAlertDialog();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(getString(R.string.item_map));
        mapView.onResume();

        getActivity().invalidateOptionsMenu();

        IntentFilter filter = new IntentFilter("Data");
        receiver = new EcoMapReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);

        setUpMap();

        addProblemSnackbar = Snackbar.make(rootLayout, getString(R.string.choose_location), Snackbar.LENGTH_INDEFINITE);
        View snackBarView = addProblemSnackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.white));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_action);

        textView.setTextColor(Color.RED);

        addProblemSnackbar.setAction(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAddProblemMode();
            }
        });

        fabAddProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MainActivity.isUserIdSet()) {

                    if (mapClusterer.getMarker() == null) {
                        enableAddProblemMode();
                    } else {
                        ((MainActivity) getActivity()).selectItem(MainActivity.NAV_ADD_PROBLEM);
                    }

                } else {
                    signInAlertDialog();
                }
            }
        });
    }

    // saving map position for restoring after rotation or backstack
    @Override
    public void onPause() {
        super.onPause();
        cameraPosition = mMap.getCameraPosition();
        // unregistering receiver after pausing fragment
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);

        //mSlidingLayer.closeLayer(true);
        MainActivity.currentProblem = null;

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

        if (cameraPosition != null){
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

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

        if (cameraPosition != null){
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

        //for displaying marker and enableAddProblemMode() after screen rotation
        if (markerPosition != null) {
            mapClusterer.addMarkerToMap(markerPosition);
        }

        if (addproblemModeIsEnabled) {
            enableAddProblemMode();
        }
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

    public LatLng getMarkerPosition() {
        return markerPosition;
    }

    public void setMarkerPosition(LatLng position) {
        markerPosition = position;
    }

    public void fillSlidingPanel(final Problem problem){

        if (mMap.getCameraPosition().zoom < 13.0f) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(problem.getPosition(), 13.0f));
        }

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
                if (MainActivity.isUserIsAuthorized()) {
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
                } else {
                    SnackBarHelper.showInfoSnackBar(getActivity(), getActivity().getWindow().getDecorView(), R.string.message_log_in_to_vote, Snackbar.LENGTH_SHORT);
                }
            }
        });

        MainActivity.currentProblem = problem;
        MainActivity.slidingLayer = mSlidingLayer;
        cameraPosition = mMap.getCameraPosition();
        getActivity().invalidateOptionsMenu();

//        if (User.canUserDeleteProblem(problem)){
//            deleteProblem.setVisibility(View.VISIBLE);
//
//            deleteProblem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//
//                    cameraPosition = mMap.getCameraPosition();
//
//                    alert.setMessage(getString(R.string.want_delete_problem));
//                    alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).isNetworkAvailable()) {
//                                new DeleteTask(mContext).execute(String.valueOf(problem.getId()));
//                                mSlidingLayer.closeLayer(true);
//                            }
//                        }
//                    });
//
//
//                    alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    alert.show();
//                }
//            });
//        } else {
//            deleteProblem.setVisibility(View.INVISIBLE);
//        }



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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Getting photo paths from lib
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                selectedPhotos.clear();
                selectedPhotos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);

                if (selectedPhotos.size()>0){
                    Intent intent = new Intent(mContext, CommentPhotoActivity.class);
                    intent.putExtra("problem_id", lastOpenProblem.getId());
                    intent.putExtra("selectedPhotos", selectedPhotos);
                    startActivityForResult(intent, REQUEST_CODE_PHOTOS_ADDED);
                }

            }
        }else if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_PHOTOS_ADDED) {
                ArrayList<ProblemPhotoEntry> photos = data.getParcelableArrayListExtra("photos");
                for (ProblemPhotoEntry photo:photos){
                    new UploadPhotoTask(mContext, lastOpenProblem.getId(), photo.getImgURL(), photo.getCaption()).execute();
                }
            new GetPhotosTask(this).execute(lastOpenProblem.getId());
        }
    }

    public static CameraPosition getCameraPosition(){
        return cameraPosition;
    }

    private void enableAddProblemMode(){
        addproblemModeIsEnabled = true;
        setMarkerClickType(2);

        addProblemSnackbar.show();
        fabAddProblem.setImageResource(R.drawable.ic_done_white_24dp);
    }

    public void disableAddProblemMode(){
        addproblemModeIsEnabled = false;
        setMarkerClickType(0);

        mapClusterer.deleteMarker();

        addProblemSnackbar.dismiss();
        fabAddProblem.setImageResource(R.drawable.ic_location_on_white_24dp);

        ((MainActivity) getActivity()).deleteAddproblemFragment();

    }

    public boolean isAddproblemModeIsEnabled() {
        return addproblemModeIsEnabled;
    }

}

