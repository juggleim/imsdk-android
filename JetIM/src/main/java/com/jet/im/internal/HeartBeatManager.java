package com.jet.im.internal;

import com.jet.im.internal.core.JetIMCore;
import com.jet.im.utils.LoggerUtils;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeatManager {
    public HeartBeatManager(JetIMCore core) {
        mCore = core;
    }
    public void start() {
        stop();
        mPingTimer = new Timer();
        mPingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LoggerUtils.d("send ping");
                mCore.getWebSocket().ping();
            }
        }, PING_INTERVAL, PING_INTERVAL);
    }

    public void stop() {
        if (mPingTimer != null) {
            mPingTimer.cancel();
            mPingTimer = null;
        }
    }

    private final JetIMCore mCore;
    private Timer mPingTimer;
    private static final int PING_INTERVAL = 30*1000;

}
