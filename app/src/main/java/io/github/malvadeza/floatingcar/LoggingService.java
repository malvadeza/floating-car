package io.github.malvadeza.floatingcar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.malvadeza.floatingcar.bluetooth.BluetoothConnection;
import io.github.malvadeza.floatingcar.database.FloatingCarContract;
import io.github.malvadeza.floatingcar.database.FloatingCarDbHelper;

public class LoggingService extends Service {
    private static final String TAG = LoggingService.class.getSimpleName();


    private static boolean RUNNING = false;

    public static final String SERVICE_START =
            "io.github.malvadeza.floatingcar.logging_service.service_start";
    public static final String SERVICE_START_LOGGING =
            "io.github.malvadeza.floatingcar.logging_service.service_start_logging";
    public static final String SERVICE_STOP_LOGGING =
            "io.github.malvadeza.floatingcar.logging_service.service_stop_logging";
    public static final String SERVICE_BROADCAST_MESSAGE =
            "io.github.malvadeza.floatingcar.logging_service.broadcast_message";
    public static final String SERVICE_STARTED =
            "io.github.malvadeza.floatingcar.logging_service.service_started";
    public static final String SERVICE_NEW_DATA =
            "io.github.malvadeza.floatingcar.logging_service.location_changed";
    public static final String SERVICE_LOCATION_LATLNG =
            "io.github.malvadeza.floatingcar.logging_service.location_latlng";
    public static final String SERVICE_DATA_SPEED =
            "io.github.malvadeza.floatingcar.logging_service.data_speed";
    public static final String SERVICE_DATA_THROTTLE =
            "io.github.malvadeza.floatingcar.logging_service.data_throttle";
    public static final String SERVICE_DATA_RPM =
            "io.github.malvadeza.floatingcar.logging_service.data_rpm";
    public static final String SERVICE_DATA_TEMPERATURE =
            "io.github.malvadeza.floatingcar.logging_service.data_temperature";
    public static final String SERVICE_LOCATION_ERROR =
            "io.github.malvadeza.floatingcar.logging_service.location_latlng";
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

    private LoggingThread mLoggingThread;

    private BluetoothHandler mBtHandler;
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
//            mLoggingThread = new LoggingThread(this, mBtConnection.getSocket());
            mLoggingThread = new LoggingThread(this);

            mBtHandler = null;
            mBtConnection = null;

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(mLoggingThread)
                        .addOnConnectionFailedListener(mLoggingThread)
                        .addApi(LocationServices.API)
                        .build();
            }

            if (mLocationRequest == null) {
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(3 * 1000)
                        .setFastestInterval(1 * 1000);
            }

            mGoogleApiClient.connect();

            Intent stopIntent = new Intent(this, LoggingService.class);
            stopIntent.setAction(SERVICE_STOP_LOGGING);

            PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.notification_content_title))
                    .setTicker("Ticker text")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_dialog_alert, "Stop Logging", pStopIntent)
                    .build();

            startForeground(10, notification);

            new Thread(mLoggingThread).start();
        } else if (intent.getAction().equals(SERVICE_STOP_LOGGING)) {
            stopForeground(true);
            mLoggingThread.stopLogging();
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
                case BluetoothConnection.BLUETOOTH_CONNECTION_ERROR: {
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

    @SuppressWarnings({"MissingPermission"})
    private static class LoggingThread
            implements Runnable,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener {
        private final WeakReference<LoggingService> loggingServiceReference;
        private final BluetoothSocket mBtSocket;

        private final long mTripId;

        private final SQLiteDatabase mDb;

        private boolean shouldBeLogging = true;

        private Location lastLocation;

        public LoggingThread(LoggingService service) {
            this(service, null);
        }

        public LoggingThread(LoggingService service, BluetoothSocket btSocket) {
            loggingServiceReference = new WeakReference<LoggingService>(service);
            mBtSocket = btSocket;
            mDb = new FloatingCarDbHelper(service).getWritableDatabase();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

            ContentValues trip = new ContentValues();
            trip.put(FloatingCarContract.TripEntry.STARTED_AT, formatter.format(new Date()));

            mTripId = mDb.insert(FloatingCarContract.TripEntry.TABLE_NAME, null, trip);
        }

        @Override
        public void run() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
            while (shouldBeLogging) {
                try {
                    /**
                     * Here I get all the stuff and save in database
                     * and send to activity (if there's any)
                     */
                    Thread.sleep(5 * 1000);

                    ContentValues sample = new ContentValues();
                    sample.put(FloatingCarContract.SampleEntry.TIMESTAMP, formatter.format(new Date()));
                    sample.put(FloatingCarContract.SampleEntry.TRIP_ID, mTripId);

                    final long sampleId = mDb.insert(FloatingCarContract.SampleEntry.TABLE_NAME, null, sample);

                    ContentValues phoneData = new ContentValues();
                    phoneData.put(FloatingCarContract.PhoneDataEntry.LATITUDE, Double.toString(lastLocation != null ? lastLocation.getLatitude() : 0.0));
                    phoneData.put(FloatingCarContract.PhoneDataEntry.LONGITUDE, Double.toString(lastLocation != null ? lastLocation.getLongitude() : 0.0));
                    phoneData.put(FloatingCarContract.PhoneDataEntry.SAMPLE_ID, sampleId);

                    mDb.insert(FloatingCarContract.PhoneDataEntry.TABLE_NAME, null, phoneData);

                    LoggingService service = loggingServiceReference.get();

                    Intent intent = new Intent(SERVICE_BROADCAST_MESSAGE);
                    intent.putExtra(SERVICE_MESSAGE, SERVICE_NEW_DATA);
                    intent.putExtra(SERVICE_LOCATION_LATLNG, lastLocation);

                    service.mBroadcastManager.sendBroadcast(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ContentValues trip = new ContentValues();
            trip.put(FloatingCarContract.TripEntry.FINISHED_AT, formatter.format(new Date()));
            mDb.update(FloatingCarContract.TripEntry.TABLE_NAME, trip,
                    FloatingCarContract.TripEntry._ID + " = ?",
                    new String[]{Long.toString(mTripId)}
            );

            Log.d(TAG, "Finished logging");
        }

        public synchronized void stopLogging() {
            Log.d(TAG, "stopLogging");
            shouldBeLogging = false;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "onConnected");

            LoggingService service = loggingServiceReference.get();

            if (service == null) return;

            LocationServices.FusedLocationApi.requestLocationUpdates(service.mGoogleApiClient, service.mLocationRequest, this);

            Location location = LocationServices.FusedLocationApi.getLastLocation(service.mGoogleApiClient);

            if (location != null) {
                synchronized (this) {
                    lastLocation = location;
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");

            LoggingService service = loggingServiceReference.get();

            if (service == null) return;

            synchronized (this) {
                lastLocation = location;
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed");

            LoggingService service = loggingServiceReference.get();
            if (service == null) return;

            Intent intent = new Intent(SERVICE_BROADCAST_MESSAGE);
            intent.putExtra(SERVICE_MESSAGE, SERVICE_LOCATION_ERROR);
            service.mBroadcastManager.sendBroadcast(intent);

            intent = new Intent(service, LoggingService.class);
            intent.setAction(SERVICE_STOP_LOGGING);
            service.startService(intent);

        }
    }
}
