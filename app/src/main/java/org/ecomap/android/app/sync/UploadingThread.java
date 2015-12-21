package org.ecomap.android.app.sync;

import android.os.HandlerThread;

/**
 * Created by yrid on 21.12.2015.
 */
public class UploadingThread extends HandlerThread {

    public UploadingThread(String name) {
        super(name);
    }

    public UploadingThread(String name, int priority) {
        super(name, priority);
    }
}
