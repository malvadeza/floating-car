package io.github.malvadeza.floatingcar;

import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;

@SuppressWarnings({"MissingPermission"})
public class LoggingThread implements Runnable,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {
    private static final String TAG = LoggingThread.class.getSimpleName();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

    private static final int UPDATE_TIME =  2 * 1000;

    private final WeakReference<LoggingService> mLoggingServiceReference;
    private final SQLiteDatabase mDb;

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;

    private final BluetoothSocket mBtSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private boolean shouldBeLogging = true;

    private final String mTripSha;

    private Location mLastLocation;

    private float mAccelerometerX;
    private float mAccelerometerY;
    private float mAccelerometerZ;

    public LoggingThread(LoggingService service) {
        this(service, null);
    }

    public LoggingThread(LoggingService service, BluetoothSocket btSocket) {
        mLoggingServiceReference = new WeakReference<LoggingService>(service);
        mBtSocket = btSocket;
        mDb = FloatingCarDbHelper.getInstance(service).getWritableDatabase();

        mTripSha = Hashing.sha256().hashString(Long.toString(System.currentTimeMillis()), StandardCharsets.UTF_8).toString();
        ContentValues trip = new ContentValues();
        trip.put(FloatingCarContract.TripEntry.STARTED_AT, formatter.format(new Date()));
        trip.put(FloatingCarContract.TripEntry.SHA_256, mTripSha);

        // TODO: Test if insert successful
        mDb.insert(FloatingCarContract.TripEntry.TABLE_NAME, null, trip);

        service.mBroadcastManager.sendBroadcast(new Intent(LoggingService.SERVICE_NEW_TRIP));

        mSensorManager = (SensorManager) service.getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        try {
            mInputStream = mBtSocket.getInputStream();
            mOutputStream = mBtSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        setupObd();

        while (shouldBeLogging) {
            try {
                Thread.sleep(UPDATE_TIME);

                final String sampleSha = Hashing.sha256().hashString(Long.toString(System.currentTimeMillis()), StandardCharsets.UTF_8).toString();

                ContentValues sample = new ContentValues();
                sample.put(FloatingCarContract.SampleEntry.TIMESTAMP, formatter.format(new Date()));
                sample.put(FloatingCarContract.SampleEntry.SHA_256, sampleSha);
                sample.put(FloatingCarContract.SampleEntry.SHA_TRIP, mTripSha);

                // TODO: Test if insert successful
                mDb.insert(FloatingCarContract.SampleEntry.TABLE_NAME, null, sample);

                ContentValues phoneData = createPhoneDataEntry(sampleSha);
                mDb.insert(FloatingCarContract.PhoneDataEntry.TABLE_NAME, null, phoneData);

                final long time = System.currentTimeMillis();
                /* for every OBD value do this */
                final int speed = getSpeed();
                final int rpm = getRPM();

                final long deltaTime = System.currentTimeMillis() - time;

                Log.d(TAG, "Comm delay -> " + deltaTime);

                ContentValues obdSpeed = new ContentValues();
                obdSpeed.put(FloatingCarContract.OBDDataEntry.PID, "01 0D");
                obdSpeed.put(FloatingCarContract.OBDDataEntry.VALUE, speed);
                obdSpeed.put(FloatingCarContract.OBDDataEntry.SHA_SAMPLE, sampleSha);
                mDb.insert(FloatingCarContract.OBDDataEntry.TABLE_NAME, null, obdSpeed);

                ContentValues obdRPM = new ContentValues();
                obdRPM.put(FloatingCarContract.OBDDataEntry.PID, "01 0C");
                obdRPM.put(FloatingCarContract.OBDDataEntry.VALUE, rpm);
                obdRPM.put(FloatingCarContract.OBDDataEntry.SHA_SAMPLE, sampleSha);
                mDb.insert(FloatingCarContract.OBDDataEntry.TABLE_NAME, null, obdRPM);

                sendInformationBroadcast(speed, rpm); // send the read data from obd in the array
            } catch (InterruptedException e) {
                Log.e(TAG, "Error", e);
                e.printStackTrace();
            }
        }

        ContentValues trip = new ContentValues();
        trip.put(FloatingCarContract.TripEntry.FINISHED_AT, formatter.format(new Date()));

        // TODO: Test if update successful
        mDb.update(FloatingCarContract.TripEntry.TABLE_NAME, trip,
                FloatingCarContract.TripEntry.SHA_256 + " = ?",
                new String[]{mTripSha}
        );

        LoggingService service = mLoggingServiceReference.get();
        if (service != null) {
            service.mBroadcastManager.sendBroadcast(new Intent(LoggingService.SERVICE_NEW_TRIP));
        }

        try {
            mInputStream.close();
            mOutputStream.close();

            mBtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Finished logging");
    }

    private void setupObd() {
        Log.d(TAG, "setupObd");
        try {
            // Set Defaults
            mOutputStream.write("AT D\r".getBytes());
            getResponse();

            // Reset all
            mOutputStream.write("AT Z\r".getBytes());
            getResponse();

            // Disable echo
            mOutputStream.write("AT E0\r".getBytes());
            getResponse();

            // Disable line feed
            mOutputStream.write("AT L0\r".getBytes());
            getResponse();

            // Disable spaces
            mOutputStream.write("AT S0\r".getBytes());
            getResponse();

            // Disable headers
            mOutputStream.write("AT H0\r".getBytes());
            getResponse();
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            e.printStackTrace();
        }
    }

    private synchronized String getResponse() {
        Log.d(TAG, "getResponse");

        StringBuilder res = new StringBuilder();
        byte buffer;

        try {
            while ( ((char)(buffer = (byte) mInputStream.read()) != '>')) {
                res.append((char) buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return res.toString().replaceAll("SEARCHING", "").replaceAll("\\s", "");
    }

    private synchronized int getSpeed() {
        String command = "01 0D\r";

        try {
            mOutputStream.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = getResponse();

        int ret;

        try {
            ret = Integer.parseInt(response.substring(4), 16);
        } catch (Exception e) {
            ret = 0;
        }

        return ret;
    }

    private synchronized int getRPM() {
        String command = "01 0C\r";

        try {
            mOutputStream.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = getResponse();
        int ret;

        try {
            ret = Integer.parseInt(response.substring(4), 16) / 4;
        } catch (Exception e) {
            ret = 0;
        }

        return ret;
    }

    private void sendInformationBroadcast(int speedValue, int rpmValue) {
        LoggingService service = mLoggingServiceReference.get();

        Intent intent = new Intent(LoggingService.SERVICE_BROADCAST_MESSAGE);
        intent.putExtra(LoggingService.SERVICE_MESSAGE, LoggingService.SERVICE_NEW_DATA);
        intent.putExtra(LoggingService.SERVICE_LOCATION_LATLNG, mLastLocation);
        intent.putExtra(LoggingService.SERVICE_ACCELEROMETER_X, mAccelerometerX);
        intent.putExtra(LoggingService.SERVICE_ACCELEROMETER_Y, mAccelerometerY);
        intent.putExtra(LoggingService.SERVICE_ACCELEROMETER_Z, mAccelerometerZ);
        intent.putExtra(LoggingService.SERVICE_DATA_SPEED, speedValue);
        intent.putExtra(LoggingService.SERVICE_DATA_RPM, rpmValue);

        service.mBroadcastManager.sendBroadcast(intent);
    }

    @NonNull
    private ContentValues createPhoneDataEntry(String sampleSha) {
        ContentValues phoneData = new ContentValues();

        if (mLastLocation != null) {
            phoneData.put(FloatingCarContract.PhoneDataEntry.LATITUDE, Double.toString(mLastLocation.getLatitude()));
            phoneData.put(FloatingCarContract.PhoneDataEntry.LONGITUDE, Double.toString(mLastLocation.getLongitude()));
        }

        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_X, mAccelerometerX);
        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Y, mAccelerometerY);
        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Z, mAccelerometerZ);


        phoneData.put(FloatingCarContract.PhoneDataEntry.SHA_SAMPLE, sampleSha);

        return phoneData;
    }

    public synchronized void stopLogging() {
        Log.d(TAG, "stopLogging");

        shouldBeLogging = false;
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

        LoggingService service = mLoggingServiceReference.get();

        if (service == null) return;

        synchronized (this) {
            mLastLocation = location;
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
//        Log.d(TAG, "onSensorChanged");

        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerX = event.values[0];
            mAccelerometerY = event.values[1];
            mAccelerometerZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.d(TAG, "onAccuracyChanged");
    }
}