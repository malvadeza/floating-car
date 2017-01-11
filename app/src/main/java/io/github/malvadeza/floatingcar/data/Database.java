package io.github.malvadeza.floatingcar.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class Database {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

    private final SQLiteDatabase mDb;
    private final DatabaseThread mDbThread;

    private String mTripUUID;

    private static class DatabaseThread extends HandlerThread {
        private Handler mWorkHandler;

        DatabaseThread(String name) {
            super(name);
        }

        void prepareHandler() {
            mWorkHandler = new Handler(getLooper());
        }

        boolean post(Runnable task) {
            return mWorkHandler.post(task);
        }
    }

    public Database(Context context) {
        mDb = FloatingCarDbHelper.getInstance(context).getWritableDatabase();
        mDbThread = new DatabaseThread("DatabaseThread");
        mDbThread.start();
        mDbThread.prepareHandler();
    }

    public void startTrip() {
        mTripUUID = UUID.randomUUID().toString();

        mDbThread.post(new Runnable() {
            @Override
            public void run() {
                ContentValues trip = new ContentValues();
                trip.put(FloatingCarContract.TripEntry.STARTED_AT, formatter.format(new Date()));
                trip.put(FloatingCarContract.TripEntry.SHA_256, mTripUUID);

                // TODO: Test if insert successful
                mDb.insert(FloatingCarContract.TripEntry.TABLE_NAME, null, trip);
            }
        });
    }

    public void endTrip() {
        mDbThread.post(new Runnable() {
            @Override
            public void run() {
                ContentValues trip = new ContentValues();
                trip.put(FloatingCarContract.TripEntry.FINISHED_AT, formatter.format(new Date()));

                mDb.update(FloatingCarContract.TripEntry.TABLE_NAME, trip,
                        FloatingCarContract.TripEntry.SHA_256 + " = ?",
                        new String[]{mTripUUID}
                );
            }
        });

        mDbThread.quitSafely();
    }

    public void saveSample(Location location, float[] acc, List<ObdValue> obdValues) {
        final String sampleUUID = UUID.randomUUID().toString();

        final ContentValues sample = new ContentValues();
        sample.put(FloatingCarContract.SampleEntry.TIMESTAMP, formatter.format(new Date()));
        sample.put(FloatingCarContract.SampleEntry.SHA_256, sampleUUID);
        sample.put(FloatingCarContract.SampleEntry.SHA_TRIP, mTripUUID);

        final List<ContentValues> obdData = new ArrayList<>();
        for (ObdValue obdValue: obdValues) {
            ContentValues obdDatum = new ContentValues();
            obdDatum.put(FloatingCarContract.OBDDataEntry.PID, obdValue.getPid());
            obdDatum.put(FloatingCarContract.OBDDataEntry.VALUE, obdValue.getValue());
            obdDatum.put(FloatingCarContract.OBDDataEntry.SHA_SAMPLE, sampleUUID);

            obdData.add(obdDatum);
        }

        final ContentValues phoneData = new ContentValues();

        if (location != null) {
            phoneData.put(FloatingCarContract.PhoneDataEntry.LATITUDE, location.getLatitude());
            phoneData.put(FloatingCarContract.PhoneDataEntry.LONGITUDE, location.getLongitude());
        }

        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_X, acc[0]);
        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Y, acc[1]);
        phoneData.put(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Z, acc[2]);

        phoneData.put(FloatingCarContract.PhoneDataEntry.SHA_SAMPLE, sampleUUID);

        mDbThread.post(new Runnable() {
            @Override
            public void run() {
                mDb.insert(FloatingCarContract.SampleEntry.TABLE_NAME, null, sample);
                mDb.insert(FloatingCarContract.PhoneDataEntry.TABLE_NAME, null, phoneData);

                for (ContentValues obdDatum: obdData) {
                    mDb.insert(FloatingCarContract.OBDDataEntry.TABLE_NAME, null, obdDatum);
                }
            }
        });
    }

}
