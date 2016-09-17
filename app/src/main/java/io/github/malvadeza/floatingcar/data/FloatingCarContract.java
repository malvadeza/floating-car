package io.github.malvadeza.floatingcar.data;

import android.provider.BaseColumns;

public class FloatingCarContract {

    private FloatingCarContract() {
    }

    public static class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";

        public static final String STARTED_AT = "started_at";
        public static final String FINISHED_AT = "finished_at";

        public static final String SHA_256 = "sha_256";

        public static final String SYNCED = "synced";
    }

    public static class SampleEntry implements BaseColumns {
        public static final String TABLE_NAME = "sample";

        public static final String TIMESTAMP = "timestamp";

        public static final String SHA_256 = "sha_256";
        public static final String SHA_TRIP = "sha_trip";

        public static final String SYNCED = "synced";
    }

    public static class PhoneDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "phone_data";

        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACCELEROMETER_X = "accelerometer_x";
        public static final String ACCELEROMETER_Y = "accelerometer_y";
        public static final String ACCELEROMETER_Z = "accelerometer_z";

        public static final String SHA_SAMPLE = "sha_sample";

        public static final String SYNCED = "synced";
    }

    public static class OBDDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "obd_data";

        public static final String PID = "pid";
        public static final String VALUE = "value";

        public static final String SHA_SAMPLE = "sha_sample";

        public static final String SYNCED = "synced";
    }
}
