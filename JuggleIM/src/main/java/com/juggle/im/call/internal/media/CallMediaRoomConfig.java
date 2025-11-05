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

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private boolean mIsUserStatusNotify;
    private String mToken;
    private String mUrl;
}
