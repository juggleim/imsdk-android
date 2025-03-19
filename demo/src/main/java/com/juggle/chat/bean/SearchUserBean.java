package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

public class SearchUserBean {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    @SerializedName("user_id")
    private String userId;
    private String nickname;
    private String avatar;
    @SerializedName("is_friend")
    private boolean isFriend;

}
