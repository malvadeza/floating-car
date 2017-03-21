package io.github.malvadeza.floatingcar.data.source.local;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;
import io.github.malvadeza.floatingcar.data.ObdData;
import io.github.malvadeza.floatingcar.data.PhoneData;
import io.github.malvadeza.floatingcar.data.Sample;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.data.source.TripsDataSource;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class TripsLocalDataSource implements TripsDataSource {

    private static final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

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

    private List<Sample> getSamples(@NonNull Trip trip) throws ParseException {
        List<Sample> samples = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(FloatingCarContract.SampleEntry.TABLE_NAME,
                new String[]{
                        FloatingCarContract.SampleEntry._ID,
                        FloatingCarContract.SampleEntry.TIMESTAMP,
                        FloatingCarContract.SampleEntry.SHA_256
                }, FloatingCarContract.SampleEntry.SHA_TRIP + " = ?",
                new String[]{trip.getSha256()}, null, null,
                FloatingCarContract.SampleEntry.TIMESTAMP + " ASC"
        );

        try {
            final int idIndex = cursor.getColumnIndex(FloatingCarContract.SampleEntry._ID);
            final int timeStampIndex = cursor.getColumnIndex(FloatingCarContract.SampleEntry.TIMESTAMP);

            while (cursor.moveToNext()) {
                final Long id = cursor.getLong(idIndex);
                final String timeStamp = cursor.getString(timeStampIndex);

                Sample sample = new Sample();
                sample.setId(id);
                sample.setTakenAt(iso8601Format.parse(timeStamp));

                samples.add(sample);
            }
        } finally {
            cursor.close();
        }

        return samples;
    }



    @Override
    public Observable<List<Trip>> getTrips() {
        return Observable.fromCallable(new Callable<List<Trip>>() {
            @Override
            public List<Trip> call() throws Exception {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                List<Trip> trips = new ArrayList<>();

                Cursor cursor = db.query(FloatingCarContract.TripEntry.TABLE_NAME,
                        new String[]{
                                FloatingCarContract.TripEntry._ID,
                                FloatingCarContract.TripEntry.STARTED_AT,
                                FloatingCarContract.TripEntry.FINISHED_AT,
                                FloatingCarContract.TripEntry.SHA_256
                        },
                        FloatingCarContract.TripEntry.FINISHED_AT + " NOT NULL",
                        null,
                        null,
                        null,
                        FloatingCarContract.TripEntry.STARTED_AT + " DESC"
                );

                try {
                    final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
                    final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
                    final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);
                    final int shaIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.SHA_256);

                    while (cursor.moveToNext()) {
                        final Long id = cursor.getLong(idIndex);
                        final String startedAt = cursor.getString(startedAtIndex);
                        final String finishedAt = cursor.getString(finishedAtIndex);
                        final String sha256 = cursor.getString(shaIndex);

                        Trip trip = new Trip(id, iso8601Format.parse(startedAt), iso8601Format.parse(finishedAt));
                        trip.setSha256(sha256);

                        trips.add(trip);
                    }
                } finally {
                    cursor.close();
                }

                for (Trip trip: trips) {
                    trip.setSamples(getSamples(trip));
                }

                return trips;
            }
        });
    }

    @Override
    public Observable<Trip> getTrip(final String tripId) {
        return Observable.fromCallable(new Callable<Trip>() {
            @Override
            public Trip call() throws Exception {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Trip trip = null;

                Cursor cursor = db.query(FloatingCarContract.TripEntry.TABLE_NAME,
                        new String[]{
                                FloatingCarContract.TripEntry._ID,
                                FloatingCarContract.TripEntry.STARTED_AT,
                                FloatingCarContract.TripEntry.FINISHED_AT
                        },
                        FloatingCarContract.TripEntry._ID + " = ?",
                        new String[]{tripId},
                        null,
                        null,
                        null
                );

                try {
                    final int idIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry._ID);
                    final int startedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.STARTED_AT);
                    final int finishedAtIndex = cursor.getColumnIndex(FloatingCarContract.TripEntry.FINISHED_AT);

                    if (cursor.moveToFirst()) {
                        final Long id = cursor.getLong(idIndex);
                        final String startedAt = cursor.getString(startedAtIndex);
                        final String finishedAt = cursor.getString(finishedAtIndex);

                        trip = new Trip(id, iso8601Format.parse(startedAt), iso8601Format.parse(finishedAt));
                    }
                } finally {
                    cursor.close();
                }


                List<Sample> samples = new ArrayList<>();

                cursor = db.rawQuery(
                        "SELECT "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry._ID + ", "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.TIMESTAMP + ", "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.LATITUDE + ", "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.LONGITUDE + ", "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.ACCELEROMETER_X + ", "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Y + ", "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Z
                                + " FROM "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME
                                + " JOIN "
                                + FloatingCarContract.SampleEntry.TABLE_NAME
                                + " ON "
                                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.SHA_SAMPLE
                                + " = "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_256
                                + " JOIN "
                                + FloatingCarContract.TripEntry.TABLE_NAME
                                + " ON "
                                + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256
                                + " = "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_TRIP
                                + " WHERE "
                                + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry._ID
                                + " = ? "
                                + " ORDER BY "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.TIMESTAMP
                                + " ASC ",
                        new String[]{tripId}
                );

                try {
                    final int idIndex = cursor.getColumnIndex(FloatingCarContract.SampleEntry._ID);
                    final int timeStampIndex = cursor.getColumnIndex(FloatingCarContract.SampleEntry.TIMESTAMP);
                    final int latIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.LATITUDE);
                    final int lngIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.LONGITUDE);
                    final int accXIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_X);
                    final int accYIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Y);
                    final int accZIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.ACCELEROMETER_Z);

                    while (cursor.moveToNext()) {
                        final Long id = cursor.getLong(idIndex);
                        final String timeStamp = cursor.getString(timeStampIndex);

                        Sample sample = new Sample();
                        sample.setId(id);
                        sample.setTakenAt(iso8601Format.parse(timeStamp));

                        final double lat = cursor.getDouble(latIndex);
                        final double lng = cursor.getDouble(lngIndex);
                        final double accX = cursor.getDouble(accXIndex);
                        final double accY = cursor.getDouble(accYIndex);
                        final double accZ = cursor.getDouble(accZIndex);

                        PhoneData phoneData = new PhoneData();
                        phoneData.setLatLng(lat, lng);
                        phoneData.setAccelerometer(accX, accY, accZ);

                        sample.setPhoneData(phoneData);

                        samples.add(sample);
                    }
                } finally {
                    cursor.close();
                }

                Observable.fromIterable(samples).forEach(new Consumer<Sample>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Sample sample) throws Exception {
                        sample.setObdData(new ArrayList<ObdData>());
                    }
                });

                cursor = db.rawQuery(
                        "SELECT "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry._ID + ", "
                                + FloatingCarContract.OBDDataEntry.TABLE_NAME + "." + FloatingCarContract.OBDDataEntry.PID + ", "
                                + FloatingCarContract.OBDDataEntry.TABLE_NAME + "." + FloatingCarContract.OBDDataEntry.VALUE
                                + " FROM "
                                + FloatingCarContract.OBDDataEntry.TABLE_NAME
                                + " JOIN "
                                + FloatingCarContract.SampleEntry.TABLE_NAME
                                + " ON "
                                + FloatingCarContract.OBDDataEntry.TABLE_NAME + "." + FloatingCarContract.OBDDataEntry.SHA_SAMPLE
                                + " = "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_256
                                + " JOIN "
                                + FloatingCarContract.TripEntry.TABLE_NAME
                                + " ON "
                                + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry.SHA_256
                                + " = "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_TRIP
                                + " WHERE "
                                + FloatingCarContract.TripEntry.TABLE_NAME + "." + FloatingCarContract.TripEntry._ID
                                + " = ? "
                                + " ORDER BY "
                                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.TIMESTAMP
                                + " ASC ",
                        new String[]{tripId}
                );

                try {
                    final int idIndex = cursor.getColumnIndex(FloatingCarContract.SampleEntry._ID);
                    final int pidIndex = cursor.getColumnIndex(FloatingCarContract.OBDDataEntry.PID);
                    final int valueIndex = cursor.getColumnIndex(FloatingCarContract.OBDDataEntry.VALUE);

                    while (cursor.moveToNext()) {
                        final Long id = cursor.getLong(idIndex);
                        final String pid = cursor.getString(pidIndex);
                        final String value = cursor.getString(valueIndex);

                        ObdData obdData = new ObdData();
                        obdData.setPid(pid);
                        obdData.setValue(value);

                        int idx = (int) (id - samples.get(0).getId());

                        samples.get(idx).getObdData().add(obdData);
                    }
                } finally {
                    cursor.close();
                }

                if (trip != null)
                    trip.setSamples(samples);

                return trip;
            }
        });
    }
}
