package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.upload.UploadFileType;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ImageMessage extends MediaMessageContent {

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
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
            jsonObject.put(SIZE, mSize);
        } catch (JSONException e) {
            JLogger.e("ImageMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("ImageMessage decode data is null");
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
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
            if (jsonObject.has(SIZE)) {
                mSize = jsonObject.optLong(SIZE);
            }
        } catch (JSONException e) {
            JLogger.e("ImageMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    @Override
    public UploadFileType getUploadFileType() {
        return UploadFileType.IMAGE;
    }

    public String getThumbnailLocalPath() {
        return mThumbnailLocalPath;
    }

    public void setThumbnailLocalPath(String thumbnailLocalPath) {
        this.mThumbnailLocalPath = thumbnailLocalPath;
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

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    private String mThumbnailLocalPath;
    private String mThumbnailUrl;
    private int mHeight;
    private int mWidth;
    private String mExtra;
    private long mSize;
    private static final String URL = "url";
    private static final String THUMBNAIL = "thumbnail";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String EXTRA = "extra";
    private static final String SIZE = "size";
    private static final String DIGEST = "[Image]";
}
