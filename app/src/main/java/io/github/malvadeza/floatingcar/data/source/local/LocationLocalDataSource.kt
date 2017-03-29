package io.github.malvadeza.floatingcar.data.source.local

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.HandlerThread
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.github.malvadeza.floatingcar.data.source.LocationDataSource
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger


class LocationLocalDataSource private constructor(private val context: Context): LocationDataSource {
    companion object {
        private var instance: LocationDataSource? = null
        private var looperThread: HandlerThread? = null
        private val subscribersCount = AtomicInteger()

        @JvmStatic
        fun getInstance(context: Context): LocationDataSource {
            if (instance == null) {
                instance = LocationLocalDataSource(context)
            }

            return instance!!
        }
    }

    override fun listen(locationRequest: LocationRequest): Observable<Location> {
        val locationSubscriber = LocationSubscriber()

        val googleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(locationSubscriber)
                .addOnConnectionFailedListener(locationSubscriber)
                .addApi(LocationServices.API)
                .build()

        var locationListener: LocationListener? = null

        return Observable.create(ObservableOnSubscribe<Location> { subscriber ->
            locationListener = LocationListener {
                Timber.d("Thread name -> ${Thread.currentThread().name}")
                subscriber.onNext(it)
            }

            locationSubscriber.connectionListener = object : LocationSubscriber.OnConnectionListener {
                override fun onConnectionFailed(connectionResult: ConnectionResult) {
                    subscriber.onError(Error(connectionResult.errorMessage))
                }

                override fun onConnected(bundle: Bundle?) {
                    val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)

                    if (location != null)
                        subscriber.onNext(location)

                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, locationListener, looperThread?.looper)
                }
            }

            googleApiClient.connect()
        }).doOnSubscribe {
            if (subscribersCount.getAndIncrement() == 0) {
                Timber.d("Starting HandlerThread")
                looperThread = HandlerThread("LocationLocalDataSource")
                looperThread?.start()
            }
        }.doOnDispose {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
            googleApiClient.disconnect()

            if (subscribersCount.decrementAndGet() == 0) {
                Timber.d("Stopping HandlerThread")
                looperThread?.quitSafely()
                looperThread = null
            }
        }
    }

    class LocationSubscriber: GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
        lateinit var connectionListener: OnConnectionListener

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            connectionListener.onConnectionFailed(connectionResult)
        }

        override fun onConnected(bundle: Bundle?) {
            connectionListener.onConnected(bundle)
        }

        override fun onConnectionSuspended(i: Int) {
        }

        interface OnConnectionListener {
            fun onConnectionFailed(connectionResult: ConnectionResult)

            fun onConnected(bundle: Bundle?)
        }
    }
}
