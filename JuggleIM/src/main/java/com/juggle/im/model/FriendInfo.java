package com.juggle.im.model;

public class FriendInfo {
    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public boolean isFriend() {
        return mIsFriend;
    }

    public void setFriend(boolean friend) {
        mIsFriend = friend;
    }

    public String getAlias() {
        return mAlias;
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        mUpdatedTime = updatedTime;
    }

    private String mUserId;
    private boolean mIsFriend;
    private String mAlias;
    private long mUpdatedTime;
}
