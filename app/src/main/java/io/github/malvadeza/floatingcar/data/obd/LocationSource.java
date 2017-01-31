package io.github.malvadeza.floatingcar.data.source;


import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

public class LocationSource implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = LocationSource.class.getSimpleName();
    private static final float DISTANCE_THRESHOLD = 10;

    private Location mLastLocation;
    private Location mSegmentBeginning;

    public Location getLastLocation() {
        return mLastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");

        mLastLocation = location;

        if (mSegmentBeginning == null) {
            mSegmentBeginning = location;
        } else if (mSegmentBeginning.distanceTo(mLastLocation) >= DISTANCE_THRESHOLD) {
            // TODO: Trigger save data
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
