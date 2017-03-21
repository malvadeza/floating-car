package io.github.malvadeza.floatingcar.trips;


import android.support.annotation.NonNull;
import android.util.Log;

import io.github.malvadeza.floatingcar.BasePresenter;
import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.trips.domain.usecase.GetTrips;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TripsPresenter implements BasePresenter {
    private static final String TAG = TripsPresenter.class.getSimpleName();

    private final TripsFragment view;
    private final GetTrips getTrips;

    public TripsPresenter(@NonNull GetTrips getTrips,
                          @NonNull TripsFragment view/*,
                          @NonNull Loader*/) {
        this.view = view;
        this.getTrips = getTrips;

        this.view.setPresenter(this);
    }

    @Override
    public void start() {
        //view.initLoader();
        Log.d(TAG, "start");
        loadTrips();
    }

    public void startTrip() {
        Log.d(TAG, "startTrip()");
    }
    /*
    @Override
    public void getTripsLoader() {

    }
    */

    public void openTripDetails(Trip trip) {
        view.showTripDetailsActivity(trip.getId());
    }

    public void refreshTrips() {
        view.clearTrips();
        loadTrips();
    }

    private void loadTrips() {
        view.showProgressBar();

        getTrips.run(null).subscribe(new Observer<GetTrips.ResponseValue>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "loadTrips#onSubscribe");
            }

            @Override
            public void onNext(GetTrips.ResponseValue responseValue) {
                Log.d(TAG, "loadTrips#onNext");
                view.showTrips(responseValue.getTrips());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "loadTrips#onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "loadTrips#onComplete");
                view.hideProgressBar();
            }
        });
    }
}
