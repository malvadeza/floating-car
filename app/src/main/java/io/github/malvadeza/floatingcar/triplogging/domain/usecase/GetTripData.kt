package io.github.malvadeza.floatingcar.triplogging.domain.usecase

import android.hardware.SensorEvent
import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.github.malvadeza.floatingcar.UseCase
import io.github.malvadeza.floatingcar.data.source.LocationDataSource
import io.github.malvadeza.floatingcar.data.source.SensorDataSource
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class GetTripData(private val locationDataSource: LocationDataSource, private val sensorDataSource: SensorDataSource) : UseCase<GetTripData.RequestValues, GetTripData.ResponseValue>(Schedulers.io()) {

    override fun execute(requestValues: RequestValues?): Observable<ResponseValue> {
        return Observable.combineLatest(
                locationDataSource.listen(requestValues!!.locationRequest),
                sensorDataSource.listen(),
                BiFunction<Location, SensorEvent, ResponseValue>(::ResponseValue)
        )
    }

    class RequestValues(val locationRequest: LocationRequest) : UseCase.RequestValues

    class ResponseValue(val location: Location, val sensorEvent: SensorEvent) : UseCase.ResponseValue

}
