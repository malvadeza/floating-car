package io.github.malvadeza.floatingcar.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.malvadeza.floatingcar.database.FloatingCarContract.TripEntry;
import io.github.malvadeza.floatingcar.database.FloatingCarContract.SampleEntry;
import io.github.malvadeza.floatingcar.database.FloatingCarContract.PhoneDataEntry;
import io.github.malvadeza.floatingcar.database.FloatingCarContract.OBDDataEntry;

/**
 * Created by tonho on 12/09/2016.
 */
public class FloatingCarDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FloatingCar.db";

    public FloatingCarDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TRIP_TABLE = "CREATE TABLE " + TripEntry.TABLE_NAME + " ("
                + TripEntry._ID + " INTEGER PRIMARY KEY,"
                + TripEntry.STARTED_AT + " DATETIME NOT NULL,"
                + TripEntry.FINISHED_AT + " DATETIME,"
                + TripEntry.USER_ID + " INTEGER,"
                + TripEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0"
                + ");";

        final String SQL_CREATE_SAMPLE_TABLE = "CREATE TABLE " + SampleEntry.TABLE_NAME + " ("
                + SampleEntry._ID + " INTEGER PRIMARY KEY,"
                + SampleEntry.TIMESTAMP + " DATETIME NOT NULL,"
                + SampleEntry.TRIP_ID + " INTEGER NOT NULL,"
                + SampleEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + SampleEntry.TRIP_ID + ") REFERENCES "
                + TripEntry.TABLE_NAME + " (" + TripEntry._ID + ") "
                + ");";

        final String SQL_CREATE_PHONE_DATA_TABLE = "CREATE TABLE " + PhoneDataEntry.TABLE_NAME + " ("
                + PhoneDataEntry._ID + " INTEGER PRIMARY KEY,"
                + PhoneDataEntry.LATITUDE + " REAL,"
                + PhoneDataEntry.LONGITUDE + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_X + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_Y + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_Z + " REAL,"
                + PhoneDataEntry.SAMPLE_ID + " INTEGER NOT NULL,"
                + PhoneDataEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + PhoneDataEntry.SAMPLE_ID + ") REFERENCES "
                + SampleEntry.TABLE_NAME + " (" + SampleEntry._ID +") "
                + ");";

        final String SQL_CREATE_OBD_DATA_TABLE = "CREATE TABLE "+ OBDDataEntry.TABLE_NAME + " ("
                + OBDDataEntry._ID + " INTEGER PRIMARY KEY,"
                + OBDDataEntry.VALUE + " TEXT NOT NULL,"
                + OBDDataEntry.PID + " TEXT NOT NULL,"
                + OBDDataEntry.SAMPLE_ID + " INTEGER NOT NULL,"
                + OBDDataEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + OBDDataEntry.SAMPLE_ID + ") REFERENCES "
                + SampleEntry.TABLE_NAME + " (" + SampleEntry._ID +") "
                + ");";

        db.execSQL(SQL_CREATE_TRIP_TABLE);
        db.execSQL(SQL_CREATE_SAMPLE_TABLE);
        db.execSQL(SQL_CREATE_PHONE_DATA_TABLE);
        db.execSQL(SQL_CREATE_OBD_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OBDDataEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + PhoneDataEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + SampleEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME + ";");

        onCreate(db);
    }
}
