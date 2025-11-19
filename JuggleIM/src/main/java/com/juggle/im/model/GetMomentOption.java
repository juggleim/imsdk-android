package com.juggle.im.model;

import com.juggle.im.JIMConst;

public class GetMomentOption {
    public JIMConst.PullDirection getDirection() {
        return mDirection;
    }

    public void setDirection(JIMConst.PullDirection direction) {
        mDirection = direction;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    private long mStartTime;
    private int mCount;
    private JIMConst.PullDirection mDirection;
}
