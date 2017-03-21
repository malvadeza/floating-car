package io.github.malvadeza.floatingcar;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

public class FloatingCarApplication extends Application {
    private static final String TAG = FloatingCarApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);
    }
}
