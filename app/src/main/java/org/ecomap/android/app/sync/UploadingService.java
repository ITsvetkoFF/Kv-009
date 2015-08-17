package org.ecomap.android.app.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.ecomap.android.app.PersistentCookieStore;
import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.tasks.UploadPhotoTask;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;

//import org.ecomap.android.app.IRemoteServiceCallback;

/**
 * Created by y.ridkous@gmail.com on 03.07.2015.
 */
public class UploadingService extends Service {


    private boolean DEBUG = true;

    public static final String LOG = UploadingService.class.getSimpleName();

    /**
     * Command to the service to register a messenger, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the messenger where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UPLOAD_PHOTO = 4;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_VALUE = 3;
    public static final int MSG_GET_TASKS_LIST = 5;
    public static final int MSG_TASK_FINISHED = 6;


    public static final String ACTION_DISMISS = "ACTION_DISMISS";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public static final int UPLOADING_NOTIFICATION_ID = 1;

    ServiceClientManager scManager = new ServiceClientManager();

    /**
     * For showing and hiding our notification.
     */
    NotificationManager mNM;

    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    /**
     * Holds last value set by a messenger.
     */
    int mValue = 0;

    private NotificationCompat.Builder foregroundNotificationBuilder;

    /**
     * If service runs in dedicated process it needs to load cookies from shared preferences
     */
    private CookieManager cookieManager;
    private boolean mIsForeground;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //android.os.Debug.waitForDebugger();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_DISMISS)) {
            Log.v(LOG, intent.getAction());
            stopForeground(true);
            stopSelf();
            //mNM.cancel(333);
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) {
            Log.v(LOG, "binding, action: " + intent.getAction());
            Log.v(LOG, "Thread id: " + Thread.currentThread().getId());
        }

        //mNM.notify(333, getForegroundNotification());


        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.
        //showNotification();

        foregroundNotificationBuilder = getNotification();

        cookieManager = new CookieManager(new PersistentCookieStore(this), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);

    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        //mNM.cancel(333);
        // Tell the user we stopped.
        if (DEBUG) Log.v(LOG, "remote_service_stopped");
    }

    public void setDebugMode(boolean flag) {
        DEBUG = flag;
    }



    private Notification getForegroundNotification(){

        final Notification notification = foregroundNotificationBuilder.build();

/*
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_uploading);
        contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.title, "Custom notification");
        contentView.setTextViewText(R.id.text, "This is a custom layout");
        notification.bigContentView = contentView;
*/

        return notification;
    }

    /**
     * Show a notification while this service is running.
     */
    private NotificationCompat.Builder getNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.notify_photos_loading));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ic_clear_white_24dp);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // The PendingIntent to launch our activity if the user selects this notification
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //builder.setContentIntent(contentIntent);

        Intent dismissIntent = new Intent(this, UploadingService.class);
        dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        //builder.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(getString(R.string.notify_photos_loading)));
        builder.addAction(R.drawable.ic_clear_white_24dp, "Cancel", piDismiss);

        //builder.

        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        return builder;
    }


    /**
     * Hold references on Activity and associated AsyncTasks
     */
    private static class ClientActivityHolder {
        Messenger messenger;
        ArrayList<AsyncTask> asyncTasks;

        public ClientActivityHolder(Messenger clientMessenger) {
            this.messenger = clientMessenger;
            this.asyncTasks = new ArrayList<>();
        }

        public ClientActivityHolder(Messenger clientMessenger, ArrayList<AsyncTask> asyncTasks) {
            this.messenger = clientMessenger;
            this.asyncTasks = asyncTasks;
        }
    }

    /**
     * Manage clients that connect to the service.
     * Store and update ClientActivityHolders.
     */
    private class ServiceClientManager {

        public final String TAG = getClass().getSimpleName();

        private HashMap<String, ClientActivityHolder> clientsMap = new HashMap<>();
        private int pendingTasks;
        private int finishedTasks;
        private int globalProgress;


        public void onTaskStarted() {
            pendingTasks++;
            if (mIsForeground) {
                foregroundNotificationBuilder.setContentInfo("" + finishedTasks + "/" + pendingTasks);
                mNM.notify(UPLOADING_NOTIFICATION_ID, getForegroundNotification());
            }
        }

        public void onTaskFinished() {
            finishedTasks++;
            if (mIsForeground) {
                foregroundNotificationBuilder.setContentInfo("" + finishedTasks + "/" + pendingTasks);
                mNM.notify(UPLOADING_NOTIFICATION_ID, getForegroundNotification());
            }
            if(finishedTasks == pendingTasks){
                allTasksFinished();
            }
        }


        public void updateGlobalProgress(int progress){
            globalProgress += progress;
            if(DEBUG) Log.v(LOG, "pending task: " + (100 * pendingTasks) + " | globalProgress: " + globalProgress);
            if(mIsForeground) {
                foregroundNotificationBuilder.setProgress(100 * pendingTasks, globalProgress, false);
                mNM.notify(UploadingService.UPLOADING_NOTIFICATION_ID, getForegroundNotification());
            }
        }

        public void allTasksFinished(){
            stopForeground(true);
            stopSelf();
        }

        /**
         * Register messenger's messengers into the map. If messenger already exists,
         * updates link to messenger on case caller activity was recreated.
         *
         * @param className Activity class name
         * @param msn       Activity incoming messenger for callbacks
         */
        public boolean registerClient(String className, Messenger msn) {
            if (!clientsMap.containsKey(className)) {
                clientsMap.put(className, new ClientActivityHolder(msn));
                return true;
            } else {
                final ClientActivityHolder activityHandler = clientsMap.get(className);
                activityHandler.messenger = msn;
                return false;
            }

        }

        public String[] getTaskList(String className, Messenger replyTo) {
            return null;
        }

        /**
         * Send message to the client, if client was killed while AsyncTask
         * was doing its job, find and send message to the current client.
         *
         * @param className
         * @param msg
         */
        private void sendMessageToClient(String className, Message msg) {
            try {
                ClientActivityHolder clientHandler = clientsMap.get(className);
                clientHandler.messenger.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "sendMessageToClient: " + e.getMessage());
            }
        }

        public void addAndStartUploadingTask(final String className, Messenger msn, final int problemId, final String photoURL, String comment) {

            registerClient(className, msn);

            final ClientActivityHolder clientHandler = clientsMap.get(className);
            final UploadPhotoTask asyncTask = new UploadPhotoTask(getBaseContext(), problemId, photoURL, comment) {

                public final String LOG_TAG = getClass().getSimpleName();

                @Override
                protected void onPreExecute() {
                    Log.d(LOG_TAG, "onPreExecute");
                    if (!mIsForeground) {
                        startForeground(UPLOADING_NOTIFICATION_ID, getForegroundNotification());
                        mIsForeground = true;
                    }
                    onTaskStarted();
                }


                @Override
                protected void onProgressUpdate(Integer... values) {
                    if(DEBUG) Log.d(LOG_TAG, "onProgressUpdate " + values[0]);
                    updateGlobalProgress(values[0]);
                }


                @Override
                protected void onPostExecute(Void o) {
                    if(DEBUG) Log.d(LOG_TAG, "onPostExecute ");

                    onTaskFinished();

                    final Message message = Message.obtain(null, MSG_TASK_FINISHED, null);
                    Bundle params = new Bundle();
                    params.putInt("PROBLEM_ID", problemId);
                    params.putString("PHOTO_URL", photoURL);
                    message.setData(params);
                    sendMessageToClient(className, message);

                }
            };
            clientHandler.asyncTasks.add(asyncTask);
            asyncTask.execute();

        }

    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT: {
                    mClients.add(msg.replyTo);
                    Bundle data = msg.getData();
                    String className = data.getString("CLASS_NAME");
                    scManager.registerClient(className, msg.replyTo);
                    if(DEBUG) Log.v("MSG_REGISTER_CLIENT", msg.replyTo.toString());
                    break;
                }
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;

                case MSG_GET_TASKS_LIST: {
                    Bundle data = msg.getData();
                    String className = data.getString("CLASS_NAME");
                    scManager.getTaskList(className, msg.replyTo);
                }

                case MSG_UPLOAD_PHOTO:
                    Bundle data = msg.getData();
                    String className = data.getString("CLASS_NAME");
                    int problemId = data.getInt("PROBLEM_ID");
                    String photoURL = data.getString("PHOTO_URL");
                    String comment = data.getString("COMMENT");

                    scManager.addAndStartUploadingTask(className, msg.replyTo, problemId, photoURL, comment);

                    if(DEBUG) Log.v("MSG_UPLOAD_PHOTO", photoURL);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
