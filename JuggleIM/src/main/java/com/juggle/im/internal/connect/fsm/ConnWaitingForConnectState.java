package com.juggle.im.internal.connect.fsm;

import android.os.Message;

import com.juggle.im.JErrorCode;
import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.util.JLogger;

import java.util.Timer;
import java.util.TimerTask;

public class ConnWaitingForConnectState extends ConnBaseState {
    private Timer mReconnectTimer;

    @Override
    public void enter() {
        super.enter();
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.setConnectionStatus(JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING);
            startTimer();
        }
    }

    @Override
    public void exit() {
        super.exit();
        stopTimer();
    }

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        ConnectionManager manager = getConnectionManager();
        if (manager == null) {
            JLogger.e("FSM-Sm", "connectionManager is null");
            return true;
        }

        switch (msg.what) {
            case ConnEvent.USER_CONNECT:
                String token = (String) msg.obj;
                boolean isSame = manager.isSameToken(token);
                if (isSame) {
                    // same token, do nothing
                } else {
                    manager.notifyDisconnected(JErrorCode.NONE, "");
                    manager.transitionToIdleState();
                    manager.sendMessage(msg);
                }
                break;

            case ConnEvent.USER_DISCONNECT:
                manager.disconnectWithoutWS();
                manager.transitionToIdleState();
                break;

            case ConnEvent.RECONNECT_TIMER_FIRE:
                manager.transitionToConnectingState();
                break;

            case ConnEvent.ENTER_FOREGROUND:
                manager.transitionToConnectingState();
                break;

            case ConnEvent.NETWORK_AVAILABLE:
                manager.transitionToConnectingState();
                break;

            default:
                return false;
        }
        return true;
    }

    private void startTimer() {
        ConnectionManager manager = getConnectionManager();
        if (manager == null) {
            JLogger.e("FSM-Sm", "connectionManager is null");
            return;
        }
        mReconnectTimer = new Timer();
        mReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                manager.sendMessage(ConnEvent.RECONNECT_TIMER_FIRE);
            }
        }, manager.getReconnectInterval());
    }

    private void stopTimer() {
        if (mReconnectTimer != null) {
            mReconnectTimer.cancel();
            mReconnectTimer = null;
        }
    }
}
