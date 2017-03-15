package io.github.malvadeza.floatingcar.trips;


import android.support.annotation.NonNull;
import android.util.Log;

import io.github.malvadeza.floatingcar.trips.domain.usecase.GetTrips;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TripsPresenter implements TripsContract.Presenter {
    private static final String TAG = TripsPresenter.class.getSimpleName();

    private final TripsContract.View tripsView;
    private final GetTrips getTrips;

    public TripsPresenter(@NonNull GetTrips getTrips,
                          @NonNull TripsContract.View tripsView/*,
                          @NonNull Loader*/) {
        this.tripsView = tripsView;
        this.getTrips = getTrips;

        this.tripsView.setPresenter(this);
    }

    @Override
    public void start() {
        //tripsView.initLoader();
        Log.d(TAG, "start");
        loadTrips();
    }

    @Override
    public void startTrip() {
        Log.d(TAG, "startTrip()");
    }
    /*
    @Override
    public void getTripsLoader() {

    }
    */

    private void loadTrips() {
        Log.d(TAG, "loadTrips");

        tripsView.showProgressBar();

        getTrips.run(null).subscribe(new Observer<GetTrips.ResponseValue>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "loadTrips#onSubscribe");
            }

            @Override
            public void onNext(GetTrips.ResponseValue responseValue) {
                Log.d(TAG, "loadTrips#onNext");
                tripsView.showTrips(responseValue.getTrips());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "loadTrips#onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "loadTrips#onComplete");
                tripsView.hideProgressBar();
            }
        });
    }
}
