package com.juggle.im.model;

import java.util.Map;

public class GroupInfo {
    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public void setExtra(Map<String, String> extra) {
        mExtra = extra;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        mUpdatedTime = updatedTime;
    }

    private String mGroupId;
    private String mGroupName;
    private String mPortrait;
    private Map<String, String> mExtra;
    private long mUpdatedTime;
}
