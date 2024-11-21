package com.juggle.im.call.internal.media;

public class CallMediaRoomConfig {
    public String getZegoToken() {
        return mZegoToken;
    }

    public void setZegoToken(String zegoToken) {
        mZegoToken = zegoToken;
    }

    public boolean isUserStatusNotify() {
        return mIsUserStatusNotify;
    }

    public void setUserStatusNotify(boolean userStatusNotify) {
        mIsUserStatusNotify = userStatusNotify;
    }

    private boolean mIsUserStatusNotify;
    private String mZegoToken;
}
