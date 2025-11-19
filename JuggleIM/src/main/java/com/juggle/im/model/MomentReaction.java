package com.juggle.im.model;

import java.util.List;

public class MomentReaction {
    public List<UserInfo> getUserList() {
        return mUserList;
    }

    public void setUserList(List<UserInfo> userList) {
        mUserList = userList;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    private String mKey;
    private List<UserInfo> mUserList;
}
