package io.github.malvadeza.floatingcar.data.source.local;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.data.source.TripsDataSource;
import io.reactivex.Observable;

public class TripsLocalDataSource implements TripsDataSource {

    private static TripsLocalDataSource instance;

    private FloatingCarDbHelper dbHelper;

    private TripsLocalDataSource(@NonNull Context context) {
        dbHelper = FloatingCarDbHelper.getInstance(context);
    }

    public static TripsLocalDataSource getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new TripsLocalDataSource(context);
        }

        return instance;
    }

    @Override
    public Observable<List<TripAdapter.TripHolder>> getTrips() {
        return Observable.fromCallable(new Callable<List<TripAdapter.TripHolder>>(){
            @Override
            public List<TripAdapter.TripHolder> call() throws Exception {
                Log.d("TripsLocalDataSource", "Thread name -> " + Thread.currentThread().getName());

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                List<TripAdapter.TripHolder> ret = new ArrayList<>();
                Cursor cursor = db.rawQuery(
                        "SELECT "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry._ID + ", "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256 + ", "
                        + FloatingCarContract.TripEntry.STARTED_AT + ", "
                        + FloatingCarContract.TripEntry.FINISHED_AT + ", "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256 + ", "
                        + "COUNT(*) as " + FloatingCarContract.TripEntry._COUNT
                        + " FROM "
                        + FloatingCarContract.TripEntry.TABLE_NAME
                        + " JOIN "
                        + FloatingCarContract.SampleEntry.TABLE_NAME
                        + " ON "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256
                        + " = "
                        + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_TRIP
                        + " WHERE "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.FINISHED_AT
                        + " NOT NULL "
                        + " GROUP BY "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256
                        + " ORDER BY "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.STARTED_AT + " DESC ",
                        null
                );

                try {
                    final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
                    final int shaIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.SHA_256);
                    final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
                    final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
                    final int countIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._COUNT);

                    while (cursor.moveToNext()) {
                        final long id = cursor.getLong(idIndex);
                        final String sha = cursor.getString(shaIndex);
                        final String startedAtStr = cursor.getString(startedAtIndex);
                        final String finishedAtStr = cursor.getString(finishedAtIndex);
                        final int count = cursor.getInt(countIndex);

                        ret.add(new TripAdapter.TripHolder(id, sha, startedAtStr, finishedAtStr, count));
                    }
                } finally {
                    cursor.close();
                }

                return ret;
            }
        });
    }

    @Override
    public Observable<TripAdapter.TripHolder> getTrip(@NonNull String tripId) {
        return null;
    }
}
