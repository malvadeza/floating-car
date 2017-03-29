package io.github.malvadeza.floatingcar.trips;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.data.source.TripsRepository;
import io.github.malvadeza.floatingcar.data.source.local.TripsLocalDataSource;
import io.github.malvadeza.floatingcar.trips.domain.usecase.GetTrips;
import timber.log.Timber;

public class TripsActivity extends AppCompatActivity {
    private static final String TAG = TripsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.trips_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TripsFragment tripsFragment = (TripsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (tripsFragment == null) {
            tripsFragment = TripsFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, tripsFragment);
            transaction.commit();
        }

        TripsPresenter tripsPresenter = new TripsPresenter(
                new GetTrips(TripsRepository.getInstance(TripsLocalDataSource.getInstance(getApplicationContext()))),
                tripsFragment
        );

        Timber.d("onCreate Finished");
    }

    /*
    @Override
    public void initLoader() {
        getSupportLoaderManager().initLoader(ID, args, this);
    }

    Loader onCreateLoader(int id, Bundle args) {
        return tripsPresenter.getTripsLoader();
    }

    void onLoadFinished(Loader<D> loader, D data) {
    }

    void onLoaderReset(Loader<D> loader) {
    }
    */


}
