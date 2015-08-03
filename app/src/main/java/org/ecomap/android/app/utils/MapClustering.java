package org.ecomap.android.app.utils;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
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

import org.ecomap.android.app.MyIconRendered;
import org.ecomap.android.app.Problem;
import org.ecomap.android.app.R;
import org.ecomap.android.app.fragments.EcoMapFragment;

import java.util.ArrayList;

/**
 * Created by Stanislav on 27.07.2015.
 */
public class MapClustering {
    private CameraPosition cameraPosition;
    public static GoogleMap mMap;
    private Context mContext;
    private ArrayList<Problem> values;
    private Marker marker;
    private EcoMapFragment ecoMapFragment;

    private ArrayList<LatLng> points;
    private ArrayList<Marker> markers;
    private ClusterManager mClusterManager;

    public MapClustering(CameraPosition cameraPosition, GoogleMap mMap, Context mContext
            , ArrayList<Problem> values, Marker marker, EcoMapFragment ecoMapFragment){
        points = new ArrayList<>();
        markers = new ArrayList<>();

        this.cameraPosition = cameraPosition;
        this.mMap = mMap;
        this.mContext = mContext;
        this.values = values;
        this.marker = marker;
        this.ecoMapFragment = ecoMapFragment;
    }

    public void setUpClusterer() {
        if(cameraPosition != null){

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        else {
            //Position the map from static variables
            double longitude = 30.417397;
            double latitude = 50.461166;
            float zoomlevel = 5;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomlevel));
        }

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

                    if (marker != null) {
                        marker.remove();
                    }

                    marker = mMap.addMarker(new MarkerOptions().position(latLng));
                    marker.setTitle("Houston we have a problem here!");
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                    EcoMapFragment.setMarkerPosition(latLng);

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
                ecoMapFragment.lastOpenProblem = problem;



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
            EcoMapFragment.setMarkerClickType(0);
            mMap.setOnMarkerClickListener(mClusterManager);
        }
    }

    public void deleteMarker(){
        if (marker != null) {
            marker.remove();
            marker = null;
        }
    }

    public static void zoomCamera(){

        /*CameraUpdate center = CameraUpdateFactory.newLatLng(lastOpenProblem.getPosition());
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);*/

        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(40.76793169992044,-73.98180484771729));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

    }

    public Marker getMarker() {
        return marker;
    }
}
