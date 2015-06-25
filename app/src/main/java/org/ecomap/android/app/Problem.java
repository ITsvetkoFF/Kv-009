package org.ecomap.android.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Stanislav on 23.06.2015.
 */
public class Problem implements ClusterItem {
    LatLng mPos;
    String mTitle;
    MarkerOptions marker;

    Problem(double latitude, double longitude, String title){
        this.mPos = new LatLng(latitude, longitude);
        this.mTitle = title;
        setMarker(new MarkerOptions()
                .position(mPos)
                .title(title));
    }

    public String getTitle(){
        return mTitle;
    }

    public void setMarker(MarkerOptions marker) {
        this.marker = marker;
    }

    @Override
    public LatLng getPosition() {
        return mPos;
    }
}
