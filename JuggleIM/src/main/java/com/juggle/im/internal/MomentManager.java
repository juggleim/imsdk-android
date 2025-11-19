package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.interfaces.IMomentManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.GetMomentCommentOption;
import com.juggle.im.model.GetMomentOption;
import com.juggle.im.model.Moment;
import com.juggle.im.model.MomentComment;
import com.juggle.im.model.MomentMedia;
import com.juggle.im.model.MomentReaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MomentManager implements IMomentManager {
    public MomentManager(JIMCore core) {
        this.mCore = core;
    }

    @Override
    public void addMoment(String content, List<MomentMedia> mediaList, JIMConst.IResultCallback<Moment> callback) {
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", content);
        Map<String, Object> params = new HashMap<>();
        params.put("content", contentMap);
        request("/jim/posts/add", "POST", params, new JIMConst.IResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                int i = 1;
            }

            @Override
            public void onError(int errorCode) {

                int i = 1;
            }
        });

    }

    @Override
    public void removeMoment(String momentId, IMessageManager.ISimpleCallback callback) {

    }

    @Override
    public List<Moment> getCachedMomentList(GetMomentOption option) {
        return Collections.emptyList();
    }

    @Override
    public void getMomentList(GetMomentOption option, JIMConst.IResultListCallback<Moment> callback) {

    }

    @Override
    public void getMoment(String momentId, JIMConst.IResultCallback<Moment> callback) {

    }

    @Override
    public void addComment(String momentId, String parentCommentId, String content, JIMConst.IResultCallback<MomentComment> callback) {

    }

    @Override
    public void removeComment(String commentId, IMessageManager.ISimpleCallback callback) {

    }

    @Override
    public void getCommentList(GetMomentCommentOption option, JIMConst.IResultListCallback<MomentComment> callback) {

    }

    @Override
    public void addReaction(String momentId, String key, IMessageManager.ISimpleCallback callback) {

    }

    @Override
    public void removeReaction(String momentId, String key, IMessageManager.ISimpleCallback callback) {

    }

    @Override
    public void getReactionList(String momentId, JIMConst.IResultListCallback<MomentReaction> callback) {

    }

    private void request(String subUrl, String method, Map<String, Object> params, JIMConst.IResultCallback<JSONObject> callback) {
        // 1. 拼接基础URL
        String urlString = getBaseUrl() + subUrl;

        // 2. 处理GET请求参数
        if ("GET".equalsIgnoreCase(method)) {
            urlString = fetchGetUrl(urlString, params);
        }

        // 开启新线程执行网络请求（避免阻塞主线程）
        String finalUrlString = urlString;
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(finalUrlString);
                connection = (HttpURLConnection) url.openConnection();

                // 3. 设置请求方法和超时
                connection.setRequestMethod(method);
                connection.setConnectTimeout(30000); // 连接超时30秒
                connection.setReadTimeout(30000);    // 读取超时30秒

                // 4. 设置请求头
                connection.setRequestProperty("appKey", mCore.getAppKey());
                connection.setRequestProperty("Authorization", mCore.getToken());

                // 5. 处理POST请求参数（JSON格式）
                if ("POST".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    // 写入JSON请求体
                    try (OutputStream os = connection.getOutputStream();
                         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                        JSONObject jsonObject = new JSONObject(params);
                        String jsonBody = jsonObject.toString();
                        writer.write(jsonBody);
                        writer.flush();
                    }
                }

                // 6. 获取响应状态码
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // 对应OC中的statusCode != 200
                    throw new IOException("HTTP status code: " + responseCode);
                }

                // 7. 读取响应体
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                    // 处理响应内容
                    handleResponse(responseBody.toString(), finalUrlString, callback);
                }
            } catch (Exception e) {
                handleError(finalUrlString, e, callback);
            } finally {
                if (connection != null) {
                    connection.disconnect(); // 关闭连接
                }
            }
        }).start(); // 启动线程
    }

    private void handleResponse(String responseBody, String url, JIMConst.IResultCallback<JSONObject> callback) {
        try {
            // 解析响应为JSONObject
            JSONObject responseJson = new JSONObject(responseBody);

            // 检查业务状态码
            if (!responseJson.has("code")) {
                JLogger.e("Mmt-Request", "response missing 'code', url: " + url);
                if (callback != null) {
                    callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
                }
                return;
            }

            int code = responseJson.getInt("code");
            if (code != JErrorCode.NONE) {
                JLogger.e("Mmt-Request", "response error, code: " + code + ", url: " + url);
                if (callback != null) {
                    callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
                }
                return;
            }

            JSONObject dataJson = null;
            if (responseJson.has("data") && !responseJson.isNull("data")) {
                dataJson = responseJson.getJSONObject("data");
            }
            if (callback != null) {
                callback.onSuccess(dataJson);
            }
        } catch (JSONException e) {
            JLogger.e("Mmt-Request", "JSON parse error, url: " + url + ", error: " + e.getMessage());
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    private void handleError(String url, Throwable throwable, JIMConst.IResultCallback<JSONObject> callback) {
        JLogger.e("Mmt-Request", "request error" + ", url: " + url + ", error: " + throwable.getMessage());
        if (callback != null) {
            callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
        }
    }

    private String fetchGetUrl(String url, Map<String, Object> params) {
        try {
            // 1. 解析原始 URL 为 URI（处理基础 URL 部分）
            URI uri = new URI(url);
            // 构建 URL 组件（分离协议、主机、路径等）
            URL urlObj = uri.toURL();

            // 2. 初始化查询参数列表（保留已有参数）
            List<String> queryItems = new ArrayList<>();
            String existingQuery = uri.getQuery();
            if (existingQuery != null && !existingQuery.isEmpty()) {
                // 拆分已有查询参数
                String[] existingParams = existingQuery.split("&");
                Collections.addAll(queryItems, existingParams);
            }

            // 3. 遍历参数Map，添加新参数（自动编码）
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object valueObj = entry.getValue();
                    // 将参数值转为字符串（支持数字、布尔等类型）
                    String value = valueObj != null ? valueObj.toString() : "";

                    // 对键和值进行 URL 编码（处理特殊字符）
                    String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
                    String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

                    queryItems.add(encodedKey + "=" + encodedValue);
                }
            }

            // 4. 拼接所有查询参数
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < queryItems.size(); i++) {
                if (i > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(queryItems.get(i));
            }
            String newQuery = queryBuilder.toString();

            // 5. 构建最终 URL（拼接协议、主机、路径和新参数）
            URI finalUri = new URI(
                    urlObj.getProtocol(),
                    urlObj.getAuthority(),
                    urlObj.getPath(),
                    newQuery,
                    urlObj.getRef()
            );

            return finalUri.toString();

        } catch (Exception e) {
            JLogger.e("Mmt-Request", "fetch url error: " + e.getMessage());
            return url; // 错误时返回原始 URL
        }
    }

    private String getBaseUrl() {
        if (TextUtils.isEmpty(mBaseUrl)) {
            String server = "";
            if (!mCore.getServers().isEmpty()) {
                server = mCore.getServers().get(0);
            }
            if (server.startsWith("ws://")) {
                server = server.replaceFirst("^ws://", "http://");
            } else if (server.startsWith("wss://")) {
                server = server.replaceFirst("^wss://", "https://");
            }
            mBaseUrl = server;
        }
        return mBaseUrl;
    }

    private final JIMCore mCore;
    private String mBaseUrl;
}
