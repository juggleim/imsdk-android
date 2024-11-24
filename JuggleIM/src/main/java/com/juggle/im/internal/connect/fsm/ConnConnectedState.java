package com.juggle.im.internal.connect.fsm;

import android.os.Message;

import com.juggle.im.JErrorCode;
import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.util.JLogger;

public class ConnConnectedState extends ConnBaseState {
    @Override
    public void enter() {
        super.enter();
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.setConnectionStatus(JIMCore.ConnectionStatusInternal.CONNECTED);
            manager.enterConnected();
        }
    }

    @Override
    public void exit() {
        super.exit();
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.leaveConnected();
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
                if (manager.isSameToken(token)) {
                    JLogger.w("CON-Connect", "connection already exist");
                    //do nothing
                } else {
                    manager.disconnectExist(false);
                    manager.notifyDisconnected(JErrorCode.NONE, "");
                    manager.transitionToIdleState();
                    manager.sendMessage(msg.what, msg.obj);
                }
                break;

            case ConnEvent.USER_DISCONNECT:
                boolean receivePush = msg.arg1 != 0;
                manager.disconnectExist(receivePush);
                manager.notifyDisconnected(JErrorCode.NONE, "");
                manager.transitionToIdleState();
                break;

            case ConnEvent.WEBSOCKET_FAIL:
                manager.notifyConnecting();
                manager.transitionToWaitingForConnectState();
                break;

            case ConnEvent.REMOTE_DISCONNECT:
                int code = msg.arg1;
                String extra = (String) msg.obj;
                manager.handleRemoteDisconnect();
                manager.notifyDisconnected(code, extra);
                manager.transitionToIdleState();
                break;

            default:
                return false;
        }
        return true;
    }
}
