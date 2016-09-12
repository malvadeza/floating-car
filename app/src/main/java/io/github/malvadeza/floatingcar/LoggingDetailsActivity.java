package io.github.malvadeza.floatingcar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoggingDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = LoggingDetailsActivity.class.getSimpleName();

    private BroadcastReceiver mBcReceiver;

    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_details);
        Log.d(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.stop_logging);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop logging");

                if (LoggingService.isRunning()) {
                    Intent intent = new Intent(LoggingDetailsActivity.this, LoggingService.class);
                    intent.setAction(LoggingService.SERVICE_STOP_LOGGING);

                    startService(intent);
                }
            }
        });

        mBcReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LoggingService.SERVICE_BROADCAST_MESSAGE)) {
                    /**
                     * Here I should receive details from the Logging service
                     * such as GPS location and some data from OBD (SPEED RPM and like this)
                     */

                    switch (intent.getStringExtra(LoggingService.SERVICE_MESSAGE)) {
                        case LoggingService.SERVICE_LOCATION_CHANGED:
                            Log.d(TAG, "Received new Location");
                            final Location location = intent.getParcelableExtra(LoggingService.SERVICE_LOCATION_LATLNG);

                            if (location != null) {
                                Log.d(TAG, "Updating map location. Lat -> " + location.getLatitude() + " Long -> " + location.getLongitude());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        double lat = location.getLatitude();
                                        double lng = location.getLongitude();

                                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 6));
                                    }
                                });
                            }
                            break;
                    }

                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        IntentFilter filter = new IntentFilter(LoggingService.SERVICE_BROADCAST_MESSAGE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBcReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBcReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
    }
}
