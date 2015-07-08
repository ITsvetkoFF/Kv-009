package org.ecomap.android.app.sync;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by yridk_000 on 03.07.2015.
 */
public class MyService extends Service {
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    Callbacks activity;
    private long startTime = 0;
    private long millis = 0;
    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            activity.updateClient(millis); //Update Activity (client) by the implementd callback
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Do what you need in onStartCommand when service has been started
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public MyService getServiceInstance(){
            return MyService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    public void startCounter(){
        startTime = System.currentTimeMillis();
        handler.postDelayed(serviceRunnable, 0);
        Toast.makeText(getApplicationContext(), "Counter started", Toast.LENGTH_SHORT).show();
    }

    public void stopCounter(){
        handler.removeCallbacks(serviceRunnable);
    }


    //callbacks interface for communication with service clients!
    public interface Callbacks{
        public void updateClient(long data);
    }
}