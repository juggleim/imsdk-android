package com.juggle.im.model;

public class TimePeriod {
    //开始时间，格式为 "HH:mm"
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

    //结束时间，格式为 "HH:mm"
    String mEndTime;
}
