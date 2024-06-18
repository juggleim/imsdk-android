package com.jet.im.internal.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.core.db.DBManager;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.util.JUtility;

import java.util.ArrayList;
import java.util.List;

public class JetIMCore {

    public JetIMCore() {
        HandlerThread sendThread = new HandlerThread("JET_SEND");
        sendThread.start();
        mSendHandler = new Handler(sendThread.getLooper());
        mCallbackHandler = new Handler(Looper.getMainLooper());
        mWebSocket = new JWebSocket(mSendHandler);
    }

    public JWebSocket getWebSocket() {
        return mWebSocket;
    }

    public String getDeviceId() {
        if (mContext == null) {
            return "";
        }
        return JUtility.getDeviceId(mContext);
    }

    public String getPackageName() {
        if (mContext == null) {
            return "";
        }
        return mContext.getPackageName();
    }

    public String getNetworkType() {
        if (mContext == null) {
            return "";
        }
        return JUtility.getNetworkType(mContext);
    }

    public String getCarrier() {
        if (mContext == null) {
            return "";
        }
        return JUtility.getCarrier(mContext);
    }

    public List<String> getNaviUrls() {
        if (mNaviUrls == null) {
            mNaviUrls = new ArrayList<>();
            mNaviUrls.add(ConstInternal.NAVI_URL);
        }
        return mNaviUrls;
    }

    public void setNaviUrl(@NonNull List<String> naviUrls) {
        this.mNaviUrls = naviUrls;
    }

    public List<String> getServers() {
        if (mServers == null) {
            mServers = new ArrayList<>();
            mServers.add(ConstInternal.WEB_SOCKET_URL);
        }
        return mServers;
    }

    public void setServers(@NonNull List<String> servers) {
        this.mServers = servers;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public void setAppKey(@NonNull String appKey) {
        if (appKey.equals(mAppKey)) {
            return;
        }
        mAppKey = appKey;
        if (mContext == null) {
            return;
        }
        SharedPreferences sp = JUtility.getSP(mContext);
        sp.edit().putString(APP_KEY, appKey).apply();
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(@NonNull String token) {
        if (token.equals(mToken)) {
            return;
        }
        mToken = token;
        if (mContext == null) {
            return;
        }
        SharedPreferences sp = JUtility.getSP(mContext);
        sp.edit().putString(TOKEN, token).apply();
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(@NonNull String userId) {
        if (userId.equals(mUserId)) {
            return;
        }
        mUserId = userId;
        if (mContext == null) {
            return;
        }
        SharedPreferences sp = JUtility.getSP(mContext);
        sp.edit().putString(USER_ID, userId).apply();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(@NonNull Context context) {
        mContext = context.getApplicationContext();
        SharedPreferences sp = JUtility.getSP(mContext);
        mAppKey = sp.getString(APP_KEY, "");
        mUserId = sp.getString(USER_ID, "");
        mToken = sp.getString(TOKEN, "");
    }

    public int getConnectionStatus() {
        return mConnectionStatus;
    }

    public void setConnectionStatus(int connectionStatus) {
        mConnectionStatus = connectionStatus;
    }

    public DBManager getDbManager() {
        return mDbManager;
    }

    public void getSyncTimeFromDB() {
        mConversationSyncTime = mDbManager.getConversationSyncTime();
        mMessageSendSyncTime = mDbManager.getMessageSendSyncTime();
        mMessageReceiveTime = mDbManager.getMessageReceiveSyncTime();
    }

    public long getConversationSyncTime() {
        return mConversationSyncTime;
    }

    public void setConversationSyncTime(long conversationSyncTime) {
        if (conversationSyncTime > mConversationSyncTime) {
            mConversationSyncTime = conversationSyncTime;
            mDbManager.setConversationSyncTime(conversationSyncTime);
        }
    }

    public long getMessageSendSyncTime() {
        return mMessageSendSyncTime;
    }

    public void setMessageSendSyncTime(long messageSendSyncTime) {
        if (messageSendSyncTime > mMessageSendSyncTime) {
            mMessageSendSyncTime = messageSendSyncTime;
            mDbManager.setMessageSendSyncTime(messageSendSyncTime);
        }
    }

    public long getMessageReceiveTime() {
        return mMessageReceiveTime;
    }

    public void setMessageReceiveTime(long messageReceiveTime) {
        if (messageReceiveTime > mMessageReceiveTime) {
            mMessageReceiveTime = messageReceiveTime;
            mDbManager.setMessageReceiveSyncTime(messageReceiveTime);
        }
    }

    public Handler getSendHandler() {
        return mSendHandler;
    }

    public void setCallbackHandler(Handler callbackHandler) {
        if (callbackHandler == null) return;
        this.mCallbackHandler = callbackHandler;
    }

    public Handler getCallbackHandler() {
        return mCallbackHandler;
    }

    public static class ConnectionStatusInternal {
        public static final int IDLE = 0;
        public static final int CONNECTED = 1;
        public static final int DISCONNECTED = 2;
        public static final int CONNECTING = 3;
        public static final int FAILURE = 4;
        public static final int WAITING_FOR_CONNECTING = 5;
    }

    private final JWebSocket mWebSocket;
    private List<String> mNaviUrls;
    private List<String> mServers;
    private String mAppKey;
    private String mToken;
    private String mUserId;
    private Context mContext;
    private int mConnectionStatus;
    private final DBManager mDbManager = new DBManager();
    private long mConversationSyncTime;
    private long mMessageSendSyncTime;
    private long mMessageReceiveTime;
    private Handler mCallbackHandler;
    private final Handler mSendHandler;
    private final String APP_KEY = "AppKey";
    private final String TOKEN = "Token";
    private final String USER_ID = "UserId";

}
