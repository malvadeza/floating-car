package io.github.malvadeza.floatingcar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;

import io.github.malvadeza.floatingcar.bluetooth.BluetoothConnection;

public class LoggingService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = LoggingService.class.getSimpleName();

    private static boolean RUNNING = false;

    public static final String SERVICE_START =
            "io.github.malvadeza.floatingcar.logging_service.service_start";
    public static final String SERVICE_START_LOGGING =
            "io.github.malvadeza.floatingcar.logging_service.service_start_logging";
    public static final String SERVICE_BROADCAST_MESSAGE =
            "io.github.malvadeza.floatingcar.logging_service.broadcast_message";
    public static final String SERVICE_STARTED =
            "io.github.malvadeza.floatingcar.logging_service.service_started";
    public static final String SERVICE_CONNECTING =
            "io.github.malvadeza.floatingcar.logging_service.service_connecting";
    public static final String SERVICE_CONNECTED =
            "io.github.malvadeza.floatingcar.logging_service.service_connected";
    public static final String SERVICE_BLUETOOTH_ERROR =
            "io.github.malvadeza.floatingcar.logging_service.service_bluetooth_error";
    public static final String SERVICE_MESSAGE =
            "io.github.malvadeza.floatingcar.logging_service.message";

    private LocalBroadcastManager mBroadcastManager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private BluetoothConnection mBtConnection;

    private BluetoothHandler mBtHandler;
    private LoggingHandler mLogHandler;
    private BluetoothAdapter mBtAdapter;

    public LoggingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBtAdapter = bluetoothManager.getAdapter();
            }
        } else {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (mLocationRequest == null) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(3 * 1000)
                    .setFastestInterval(1 * 1000);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "onStartCommand");

        if (intent.getAction().equals(SERVICE_START)) {
            Log.d(TAG, "Starting connection to device");
            mBtHandler = new BluetoothHandler(this);

            BluetoothDevice btDevice = intent.getParcelableExtra("bluetoothDevice");

            mBtConnection = new BluetoothConnection(mBtHandler, mBtAdapter);
            mBtConnection.connect(btDevice);
        } else if (intent.getAction().equals(SERVICE_START_LOGGING)) {
            Log.d(TAG, "Start logging data");
            /**
             * Now I should start the Logging thread and pass the socket to it.
             * Then make the Service a Foreground Service.
             */

            BluetoothSocket btSocket = mBtConnection.getSocket();

            mBtHandler = null;
            mBtConnection = null;

            stopSelf();
        }

        RUNNING = true;

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        RUNNING = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static synchronized boolean isRunning() {
        return RUNNING;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public static class BluetoothHandler extends Handler {
        private final WeakReference<LoggingService> loggingServiceReference;

        public BluetoothHandler(LoggingService service) {
            loggingServiceReference = new WeakReference<LoggingService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            LoggingService service = loggingServiceReference.get();

            if (service == null) return;

            switch (msg.what) {
                case BluetoothConnection.BLUETOOTH_CONNECTING_DEVICE: {
                    // Connecting to device
                    // BLUETOOTH_TARGET_DEVICE contains the device name:macaddress
                    Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
                    intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_CONNECTING);

                    service.mBroadcastManager.sendBroadcast(intent);

                    break;
                }
                case BluetoothConnection.BLUETOOTH_CONNECTED_DEVICE: {
                    // Connected to device
                    // send broadcast to activity
                    // BLUETOOTH_TARGET_DEVICE contains the device name:macaddress
                    String address = msg.getData().getString(BluetoothConnection.BLUETOOTH_TARGET_DEVICE);

                    Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
                    intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_CONNECTED);
                    intent.putExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE, address);

                    service.mBroadcastManager.sendBroadcast(intent);

                    intent = new Intent(service, LoggingService.class);
                    intent.setAction(LoggingService.SERVICE_START_LOGGING);

                    service.startService(intent);

                    break;
                }
                case BluetoothConnection.BLUETOOTH_STATE_CHANGE: {
                    break;
                }
                case BluetoothConnection.BLUETOOTH_CONNECTION_ERROR:{
                    Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
                    intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_BLUETOOTH_ERROR);

                    service.mBroadcastManager.sendBroadcast(intent);
                    break;
                }
                default: {
                    Log.e(TAG, "msg.what -> " + msg.what);
                    throw new IllegalArgumentException("Should never be reached");
                }
            }

        }
    }

    public static class LoggingHandler extends Handler {
        private final WeakReference<LoggingService> loggingServiceReference;

        public LoggingHandler(LoggingService service) {
            loggingServiceReference = new WeakReference<LoggingService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            /**
             * Get the data from the thread, and save it to the SQLite database.
             */
        }
    }
}
