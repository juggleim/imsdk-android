package com.juggle.im.internal.util;

public class IntervalGenerator {

    public int getNextInterval() {
        int result = mInterval;
        if (result < 1000) {
            mInterval = 1000;
        } else if (result >= 32000) {

        } else {
            mInterval *= 2;
        }
        JLogger.i("J-Itvl", "interval is " + result);
        return result;
    }

    public void reset() {
        mInterval = 300;
    }

    private int mInterval = 300;
}
