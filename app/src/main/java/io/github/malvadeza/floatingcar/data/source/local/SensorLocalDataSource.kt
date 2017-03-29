package io.github.malvadeza.floatingcar.data.source.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import io.github.malvadeza.floatingcar.data.source.SensorDataSource
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class SensorLocalDataSource private constructor(private val context: Context): SensorDataSource {
    companion object {
        private var instance: SensorDataSource? = null
        private var looperThread: HandlerThread? = null
        private var handler: Handler? = null
        private val subscribersCount = AtomicInteger()


        @JvmStatic
        fun getInstance(context: Context): SensorDataSource {
            if (instance == null)
                instance = SensorLocalDataSource(context)

            return instance!!
        }
    }

    override fun listen(): Observable<SensorEvent> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var sensorListener: SensorEventListener? = null

        return Observable.create(ObservableOnSubscribe<SensorEvent> { subscriber ->
            sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                        Timber.d("Thread -> ${Thread.currentThread().name}")
                        subscriber.onNext(event)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // No-op
                }
            }

            sensorManager.registerListener(sensorListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL, handler)
        }).doOnSubscribe {
            if (subscribersCount.getAndIncrement() == 0) {
                Timber.d("Starting HandlerThread")
                looperThread = HandlerThread("SensorLocalDataSource")
                looperThread?.start()
                handler = Handler(looperThread?.looper)
            }
        }.doOnDispose {
            sensorManager.unregisterListener(sensorListener)

            if (subscribersCount.decrementAndGet() == 0) {
                Timber.d("Stopping HandlerThread")
                handler = null
                looperThread?.quitSafely()
                looperThread = null
            }
        }
    }
}
