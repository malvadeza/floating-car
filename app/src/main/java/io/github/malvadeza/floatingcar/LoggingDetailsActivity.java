package io.github.malvadeza.floatingcar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoggingDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = LoggingDetailsActivity.class.getSimpleName();

    private BroadcastReceiver mBcReceiver;

    private GoogleMap mGoogleMap;

    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mAccXView;
    private TextView mAccYView;
    private TextView mAccZView;
    private TextView mGForce;
    private TextView mSpeedView;
    private TextView mRPMView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_details);
        Log.d(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        mLatitudeView = (TextView) findViewById(R.id.latitude);
        mLongitudeView = (TextView) findViewById(R.id.longitude);

        mAccXView = (TextView) findViewById(R.id.xAxis);
        mAccYView = (TextView) findViewById(R.id.yAxis);
        mAccZView = (TextView) findViewById(R.id.zAxis);
        mGForce = (TextView) findViewById(R.id.gForce);

        mSpeedView = (TextView) findViewById(R.id.vehicleSpeed);
        mRPMView = (TextView) findViewById(R.id.engineRpm);

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
                    switch (intent.getStringExtra(LoggingService.SERVICE_MESSAGE)) {
                        case LoggingService.SERVICE_NEW_DATA:
                            Log.d(TAG, "Received new data");
                            final Location location = intent.getParcelableExtra(LoggingService.SERVICE_LOCATION_LATLNG);
                            final int speed = Integer.parseInt(intent.getStringExtra(LoggingService.SERVICE_DATA_SPEED));
                            final int rpm = Integer.parseInt(intent.getStringExtra(LoggingService.SERVICE_DATA_RPM));

                            final float[] acc = intent.getFloatArrayExtra(LoggingService.SERVICE_ACCELEROMETER);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (location != null) {
                                        double lat = location.getLatitude();
                                        double lng = location.getLongitude();

                                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));

                                        mLatitudeView.setText(Double.toString(lat));
                                        mLongitudeView.setText(Double.toString(lng));
                                    }

                                    mSpeedView.setText(speed + " km/h");
                                    mRPMView.setText(rpm + " rpm");

                                    mAccXView.setText(String.format("%.4f", acc[0]));
                                    mAccYView.setText(String.format("%.4f", acc[1]));
                                    mAccZView.setText(String.format("%.4f", acc[2]));

                                    double gForce = Math.sqrt(acc[0]*acc[0] + acc[1]*acc[1] + acc[2]*acc[2]) - SensorManager.STANDARD_GRAVITY;

                                    mGForce.setText(String.format("%.4f", gForce));
                                }
                            });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        IntentFilter filter = new IntentFilter(LoggingService.SERVICE_BROADCAST_MESSAGE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBcReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBcReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
    }
}
