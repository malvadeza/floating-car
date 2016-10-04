package io.github.malvadeza.floatingcar.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.malvadeza.floatingcar.data.FloatingCarContract.TripEntry;
import io.github.malvadeza.floatingcar.data.FloatingCarContract.SampleEntry;
import io.github.malvadeza.floatingcar.data.FloatingCarContract.PhoneDataEntry;
import io.github.malvadeza.floatingcar.data.FloatingCarContract.OBDDataEntry;

public class FloatingCarDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FloatingCar.db";

    private static FloatingCarDbHelper sInstance;

    public static synchronized FloatingCarDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FloatingCarDbHelper(context);
        }

        return sInstance;
    }

    private FloatingCarDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TRIP_TABLE = "CREATE TABLE " + TripEntry.TABLE_NAME + " ("
                + TripEntry._ID + " INTEGER PRIMARY KEY,"
                + TripEntry.STARTED_AT + " DATETIME NOT NULL,"
                + TripEntry.FINISHED_AT + " DATETIME,"
                + TripEntry.SHA_256 + " TEXT NOT NULL UNIQUE,"
                + TripEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0"
                + ");";

        final String SQL_CREATE_SAMPLE_TABLE = "CREATE TABLE " + SampleEntry.TABLE_NAME + " ("
                + SampleEntry._ID + " INTEGER PRIMARY KEY,"
                + SampleEntry.TIMESTAMP + " DATETIME NOT NULL,"
                + SampleEntry.SHA_256 + " TEXT NOT NULL UNIQUE,"
                + SampleEntry.SHA_TRIP + " TEXT NOT NULL,"
                + SampleEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + SampleEntry.SHA_TRIP + ") REFERENCES "
                + TripEntry.TABLE_NAME + " (" + TripEntry.SHA_256 + ") "
                + ");";

        final String SQL_CREATE_PHONE_DATA_TABLE = "CREATE TABLE " + PhoneDataEntry.TABLE_NAME + " ("
                + PhoneDataEntry._ID + " INTEGER PRIMARY KEY,"
                + PhoneDataEntry.LATITUDE + " REAL,"
                + PhoneDataEntry.LONGITUDE + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_X + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_Y + " REAL,"
                + PhoneDataEntry.ACCELEROMETER_Z + " REAL,"
                + PhoneDataEntry.SHA_SAMPLE + " TEXT NOT NULL UNIQUE,"
                + PhoneDataEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + PhoneDataEntry.SHA_SAMPLE + ") REFERENCES "
                + SampleEntry.TABLE_NAME + " (" + SampleEntry.SHA_256 +") "
                + ");";

        final String SQL_CREATE_OBD_DATA_TABLE = "CREATE TABLE "+ OBDDataEntry.TABLE_NAME + " ("
                + OBDDataEntry._ID + " INTEGER PRIMARY KEY,"
                + OBDDataEntry.VALUE + " TEXT NOT NULL,"
                + OBDDataEntry.PID + " TEXT NOT NULL,"
                + OBDDataEntry.SHA_SAMPLE + " TEXT NOT NULL,"
                + OBDDataEntry.SYNCED + " BOOLEAN NOT NULL DEFAULT 0,"
                + " FOREIGN KEY (" + OBDDataEntry.SHA_SAMPLE + ") REFERENCES "
//                + " UNIQUE (" + OBDDataEntry.PID + ", " + OBDDataEntry.SHA_SAMPLE + ") "
                + SampleEntry.TABLE_NAME + " (" + SampleEntry.SHA_256 +") "
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
