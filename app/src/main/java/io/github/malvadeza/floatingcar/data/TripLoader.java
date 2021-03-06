package io.github.malvadeza.floatingcar.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;

public class TripLoader extends AsyncTaskLoader<List<TripAdapter.TripHolder>> {
    private static final String TAG = TripLoader.class.getSimpleName();

    private List<TripAdapter.TripHolder> mTrips;

    public TripLoader(Context context) {
        super(context);
    }

    @Override
    public List<TripAdapter.TripHolder> loadInBackground() {
        FloatingCarDbHelper dbHelper = FloatingCarDbHelper.getInstance(getContext());
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

        final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
        final int shaIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.SHA_256);
        final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
        final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
        final int countIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._COUNT);

        try {
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

    @Override
    public void deliverResult(List<TripAdapter.TripHolder> data) {
        if (isReset()) {
            if (data != null) {
                // Release data
            }
        }

        List<TripAdapter.TripHolder> oldTrips = mTrips;
        mTrips = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldTrips != null) {
            // Release old stuff
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mTrips != null) {
            deliverResult(mTrips);
        }

        if (takeContentChanged() || mTrips == null) {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mTrips != null) {
            // Release stuff
        }

    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();

        cancelLoad();
    }

    @Override
    public void onCanceled(List<TripAdapter.TripHolder> data) {
        super.onCanceled(data);

        // Release stuff
    }
}
