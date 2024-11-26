package com.juggle.im.internal.connect.fsm;

import android.os.Message;

import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.util.JLogger;

public class ConnIdleState extends ConnBaseState {
    @Override
    public void enter() {
        super.enter();
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.setConnectionStatus(JIMCore.ConnectionStatusInternal.IDLE);
        }
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
                manager.updateToken(token);
                manager.notifyConnecting();
                manager.transitionToConnectingState();
                break;

            case ConnEvent.USER_DISCONNECT:
                manager.disconnectWithoutWS();
                break;

            default:
                return false;
        }
        return true;
    }
}
