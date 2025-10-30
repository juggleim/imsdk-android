package com.juggle.im.model;

import java.util.List;

public class GroupMessageReadInfoDetail {
    public int getReadCount() {
        return mReadCount;
    }

    public void setReadCount(int readCount) {
        mReadCount = readCount;
    }

    public int getMemberCount() {
        return mMemberCount;
    }

    public void setMemberCount(int memberCount) {
        mMemberCount = memberCount;
    }

    public List<GroupMessageMemberReadDetail> getReadMembers() {
        return mReadMembers;
    }

    public void setReadMembers(List<GroupMessageMemberReadDetail> readMembers) {
        mReadMembers = readMembers;
    }

    public List<GroupMessageMemberReadDetail> getUnreadMembers() {
        return mUnreadMembers;
    }

    public void setUnreadMembers(List<GroupMessageMemberReadDetail> unreadMembers) {
        mUnreadMembers = unreadMembers;
    }

    private int mReadCount;
    private int mMemberCount;
    private List<GroupMessageMemberReadDetail> mReadMembers;
    private List<GroupMessageMemberReadDetail> mUnreadMembers;
}
