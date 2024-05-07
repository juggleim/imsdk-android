package com.jet.im.internal.util;

import android.os.Handler;

import com.jet.im.utils.LoggerUtils;

public abstract class JSimpleTimer {

    private Handler handler = null;
    private Runnable runnable = null;
    private final int interval;

    public JSimpleTimer(int interval) {
        this.interval = interval;
    }

    public void init() {
        handler = new Handler();
        runnable = () -> {
            try {
                doAction();
            } catch (Exception e) {
                LoggerUtils.w(JSimpleTimer.class.getSimpleName() + ", " + e);
            }
            handler.postDelayed(runnable, interval);
        };
    }

    protected abstract void doAction();

    public void start(boolean immediately) {
        stop();
        onStart();
        handler.postDelayed(runnable, immediately ? 0 : interval);
    }

    protected void onStart() {
        // default do nothing
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        onStop();
    }

    protected void onStop() {
        // default do nothing
    }
}