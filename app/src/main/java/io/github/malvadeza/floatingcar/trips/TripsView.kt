package io.github.malvadeza.floatingcar.trips

import io.github.malvadeza.floatingcar.BaseView
import io.github.malvadeza.floatingcar.data.Trip

interface TripsView: BaseView<TripsPresenter> {

    fun clearTrips()

    fun showTrips(trips: List<Trip>)

    fun showLoadingStatus()

    fun hideLoadingStatus()

    fun openTripDetailsActivity(tripId: Long)

    fun startTripLoggingActivity()

}
