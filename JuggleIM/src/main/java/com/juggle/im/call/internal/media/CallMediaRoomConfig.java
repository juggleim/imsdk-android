package com.juggle.im.call.internal.media;

public class CallMediaRoomConfig {
    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public boolean isUserStatusNotify() {
        return mIsUserStatusNotify;
    }

    public void setUserStatusNotify(boolean userStatusNotify) {
        mIsUserStatusNotify = userStatusNotify;
    }

    private boolean mIsUserStatusNotify;
    private String mToken;
}
