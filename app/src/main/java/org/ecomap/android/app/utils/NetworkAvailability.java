package org.ecomap.android.app.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkAvailability {

    private final ConnectivityManager connectivityManager;

    public NetworkAvailability(Object systemService){
        this.connectivityManager = (ConnectivityManager) systemService;
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
