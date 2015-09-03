package org.ecomap.android.app.utils;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import org.ecomap.android.app.MyIconRendered;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.EcoMapFragment;

import java.util.ArrayList;

public class MapClustering {
    private CameraPosition cameraPosition;
    private final GoogleMap mMap;
    private final Context mContext;
    private ArrayList<Problem> values;
    private Marker marker;
    private EcoMapFragment ecoMapFragment;

    private ArrayList<LatLng> points;
    private ArrayList<Marker> markers;
    private ClusterManager<Problem> mClusterManager;

    public MapClustering(CameraPosition cameraPosition, GoogleMap mMap, Context mContext, EcoMapFragment ecoMapFragment){
        points = new ArrayList<>();
        markers = new ArrayList<>();

        this.cameraPosition = cameraPosition;
        this.mMap = mMap;
        this.mContext = mContext;
        this.ecoMapFragment = ecoMapFragment;

        //Initialize the manager with the mContext and the map.
        mClusterManager = new ClusterManager<>(mContext, mMap);
    }

    public MapClustering (GoogleMap mMap, Context mContext) {
        this.mMap = mMap;
        this.mContext = mContext;
    }

    public void setUpClusterer() {
        if(cameraPosition != null){
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5));
        }

        //Point the map's listeners at the listeners implemented by the cluster.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (EcoMapFragment.getMarkerClickType() == 1) {
                    Marker m = mMap.addMarker(new MarkerOptions().position(latLng));
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    markers.add(m);
                    points.add(latLng);
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (EcoMapFragment.getMarkerClickType() == 1) {
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

                } else if (EcoMapFragment.getMarkerClickType() == 2) {

                    addMarkerToMap(latLng);

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

                ecoMapFragment.fillSlidingPanel(problem);

                //Set part of sliding layer visible
                ecoMapFragment.mSlidingLayer.openPreview(true);

                //save last open Problem for rotating screen
                EcoMapFragment.lastOpenProblem = problem;

                return false;
            }
        });

        mClusterManager.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<Problem>());
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
            EcoMapFragment.setMarkerClickType(0);
            mMap.setOnMarkerClickListener(mClusterManager);
        }
    }

    public void deleteMarker(){
        if (marker != null) {
            marker.remove();
            marker = null;
            EcoMapFragment.setMarkerPosition(null);
        }
    }

    public Marker getMarker() {
        return marker;
    }

    public void addMarkerToMap(LatLng position){

        if (marker != null) {
            marker.remove();
        }

        marker = mMap.addMarker(new MarkerOptions().draggable(true).position(position));
        marker.setTitle(mContext.getString(R.string.have_problem));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        EcoMapFragment.setMarkerPosition(marker.getPosition());
    }

    public void updateEntryParameters(ArrayList<Problem> values){
        this.values = values;

        mClusterManager.clearItems();
        mClusterManager.addItems(values);
    }
}
