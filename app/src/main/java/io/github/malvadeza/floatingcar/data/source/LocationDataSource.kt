package io.github.malvadeza.floatingcar.data.source

import android.location.Location
import com.google.android.gms.location.LocationRequest

import io.reactivex.Observable

interface LocationDataSource {

    fun listen(locationRequest: LocationRequest): Observable<Location>

}
