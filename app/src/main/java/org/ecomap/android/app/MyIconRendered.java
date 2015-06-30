package org.ecomap.android.app;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Stanislav on 27.06.2015.
 */
public class MyIconRendered extends DefaultClusterRenderer<Problem> {
    public MyIconRendered(Context context, GoogleMap map, ClusterManager<Problem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(Problem problem,
                                               MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(problem.res_id));
    }
}
