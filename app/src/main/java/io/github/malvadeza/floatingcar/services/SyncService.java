package io.github.malvadeza.floatingcar.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.malvadeza.floatingcar.LoggingService;
import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;

public class SyncService extends IntentService {
    private static final String TAG = SyncService.class.getSimpleName();
    public static final String SYNC_TRIPS =
            "io.github.malvadeza.floatingcar.syncservice.sync_trip";
    public static final String SYNC_SAMPLES =
            "io.github.malvadeza.floatingcar.syncservice.sync_sample";

    private static final Uri BASE_URI = new Uri.Builder()
            .scheme("http")
            .authority("")
            .build();;

    public SyncService() {
        super("SyncService");

        Log.d(TAG, "SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (LoggingService.isRunning()) {
            // Should not sync now
        }

        switch (intent.getAction()) {
            case SYNC_TRIPS: {
                syncTrips();
                break;
            }
            case SYNC_SAMPLES: {
                syncSamples();
                break;
            }
        }
    }

    private void syncTrips() {
        Log.d(TAG, "syncTrips");

        FloatingCarDbHelper dbHelper = FloatingCarDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trips = db.query(FloatingCarContract.TripEntry.TABLE_NAME,
                new String[]{
                        FloatingCarContract.TripEntry.STARTED_AT,
                        FloatingCarContract.TripEntry.FINISHED_AT,
                        FloatingCarContract.TripEntry.SYNCED,
                        FloatingCarContract.TripEntry.SHA_256
                },
                FloatingCarContract.TripEntry.SYNCED + " = ?"
                        + " AND " + FloatingCarContract.TripEntry.FINISHED_AT + " IS NOT NULL",
                new String[]{"0"}, null, null, null
        );

        try {
            final int startedAtIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
            final int finishedAtIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
            final int syncedIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.SYNCED);
            final int shaIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.SHA_256);

            while (trips.moveToNext()) {
                JSONObject trip = new JSONObject();
                trip.put(FloatingCarContract.TripEntry.STARTED_AT, trips.getString(startedAtIndex));
                trip.put(FloatingCarContract.TripEntry.FINISHED_AT, trips.getString(finishedAtIndex));
                trip.put(FloatingCarContract.TripEntry.SHA_256, trips.getString(shaIndex));

                sendJSONObject(trip, "trip");

                /* if syncing with server is successful then update SYNCED column */
//                ContentValues tripValue = new ContentValues();
//                trip.put(FloatingCarContract.TripEntry.SYNCED, "1");
//
//                db.update(FloatingCarContract.TripEntry.TABLE_NAME, tripValue,
//                        FloatingCarContract.TripEntry.SHA_256 + " = ?",
//                        new String[]{trips.getString(shaIndex)}
//                );
            }
        } catch(JSONException e) {
            e.printStackTrace();
        } finally {
            trips.close();
            db.close();
        }
    }

    private void syncSamples() {
        Log.d(TAG, "syncSamples");

        FloatingCarDbHelper dbHelper = FloatingCarDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor samples = db.query(FloatingCarContract.SampleEntry.TABLE_NAME,
                new String[]{
                        FloatingCarContract.SampleEntry.TIMESTAMP,
                        FloatingCarContract.SampleEntry.SHA_256,
                        FloatingCarContract.SampleEntry.SHA_TRIP,
                        FloatingCarContract.SampleEntry.SYNCED
                },
                FloatingCarContract.SampleEntry.SYNCED + " = ?",
                new String[]{"0"}, null, null, null
        );

        try {
            final int timestampIndex = samples.getColumnIndex(FloatingCarContract.SampleEntry.TIMESTAMP);
            final int shaIndex = samples.getColumnIndex(FloatingCarContract.SampleEntry.SHA_256);
            final int shaTripIndex = samples.getColumnIndex(FloatingCarContract.SampleEntry.SHA_TRIP);
            final int syncedIndex = samples.getColumnIndex(FloatingCarContract.SampleEntry.SYNCED);

            while (samples.moveToNext()) {
                JSONObject sampleJSON = new JSONObject();
                sampleJSON.put(FloatingCarContract.SampleEntry.TIMESTAMP, samples.getString(timestampIndex));
                sampleJSON.put(FloatingCarContract.SampleEntry.SHA_256, samples.getString(shaIndex));
                sampleJSON.put(FloatingCarContract.SampleEntry.SHA_TRIP, samples.getString(shaTripIndex));

                sendJSONObject(sampleJSON, "samples");

                /* if syncing with server is successful then update SYNCED column */
            }
        } catch (JSONException e) {
        } finally {
            samples.close();
            db.close();
        }
    }

    private void sendJSONObject(JSONObject object, String endPoint) {
        Log.d(TAG, "sendJSONObject");

        Uri uri = BASE_URI.buildUpon().appendPath(endPoint).build();
    }
}
