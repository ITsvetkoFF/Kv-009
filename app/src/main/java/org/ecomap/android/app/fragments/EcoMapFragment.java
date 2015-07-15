package org.ecomap.android.app.fragments;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.wunderlist.slidinglayer.SlidingLayer;

import org.ecomap.android.app.MyIconRendered;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.sync.EcoMapService;

import java.util.ArrayList;

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


    private static int markerClickType;
    private View v;
    private SlidingLayer slidingLayer;
    private SlidingLayer addProblemSliding;
    private ImageView showType, showLike;
    private TextView showTitle, showByTime, showContent, showProposal, showNumOfLikes, showStatus;
    private RelativeLayout showHead;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.map_layout_main, container, false);

        mapView = (MapView) v.findViewById(R.id.mapview);

        //Temporary is to initialize mapView by null to get rotation works without exceptions.
        mapView.onCreate(null);

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
        slidingLayer = (SlidingLayer) v.findViewById(R.id.show_problem_sliding_layer);

        /*actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProblemSliding.openLayer(true);
                //call the wunderlist
            }
        });*/



        cancelButton=(Button)v.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProblemSliding.closeLayer(true);
            }
        });

        showType = (ImageView) v.findViewById(R.id.show_type);
        showLike = (ImageView) v.findViewById(R.id.show_like);
        showTitle = (TextView) v.findViewById(R.id.show_title);
        showByTime = (TextView) v.findViewById(R.id.show_by_time);
        showContent = (TextView) v.findViewById(R.id.show_content);
        showProposal = (TextView) v.findViewById(R.id.show_proposal);
        showNumOfLikes = (TextView) v.findViewById(R.id.show_numOfLikes);
        showHead = (RelativeLayout) v.findViewById(R.id.show_head);
        showStatus = (TextView) v.findViewById(R.id.show_status);



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
        mapView.onDestroy();
        // if current map fragment is last in backstack - kill activity
        if (getFragmentManager().getBackStackEntryCount() == 0){
            getActivity().finish();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void setUpMap() {

        //Start service to get a new number of revision and new data
        Intent intent = new Intent(this.getActivity(), EcoMapService.class);
        getActivity().startService(intent);

    }

    public void fillMap() {

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
                        mMap.getCameraPosition().zoom));

                //Set Problem object parameters to a view at show problem fragment
                showType.setImageResource(problem.getRes_id());
                showTitle.setText(problem.getTitle());
                showByTime.setText(problem.getByTime());
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
                        } else if (problem.isLiked()) {
                            problem.setNumberOfLikes(-1);
                            problem.setLiked(false);
                        }
                        showNumOfLikes.setText(problem.getNumberOfLikes());
                    }
                });

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

    private class EcoMapReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            fillMap();


        }
    }


}

