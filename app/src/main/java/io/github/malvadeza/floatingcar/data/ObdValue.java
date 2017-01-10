package io.github.malvadeza.floatingcar.data;


public class ObdValue {
    private String mPid;
    private String mValue;

    public ObdValue(String pid, String value) {
        mPid = pid;
        mValue = value;
    }

    public String getPid() {
        return mPid;
    }

    public String getValue() {
        return mValue;
    }


}
