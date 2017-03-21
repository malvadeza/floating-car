package io.github.malvadeza.floatingcar.data;


public final class ObdData {

    private String value;

    private String pid;

    public ObdData() {

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

}
