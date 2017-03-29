package io.github.malvadeza.floatingcar.triplogging;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.tripdetail.TripDetailsPresenter;

public class TripLoggingFragment extends Fragment implements BaseView<TripLoggingPresenter> {

    private static final String TAG = TripLoggingFragment.class.getSimpleName();

    private TripLoggingPresenter presenter;

    private TextView latitude;
    private TextView longitude;

    private TextView accX;
    private TextView accY;
    private TextView accZ;

    private GoogleMap googleMap;

    public static TripLoggingFragment newInstance() {
        return new TripLoggingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trip_logging_fragment, container, false);

        latitude = (TextView) view.findViewById(R.id.latitude);
        longitude = (TextView) view.findViewById(R.id.longitude);
        accX = (TextView) view.findViewById(R.id.xAxis);
        accY = (TextView) view.findViewById(R.id.yAxis);
        accZ = (TextView) view.findViewById(R.id.zAxis);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.map_fragment, mapFragment);
            transaction.commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        Log.d(TAG, "onMapLoaded");
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        presenter.unsubscribe();
    }

    @Override
    public void setPresenter(TripLoggingPresenter presenter) {
        this.presenter = presenter;
    }

    public void updateMap(Location location) {

    }

    public void updateLocationView(Location location) {
        latitude.setText(Double.toString(location.getLatitude()));
        longitude.setText(Double.toString(location.getLongitude()));
    }

    public void updateAccelerometer(float[] values) {
        accX.setText(Float.toString(values[0]));
        accY.setText(Float.toString(values[1]));
        accZ.setText(Float.toString(values[2]));
    }
}
