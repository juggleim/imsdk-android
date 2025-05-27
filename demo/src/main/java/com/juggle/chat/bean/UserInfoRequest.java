package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

public class UserInfoRequest {
    @SerializedName("user_id")
    private String userId;
    private String nickname;
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
