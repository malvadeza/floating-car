package io.github.malvadeza.floatingcar.trips;


import android.support.annotation.NonNull;

import io.github.malvadeza.floatingcar.UseCaseHandler;

public class TripsPresenter implements TripsContract.Presenter {
    private final UseCaseHandler useCaseHandler;
    private final TripsContract.View tripsView;

    public TripsPresenter(@NonNull UseCaseHandler useCaseHandler,
                          @NonNull TripsContract.View tripsView/*,
                          @NonNull Loader*/) {
        this.useCaseHandler = useCaseHandler;
        this.tripsView = tripsView;
    }

    @Override
    public void start() {
        //tripsView.initLoader();
    }

    @Override
    public void startTrip() {

    }
    /*
    @Override
    public void getTripsLoader() {

    }
    */
}
