package io.github.malvadeza.floatingcar.trips;


import java.util.List;

import io.github.malvadeza.floatingcar.BasePresenter;
import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;

public interface TripsContract {

    interface View extends BaseView<Presenter> {
        //void initLoader();

        void showTrips(List<TripAdapter.TripHolder> trips);

        void showProgressBar();

        void hideProgressBar();

    }

    interface Presenter extends BasePresenter {
        void startTrip();
        //TripsLoader getTripsLoader();
    }

}
