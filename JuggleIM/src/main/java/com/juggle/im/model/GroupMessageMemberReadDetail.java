package com.juggle.im.model;

public class GroupMessageMemberReadDetail {
    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public long getReadTime() {
        return mReadTime;
    }

    public void setReadTime(long readTime) {
        mReadTime = readTime;
    }

    private UserInfo mUserInfo;
    private long mReadTime;
}
