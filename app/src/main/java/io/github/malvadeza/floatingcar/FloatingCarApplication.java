package io.github.malvadeza.floatingcar;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class FloatingCarApplication extends Application {
    private static final String TAG = FloatingCarApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        Timber.plant(new Timber.DebugTree());

        LeakCanary.install(this);
    }
}
