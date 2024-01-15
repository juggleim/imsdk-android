package com.jet.im.internal.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.util.JUtility;

public class JetIMCore {

    public JetIMCore() {
    }
    public JWebSocket getWebSocket() {
        return mWebSocket;
    }
    public void setWebSocket(JWebSocket ws) {
        this.mWebSocket = ws;
    }
    public String getNaviUrl() {
        return mNaviUrl;
    }

    public void setNaviUrl(@NonNull String naviUrl) {
        this.mNaviUrl = naviUrl;
    }

    public String[] getServers() {
        if (mServers == null) {
            mServers = new String[]{ConstInternal.WEB_SOCKET_URL};
        }
        return mServers;
    }

    public void setServers(@NonNull String[] servers) {
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

    private JWebSocket mWebSocket;
    private String mNaviUrl;
    private String[] mServers;
    private String mAppKey;
    private String mToken;
    private String mUserId;
    private Context mContext;

    private final String APP_KEY = "AppKey";
    private final String TOKEN = "Token";
    private final String USER_ID = "UserId";

}
