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
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
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
import java.util.Iterator;
import java.util.List;

public class MomentManager implements IMomentManager {
    public MomentManager(JIMCore core) {
        this.mCore = core;
    }

    @Override
    public void addMoment(String content, List<MomentMedia> mediaList, JIMConst.IResultCallback<Moment> callback) {
        JSONObject paramJson = new JSONObject();
        try {
            JSONObject contentJson = new JSONObject();
            contentJson.put("text", content);
            if (mediaList != null) {
                JSONArray mediaJsonArray = new JSONArray();
                for (MomentMedia media : mediaList) {
                    JSONObject mediaJson = media.toJson();
                    mediaJsonArray.put(mediaJson);
                }
                contentJson.put("medias", mediaJsonArray);
            }
            paramJson.put("content", contentJson);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
            return;
        }

        request("/momentgateway/moments/add", "POST", paramJson, new JIMConst.IResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                Moment moment = new Moment();
                moment.setContent(content);
                moment.setMediaList(mediaList);
                moment.setMomentId(data.optString("moment_id"));
                moment.setCreateTime(data.optLong("moment_time"));
                JSONObject userInfoJson = data.optJSONObject("user_info");
                if (userInfoJson != null) {
                    UserInfo userInfo = UserInfo.fromJson(userInfoJson);
                    moment.setUserInfo(userInfo);
                    mCore.getDbManager().insertUserInfoList(Collections.singletonList(userInfo));
                }
                mCore.getDbManager().insertMoments(Collections.singletonList(moment));
                if (callback != null) {
                    callback.onSuccess(moment);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void removeMoment(String momentId, IMessageManager.ISimpleCallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray a = new JSONArray();
            a.put(momentId);
            jsonObject.put("moment_ids", a);
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
            return;
        }
        request("/momentgateway/moments/del", "POST", jsonObject, new JIMConst.IResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                mCore.getDbManager().removeMoment(momentId);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public List<Moment> getCachedMomentList(GetMomentOption option) {
        return mCore.getDbManager().getCachedMomentList(option);
    }

    @Override
    public void getMomentList(GetMomentOption option, JIMConst.IResultListCallback<Moment> callback) {
        try {
            request("/momentgateway/moments/list", "GET", option.toJson(), new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    JSONArray itemsArray = data.optJSONArray("items");
                    List<Moment> momentList = new ArrayList<>();
                    if (itemsArray != null) {
                        for (int i = 0; i < itemsArray.length(); i++) {
                            try {
                                JSONObject itemJson = itemsArray.getJSONObject(i);
                                Moment moment = Moment.fromJson(itemJson);
                                if (moment != null) {
                                    momentList.add(moment);
                                }
                            } catch (JSONException ignored) {

                            }
                        }
                    }
                    boolean isFinish = data.optBoolean("is_finished");
                    mCore.getDbManager().insertMoments(momentList);
                    if (callback != null) {
                        callback.onSuccess(momentList, isFinish);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void getMoment(String momentId, JIMConst.IResultCallback<Moment> callback) {
        JSONObject object = new JSONObject();
        try {
            object.put("moment_id", momentId);
            request("/momentgateway/moments/info", "GET", object, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    Moment moment = Moment.fromJson(data);
                    if (moment != null) {
                        mCore.getDbManager().insertMoments(Collections.singletonList(moment));
                    }
                    if (callback != null) {
                        callback.onSuccess(moment);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void addComment(String momentId, String parentCommentId, String content, JIMConst.IResultCallback<MomentComment> callback) {
        try {
            JSONObject param = new JSONObject();
            param.put("moment_id", momentId);
            if (!TextUtils.isEmpty(parentCommentId)) {
                param.put("parent_comment_id", parentCommentId);
            }
            JSONObject textObject = new JSONObject();
            textObject.put("text", content);
            param.put("content", textObject);
            request("/momentgateway/moments/comments/add", "POST", param, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    MomentComment comment = new MomentComment();
                    comment.setMomentId(momentId);
                    comment.setCommentId(data.optString("comment_id"));
                    comment.setParentCommentId(parentCommentId);
                    comment.setContent(content);
                    comment.setUserInfo(UserInfo.fromJson(data.optJSONObject("user_info")));
                    comment.setParentUserInfo(UserInfo.fromJson(data.optJSONObject("parent_user_info")));
                    comment.setCreateTime(data.optLong("comment_time"));
                    List<UserInfo> userList = new ArrayList<>();
                    if (comment.getUserInfo() != null) {
                        userList.add(comment.getUserInfo());
                    }
                    if (comment.getParentUserInfo() != null) {
                        userList.add(comment.getParentUserInfo());
                    }
                    if (!userList.isEmpty()) {
                        mCore.getDbManager().insertUserInfoList(userList);
                    }
                    if (callback != null) {
                        callback.onSuccess(comment);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void removeComment(String momentId, String commentId, IMessageManager.ISimpleCallback callback) {
        try {
            JSONObject param = new JSONObject();
            param.put("moment_id", momentId);
            JSONArray commentIdArray = new JSONArray();
            commentIdArray.put(commentId);
            param.put("comment_ids", commentIdArray);
            request("/momentgateway/moments/comments/del", "POST", param, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void getCommentList(GetMomentCommentOption option, JIMConst.IResultListCallback<MomentComment> callback) {
        try {
            request("/momentgateway/moments/comments/list", "GET", option.toJson(), new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    JSONArray itemsArray = data.optJSONArray("items");
                    List<MomentComment> commentList = new ArrayList<>();
                    if (itemsArray != null) {
                        for (int i = 0; i < itemsArray.length(); i++) {
                            try {
                                JSONObject itemJson = itemsArray.getJSONObject(i);
                                MomentComment comment = MomentComment.fromJson(itemJson);
                                if (comment != null) {
                                    commentList.add(comment);
                                }
                            } catch (JSONException ignored) {

                            }
                        }
                    }
                    boolean isFinish = data.optBoolean("is_finished");
                    if (callback != null) {
                        callback.onSuccess(commentList, isFinish);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void addReaction(String momentId, String key, IMessageManager.ISimpleCallback callback) {
        try {
            JSONObject param = new JSONObject();
            param.put("moment_id", momentId);
            JSONObject reactionJson = new JSONObject();
            reactionJson.put("key", key);
            param.put("reaction", reactionJson);
            request("/momentgateway/moments/reactions/add", "POST", param, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void removeReaction(String momentId, String key, IMessageManager.ISimpleCallback callback) {
        try {
            JSONObject param = new JSONObject();
            param.put("moment_id", momentId);
            JSONObject reactionJson = new JSONObject();
            reactionJson.put("key", key);
            param.put("reaction", reactionJson);
            request("/momentgateway/moments/reactions/del", "POST", param, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    @Override
    public void getReactionList(String momentId, JIMConst.IResultListCallback<MomentReaction> callback) {
        try {
            JSONObject param = new JSONObject();
            param.put("moment_id", momentId);
            request("/momentgateway/moments/reactions/list", "GET", param, new JIMConst.IResultCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    JSONArray itemsArray = data.optJSONArray("items");
                    List<MomentReaction> reactionList = MomentReaction.mergeReactionListWithJson(itemsArray);
                    if (callback != null) {
                        callback.onSuccess(reactionList, true);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } catch (JSONException e) {
            if (callback != null) {
                callback.onError(JErrorCode.MOMENT_REQUEST_ERROR);
            }
        }
    }

    private void request(String subUrl, String method, JSONObject jsonObject, JIMConst.IResultCallback<JSONObject> callback) {
        // 1. 拼接基础URL
        String urlString = getBaseUrl() + subUrl;

        // 2. 处理GET请求参数
        if ("GET".equalsIgnoreCase(method)) {
            urlString = fetchGetUrl(urlString, jsonObject);
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
                if ("POST".equalsIgnoreCase(method) && jsonObject != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    // 写入JSON请求体
                    try (OutputStream os = connection.getOutputStream();
                         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
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

    private String fetchGetUrl(String url, JSONObject json) {
        try {
            // 1. 解析原始 URL 为 URI（处理基础 URL 部分）
            URI uri = new URI(url);
            URL urlObj = uri.toURL();

            // 2. 初始化查询参数列表（保留已有参数）
            List<String> queryItems = new ArrayList<>();
            String existingQuery = uri.getQuery();
            if (existingQuery != null && !existingQuery.isEmpty()) {
                String[] existingParams = existingQuery.split("&");
                Collections.addAll(queryItems, existingParams);
            }

            // 3. 遍历 JSONObject，添加新参数（自动编码，兼容 JSON 基础类型）
            if (json != null && json.length() > 0) {
                // 使用迭代器遍历 JSON 键值对
                Iterator<String> keyIterator = json.keys();
                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    try {
                        // 获取参数值（兼容 String/Number/Boolean 等 JSON 支持类型）
                        Object valueObj = json.get(key);
                        String value = valueObj.toString();

                        // 对键和值进行 URL 编码（处理中文、特殊字符）
                        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
                        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

                        queryItems.add(encodedKey + "=" + encodedValue);
                    } catch (JSONException e) {
                        // 单个参数解析失败不影响整体，仅日志输出
                        JLogger.e("Mmt-Request", "parse json param error, key: " + key, e.getMessage());
                    }
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
                    newQuery.isEmpty() ? null : newQuery, // 无参数时传 null，避免 URL 末尾多 "?"
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
