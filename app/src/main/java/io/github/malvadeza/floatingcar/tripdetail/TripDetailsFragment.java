package io.github.malvadeza.floatingcar.tripdetail;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.R;

public class TripDetailsFragment extends Fragment implements BaseView<TripDetailsPresenter> {

    private static final String TAG = TripDetailsPresenter.class.getSimpleName();

    private TripDetailsPresenter presenter;

    private GoogleMap googleMap;

    public TripDetailsFragment() {
    }

    public static TripDetailsFragment newInstance() {
        return new TripDetailsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trip_details_fragment, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.map_fragment, mapFragment);
            transaction.commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        Log.d(TAG, "MapLoaded");
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void setPresenter(TripDetailsPresenter presenter) {
        this.presenter = presenter;
    }
}
