package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ImageMessage extends MessageContent {

    public ImageMessage() {
        this.mContentType = "jg:img";
    }
    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mUrl)) {
                jsonObject.put(URL, mUrl);
            }
            if (!TextUtils.isEmpty(mThumbnailUrl)) {
                jsonObject.put(THUMBNAIL, mThumbnailUrl);
            }
            jsonObject.put(HEIGHT, mHeight);
            jsonObject.put(WIDTH, mWidth);
        } catch (JSONException e) {
            LoggerUtils.e("TextMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("ImageMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(URL)) {
                mUrl = jsonObject.optString(URL);
            }
            if (jsonObject.has(THUMBNAIL)) {
                mThumbnailUrl = jsonObject.optString(THUMBNAIL);
            }
            if (jsonObject.has(HEIGHT)) {
                mHeight = jsonObject.optInt(HEIGHT);
            }
            if (jsonObject.has(WIDTH)) {
                mWidth = jsonObject.optInt(WIDTH);
            }
        } catch (JSONException e) {
            LoggerUtils.e("ImageMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }
    private String mUrl;
    private String mThumbnailUrl;
    private int mHeight;
    private int mWidth;
    private static final String URL = "url";
    private static final String THUMBNAIL = "thumbnail";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String DIGEST = "[Image]";
}