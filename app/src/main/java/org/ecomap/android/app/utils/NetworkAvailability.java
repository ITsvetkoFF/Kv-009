package org.ecomap.android.app.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Stanislav on 27.07.2015.
 */
public class NetworkAvailability {

    private ConnectivityManager connectivityManager;

    public NetworkAvailability(Object systemService){
        this.connectivityManager = (ConnectivityManager) systemService;
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
