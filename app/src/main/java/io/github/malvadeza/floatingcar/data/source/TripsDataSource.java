package io.github.malvadeza.floatingcar.data.source;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.Trip;
import io.reactivex.Observable;

public interface TripsDataSource {

    Observable<List<Trip>> getTrips();

    Observable<Trip> getTrip(final String tripId);
}
