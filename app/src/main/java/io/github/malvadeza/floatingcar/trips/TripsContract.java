package io.github.malvadeza.floatingcar.trips;


import io.github.malvadeza.floatingcar.BasePresenter;
import io.github.malvadeza.floatingcar.BaseView;

public interface TripsContract {

    interface View extends BaseView<Presenter> {
        //void initLoader();
    }

    interface Presenter extends BasePresenter {
        void startTrip();
        //TripsLoader getTripsLoader();
    }

}
