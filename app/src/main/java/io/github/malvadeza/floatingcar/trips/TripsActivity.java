package io.github.malvadeza.floatingcar.trips;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.UseCaseHandler;

public class TripsActivity extends AppCompatActivity implements TripsContract.View {
    private TripsPresenter tripsPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.start_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripsPresenter.startTrip();
            }
        });

        tripsPresenter = new TripsPresenter(UseCaseHandler.getInstance(),
                this/*, Loader*/);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tripsPresenter.start();
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
