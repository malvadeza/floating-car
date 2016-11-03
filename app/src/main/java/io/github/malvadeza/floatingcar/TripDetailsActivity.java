package io.github.malvadeza.floatingcar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import io.github.malvadeza.floatingcar.data.FloatingCarContract;
import io.github.malvadeza.floatingcar.data.FloatingCarDbHelper;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private static final String TAG = TripDetailsActivity.class.getSimpleName();

    public static final String TRIP_SHA = "io.github.malvadeza.floatingcar.trip_sha";

    private GoogleMap mGoogleMap;
    private List<LatLng> latLngs;
    private LatLngBounds.Builder mMapBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        Log.d(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String tripSha = getIntent().getStringExtra(TRIP_SHA);
        loadTrip(tripSha);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        mGoogleMap.addPolyline(new PolylineOptions().addAll(latLngs));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapBound.build(), 15));
    }

    private void loadTrip(String tripSha) {
        Log.d(TAG, "loadTrip");

        SQLiteDatabase db = FloatingCarDbHelper.getInstance(getApplicationContext()).getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT "
                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.LATITUDE + ", "
                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "."  + FloatingCarContract.PhoneDataEntry.LONGITUDE
                + " FROM "
                + FloatingCarContract.PhoneDataEntry.TABLE_NAME
                + " JOIN "
                + FloatingCarContract.SampleEntry.TABLE_NAME
                + " ON "
                + FloatingCarContract.PhoneDataEntry.TABLE_NAME + "." + FloatingCarContract.PhoneDataEntry.SHA_SAMPLE
                + " = "
                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_256
                + " WHERE "
                + FloatingCarContract.SampleEntry.TABLE_NAME + "." + FloatingCarContract.SampleEntry.SHA_TRIP
                + " = ? "
                + " ORDER BY "
                + FloatingCarContract.SampleEntry.TIMESTAMP
                + " ASC ",
                new String[] {tripSha}
        );

        try {
            latLngs = new ArrayList<>();

            final int latIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.LATITUDE);
            final int lngIndex = cursor.getColumnIndex(FloatingCarContract.PhoneDataEntry.LONGITUDE);

            mMapBound = new LatLngBounds.Builder();

            while (cursor.moveToNext()) {
                double lat = cursor.getDouble(latIndex);
                double lng = cursor.getDouble(lngIndex);

                LatLng latLng = new LatLng(lat, lng);

                latLngs.add(latLng);
                mMapBound.include(latLng);
            }
        } finally {
            cursor.close();
        }

        Log.d(TAG, "tripLoaded");
    }
}
