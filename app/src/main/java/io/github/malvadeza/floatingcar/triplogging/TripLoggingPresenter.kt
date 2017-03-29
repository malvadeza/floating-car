package io.github.malvadeza.floatingcar.triplogging

import com.google.android.gms.location.LocationRequest
import io.github.malvadeza.floatingcar.BasePresenter
import io.github.malvadeza.floatingcar.triplogging.domain.usecase.GetTripData
import io.reactivex.disposables.CompositeDisposable

class TripLoggingPresenter(private val getTripData: GetTripData, private var view: TripLoggingFragment?) : BasePresenter {
    private val compositeDisposable = CompositeDisposable()

    init {
        view?.setPresenter(this)
    }

    companion object {
        private val TAG = TripLoggingPresenter::class.java.simpleName
    }

    override fun start() {
        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((3 * 1000).toLong())
                .setFastestInterval((1 * 1000).toLong());
        val requestValues = GetTripData.RequestValues(locationRequest)

        val locationDisposable = getTripData.run(requestValues).subscribe {
            view?.updateLocationView(it.location)
            view?.updateAccelerometer(it.sensorEvent.values)
        }
        compositeDisposable.add(locationDisposable)
    }

    fun unsubscribe() {
        compositeDisposable.clear()
        view = null
    }

}
