package io.github.malvadeza.floatingcar.data;


import com.google.android.gms.maps.model.LatLng;

public final class PhoneData {

    private LatLng latlng;

    private Accelerometer acc;

    public PhoneData() {

    }

    public void setLatLng(double latitude, double longitude) {
        latlng = new LatLng(latitude, longitude);
    }

    public void setAccelerometer(float x, float y, float z) {
        acc = new Accelerometer(x, y, z);
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public Accelerometer getAccelerometer() {
        return acc;
    }

    public static class Accelerometer {
        private float xAxis;
        private float yAxis;
        private float zAxis;

        public Accelerometer(float xAxis, float yAxis, float zAxis) {
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            this.zAxis = zAxis;
        }

    }
}
