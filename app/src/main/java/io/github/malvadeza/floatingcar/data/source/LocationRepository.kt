package io.github.malvadeza.floatingcar.data.source

import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable


class LocationRepository private constructor(val localDataSource: LocationDataSource): LocationDataSource {

    companion object {
        private var instance: LocationDataSource? = null

        @JvmStatic
        fun getInstance(localDataSource: LocationDataSource): LocationDataSource {
            if (instance == null)
                instance = LocationRepository(localDataSource)

            return instance!!
        }
    }

    override fun listen(locationRequest: LocationRequest): Observable<Location> = localDataSource.listen(locationRequest)

}
