package com.juggle.im.internal.core.network;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JSimpleTimer;

import java.util.concurrent.atomic.AtomicLong;

public class HeartbeatManager {
    private final static String TAG = HeartbeatManager.class.getSimpleName();
    private final static int HEARTBEAT_INTERVAL = 30 * 1000;
    private final static int HEARTBEAT_DETECTION_INTERVAL = 10 * 1000;
    private final static int HEARTBEAT_DETECTION_TIME_OUT = HEARTBEAT_INTERVAL * 3;

    private final JWebSocket mJWebsocket;
    private JSimpleTimer mHeartbeatTimer = null;
    private JSimpleTimer mHeartbeatDetectionTimer = null;
    private final AtomicLong mLastMessageReceivedTime = new AtomicLong(0);
    private boolean mIsInit = false;
    private boolean mIsRunning = false;

    public HeartbeatManager(JWebSocket jWebSocket) {
        this.mJWebsocket = jWebSocket;
        init();
    }

    public boolean isInit() {
        return mIsInit;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void start(boolean immediately) {
        JLogger.i("HB-Start", "start, immediately is " + immediately);
        stop();
        mHeartbeatTimer.start(immediately);
        mHeartbeatDetectionTimer.start(immediately);
        mIsRunning = true;
    }

    public void stop() {
        JLogger.i("HB-Stop", "stop");
        mHeartbeatTimer.stop();
        mHeartbeatDetectionTimer.stop();
        mIsRunning = false;
    }

    public void updateLastMessageReceivedTime() {
        mLastMessageReceivedTime.set(System.currentTimeMillis());
    }

    private void init() {
        if (mIsInit) return;

        mHeartbeatTimer = new JSimpleTimer(HEARTBEAT_INTERVAL) {
            @Override
            protected void doAction() {
                doHeartbeat();
            }
        };
        mHeartbeatTimer.init();

        mHeartbeatDetectionTimer = new JSimpleTimer(HEARTBEAT_DETECTION_INTERVAL) {
            @Override
            protected void doAction() {
                doHeartbeatDetection();
            }
        };
        mHeartbeatDetectionTimer.init();

        mIsInit = true;
    }

    private void doHeartbeat() {
        if (mJWebsocket != null) {
            mJWebsocket.ping();
        }
    }

    private void doHeartbeatDetection() {
        if (mLastMessageReceivedTime.longValue() != 0) {
            //获取当前系统时间
            long now = System.currentTimeMillis();
            //如果当前时系统时间和lastMessageReceivedTime的差值大于HEARTBEAT_DETECTION_TIME_OUT，认为心跳超时，执行timeout回调
            if (now - mLastMessageReceivedTime.longValue() >= HEARTBEAT_DETECTION_TIME_OUT) {
                JLogger.e("HB-TimeOut", "timeout");
                notifyHeartbeatTimeout();
            }
        }
    }

    private void notifyHeartbeatTimeout() {
        stop();
        if (mJWebsocket != null) {
            mJWebsocket.handleHeartbeatTimeout();
        }
    }
}