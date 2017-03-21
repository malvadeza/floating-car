package io.github.malvadeza.floatingcar.data;


import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

public final class Trip {

    private Long id;

    private Date startedAt;

    private Date finishedAt;

    private String sha256;

    private List<Sample> samples;

    public Trip(@Nullable Long id, @Nullable Date startedAt, @Nullable Date finishedAt) {
        this(id, startedAt, finishedAt, null);
    }

    public Trip(@Nullable Long id, @Nullable Date startedAt, @Nullable Date finishedAt, @Nullable List<Sample> samples) {
        this.id = id;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.samples = samples;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public long getDurationInMinutes() {
        long diff = finishedAt.getTime() - startedAt.getTime();
        diff = diff / (60 * 1000) % 60;

        return diff;
    }
}
