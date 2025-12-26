package com.juggle.im.model;

import com.juggle.im.JIMConst;

import org.json.JSONException;
import org.json.JSONObject;

public class GetMomentOption {

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("start", mStartTime);
        json.put("limit", mCount);
        int order = (mDirection == JIMConst.PullDirection.OLDER) ? 0 : 1;
        json.put("order", order);

        return json;
    }

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
    private JIMConst.PullDirection mDirection = JIMConst.PullDirection.OLDER;
}
