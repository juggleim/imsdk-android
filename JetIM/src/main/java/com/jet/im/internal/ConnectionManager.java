package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.internal.util.JLogger;
import com.jet.im.push.PushChannel;
import com.jet.im.push.PushManager;

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

        if (!mCore.getToken().equals(token)) {
            mCore.setToken(token);
            mCore.setUserId("");
        }
        openDB();
        changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTING, ConstInternal.ErrorCode.NONE, "");

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
                if (errorCode == ConstInternal.ErrorCode.TOKEN_ILLEGAL) {
                    changeStatus(JetIMCore.ConnectionStatusInternal.FAILURE, errorCode, "");
                } else {
                    changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, errorCode, "");
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
        changeStatus(JetIMCore.ConnectionStatusInternal.DISCONNECTED, ConstInternal.ErrorCode.NONE, "");
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

    public ConnectionManager(JetIMCore core, ConversationManager conversationManager, MessageManager messageManager, UserInfoManager userInfoManager) {
        this.mCore = core;
        this.mCore.setConnectionStatus(JetIMCore.ConnectionStatusInternal.IDLE);
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
            changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTED, ConstInternal.ErrorCode.NONE, extra);
            mConversationManager.syncConversations(mMessageManager::syncMessage);
            PushManager.getInstance().getToken(mCore.getContext());
        } else {
            if (checkConnectionFailure(errorCode)) {
                changeStatus(JetIMCore.ConnectionStatusInternal.FAILURE, errorCode, extra);
            } else {
                changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE, extra);
            }
        }
    }

    @Override
    public void onDisconnect(int errorCode, String extra) {
        changeStatus(JetIMCore.ConnectionStatusInternal.DISCONNECTED, errorCode, extra);
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
        if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.DISCONNECTED
                || mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.FAILURE) {
            return;
        }
        changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE, "");
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
            if (status == JetIMCore.ConnectionStatusInternal.IDLE) {
                mCore.setConnectionStatus(status);
                return;
            }
            if (status == JetIMCore.ConnectionStatusInternal.CONNECTED
                    && mCore.getConnectionStatus() != JetIMCore.ConnectionStatusInternal.CONNECTED) {
                mCore.getWebSocket().startHeartbeat();
            }
            if (status != JetIMCore.ConnectionStatusInternal.CONNECTED
                    && mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.CONNECTED) {
                mCore.getWebSocket().stopHeartbeat();
                mCore.getWebSocket().pushRemainCmdAndCallbackError();
            }
            JetIMConst.ConnectionStatus outStatus = JetIMConst.ConnectionStatus.IDLE;
            switch (status) {
                case JetIMCore.ConnectionStatusInternal.CONNECTED:
                    outStatus = JetIMConst.ConnectionStatus.CONNECTED;
                    break;
                case JetIMCore.ConnectionStatusInternal.DISCONNECTED:
                    closeDB();
                    stopReconnectTimer();
                    outStatus = JetIMConst.ConnectionStatus.DISCONNECTED;
                    break;

                case JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING:
                    reconnect();
                    //无需 break，跟 CONNECTING 一起处理
                case JetIMCore.ConnectionStatusInternal.CONNECTING:
                    //已经在连接中，不需要再对外抛回调
                    if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.CONNECTING
                            || mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING) {
                        mCore.setConnectionStatus(status);
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
                JetIMConst.ConnectionStatus finalOutStatus = outStatus;
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
                if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING) {
                    connect(mCore.getToken());
                }
            }
        }, RECONNECT_INTERVAL);
    }

    private void dbStatusNotice(boolean isOpen) {
        JLogger.i("CON-Db", "db notice, isOpen is " + isOpen);
        if (isOpen) {
            mCore.getSyncTimeFromDB();
            for (Map.Entry<String, IConnectionStatusListener> entry :
                    mConnectionStatusListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onDbOpen();
                });
            }
        } else {
            for (Map.Entry<String, IConnectionStatusListener> entry :
                    mConnectionStatusListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onDbClose();
                });
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

    private final JetIMCore mCore;
    private final ConversationManager mConversationManager;
    private final MessageManager mMessageManager;
    private final UserInfoManager mUserInfoManager;
    private ConcurrentHashMap<String, IConnectionStatusListener> mConnectionStatusListenerMap;
    private Timer mReconnectTimer;
    private PushChannel mPushChannel;
    private String mPushToken;
    private static final int RECONNECT_INTERVAL = 5 * 1000;
}
