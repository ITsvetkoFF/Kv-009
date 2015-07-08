package org.ecomap.android.app.fragments;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.software.shell.fab.ActionButton;
import com.wunderlist.slidinglayer.SlidingLayer;

import org.ecomap.android.app.MyIconRendered;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.sync.EcoMapService;

import org.ecomap.android.app.R;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class EcoMapFragment extends Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ClusterManager<Problem> mClusterManager;
    Context mContext;
    private ArrayList<Problem> values;
    private ArrayList<LatLng> points;
    private ArrayList<Marker> markers;
    Cursor cursor;
    EcoMapReceiver receiver;
    ActionButton actionButton;
    SlidingLayer slidingLayer;
    Button cancelButton;
    
    MapView mapView;
    // Might be null if Google Play services APK is not available.


    private static int markerClickType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_layout_main, container, false);

        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);



        MapsInitializer.initialize(this.getActivity());

        slidingLayer= (SlidingLayer) v.findViewById(R.id.slidingLayer1);
        slidingLayer.setSlidingEnabled(false);
        cancelButton=(Button)v.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingLayer.closeLayer(true);
                actionButton.show();
            }
        });




        actionButton = (ActionButton) v.findViewById(R.id.action_button);
        actionButton.show();
        actionButton.setType(ActionButton.Type.DEFAULT);
        actionButton.setButtonColor(getResources().getColor(R.color.fab_material_lime_500));
        actionButton.setImageResource(R.drawable.fab_plus_icon);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButton.hide();
                slidingLayer.openLayer(true);
                //call the wunderlist
            }
        });
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        values = new ArrayList<>();
        points = new ArrayList<>();
        markers = new ArrayList<>();
        mContext = getActivity();

        IntentFilter filter = new IntentFilter("Data");
        receiver = new EcoMapReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);

        setUpMap();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void setUpMap() {

        cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null, null, null);

        //Start service to get a new number of revision and new data
        Intent intent = new Intent(this.getActivity(), EcoMapService.class);
        getActivity().startService(intent);

        fillMap();

    }

    public void fillMap() {
        double latitude, longitude;
        String title;
        int type_id;

        values.clear();
        mMap.clear();

        cursor = getActivity().getContentResolver()
                .query(EcoMapContract.ProblemsEntry.CONTENT_URI, null, null, null, null, null);

        while (cursor.moveToNext()) {
            latitude = cursor.getDouble(cursor
                    .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE));
            longitude = cursor.getDouble(cursor
                    .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE));
            title = cursor.getString(cursor
                    .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_TITLE));
            type_id = cursor.getInt(cursor
                    .getColumnIndex(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID));
            Problem p = new Problem(latitude, longitude, title, type_id, getActivity());
            values.add(p);
        }

        cursor.close();
        setUpClusterer();
    }

    public void setUpClusterer() {
        //Position the map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5));

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
        mClusterManager.setRenderer(new MyIconRendered(mContext, mMap, mClusterManager));
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

