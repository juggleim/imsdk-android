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
            if (mediaList != null && !mediaList.isEmpty()) {
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
        // 1. Build the base URL
        String urlString = getBaseUrl() + subUrl;

        // 2. Handle GET request parameters
        if ("GET".equalsIgnoreCase(method)) {
            urlString = fetchGetUrl(urlString, jsonObject);
        }

        // Start a new thread for the network request to avoid blocking the main thread
        String finalUrlString = urlString;
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(finalUrlString);
                connection = (HttpURLConnection) url.openConnection();

                // 3. Set the request method and timeout
                connection.setRequestMethod(method);
                connection.setConnectTimeout(30000); // Connection timeout: 30 seconds
                connection.setReadTimeout(30000);    // Read timeout: 30 seconds

                // 4. Set request headers
                connection.setRequestProperty("appKey", mCore.getAppKey());
                connection.setRequestProperty("Authorization", mCore.getToken());

                // 5. Handle POST request parameters in JSON format
                if ("POST".equalsIgnoreCase(method) && jsonObject != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    // Write the JSON request body
                    try (OutputStream os = connection.getOutputStream();
                         OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                        String jsonBody = jsonObject.toString();
                        writer.write(jsonBody);
                        writer.flush();
                    }
                }

                // 6. Get the response status code
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // Corresponds to statusCode != 200 in Objective-C
                    throw new IOException("HTTP status code: " + responseCode);
                }

                // 7. Read the response body
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder responseBody = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                    // Handle the response content
                    handleResponse(responseBody.toString(), finalUrlString, callback);
                }
            } catch (Exception e) {
                handleError(finalUrlString, e, callback);
            } finally {
                if (connection != null) {
                    connection.disconnect(); // Close the connection
                }
            }
        }).start(); // Start the thread
    }

    private void handleResponse(String responseBody, String url, JIMConst.IResultCallback<JSONObject> callback) {
        try {
            // Parse the response as a JSONObject
            JSONObject responseJson = new JSONObject(responseBody);

            // Check the business status code
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
            // 1. Parse the original URL as a URI and handle the base URL part
            URI uri = new URI(url);
            URL urlObj = uri.toURL();

            // 2. Initialize the query parameter list and keep existing parameters
            List<String> queryItems = new ArrayList<>();
            String existingQuery = uri.getQuery();
            if (existingQuery != null && !existingQuery.isEmpty()) {
                String[] existingParams = existingQuery.split("&");
                Collections.addAll(queryItems, existingParams);
            }

            // 3. Iterate over the JSONObject and add new parameters with encoding for basic JSON types
            if (json != null && json.length() > 0) {
                // Iterate over JSON key-value pairs
                Iterator<String> keyIterator = json.keys();
                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    try {
                        // Get the parameter value for JSON-supported types such as String, Number, and Boolean
                        Object valueObj = json.get(key);
                        String value = valueObj.toString();

                        // URL-encode keys and values to handle non-ASCII and special characters
                        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
                        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

                        queryItems.add(encodedKey + "=" + encodedValue);
                    } catch (JSONException e) {
                        // A single parameter parse failure does not affect the whole operation; only log it
                        JLogger.e("Mmt-Request", "parse json param error, key: " + key, e.getMessage());
                    }
                }
            }

            // 4. Join all query parameters
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < queryItems.size(); i++) {
                if (i > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(queryItems.get(i));
            }
            String newQuery = queryBuilder.toString();

            // 5. Build the final URL from the scheme, host, path, and new parameters
            URI finalUri = new URI(
                    urlObj.getProtocol(),
                    urlObj.getAuthority(),
                    urlObj.getPath(),
                    newQuery.isEmpty() ? null : newQuery, // Pass null when there are no parameters to avoid a trailing "?"
                    urlObj.getRef()
            );

            return finalUri.toString();

        } catch (Exception e) {
            JLogger.e("Mmt-Request", "fetch url error: " + e.getMessage());
            return url; // Return the original URL on error
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
