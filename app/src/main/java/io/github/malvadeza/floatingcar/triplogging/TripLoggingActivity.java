package io.github.malvadeza.floatingcar.triplogging;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.data.source.LocationRepository;
import io.github.malvadeza.floatingcar.data.source.SensorRepository;
import io.github.malvadeza.floatingcar.data.source.local.LocationLocalDataSource;
import io.github.malvadeza.floatingcar.data.source.local.SensorLocalDataSource;
import io.github.malvadeza.floatingcar.triplogging.domain.usecase.GetTripData;

public class TripLoggingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_logging_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TripLoggingFragment tripLoggingFragment = (TripLoggingFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (tripLoggingFragment == null) {
            tripLoggingFragment = TripLoggingFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, tripLoggingFragment);
            transaction.commit();
        }

        TripLoggingPresenter tripLoggingPresenter = new TripLoggingPresenter(
                new GetTripData(
                        LocationRepository.getInstance(LocationLocalDataSource.getInstance(getApplicationContext())),
                        SensorRepository.getInstance(SensorLocalDataSource.getInstance(getApplicationContext()))
                ),
                tripLoggingFragment
        );
    }
}
