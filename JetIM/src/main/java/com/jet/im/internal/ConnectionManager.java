package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.utils.LoggerUtils;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager implements IConnectionManager {
    @Override
    public void connect(String token) {
        LoggerUtils.i("connect, token is " + token);
        //todo 校验，是否有连接，mCore.getWebSocket 是不是为空

        if (!mCore.getToken().equals(token)) {
            mCore.setToken(token);
            mCore.setUserId("");
        }
        if (!TextUtils.isEmpty(mCore.getUserId())) {
            if (mCore.getDbManager().openIMDB(mCore.getContext(), mCore.getAppKey(), mCore.getUserId())) {
                dbOpenNotice(JetIMCore.DbStatus.OPEN);
            }
        }
        changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTING, ConstInternal.ErrorCode.NONE);
        if (mCore.getWebSocket() == null) {
            URI uri = JWebSocket.createWebSocketUri(mCore.getServers()[0]);
            mCore.setWebSocket(new JWebSocket(mCore.getAppKey(), token, uri, mCore.getContext()));
        } else {
            mCore.getWebSocket().setAppKey(mCore.getAppKey());
            mCore.getWebSocket().setToken(token);
        }
        mCore.getWebSocket().setConnectionListener(new JWebSocket.IWebSocketConnectListener() {
            @Override
            public void onConnectComplete(int errorCode, String userId) {
                if (errorCode == ConstInternal.ErrorCode.NONE) {
                    mCore.setUserId(userId);
                    if (mCore.getDbStatus() != JetIMCore.DbStatus.OPEN) {
                        if (mCore.getDbManager().openIMDB(mCore.getContext(), mCore.getAppKey(), userId)) {
                            dbOpenNotice(JetIMCore.DbStatus.OPEN);
                        } else {
                            LoggerUtils.e("open db fail");
                        }
                    }
                    changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTED, ConstInternal.ErrorCode.NONE);
                    mConversationManager.syncConversations(new ConversationManager.ICompleteCallback() {
                        @Override
                        public void onComplete() {

                        }
                    });
                    //todo sync conversation
                } else {
                    if (checkConnectionFailure(errorCode)) {
                        changeStatus(JetIMCore.ConnectionStatusInternal.FAILURE, errorCode);
                    } else {
                        changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE);
                    }

                }
            }

            @Override
            public void onWebSocketFail() {
                changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE);
            }

            @Override
            public void onWebSocketClose() {
                if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.DISCONNECTED
                || mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.FAILURE) {
                    return;
                }
                changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE);
            }
        });
        mCore.getWebSocket().connect();
    }

    @Override
    public void disconnect(boolean receivePush) {
        changeStatus(JetIMCore.ConnectionStatusInternal.DISCONNECTED, ConstInternal.ErrorCode.NONE);
        mCore.getWebSocket().disconnect(receivePush);
    }

    @Override
    public void addConnectionStatusListener(String key, IConnectionStatusListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mConnectionStatusListenerMap == null) {
            mConnectionStatusListenerMap = new ConcurrentHashMap<>();
        }
        mConnectionStatusListenerMap.put(key, listener);
    }

    @Override
    public void removeConnectionStatusListener(String key) {
        if (!TextUtils.isEmpty(key) && mConnectionStatusListenerMap != null) {
            mConnectionStatusListenerMap.remove(key);
        }
    }

    public ConnectionManager(JetIMCore core, ConversationManager conversationManager, MessageManager messageManager) {
        this.mCore = core;
        this.mCore.setConnectionStatus(JetIMCore.ConnectionStatusInternal.IDLE);
        this.mConversationManager = conversationManager;
        this.mMessageManager = messageManager;
    }

    private boolean checkConnectionFailure(int errorCode) {
        return errorCode == ConstInternal.ErrorCode.APP_KEY_EMPTY
                || errorCode == ConstInternal.ErrorCode.TOKEN_EMPTY
                || errorCode == ConstInternal.ErrorCode.APP_KEY_INVALID
                || errorCode == ConstInternal.ErrorCode.TOKEN_ILLEGAL
                || errorCode == ConstInternal.ErrorCode.TOKEN_UNAUTHORIZED
                || errorCode == ConstInternal.ErrorCode.TOKEN_EXPIRED
                || errorCode == ConstInternal.ErrorCode.APP_PROHIBITED
                || errorCode == ConstInternal.ErrorCode.USER_PROHIBITED
                || errorCode == ConstInternal.ErrorCode.USER_KICKED_BY_OTHER_CLIENT
                || errorCode == ConstInternal.ErrorCode.USER_LOG_OUT;
    }

    private void changeStatus(int status, int errorCode) {
        //todo thread
        if (status == JetIMCore.ConnectionStatusInternal.IDLE) {
            mCore.setConnectionStatus(status);
            return;
        }
        if (status == JetIMCore.ConnectionStatusInternal.CONNECTED
                && mCore.getConnectionStatus() != JetIMCore.ConnectionStatusInternal.CONNECTED) {
            //todo
            //mHeartBeatManager.start();
        }
        if (status != JetIMCore.ConnectionStatusInternal.CONNECTED
                && mCore.getConnectionStatus() != JetIMCore.ConnectionStatusInternal.CONNECTED) {
            //todo
            //mHeartBeatManager.stop();
        }
        JetIMConst.ConnectionStatus outStatus = JetIMConst.ConnectionStatus.IDLE;
        switch (status) {
            case JetIMCore.ConnectionStatusInternal.CONNECTED:
                outStatus = JetIMConst.ConnectionStatus.CONNECTED;
                break;
            case JetIMCore.ConnectionStatusInternal.DISCONNECTED:
                outStatus = JetIMConst.ConnectionStatus.DISCONNECTED;
                break;
            case JetIMCore.ConnectionStatusInternal.CONNECTING:
                outStatus = JetIMConst.ConnectionStatus.CONNECTING;
                break;
            case JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING:
                reconnect();
                //已经在连接中，不需要再对外抛回调
                if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.CONNECTING
                        || mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING) {
                    mCore.setConnectionStatus(JetIMCore.ConnectionStatusInternal.CONNECTING);
                    return;
                }
                outStatus = JetIMConst.ConnectionStatus.CONNECTING;
                break;
            case JetIMCore.ConnectionStatusInternal.FAILURE:
                outStatus = JetIMConst.ConnectionStatus.FAILURE;
            default:
                break;
        }
        mCore.setConnectionStatus(status);
        if (mConnectionStatusListenerMap != null) {
            for (Map.Entry<String, IConnectionStatusListener> entry :
                 mConnectionStatusListenerMap.entrySet()) {
                entry.getValue().onStatusChange(outStatus, errorCode);
            }
        }
    }

    private void reconnect() {
        LoggerUtils.i("reconnect");
        //todo
    }

    private void dbOpenNotice(int status) {
        mCore.setDbStatus(status);
        if (status == JetIMCore.DbStatus.OPEN) {
            mCore.getSyncTimeFromDB();
            for (Map.Entry<String, IConnectionStatusListener> entry :
                    mConnectionStatusListenerMap.entrySet()) {
                entry.getValue().onDbOpen();
            }
        }
    }


    private JetIMCore mCore;
    private ConversationManager mConversationManager;
    private MessageManager mMessageManager;
    private ConcurrentHashMap<String, IConnectionStatusListener> mConnectionStatusListenerMap;
}
