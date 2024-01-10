package com.jet.im.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.java_websocket.client.WebSocketClient;

import java.util.List;

public class JetIMCore {

    public JetIMCore() {

    }
    public String getNaviUrl() {
        return mNaviUrl;
    }

    public void setNaviUrl(@NonNull String naviUrl) {
        this.mNaviUrl = naviUrl;
    }

    public List<String> getServers() {
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
        SharedPreferences sp = getSP(mContext);
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
        SharedPreferences sp = getSP(mContext);
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
        SharedPreferences sp = getSP(mContext);
        sp.edit().putString(USER_ID, userId).apply();
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(@NonNull Context context) {
        mContext = context.getApplicationContext();
        SharedPreferences sp = getSP(mContext);
        mAppKey = sp.getString(APP_KEY, "");
        mUserId = sp.getString(USER_ID, "");
        mToken = sp.getString(TOKEN, "");
    }

    private SharedPreferences getSP(@NonNull Context context) {
        return context.getSharedPreferences(SP_NAME, 0);
    }
    private String mNaviUrl;
    private List<String> mServers;
    private String mAppKey;
    private String mToken;
    private String mUserId;
    private Context mContext;

    private final String SP_NAME = "j_im_core";
    private final String APP_KEY = "AppKey";
    private final String TOKEN = "Token";
    private final String USER_ID = "UserId";

}
