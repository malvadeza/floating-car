package io.github.malvadeza.floatingcar.trips.domain.usecase;


import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import io.github.malvadeza.floatingcar.UseCase;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.source.TripsRepository;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GetTrips extends UseCase<GetTrips.RequestValues, GetTrips.ResponseValue> {

    private TripsRepository tripsRepository;

    public GetTrips(@NonNull TripsRepository tripsRepository) {
        super(Schedulers.io());
        this.tripsRepository = tripsRepository;
    }

    @Override
    protected Observable<ResponseValue> execute(final RequestValues requestValues) {
        return tripsRepository.getTrips().map(new Function<List<TripAdapter.TripHolder>, ResponseValue>() {
            @Override
            public ResponseValue apply(@io.reactivex.annotations.NonNull List<TripAdapter.TripHolder> tripHolders) throws Exception {
                return new ResponseValue(tripHolders);
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final List<TripAdapter.TripHolder> trips;

        public ResponseValue(@NonNull List<TripAdapter.TripHolder> trips) {
            this.trips = trips;
        }

        public List<TripAdapter.TripHolder> getTrips() {
            return trips;
        }

    }
}
