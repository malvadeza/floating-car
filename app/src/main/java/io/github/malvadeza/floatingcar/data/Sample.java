package io.github.malvadeza.floatingcar.data;

import android.provider.ContactsContract;

import java.util.Date;
import java.util.List;

public final class Sample {

    private Long id;

    private Date takenAt;

    private PhoneData phoneData;

    private List<ObdData> obdData;

    public Sample() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Date getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Date takenAt) {
        this.takenAt = takenAt;
    }

    public PhoneData getPhoneData() {
        return phoneData;
    }

    public void setPhoneData(PhoneData phoneData) {
        this.phoneData = phoneData;
    }

    public List<ObdData> getObdData() {
        return obdData;
    }

    public void setObdData(List<ObdData> obdData) {
        this.obdData = obdData;
    }
}
