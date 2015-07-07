package org.ecomap.android.app.fragments;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.software.shell.fab.ActionButton;

import org.ecomap.android.app.FloatingButton;
import org.ecomap.android.app.ProblemsTask;
import org.ecomap.android.app.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class EcoMapFragment extends Fragment {

    private GoogleMap map;
    MapView mapView;
    // Might be null if Google Play services APK is not available.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_layout_main, container, false);


        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);



            MapsInitializer.initialize(this.getActivity());

        setUpMap();

        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate); */


       /* FloatingButton fabButton = new FloatingButton.Builder(getActivity())

                .withButtonColor(Color.WHITE)
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 16, 16)
                .withDrawable(getResources().getDrawable(R.drawable.plusic_blue))
                .create(); */
        ActionButton actionButton = (ActionButton) v.findViewById(R.id.action_button);
        actionButton.show();
        actionButton.setType(ActionButton.Type.DEFAULT);
        actionButton.setButtonColor(getResources().getColor(R.color.fab_material_lime_500));
        actionButton.setImageResource(R.drawable.fab_plus_icon);
        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();

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
        ProblemsTask p = new ProblemsTask(map, getActivity());
        p.execute();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.461166, 30.417397), 5));
    }
}

