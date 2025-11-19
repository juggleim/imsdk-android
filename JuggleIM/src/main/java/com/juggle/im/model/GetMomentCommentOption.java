package com.juggle.im.model;

import com.juggle.im.JIMConst;

public class GetMomentCommentOption {
    public String getMomentId() {
        return mMomentId;
    }

    public void setMomentId(String momentId) {
        mMomentId = momentId;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public JIMConst.PullDirection getDirection() {
        return mDirection;
    }

    public void setDirection(JIMConst.PullDirection direction) {
        mDirection = direction;
    }

    private String mMomentId;
    private long mStartTime;
    private int mCount;
    private JIMConst.PullDirection mDirection;
}
