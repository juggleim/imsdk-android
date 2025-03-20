package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

public class FriendApplicationBean {
    @SerializedName("target_user")
    private FriendBean userInfo;
    @SerializedName("is_sponsor")
    private boolean isSponsor;
    private int status;
    @SerializedName("apply_time")
    private long applyTime;

    public boolean isSponsor() {
        return isSponsor;
    }

    public void setSponsor(boolean sponsor) {
        isSponsor = sponsor;
    }

    public FriendBean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(FriendBean userInfo) {
        this.userInfo = userInfo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(long applyTime) {
        this.applyTime = applyTime;
    }
}
