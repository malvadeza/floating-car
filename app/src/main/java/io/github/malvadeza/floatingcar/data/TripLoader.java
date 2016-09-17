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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.malvadeza.floatingcar.LoggingService;

// TODO: Change from TripEntry to another class
public class TripLoader extends AsyncTaskLoader<List<FloatingCarContract.TripEntry>> {
    private static final String TAG = TripLoader.class.getSimpleName();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
    private BroadcastReceiver mBroadcastReceiver;

    private List<FloatingCarContract.TripEntry> mTrips;

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
                ;
            }
        }
    }

    public TripLoader(Context context) {
        super(context);
    }

    @Override
    public List<FloatingCarContract.TripEntry> loadInBackground() {
        FloatingCarDbHelper dbHelper = new FloatingCarDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<FloatingCarContract.TripEntry> ret = new ArrayList<>();

        Cursor cursor = db.query(FloatingCarContract.TripEntry.TABLE_NAME,
                new String[]{
                        FloatingCarContract.TripEntry._ID,
                        FloatingCarContract.TripEntry.STARTED_AT,
                        FloatingCarContract.TripEntry.FINISHED_AT,
                }, null, null, null, null, FloatingCarContract.TripEntry.STARTED_AT + " DESC");

        final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
        final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
        final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);

        try {
            while (cursor.moveToNext()) {
                final long id = cursor.getLong(idIndex);
                final String startedAtStr = cursor.getString(startedAtIndex);
                final String finishedAtStr = cursor.getString(finishedAtIndex);
                final Date startedAt = parseDate(startedAtStr);
                final Date finishedAt = parseDate(finishedAtStr);

                // TODO: Add Trip object here
            }
        } finally {
            cursor.close();
        }

        return ret;
    }

    private Date parseDate(String dateStr) {
        try {
            return formatter.parse(dateStr);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public void deliverResult(List<FloatingCarContract.TripEntry> data) {
        if (isReset()) {
            if (data != null) {
                // Release data
            }
        }

        List<FloatingCarContract.TripEntry> oldTrips = mTrips;
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
            IntentFilter filter = new IntentFilter(LoggingService.SERVICE_NEW_TRIP);

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
    public void onCanceled(List<FloatingCarContract.TripEntry> data) {
        super.onCanceled(data);

        // Release stuff
    }
}
