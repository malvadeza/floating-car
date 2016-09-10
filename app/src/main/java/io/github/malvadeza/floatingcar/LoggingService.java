package io.github.malvadeza.floatingcar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
    public static final String SERVICE_BROADCAST_MESSAGE =
            "io.github.malvadeza.floatingcar.logging_service.broadcast_message";
    public static final String SERVICE_STARTED =
            "io.github.malvadeza.floatingcar.logging_service.service_started";
    public static final String SERVICE_MESSAGE =
            "io.github.malvadeza.floatingcar.logging_service.message";

    private LocalBroadcastManager mBroadcastManager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private BluetoothConnection mBtConnection;

    private BluetoothHandler mHandler;
    private BluetoothAdapter mBtAdapter;

    public LoggingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mHandler = new BluetoothHandler(this);

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
            /**
             * Here I should instantiate the Handler and the Bluetooth Connection class
             * passing the handler to it. In the handler "handleMessage" method, I should
             * receive if the connection was either successful or a failure, acting accordingly.
             *
             * If the connection is successful, I should first broadcast a message telling
             * it. Then, make the Service a Foreground Service and start a new Thread to
             * make the logging.
             */
            BluetoothDevice btDevice = intent.getParcelableExtra("bluetoothDevice");

            mBtConnection = new BluetoothConnection(mHandler, mBtAdapter);
            mBtConnection.connect(btDevice);
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
                case BluetoothConnection.BLUETOOTH_CONNECTING_DEVICE:
                    // Connecting to device
                    // BLUETOOTH_TARGET_DEVICE contains the device name:macaddress
                    break;
                case BluetoothConnection.BLUETOOTH_CONNECTED_DEVICE:
                    // Connected to device
                    // send broadcast to activity
                    // BLUETOOTH_TARGET_DEVICE contains the device name:macaddress
                    break;
                default:
                    throw new IllegalArgumentException("Should never be reached");
            }

        }
    }
}
