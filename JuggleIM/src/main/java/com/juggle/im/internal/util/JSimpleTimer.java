package com.juggle.im.internal.util;

import android.os.Handler;
import android.os.HandlerThread;

public abstract class JSimpleTimer {

    private Handler mHandler = null;
    private Runnable mRunnable = null;
    private HandlerThread mHandlerThread = null;
    private final int mInterval;

    public JSimpleTimer(int interval) {
        this.mInterval = interval;
    }

    public synchronized void init() {
        if (mHandler != null) {
            return;
        }
        // Run timer callbacks on a dedicated background looper instead of the caller thread.
        mHandlerThread = new HandlerThread("JSimpleTimer-" + Integer.toHexString(System.identityHashCode(this)));
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mRunnable = () -> {
            mHandler.postDelayed(mRunnable, mInterval);
            try {
                doAction();
            } catch (Exception e) {
                JLogger.e("J-Timer", "runnable error, exception is " + e);
            }
        };
    }

    protected abstract void doAction();

    public void start(boolean immediately) {
        if (mHandler == null || mRunnable == null) {
            init();
        }
        stop();
        onStart();
        mHandler.postDelayed(mRunnable, immediately ? 0 : mInterval);
    }

    protected void onStart() {
        // default do nothing
    }

    public void stop() {
        if (mHandler == null || mRunnable == null) {
            onStop();
            return;
        }
        mHandler.removeCallbacks(mRunnable);
        onStop();
    }

    protected void onStop() {
        // default do nothing
    }
}
