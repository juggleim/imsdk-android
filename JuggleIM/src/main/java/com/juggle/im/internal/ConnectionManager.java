package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.WebSocketSimpleCallback;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.push.PushChannel;
import com.juggle.im.push.PushManager;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager implements IConnectionManager, JWebSocket.IWebSocketConnectListener {
    @Override
    public void connect(String token) {
        JLogger.i("CON-Connect", "token is " + token);
        //todo 校验，是否有连接

        if (mCore.getToken() != null && !mCore.getToken().equals(token)) {
            mCore.setToken(token);
            mCore.setUserId("");
        }
        openDB();
        changeStatus(JIMCore.ConnectionStatusInternal.CONNECTING, ConstInternal.ErrorCode.NONE, "");

        NaviTask task = new NaviTask(mCore.getNaviUrls(), mCore.getAppKey(), mCore.getToken(), new NaviTask.IRequestCallback() {
            @Override
            public void onSuccess(String userId, List<String> servers) {
                mCore.getSendHandler().post(() -> {
                    JLogger.i("CON-Navi", "success, servers is " + servers);
                    mCore.setServers(servers);
                    connectWebSocket(token);
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.i("CON-Navi", "fail, errorCode is " + errorCode);
                if (checkConnectionFailure(errorCode)) {
                    changeStatus(JIMCore.ConnectionStatusInternal.FAILURE, errorCode, "");
                } else {
                    changeStatus(JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, errorCode, "");
                }
            }
        });

        task.start();
    }

    @Override
    public void disconnect(boolean receivePush) {
        JLogger.i("CON-Disconnect", "user disconnect receivePush is " + receivePush);
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().disconnect(receivePush);
        }
        changeStatus(JIMCore.ConnectionStatusInternal.DISCONNECTED, ConstInternal.ErrorCode.NONE, "");
    }

    @Override
    public void registerPushToken(PushChannel channel, String token) {
        JLogger.i("CON-Push", "registerPushToken, channel is " + channel.getName() + ", token is " + token);
        mPushChannel = channel;
        mPushToken = token;
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.w("CON-Push", "registerPushToken error, errorCode is " + errorCode);
            return;
        }
        mCore.getWebSocket().registerPushToken(channel, token, mCore.getDeviceId(), mCore.getPackageName(), mCore.getUserId(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CON-Push", "registerPushToken success");
            }

            @Override
            public void onError(int errorCode) {
                JLogger.w("CON-Push", "registerPushToken error, errorCode is " + errorCode);
            }
        });
    }

    @Override
    public JIMConst.ConnectionStatus getConnectionStatus() {
        return JIMConst.ConnectionStatus.setStatus(mCore.getConnectionStatus());
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

    public ConnectionManager(JIMCore core, ConversationManager conversationManager, MessageManager messageManager, UserInfoManager userInfoManager) {
        this.mCore = core;
        this.mCore.setConnectionStatus(JIMCore.ConnectionStatusInternal.IDLE);
        this.mCore.getWebSocket().setConnectionListener(this);
        this.mConversationManager = conversationManager;
        this.mMessageManager = messageManager;
        this.mUserInfoManager = userInfoManager;
    }

    @Override
    public void onConnectComplete(int errorCode, String userId, String session, String extra) {
        if (errorCode == ConstInternal.ErrorCode.NONE) {
            mCore.setUserId(userId);
            openDB();
            mMessageManager.connectSuccess();
            mConversationManager.connectSuccess();
            changeStatus(JIMCore.ConnectionStatusInternal.CONNECTED, ConstInternal.ErrorCode.NONE, extra);
            mConversationManager.syncConversations(mMessageManager::syncMessage);
            PushManager.getInstance().getToken(mCore.getContext());
        } else {
            if (checkConnectionFailure(errorCode)) {
                changeStatus(JIMCore.ConnectionStatusInternal.FAILURE, errorCode, extra);
            } else {
                changeStatus(JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE, extra);
            }
        }
    }

    @Override
    public void onDisconnect(int errorCode, String extra) {
        changeStatus(JIMCore.ConnectionStatusInternal.DISCONNECTED, errorCode, extra);
    }

    @Override
    public void onWebSocketFail() {
        handleWebsocketFail();
    }

    @Override
    public void onWebSocketClose() {
        handleWebsocketFail();
    }

    @Override
    public void onTimeOut() {
        handleWebsocketFail();
    }

    private void connectWebSocket(String token) {
        mCore.getWebSocket().connect(mCore.getAppKey(), token, mCore.getDeviceId(), mCore.getPackageName(), mCore.getNetworkType(), mCore.getCarrier(), mPushChannel, mPushToken, mCore.getServers());
    }

    private void handleWebsocketFail() {
        if (mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.DISCONNECTED
                || mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.FAILURE) {
            return;
        }
        changeStatus(JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE, "");
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

    private void changeStatus(int status, int errorCode, String extra) {
        mCore.getSendHandler().post(() -> {
            JLogger.i("CON-Status", "status is " + status + ", code is " + errorCode + ", extra is " + extra);
            if (status == mCore.getConnectionStatus()) {
                return;
            }
            if (status == JIMCore.ConnectionStatusInternal.IDLE) {
                mCore.setConnectionStatus(status);
                return;
            }
            if (status == JIMCore.ConnectionStatusInternal.CONNECTED
                    && mCore.getConnectionStatus() != JIMCore.ConnectionStatusInternal.CONNECTED) {
                mCore.getWebSocket().startHeartbeat();
            }
            if (status != JIMCore.ConnectionStatusInternal.CONNECTED
                    && mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.CONNECTED) {
                mCore.getWebSocket().stopHeartbeat();
                mCore.getWebSocket().pushRemainCmdAndCallbackError();
            }
            JIMConst.ConnectionStatus outStatus = JIMConst.ConnectionStatus.IDLE;
            switch (status) {
                case JIMCore.ConnectionStatusInternal.CONNECTED:
                    outStatus = JIMConst.ConnectionStatus.CONNECTED;
                    break;
                case JIMCore.ConnectionStatusInternal.DISCONNECTED:
                    closeDB();
                    stopReconnectTimer();
                    outStatus = JIMConst.ConnectionStatus.DISCONNECTED;
                    break;

                case JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING:
                    reconnect();
                    //无需 break，跟 CONNECTING 一起处理
                case JIMCore.ConnectionStatusInternal.CONNECTING:
                    //已经在连接中，不需要再对外抛回调
                    if (mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.CONNECTING
                            || mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING) {
                        mCore.setConnectionStatus(status);
                        return;
                    }
                    outStatus = JIMConst.ConnectionStatus.CONNECTING;
                    break;
                case JIMCore.ConnectionStatusInternal.FAILURE:
                    outStatus = JIMConst.ConnectionStatus.FAILURE;
                default:
                    break;
            }
            mCore.setConnectionStatus(status);

            if (mConnectionStatusListenerMap != null) {
                JIMConst.ConnectionStatus finalOutStatus = outStatus;
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onStatusChange(finalOutStatus, errorCode, extra);
                    });
                }
            }
        });
    }

    private void stopReconnectTimer() {
        if (mReconnectTimer != null) {
            mReconnectTimer.cancel();
            mReconnectTimer = null;
        }
    }

    private void reconnect() {
        JLogger.i("CON-Reconnect", "reconnect");
        //todo 线程控制，间隔控制
        if (mReconnectTimer != null) {
            return;
        }
        mReconnectTimer = new Timer();
        mReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopReconnectTimer();
                //todo 重连整理
                if (mCore.getConnectionStatus() == JIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING) {
                    connect(mCore.getToken());
                }
            }
        }, RECONNECT_INTERVAL);
    }

    private void dbStatusNotice(boolean isOpen) {
        JLogger.i("CON-Db", "db notice, isOpen is " + isOpen);
        if (isOpen) {
            mCore.getSyncTimeFromDB();
            if (mConnectionStatusListenerMap != null) {
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onDbOpen();
                    });
                }
            }
        } else {
            if (mConnectionStatusListenerMap != null) {
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onDbClose();
                    });
                }
            }
        }
    }

    private void openDB() {
        if (!mCore.getDbManager().isOpen()) {
            mUserInfoManager.clearCache();
            if (!TextUtils.isEmpty(mCore.getUserId())) {
                if (mCore.getDbManager().openIMDB(mCore.getContext(), mCore.getAppKey(), mCore.getUserId())) {
                    dbStatusNotice(true);
                } else {
                    JLogger.e("CON-Db", "open db fail");
                }
            }
        }
    }

    private void closeDB() {
        mCore.getDbManager().closeDB();
        mUserInfoManager.clearCache();
        dbStatusNotice(false);
    }

    private final JIMCore mCore;
    private final ConversationManager mConversationManager;
    private final MessageManager mMessageManager;
    private final UserInfoManager mUserInfoManager;
    private ConcurrentHashMap<String, IConnectionStatusListener> mConnectionStatusListenerMap;
    private Timer mReconnectTimer;
    private PushChannel mPushChannel;
    private String mPushToken;
    private static final int RECONNECT_INTERVAL = 5 * 1000;
}
