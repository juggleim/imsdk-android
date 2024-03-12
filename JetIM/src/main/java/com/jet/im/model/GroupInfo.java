package com.jet.im.model;

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

    private String mGroupId;
    private String mGroupName;
    private String mPortrait;
}
