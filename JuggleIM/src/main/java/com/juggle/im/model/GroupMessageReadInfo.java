package com.juggle.im.model;

public class GroupMessageReadInfo {

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

    private int mReadCount;
    private int mMemberCount;
}
