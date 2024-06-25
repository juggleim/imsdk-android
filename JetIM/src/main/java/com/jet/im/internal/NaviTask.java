package com.jet.im.internal;

import android.text.TextUtils;
import com.jet.im.internal.util.JLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class NaviTask {
    NaviTask(List<String> urls, String appKey, String token, IRequestCallback callback) {
        mRequestMap = new ConcurrentHashMap<>();
        if (urls.size() > MAX_CONCURRENT_COUNT) {
            urls = urls.subList(0, MAX_CONCURRENT_COUNT);
        }
        for (String url : urls) {
            mRequestMap.put(url, TaskStatus.IDLE);
        }
        mAppKey = appKey;
        mToken = token;
        mCallback = callback;
    }

    interface IRequestCallback {
        void onSuccess(String userId, List<String> servers);

        void onError(int errorCode);
    }

    void start() {
        JLogger.i("NAV-Start", "urls is " + mRequestMap.keySet());
        if (mRequestMap.size() == 0 && mCallback != null) {
            mCallback.onError(ConstInternal.ErrorCode.SERVER_SET_ERROR);
            return;
        }
        for (String url : mRequestMap.keySet()) {
            Thread t = new Thread(() -> request(url, mAppKey, mToken));
            t.start();
        }
    }

    private void request(String url, String appKey, String token) {
        JLogger.i("NAV-Request", "url is " + url);
        String realUrl = url + NAVI_SERVER_SUFFIX;
        try {
            URL u = new URL(realUrl);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(APP_KEY, appKey);
            con.setRequestProperty(TOKEN, token);
            con.connect();
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                JLogger.e("NAV-Request", "get navi error, url is " + con.getURL() + ", responseCode is " + responseCode);
                if (responseCode == 401) {
                    responseError(url, ConstInternal.ErrorCode.TOKEN_ILLEGAL);
                } else {
                    responseError(url, ConstInternal.ErrorCode.NAVI_FAILURE);
                }
                return;
            }
            InputStream is = con.getInputStream();
            BufferedInputStream stream = new BufferedInputStream(is);
            ByteArrayOutputStream responseData = new ByteArrayOutputStream(512);
            int c;
            while ((c = stream.read()) != -1) {
                responseData.write(c);
            }
            String s = new String(responseData.toByteArray(), StandardCharsets.UTF_8).trim();
            if (TextUtils.isEmpty(s)) {
                JLogger.e("NAV-Request", "get navi error, url is " + con.getURL() + ", response is empty");
                responseError(url, ConstInternal.ErrorCode.NAVI_FAILURE);
                return;
            }
            JSONObject json = new JSONObject(s);
            JSONObject data = json.getJSONObject(DATA);
            String userId = data.optString(USER_ID);
            JSONArray servers = data.optJSONArray(SERVERS);
            List<String> serverList = new ArrayList<>();
            if (servers != null) {
                for (int i = 0; i < servers.length(); i++) {
                    serverList.add(servers.optString(i));
                }
            }
            responseSuccess(url, userId, serverList);
        } catch (Exception e) {
            JLogger.e("NAV-Request", "get navi error, url is " + url + ", exception is " + e.getMessage());
            responseError(url, ConstInternal.ErrorCode.NAVI_FAILURE);
        }
    }

    void responseError(String url, int errorCode) {
        boolean allFailed = true;
        synchronized (this) {
            if (mIsFinish) {
                return;
            }
            mRequestMap.put(url, TaskStatus.FAILURE);
            for (TaskStatus status : mRequestMap.values()) {
                if (status != TaskStatus.FAILURE) {
                    allFailed = false;
                    break;
                }
            }
        }
        if (allFailed && mCallback != null) {
            mCallback.onError(errorCode);
        }
    }

    void responseSuccess(String url, String userId, List<String> servers) {
        synchronized (this) {
            if (mIsFinish) {
                JLogger.i("NAV-Request", "compete fail, url is " + url);
                return;
            }
            mIsFinish = true;
            mRequestMap.put(url, TaskStatus.SUCCESS);
        }
        JLogger.i("NAV-Request", "compete success, url is " + url);
        if (mCallback != null) {
            mCallback.onSuccess(userId, servers);
        }
    }

    private enum TaskStatus { IDLE, FAILURE, SUCCESS }

    private static final String NAVI_SERVER_SUFFIX = "/navigator/general";
    private static final String APP_KEY = "x-appkey";
    private static final String TOKEN = "x-token";
    private static final String DATA = "data";
    private static final String USER_ID = "user_id";
    private static final String SERVERS = "servers";
    private static final int MAX_CONCURRENT_COUNT = 5;
    private boolean mIsFinish = false;
    private final ConcurrentHashMap<String, TaskStatus> mRequestMap;
    private final String mAppKey;
    private final String mToken;
    private final IRequestCallback mCallback;
}
