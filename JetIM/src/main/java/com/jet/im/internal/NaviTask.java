package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class NaviTask {
    interface IRequestCallback {
        void onSuccess(String userId, List<String> servers);
        void onError(int errorCode);
    }
    static void request(List<String> urls, String appKey, String token, IRequestCallback callback) {
        if (urls.size() > MAX_CONCURRENT_COUNT) {
            urls = urls.subList(0, MAX_CONCURRENT_COUNT);
        }
        for (String url : urls) {
            Thread t = new Thread(() -> request(url, appKey, token, callback));
            t.start();
        }
    }

    private static void request(String url, String appKey, String token, IRequestCallback callback) {
        url = url + NAVI_SERVER_SUFFIX;
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(APP_KEY, appKey);
            con.setRequestProperty(TOKEN, token);
            con.connect();
            synchronized (NaviTask.class) {
                if (sIsFinish) {
                    return;
                }
                sIsFinish = true;
                responseConnection(con, callback);
            }
        } catch (IOException | JSONException e) {
            JLogger.e("get navi exception, e is " + e.getMessage());
            if (callback != null) {
                callback.onError(ConstInternal.ErrorCode.NAVI_FAILURE);
            }
        }
    }

    private static void responseConnection(HttpURLConnection con, IRequestCallback callback) throws IOException, JSONException {
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            if (responseCode == 401) {
                if (callback != null) {
                    callback.onError(ConstInternal.ErrorCode.TOKEN_ILLEGAL);
                }
            } else {
                if (callback != null) {
                    callback.onError(ConstInternal.ErrorCode.NAVI_FAILURE);
                }
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
            if (callback != null) {
                callback.onError(ConstInternal.ErrorCode.NAVI_FAILURE);
            }
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
        if (callback != null) {
            callback.onSuccess(userId, serverList);
        }
    }

    private static final String NAVI_SERVER_SUFFIX = "/navigator/general";
    private static final String APP_KEY = "x-appkey";
    private static final String TOKEN = "x-token";
    private static final String DATA = "data";
    private static final String USER_ID = "user_id";
    private static final String SERVERS = "servers";
    private static final int MAX_CONCURRENT_COUNT = 5;
    private static boolean sIsFinish = false;
}
