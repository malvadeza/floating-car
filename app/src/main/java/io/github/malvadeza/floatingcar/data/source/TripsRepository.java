package io.github.malvadeza.floatingcar.data.source;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.Trip;
import io.reactivex.Observable;

public class TripsRepository implements TripsDataSource {

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

    public Observable<List<Trip>> getTrips() {
        return localDataSource.getTrips();
    }

    @Override
    public Observable<Trip> getTrip(@NonNull String tripId) {
        Log.d("TripsRepository", "gettingTrip");
        return localDataSource.getTrip(tripId);
    }

}
