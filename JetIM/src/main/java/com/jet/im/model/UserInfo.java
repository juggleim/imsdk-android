package com.jet.im.model;

public class UserInfo {

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    private String mUserId;
    private String mUserName;
    private String mPortrait;
}
