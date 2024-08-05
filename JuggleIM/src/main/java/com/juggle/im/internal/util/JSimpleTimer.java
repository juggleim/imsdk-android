package com.juggle.im.internal.util;

import android.os.Handler;

public abstract class JSimpleTimer {

    private Handler mHandler = null;
    private Runnable mRunnable = null;
    private final int mInterval;

    public JSimpleTimer(int interval) {
        this.mInterval = interval;
    }

    public void init() {
        mHandler = new Handler();
        mRunnable = () -> {
            try {
                doAction();
            } catch (Exception e) {
                JLogger.e("J-Timer", "runnable error, exception is " + e);
            }
            mHandler.postDelayed(mRunnable, mInterval);
        };
    }

    protected abstract void doAction();

    public void start(boolean immediately) {
        stop();
        onStart();
        mHandler.postDelayed(mRunnable, immediately ? 0 : mInterval);
    }

    protected void onStart() {
        // default do nothing
    }

    public void stop() {
        mHandler.removeCallbacks(mRunnable);
        onStop();
    }

    protected void onStop() {
        // default do nothing
    }
}