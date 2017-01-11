package io.github.malvadeza.floatingcar;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.List;

import io.github.malvadeza.floatingcar.data.Database;
import io.github.malvadeza.floatingcar.data.ObdValue;
import io.github.malvadeza.floatingcar.data.obd.ObdReader;

@SuppressWarnings({"MissingPermission"})
public class LoggingThread implements Runnable,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {
    private static final String TAG = LoggingThread.class.getSimpleName();
    private static final float DISTANCE_THRESHOLD = 10;

    private static final int UPDATE_TIME = 2 * 1000;

    private final WeakReference<LoggingService> mLoggingServiceReference;
    private final Database mDb;

    private final ObdReader mObdReader;

    private boolean mShouldBeLogging = true;

    private Location mLastLocation;
    private Location mSegmentBeginning;

    private float[] mAcc;

    public LoggingThread(LoggingService service) {
        this(service, null);
    }

    public LoggingThread(final LoggingService service, BluetoothSocket btSocket) {
        mLoggingServiceReference = new WeakReference<LoggingService>(service);
        mObdReader = new ObdReader(btSocket);
        mDb = new Database(service);

        SensorManager sensorManager = (SensorManager) service.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void run() {
        mObdReader.setupObd();

        mDb.startTrip();

        while (mShouldBeLogging) {
            try {
                Thread.sleep(UPDATE_TIME);

                final long starTime = System.currentTimeMillis();

                List<ObdValue> obdValues = mObdReader.readValues();

                final long deltaTime = System.currentTimeMillis() - starTime;
                Log.d(TAG, "Comm delay -> " + deltaTime);

                mDb.saveSample(mLastLocation, mAcc, obdValues);

                sendInformationBroadcast(obdValues.get(0).getValue(), obdValues.get(1).getValue());
            } catch (InterruptedException e) {
                Log.e(TAG, "Error", e);
                e.printStackTrace();
            }
        }

        mDb.endTrip();
        mObdReader.disconnect();

        Log.d(TAG, "Finished logging");
    }

    private void sendInformationBroadcast(String speedValue, String rpmValue) {
        LoggingService service = mLoggingServiceReference.get();

        if (service == null) return;

        Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
        intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_NEW_DATA);
        intent.putExtra(LoggingService.SERVICE_LOCATION_LATLNG, mLastLocation);
        intent.putExtra(LoggingService.SERVICE_ACCELEROMETER, mAcc);
        intent.putExtra(LoggingService.SERVICE_DATA_SPEED, speedValue);
        intent.putExtra(LoggingService.SERVICE_DATA_RPM, rpmValue);

        service.mBroadcastManager.sendBroadcast(intent);
    }

    protected synchronized void stopLogging() {
        Log.d(TAG, "stopLogging");

        mShouldBeLogging = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        LoggingService service = mLoggingServiceReference.get();

        if (service == null) return;

        LocationServices.FusedLocationApi.requestLocationUpdates(service.mGoogleApiClient, service.mLocationRequest, this);

        Location location = LocationServices.FusedLocationApi.getLastLocation(service.mGoogleApiClient);

        if (location != null) {
            synchronized (this) {
                mLastLocation = location;
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

        mLastLocation = location;

        if (mSegmentBeginning == null) {
            mSegmentBeginning = mLastLocation;
        } else if (mSegmentBeginning.distanceTo(mLastLocation) >= DISTANCE_THRESHOLD) {
            // TODO: Trigger save data
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");

        LoggingService service = mLoggingServiceReference.get();

        if (service == null) return;

        Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
        intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_LOCATION_ERROR);
        service.mBroadcastManager.sendBroadcast(intent);

        intent = new Intent(service, LoggingService.class);
        intent.setAction(LoggingService.SERVICE_STOP_LOGGING);
        service.startService(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAcc = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}