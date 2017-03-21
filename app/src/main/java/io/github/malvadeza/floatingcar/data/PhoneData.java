package io.github.malvadeza.floatingcar.data;


import com.google.android.gms.maps.model.LatLng;

public final class PhoneData {

    private LatLng latlng;

    private double[] acc = new double[3];

    public PhoneData() {

    }

    public void setLatLng(double latitude, double longitude) {
        latlng = new LatLng(latitude, longitude);
    }

    public void setAccelerometer(double x, double y, double z) {
        acc[0] = x;
        acc[1] = y;
        acc[2] = z;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public double[] getAcc() {
        return acc;
    }
}
