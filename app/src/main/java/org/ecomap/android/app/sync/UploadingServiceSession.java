package org.ecomap.android.app.sync;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.ecomap.android.app.utils.SnackBarHelper;

/**
 * Created by y.ridkous@gmail.com on 13.08.2015.
 */

public class UploadingServiceSession {

    private Context mContext;
    private final String mHostToken;
    private Callbacks callbackListener;

    /** Messenger for communicating with service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mIsBound;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mMessenger;
    private String LOG = UploadingServiceSession.class.getSimpleName();

    public UploadingServiceSession(Context context, String hostToken, Callbacks callbackListener){
        this.mContext = context;
        this.mHostToken = hostToken;
        this.callbackListener = callbackListener;
        this.mMessenger = new Messenger(new IncomingHandler(context, this.callbackListener));
    }

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler {

        private Callbacks callbackListener;
        private Context mContext;

        public IncomingHandler(Context mContext, Callbacks callbackListener) {
            this.callbackListener = callbackListener;
            this.mContext = mContext;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UploadingService.MSG_TASK_FINISHED:
                    Bundle data = msg.getData();
                    SnackBarHelper.showSuccessSnackBar((Activity)mContext, data.getString("PHOTO_URL") + " uploaded.", Snackbar.LENGTH_SHORT);
                    break;
                case UploadingService.MSG_ALL_TASKS_FINISHED:
                    callbackListener.allTasksFinished();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            //mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, UploadingService.MSG_REGISTER_CLIENT);
                Bundle params = new Bundle();
                params.putString("CLASS_NAME", mHostToken);
                msg.setData(params);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            //mCallbackText.setText("Disconnected.");
            mIsBound = false;
        }
    };


    public void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        //if(!mIsBound) {
            final Intent intent = new Intent(mContext, UploadingService.class);
//        intent.setAction("org.ecomap.android.app.PHOTOS_UPLOADING");
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //}

    }

    public void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, UploadingService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }


            // Detach our existing connection.
            if(isMyServiceRunning(UploadingService.class)) {
                mContext.unbindService(mConnection);
            }
            //mIsBound = false;
        }
    }

    public void doStartService(){
        if(!mIsBound){
            doBindService();
            //while (!mIsBound);
        }
        Intent intent = new Intent(mContext, UploadingService.class);
        mContext.startService(intent);
    }

    public boolean isBound() {
        return mIsBound;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void sendUploadRequest(int problemId, String photoURL, String comment){
        Bundle params = new Bundle();
        params.putString("CLASS_NAME", mHostToken);
        params.putInt("PROBLEM_ID", problemId);
        params.putString("PHOTO_URL", photoURL);
        params.putString("COMMENT", comment);
        Message msg = Message.obtain(null, UploadingService.MSG_UPLOAD_PHOTO);
        msg.setData(params);
        msg.replyTo = mMessenger;

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    public interface Callbacks{

        void allTasksFinished();

    }

}
