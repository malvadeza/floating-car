package io.github.malvadeza.floatingcar.data.source

import android.hardware.SensorEvent
import io.reactivex.Observable

class SensorRepository private constructor(val localDataSource: SensorDataSource): SensorDataSource {
    companion object {
        private var instance: SensorDataSource? = null

        @JvmStatic
        fun getInstance(localDataSource: SensorDataSource): SensorDataSource {
            if (instance == null)
                instance = SensorRepository(localDataSource)

            return instance!!
        }
    }

    override fun listen(): Observable<SensorEvent> = localDataSource.listen()
}