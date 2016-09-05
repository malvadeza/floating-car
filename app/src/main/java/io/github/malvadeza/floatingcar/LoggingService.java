package io.github.malvadeza.floatingcar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LoggingService extends Service {
    private static final String TAG = LoggingService.class.getSimpleName();

    private static boolean RUNNING = false;

    public static final String START_SERVICE = "io.github.malvadeza.floatingcar.action.start_service";
    public static final String SERVICE_BROADCAST_MESSAGE = "io.github.malvadeza.floatingcar.broadcast_message";

    public LocalBroadcastManager mBroadcastManager = null;

    public LoggingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        Log.d(TAG, "onStartCommand");

        RUNNING = true;

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        RUNNING = false;
    }



    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static synchronized boolean isRunning() {
        return RUNNING;
    }
}
