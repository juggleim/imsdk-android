package com.juggle.im.internal.model;

public class E2EEInfo {
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public byte[] getPubKey() {
        return mPubKey;
    }

    public void setPubKey(byte[] pubKey) {
        mPubKey = pubKey;
    }

    private String mUserId;
    private String mDeviceId;
    private byte[] mPubKey;
}
