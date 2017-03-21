package io.github.malvadeza.floatingcar.tripdetail;


import android.support.annotation.NonNull;
import android.util.Log;

import io.github.malvadeza.floatingcar.BasePresenter;
import io.github.malvadeza.floatingcar.tripdetail.domain.usecase.GetTrip;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TripDetailsPresenter implements BasePresenter{

    private static final String TAG = TripDetailsPresenter.class.getSimpleName();

    private Long tripId;
    private TripDetailsFragment view;
    private GetTrip getTrip;

    public TripDetailsPresenter(Long tripId, @NonNull TripDetailsFragment view, @NonNull GetTrip getTrip) {
        this.tripId = tripId;
        this.getTrip = getTrip;
        this.view = view;

        this.view.setPresenter(this);
    }

    @Override
    public void start() {
        loadTrip();
    }

    private void loadTrip() {
        Log.d(TAG, "loadTrip");

        getTrip.run(new GetTrip.RequestValues(Long.toString(tripId))).subscribe(new Observer<GetTrip.ResponseValue>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(GetTrip.ResponseValue responseValue) {
                Log.d(TAG, "onNext ->" + responseValue.getTrip().getSamples().size());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "ERROR ->" + e.toString());
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
