package io.github.malvadeza.floatingcar.data.source;


import android.support.annotation.NonNull;

import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.reactivex.Observable;

public class TripsRepository {

    private static TripsRepository instance;

    private final TripsDataSource localDataSource;

    private TripsRepository(@NonNull TripsDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    public static TripsRepository getInstance(@NonNull TripsDataSource localDataSource) {
        if (instance == null) {
            instance = new TripsRepository(localDataSource);
        }

        return instance;
    }

    public Observable<List<TripAdapter.TripHolder>> getTrips() {
        return localDataSource.getTrips();
    }

}
