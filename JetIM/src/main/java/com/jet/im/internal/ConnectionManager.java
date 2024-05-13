package com.jet.im.internal;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.push.PushChannel;
import com.jet.im.utils.LoggerUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager implements IConnectionManager {
    @Override
    public void connect(String token) {
        LoggerUtils.i("connect, token is " + token);
        //todo 校验，是否有连接

        if (!mCore.getToken().equals(token)) {
            mCore.setToken(token);
            mCore.setUserId("");
        }
        if (!mCore.getDbManager().isOpen()) {
            if (!TextUtils.isEmpty(mCore.getUserId())) {
                if (mCore.getDbManager().openIMDB(mCore.getContext(), mCore.getAppKey(), mCore.getUserId())) {
                    dbStatusNotice(true);
                }
            }
        }
        changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTING, ConstInternal.ErrorCode.NONE);

        mNaviHandler.post(() -> NaviManager.request(mCore.getNaviUrl(), mCore.getAppKey(), mCore.getToken(), new NaviManager.IRequestCallback() {
            @Override
            public void onSuccess(String userId, List<String> servers) {
                mCore.setServers(servers);
                connectWebSocket(token);
            }

            @Override
            public void onError(int errorCode) {
                if (errorCode == ConstInternal.ErrorCode.TOKEN_ILLEGAL) {
                    changeStatus(JetIMCore.ConnectionStatusInternal.FAILURE, errorCode);
                } else {
                    changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, errorCode);
                }
            }
        }));
    }

    @Override
    public void disconnect(boolean receivePush) {
        LoggerUtils.i("user disconnect receivePush is " + receivePush);
        changeStatus(JetIMCore.ConnectionStatusInternal.DISCONNECTED, ConstInternal.ErrorCode.NONE);
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().disconnect(receivePush);
        }
    }

    @Override
    public void registerPushToken(PushChannel channel, String token) {
        mPushChannel = channel;
        mPushToken = token;
        if (mCore.getWebSocket() == null) {
            return;
        }
        mCore.getWebSocket().registerPushToken(channel, token, mCore.getUserId(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("register push token success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.e("register push token error, code is " + errorCode);
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

    public ConnectionManager(JetIMCore core, ConversationManager conversationManager, MessageManager messageManager) {
        this.mCore = core;
        this.mCore.setConnectionStatus(JetIMCore.ConnectionStatusInternal.IDLE);
        this.mConversationManager = conversationManager;
        this.mMessageManager = messageManager;

        HandlerThread thread = new HandlerThread("JET_NAVI");
        thread.start();
        this.mNaviHandler = new Handler(thread.getLooper());
    }

    private void connectWebSocket(String token) {
        if (mCore.getWebSocket() == null) {
            URI uri = JWebSocket.createWebSocketUri(mCore.getServers().get(0));
            mCore.setWebSocket(new JWebSocket(mCore.getAppKey(), token, uri, mCore.getContext()));
            mCore.getWebSocket().setConnectionListener(new JWebSocket.IWebSocketConnectListener() {
                @Override
                public void onConnectComplete(int errorCode, String userId) {
                    if (errorCode == ConstInternal.ErrorCode.NONE) {
                        mCore.setUserId(userId);
                        if (!mCore.getDbManager().isOpen()) {
                            if (mCore.getDbManager().openIMDB(mCore.getContext(), mCore.getAppKey(), userId)) {
                                dbStatusNotice(true);
                            } else {
                                LoggerUtils.e("open db fail");
                            }
                        }
                        changeStatus(JetIMCore.ConnectionStatusInternal.CONNECTED, ConstInternal.ErrorCode.NONE);
                        mConversationManager.syncConversations(mMessageManager::syncMessage);
                    } else {
                        if (checkConnectionFailure(errorCode)) {
                            changeStatus(JetIMCore.ConnectionStatusInternal.FAILURE, errorCode);
                        } else {
                            changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE);
                        }
                    }
                }

                @Override
                public void onDisconnect(int errorCode) {
                    changeStatus(JetIMCore.ConnectionStatusInternal.DISCONNECTED, errorCode);
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
            });
            mCore.getWebSocket().setPushChannel(mPushChannel);
            if (!TextUtils.isEmpty(mPushToken)) {
                mCore.getWebSocket().setPushToken(mPushToken);
            }
            mCore.getWebSocket().connect();
        } else {
            mCore.getWebSocket().setAppKey(mCore.getAppKey());
            mCore.getWebSocket().setToken(token);
            mCore.getWebSocket().setPushChannel(mPushChannel);
            if (!TextUtils.isEmpty(mPushToken)) {
                mCore.getWebSocket().setPushToken(mPushToken);
            }
            mCore.getWebSocket().reconnect();
        }
    }

    private void handleWebsocketFail() {
        if (mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.DISCONNECTED
                || mCore.getConnectionStatus() == JetIMCore.ConnectionStatusInternal.FAILURE) {
            return;
        }
        changeStatus(JetIMCore.ConnectionStatusInternal.WAITING_FOR_CONNECTING, ConstInternal.ErrorCode.NONE);
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
        mCore.getSendHandler().post(() -> {
            LoggerUtils.i("connection status " + status);
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
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    entry.getValue().onStatusChange(outStatus, errorCode);
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
        LoggerUtils.i("reconnect");
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
        if (isOpen) {
            mCore.getSyncTimeFromDB();
            for (Map.Entry<String, IConnectionStatusListener> entry :
                    mConnectionStatusListenerMap.entrySet()) {
                entry.getValue().onDbOpen();
            }
        } else {
            for (Map.Entry<String, IConnectionStatusListener> entry :
                    mConnectionStatusListenerMap.entrySet()) {
                entry.getValue().onDbClose();
            }
        }
    }

    private void closeDB() {
        mCore.getDbManager().closeDB();
        dbStatusNotice(false);
    }

    private final JetIMCore mCore;
    private final ConversationManager mConversationManager;
    private final MessageManager mMessageManager;
    private ConcurrentHashMap<String, IConnectionStatusListener> mConnectionStatusListenerMap;
    private Timer mReconnectTimer;
    private PushChannel mPushChannel;
    private String mPushToken;
    private final Handler mNaviHandler;
    private static final int RECONNECT_INTERVAL = 5 * 1000;
}
