package com.juggle.im.interfaces;

import java.util.Map;

public class GroupMember {

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getGroupDisplayName() {
        return mGroupDisplayName;
    }

    public void setGroupDisplayName(String groupDisplayName) {
        mGroupDisplayName = groupDisplayName;
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public void setExtra(Map<String, String> extra) {
        mExtra = extra;
    }

    private String mGroupId;
    private String mUserId;
    private String mGroupDisplayName;
    private Map<String, String> mExtra;
}
