package io.github.malvadeza.floatingcar.trips

import io.github.malvadeza.floatingcar.BasePresenter
import io.github.malvadeza.floatingcar.data.Trip
import io.github.malvadeza.floatingcar.trips.domain.usecase.GetTrips
import timber.log.Timber

class TripsPresenter(private val getTrips: GetTrips, private val view: TripsView/*, @NonNull Loader*/) : BasePresenter {

    init {
        this.view.setPresenter(this)
    }

    override fun start() {
        //view.initLoader();
        Timber.d("start")
        loadTrips()
    }

    fun startTrip() {
        Timber.d("startTrip")
        view.startTripLoggingActivity()
    }
    /*
    @Override
    public void getTripsLoader() {

    }
    */

    fun openTripDetails(trip: Trip) {
        view.openTripDetailsActivity(trip.id)
    }

    fun refreshTrips() {
        view.clearTrips()
        loadTrips()
    }

    private fun loadTrips() {
        view.showLoadingStatus()

        getTrips.run(null).subscribe({ view.showTrips(it.trips) }, { Timber.d(it) }, { view.hideLoadingStatus() })
    }
}


