package io.github.malvadeza.floatingcar.tripdetail.domain.usecase;


import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.github.malvadeza.floatingcar.UseCase;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.data.source.TripsRepository;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GetTrip extends UseCase<GetTrip.RequestValues, GetTrip.ResponseValue> {

    private TripsRepository tripsRepository;

    public GetTrip(@NonNull TripsRepository tripsRepository) {
        super(Schedulers.io());
        this.tripsRepository = tripsRepository;
    }

    @Override
    protected Observable<ResponseValue> execute(RequestValues requestValues) {
        return tripsRepository.getTrip(requestValues.getTripId()).map(new Function<Trip, ResponseValue>() {
            @Override
            public ResponseValue apply(@io.reactivex.annotations.NonNull Trip trip) throws Exception {
                return new ResponseValue(trip);
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private String tripId;

        public RequestValues(@NonNull String tripId) {
            this.tripId = tripId;
        }

        public String getTripId() {
            return tripId;
        }

    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private Trip trip;

        public ResponseValue(Trip trip) {
            this.trip = trip;
        }

        public Trip getTrip() {
            return trip;
        }
    }

}
