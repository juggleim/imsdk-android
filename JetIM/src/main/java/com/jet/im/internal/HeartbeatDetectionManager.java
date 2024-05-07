package com.jet.im.internal;

import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.util.JSimpleTimer;
import com.jet.im.utils.LoggerUtils;

import java.util.concurrent.atomic.AtomicLong;

public class HeartbeatDetectionManager {
    private final static String TAG = HeartbeatDetectionManager.class.getSimpleName();

    private static volatile HeartbeatDetectionManager instance = null;

    public static int HEARTBEAT_DETECTION_INTERVAL = 10 * 1000;
    public static int HEARTBEAT_DETECTION_TIME_OUT = HeartBeatManager.PING_INTERVAL * 3;

    private JSimpleTimer heartbeatDetectionTimeoutTimer = null;
    private JWebSocket.IWebSocketConnectListener connectListener;
    private final AtomicLong lastMessageReceivedTime = new AtomicLong(0);

    private boolean init = false;

    public static HeartbeatDetectionManager getInstance() {
        if (instance == null) {
            synchronized (HeartbeatDetectionManager.class) {
                if (instance == null) {
                    instance = new HeartbeatDetectionManager();
                }
            }
        }
        return instance;
    }

    private HeartbeatDetectionManager() {
        init();
    }

    private void init() {
        if (init) return;

        heartbeatDetectionTimeoutTimer = new JSimpleTimer(HEARTBEAT_DETECTION_INTERVAL) {
            @Override
            protected void doAction() {
                doTimeoutCheck();
            }
        };
        heartbeatDetectionTimeoutTimer.init();

        init = true;
    }

    private void doTimeoutCheck() {
        LoggerUtils.d(TAG + ", heartbeat detecting...");
        boolean isInitialedForKeepAlive = isInitialedForKeepAlive();
        if (!isInitialedForKeepAlive) {
            //获取当前系统时间
            long now = System.currentTimeMillis();
            //如果当前时系统时间和lastMessageReceivedTime的差值大于HEARTBEAT_DETECTION_TIME_OUT，认为心跳超时，执行timeout回调
            if (now - lastMessageReceivedTime.longValue() >= HEARTBEAT_DETECTION_TIME_OUT) {
                LoggerUtils.e("heartbeat has timeout, perform reconnection...");
                notifyConnectionLost();
            }
        }
    }

    private boolean isInitialedForKeepAlive() {
        return (lastMessageReceivedTime.longValue() == 0);
    }

    private void notifyConnectionLost() {
        stop();
        if (connectListener != null) {
            connectListener.onTimeOut();
        }
    }

    public void start(boolean immediately) {
        stop();
        heartbeatDetectionTimeoutTimer.start(immediately);
    }

    public void stop() {
        heartbeatDetectionTimeoutTimer.stop();
        lastMessageReceivedTime.set(0);
    }

    public boolean isInit() {
        return init;
    }

    public void updateLastMessageReceivedTime() {
        lastMessageReceivedTime.set(System.currentTimeMillis());
    }

    public void setConnectionListener(JWebSocket.IWebSocketConnectListener connectListener) {
        this.connectListener = connectListener;
    }
}