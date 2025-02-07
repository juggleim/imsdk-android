package com.juggle.im.internal.connect;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.call.internal.CallManager;
import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.internal.ChatroomManager;
import com.juggle.im.internal.ConstInternal;
import com.juggle.im.internal.ConversationManager;
import com.juggle.im.internal.MessageManager;
import com.juggle.im.internal.UserInfoManager;
import com.juggle.im.internal.connect.fsm.ConnConnectedState;
import com.juggle.im.internal.connect.fsm.ConnConnectingState;
import com.juggle.im.internal.connect.fsm.ConnEvent;
import com.juggle.im.internal.connect.fsm.ConnIdleState;
import com.juggle.im.internal.connect.fsm.ConnSuperState;
import com.juggle.im.internal.connect.fsm.ConnWaitingForConnectState;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.WebSocketSimpleCallback;
import com.juggle.im.internal.util.IntervalGenerator;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.NetworkChangeReceiver;
import com.juggle.im.internal.util.statemachine.StateMachine;
import com.juggle.im.push.PushChannel;
import com.juggle.im.push.PushManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager extends StateMachine implements IConnectionManager, JWebSocket.IWebSocketConnectListener, Application.ActivityLifecycleCallbacks, NetworkChangeReceiver.INetworkChangeReceiverListener {
    @Override
    public void connect(String token) {
        JLogger.i("CON-Connect", "token is " + token);
        sendMessage(ConnEvent.USER_CONNECT, token);
    }

    @Override
    public void disconnect(boolean receivePush) {
        JLogger.i("CON-Disconnect", "user disconnect receivePush is " + receivePush);
        sendMessage(ConnEvent.USER_DISCONNECT, receivePush);
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
    public void setLanguage(String language, ISimpleCallback callback) {
        JLogger.i("CON-Lang", "language is " + language);
        if (language == null || language.isEmpty()) {
            JLogger.w("CON-Lang", "language is empty");
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.INVALID_PARAM);
                }
            });
            return;
        }
        mCore.getWebSocket().setLanguage(language, mCore.getUserId(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CON-Lang", "success");
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CON-Lang", "error code is " + errorCode);
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
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

    public ConnectionManager(JIMCore core, ConversationManager conversationManager, MessageManager messageManager, UserInfoManager userInfoManager, ChatroomManager chatroomManager, CallManager callManager) {
        super(CONNECTION_STATE_MACHINE);

        this.mCore = core;
        this.mCore.setConnectionStatus(JIMCore.ConnectionStatusInternal.IDLE);
        this.mCore.getWebSocket().setConnectionListener(this);
        this.mConversationManager = conversationManager;
        this.mMessageManager = messageManager;
        this.mUserInfoManager = userInfoManager;
        this.mChatroomManager = chatroomManager;
        this.mCallManager = callManager;
        this.mNetworkChangeReceiver = new NetworkChangeReceiver(this);
        prepareStateMachine();
    }

    public void init() {
        ((Application)mCore.getContext()).registerActivityLifecycleCallbacks(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mCore.getContext().registerReceiver(this.mNetworkChangeReceiver, filter);
    }

    @Override
    public void onConnectComplete(int errorCode, String userId, String session, String extra) {
        if (errorCode == ConstInternal.ErrorCode.NONE) {
            mIntervalGenerator.reset();
            mCore.setUserId(userId);
            mCore.setSession(session);
            openDB();
            mMessageManager.connectSuccess();
            mConversationManager.connectSuccess();
            mChatroomManager.connectSuccess();
            mCallManager.connectSuccess();
            sendMessage(ConnEvent.CONNECT_DONE, extra);
            mConversationManager.syncConversations(mMessageManager::syncMessage);
            PushManager.getInstance().getToken(mCore.getContext());
        } else {
            if (checkConnectionFailure(errorCode)) {
                Message msg = Message.obtain();
                msg.what = ConnEvent.CONNECT_FAILURE;
                msg.arg1 = errorCode;
                msg.obj = extra;
                sendMessage(msg);
            } else {
                sendMessage(ConnEvent.WEBSOCKET_FAIL);
            }
        }
    }

    @Override
    public void onDisconnect(int errorCode, String extra) {
        Message msg = Message.obtain();
        msg.what = ConnEvent.REMOTE_DISCONNECT;
        msg.arg1 = errorCode;
        msg.obj = extra;
        sendMessage(msg);
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

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //Do nothing
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        //Do nothing
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (mTopForegroundActivity == null) {
            JLogger.i("CON-BG", "Foreground");
            mIntervalGenerator.reset();
            mIsForeground = true;
            sendMessage(ConnEvent.ENTER_FOREGROUND);
        }
        mTopForegroundActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //Do nothing
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (mTopForegroundActivity == activity) {
            JLogger.i("CON-BG", "Background");
            mIsForeground = false;
            sendMessage(ConnEvent.ENTER_BACKGROUND);
            mTopForegroundActivity = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        //Do nothing
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //Do nothing
    }

    @Override
    public void onNetworkAvailable() {
        mIntervalGenerator.reset();
        sendMessage(ConnEvent.NETWORK_AVAILABLE);
    }

    private void handleWebsocketFail() {
        sendMessage(ConnEvent.WEBSOCKET_FAIL);
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

    private void dbStatusNotice(boolean isOpen) {
        JLogger.i("CON-Db", "db notice, isOpen is " + isOpen);
        if (isOpen) {
            mCore.getSyncTimeFromDB();
            if (mConnectionStatusListenerMap != null) {
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onDbOpen());
                }
            }
        } else {
            if (mConnectionStatusListenerMap != null) {
                for (Map.Entry<String, IConnectionStatusListener> entry :
                        mConnectionStatusListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onDbClose());
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

    private void prepareStateMachine() {
        mSuperState = new ConnSuperState();
        mSuperState.setConnectionManager(this);
        mIdleState = new ConnIdleState();
        mIdleState.setConnectionManager(this);
        mConnectingState = new ConnConnectingState();
        mConnectingState.setConnectionManager(this);
        mConnectedState = new ConnConnectedState();
        mConnectedState.setConnectionManager(this);
        mWaitingState = new ConnWaitingForConnectState();
        mWaitingState.setConnectionManager(this);

        addState(mSuperState, null);
        addState(mIdleState, mSuperState);
        addState(mConnectingState, mSuperState);
        addState(mConnectedState, mSuperState);
        addState(mWaitingState, mSuperState);

        setInitialState(mIdleState);
        start();
    }

    public void setConnectionStatus(int status) {
        mCore.setConnectionStatus(status);
    }

    public boolean isSameToken(String token) {
        boolean result;
        if (mCore.getToken() == null) {
            return false;
        }
        result = mCore.getToken().equals(token);
        return result;
    }

    public boolean updateToken(String token) {
        boolean isUpdate = false;
        if (token == null) {
            token = "";
        }
        if (mCore.getToken() == null || !mCore.getToken().equals(token)) {
            mCore.setToken(token);
            mCore.setUserId("");
            isUpdate = true;
        }
        return isUpdate;
    }

    public void connect() {
        openDB();
        mCore.getWebSocket().connect(mCore.getAppKey(), mCore.getToken(), mCore.getDeviceId(), mCore.getPackageName(), mCore.getNetworkType(), mCore.getCarrier(), mPushChannel, mPushToken, mCore.getSystemLanguage(), mCore.getServers());
    }

    public void enterConnected() {
        JLogger.getInstance().removeExpiredLogs();
        mCore.getWebSocket().startHeartbeat();
        mCore.getWebSocket().pushSwitch(!mIsForeground, mCore.getUserId());
    }

    public void leaveConnected() {
        mCore.getWebSocket().stopHeartbeat();
        mCore.getWebSocket().pushRemainCmdAndCallbackError();
    }

    public void disconnectExist(boolean receivePush) {
        mChatroomManager.userDisconnect();
        mCore.getWebSocket().disconnect(receivePush);
        closeDB();
    }

    public void disconnectWithoutWS() {
        mChatroomManager.userDisconnect();
        closeDB();
    }

    public void handleRemoteDisconnect() {
        closeDB();
    }

    public int getReconnectInterval() {
        return mIntervalGenerator.getNextInterval();
    }

    public void pushSwitch(boolean isBackground) {
        mCore.getWebSocket().pushSwitch(isBackground, mCore.getUserId());
    }

    public void notifyConnecting() {
        notify(JIMConst.ConnectionStatus.CONNECTING, JErrorCode.NONE, "");
    }

    public void notifyConnected(String extra) {
        notify(JIMConst.ConnectionStatus.CONNECTED, JErrorCode.NONE, extra);
    }

    public void notifyDisconnected(int code, String extra) {
        notify(JIMConst.ConnectionStatus.DISCONNECTED, code, extra);
    }

    public void notifyFailure(int code, String extra) {
        notify(JIMConst.ConnectionStatus.FAILURE, code, extra);
    }

    private void notify(JIMConst.ConnectionStatus status, int code, String extra) {
        if (code == ConstInternal.ErrorCode.USER_KICKED_BY_OTHER_CLIENT) {
            mCallManager.imKick();
        }
        mCore.getCallbackHandler().post(() -> {
            if (mConnectionStatusListenerMap != null) {
                for (Map.Entry<String, IConnectionStatusListener> entry : mConnectionStatusListenerMap.entrySet()) {
                    entry.getValue().onStatusChange(status, code, extra);
                }
            }
        });
    }

    public void transitionToIdleState() {
        transitionTo(mIdleState);
    }

    public void transitionToConnectingState() {
        transitionTo(mConnectingState);
    }

    public void transitionToConnectedState() {
        transitionTo(mConnectedState);
    }

    public void transitionToWaitingForConnectState() {
        transitionTo(mWaitingState);
    }

    private final JIMCore mCore;
    private final ConversationManager mConversationManager;
    private final MessageManager mMessageManager;
    private final UserInfoManager mUserInfoManager;
    private final ChatroomManager mChatroomManager;
    private final CallManager mCallManager;
    private ConcurrentHashMap<String, IConnectionStatusListener> mConnectionStatusListenerMap;
    private PushChannel mPushChannel;
    private String mPushToken;
    private final IntervalGenerator mIntervalGenerator = new IntervalGenerator();
    private boolean mIsForeground;
    private Activity mTopForegroundActivity;
    private final NetworkChangeReceiver mNetworkChangeReceiver;

    private ConnSuperState mSuperState;
    private ConnIdleState mIdleState;
    private ConnConnectingState mConnectingState;
    private ConnConnectedState mConnectedState;
    private ConnWaitingForConnectState mWaitingState;
    private static final String CONNECTION_STATE_MACHINE = "ConnectionStateMachine";
}
