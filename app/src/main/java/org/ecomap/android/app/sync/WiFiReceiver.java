package org.ecomap.android.app.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.ecomap.android.app.utils.SharedPreferencesHelper;

public class WiFiReceiver extends BroadcastReceiver {



    public WiFiReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(SharedPreferencesHelper.getFlagPendingProblems()){
            ConnectivityManager conMngr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo wifi = conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //android.net.NetworkInfo mobile = conMngr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(wifi.isConnected()){

                SendPendingProblemService.startUploadingProblems(context);
            }
        }

    }
}
