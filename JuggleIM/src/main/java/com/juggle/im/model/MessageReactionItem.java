package com.juggle.im.model;

import java.util.List;

public class MessageReactionItem {

    public List<UserInfo> getUserInfoList() {
        return mUserInfoList;
    }

    public void setUserInfoList(List<UserInfo> userInfoList) {
        mUserInfoList = userInfoList;
    }

    public String getReactionId() {
        return mReactionId;
    }

    public void setReactionId(String reactionId) {
        mReactionId = reactionId;
    }

    private String mReactionId;
    private List<UserInfo> mUserInfoList;
}
