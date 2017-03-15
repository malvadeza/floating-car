package io.github.malvadeza.floatingcar.data.source;


import android.support.annotation.NonNull;

import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.Trip;
import io.reactivex.Observable;

public interface TripsDataSource {
    interface LoadTripsCallBack {

    }

    interface GetTripsCallback {

    }

    Observable<List<TripAdapter.TripHolder>> getTrips();

    Observable<TripAdapter.TripHolder> getTrip(@NonNull String tripId);
}
