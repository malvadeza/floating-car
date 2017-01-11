package io.github.malvadeza.floatingcar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import io.github.malvadeza.floatingcar.services.SyncService;

public class NetworkStatusReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkStatusReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            Log.d(TAG, "Network connected");
            // Start service for upload data

            Intent serviceIntent = new Intent(context, SyncService.class);
            serviceIntent.setAction(SyncService.SYNC_TRIPS);
            context.startService(serviceIntent);

            serviceIntent = new Intent(context, SyncService.class);
            serviceIntent.setAction(SyncService.SYNC_SAMPLES);
            context.startService(serviceIntent);
        }
    }
}
