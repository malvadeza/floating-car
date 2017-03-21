package io.github.malvadeza.floatingcar.tripdetail;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.data.source.TripsRepository;
import io.github.malvadeza.floatingcar.data.source.local.TripsLocalDataSource;
import io.github.malvadeza.floatingcar.tripdetail.domain.usecase.GetTrip;

public class TripDetailsActivity extends AppCompatActivity {

    public static final String TRIP_ID = "io.github.malvadeza.floatingcar.TRIP_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_details_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TripDetailsFragment tripDetailsFragment = (TripDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (tripDetailsFragment == null) {
            tripDetailsFragment = TripDetailsFragment.newInstance();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, tripDetailsFragment);
            transaction.commit();
        }

        long tripid = getIntent().getLongExtra(TRIP_ID, 0);

        TripDetailsPresenter presenter = new TripDetailsPresenter(tripid,
                tripDetailsFragment,
                new GetTrip(TripsRepository.getInstance(TripsLocalDataSource.getInstance((getApplicationContext())))));
    }

}
