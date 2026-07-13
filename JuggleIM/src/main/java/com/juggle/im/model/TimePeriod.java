package com.juggle.im.model;

public class TimePeriod {
    // Start time in "HH:mm" format.
    String mStartTime;

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    // End time in "HH:mm" format.
    String mEndTime;
}
