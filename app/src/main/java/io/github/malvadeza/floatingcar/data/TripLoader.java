package io.github.malvadeza.floatingcar.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.github.malvadeza.floatingcar.LoggingService;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;

public class TripLoader extends AsyncTaskLoader<List<TripAdapter.TripHolder>> {
    private static final String TAG = TripLoader.class.getSimpleName();
    private BroadcastReceiver mBroadcastReceiver;

    private List<TripAdapter.TripHolder> mTrips;

    private static class LoaderReceiver extends BroadcastReceiver {
        private final WeakReference<TripLoader> loaderReference;

        public LoaderReceiver(TripLoader loader) {
            loaderReference = new WeakReference<TripLoader>(loader);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            TripLoader loader = loaderReference.get();

            if (loader != null) {
                loader.onContentChanged();
            }
        }
    }

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
                        + " GROUP BY "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256
                        + " ORDER BY "
                        + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.STARTED_AT + " DESC ",
                null
        );

        final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
        final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
        final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
        final int countIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._COUNT);

        try {
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(idIndex);
                final String startedAtStr = cursor.getString(startedAtIndex);
                final String finishedAtStr = cursor.getString(finishedAtIndex);
                final int count = cursor.getInt(countIndex);

                // TODO: Add Trip object here
                ret.add(new TripAdapter.TripHolder(id, startedAtStr, finishedAtStr, count));
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

        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new LoaderReceiver(this);
            IntentFilter filter = new IntentFilter(LoggingService.SERVICE_NEW_TRIP_DATA);

            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(mBroadcastReceiver, filter);
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

        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
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
