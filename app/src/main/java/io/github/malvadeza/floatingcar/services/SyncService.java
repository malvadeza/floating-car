package io.github.malvadeza.floatingcar.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import io.github.malvadeza.floatingcar.LoggingService;
import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;

public class SyncService extends IntentService {
    private static final String TAG = SyncService.class.getSimpleName();
    public static final String SYNC_TRIP =
            "io.github.malvadeza.floatingcar.syncservice.sync_trip";

    private final Uri BASE_URI;

    public SyncService() {
        super("SyncService");

        Log.d(TAG, "SyncService");

        BASE_URI = new Uri.Builder()
                .scheme("http")
                .authority("")
                .build();


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (LoggingService.isRunning()) {
            // Should not sync now
        }

        switch (intent.getAction()) {
            case SYNC_TRIP: {
                syncTrip();

                break;
            }
        }
    }

    private void syncTrip() {
        Log.d(TAG, "syncTrip");

        FloatingCarDbHelper dbHelper = new FloatingCarDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor trips = db.query(FloatingCarContract.TripEntry.TABLE_NAME,
                new String[]{
                        FloatingCarContract.TripEntry.STARTED_AT,
                        FloatingCarContract.TripEntry.FINISHED_AT,
                        FloatingCarContract.TripEntry.SHA_256
                },
                FloatingCarContract.TripEntry.SYNCED + " = ? ",
                new String[]{"0"}, null, null, null
        );

        try {
            final int startedAtIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
            final int finishedAtIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
            final int shaIndex = trips.getColumnIndex(FloatingCarContract.TripEntry.SHA_256);

            while (trips.moveToNext()) {
                JSONObject trip = new JSONObject();
                trip.put(FloatingCarContract.TripEntry.STARTED_AT, trips.getString(startedAtIndex));
                trip.put(FloatingCarContract.TripEntry.FINISHED_AT, trips.getString(finishedAtIndex));
                trip.put(FloatingCarContract.TripEntry.SHA_256, trips.getString(shaIndex));

                sendJSONObject(trip, "trip");
            }
        } catch(JSONException e) {
            e.printStackTrace();
        } finally {
            trips.close();
        }
    }

    private void sendJSONObject(JSONObject object, String endPoint) {
        Log.d(TAG, "sendJSONObject");

        Uri uri = BASE_URI.buildUpon().appendPath(endPoint).build();
    }
}
