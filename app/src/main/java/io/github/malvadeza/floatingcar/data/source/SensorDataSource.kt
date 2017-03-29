package io.github.malvadeza.floatingcar.data.source

import android.hardware.SensorEvent
import io.reactivex.Observable

interface SensorDataSource {
    fun listen(): Observable<SensorEvent>
}