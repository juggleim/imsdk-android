package com.juggle.im.internal.util;

public class IntervalGenerator {

    public int getNextInterval() {
        int result = mInterval;
        if (result == 0) {
            mInterval = 1;
        } else if (result >= 32) {

        } else {
            mInterval *= 2;
        }
        JLogger.i("J-Itvl", "interval is " + result);
        return result;
    }

    public void reset() {
        mInterval = 0;
    }

    private int mInterval;
}
