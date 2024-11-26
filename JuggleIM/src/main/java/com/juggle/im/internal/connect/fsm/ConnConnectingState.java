package com.juggle.im.internal.connect.fsm;

import android.os.Message;

import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.util.JLogger;

public class ConnConnectingState extends ConnBaseState {
    private enum ConnectingStoreStatus {
        NONE, CONNECT, DISCONNECT
    }
    private String mUserToken;
    //连接过程中保存的状态
    private ConnectingStoreStatus mStoreStatus;
    private boolean mReceivePush;

    @Override
    public void enter() {
        super.enter();
        mUserToken = null;
        mStoreStatus = ConnectingStoreStatus.NONE;
        mReceivePush = false;
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.setConnectionStatus(JIMCore.ConnectionStatusInternal.CONNECTING);
            manager.connect();
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

        String extra;
        switch (msg.what) {
            case ConnEvent.USER_CONNECT:
                String token = (String) msg.obj;
                boolean isSame = manager.isSameToken(token);
                if (isSame) {
                    // same token, do nothing
                    JLogger.w("CON-Connect", "same token is connecting");
                    mUserToken = null;
                    mStoreStatus = ConnectingStoreStatus.NONE;
                } else {
                    mUserToken = token;
                    mStoreStatus = ConnectingStoreStatus.CONNECT;
                }
                break;

            case ConnEvent.CONNECT_DONE:
                extra = (String) msg.obj;
                manager.transitionToConnectedState();
                manager.notifyConnected(extra);
                if (mStoreStatus == ConnectingStoreStatus.CONNECT) {
                    if (mUserToken != null && !mUserToken.isEmpty()) {
                        manager.sendMessage(ConnEvent.USER_CONNECT, mUserToken);
                    }
                } else if (mStoreStatus == ConnectingStoreStatus.DISCONNECT) {
                    manager.sendMessage(ConnEvent.USER_DISCONNECT, mReceivePush);
                }
                break;

            case ConnEvent.CONNECT_FAILURE:
                int errorCode = msg.arg1;
                extra = (String) msg.obj;
                manager.transitionToIdleState();
                manager.notifyFailure(errorCode, extra);
                if (mStoreStatus == ConnectingStoreStatus.CONNECT) {
                    if (mUserToken != null && !mUserToken.isEmpty()) {
                        manager.sendMessage(ConnEvent.USER_CONNECT, mUserToken);
                    }
                } else if (mStoreStatus == ConnectingStoreStatus.DISCONNECT) {
                    manager.sendMessage(ConnEvent.USER_DISCONNECT, mReceivePush);
                }
                break;

            case ConnEvent.WEBSOCKET_FAIL:
                manager.transitionToWaitingForConnectState();
                if (mStoreStatus == ConnectingStoreStatus.CONNECT) {
                    if (mUserToken != null && !mUserToken.isEmpty()) {
                        manager.sendMessage(ConnEvent.USER_CONNECT, mUserToken);
                    }
                } else if (mStoreStatus == ConnectingStoreStatus.DISCONNECT) {
                    manager.sendMessage(ConnEvent.USER_DISCONNECT, mReceivePush);
                }
                break;

            case ConnEvent.USER_DISCONNECT:
                mUserToken = null;
                mStoreStatus = ConnectingStoreStatus.DISCONNECT;
                mReceivePush = (boolean) msg.obj;
                break;

            default:
                return false;
        }
        return true;
    }
}
