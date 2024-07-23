package com.juggle.im.model.messages;

import android.text.TextUtils;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MediaMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class VideoMessage extends MediaMessageContent {

    public VideoMessage() {
        this.mContentType = "jg:video";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(getUrl())) {
                jsonObject.put(URL, getUrl());
            }
            if (!TextUtils.isEmpty(getLocalPath())) {
                jsonObject.put(LOCAL, getLocalPath());
            }
            if (!TextUtils.isEmpty(mSnapshotUrl)) {
                jsonObject.put(POSTER, mSnapshotUrl);
            }
            if (!TextUtils.isEmpty(mSnapshotLocalPath)) {
                jsonObject.put(POSTER_LOCAL, mSnapshotLocalPath);
            }
            jsonObject.put(HEIGHT, mHeight);
            jsonObject.put(WIDTH, mWidth);
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
            jsonObject.put(DURATION, mDuration);
            jsonObject.put(SIZE, mSize);
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "VideoMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "VideoMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(URL)) {
                setUrl(jsonObject.optString(URL));
            }
            if (jsonObject.has(LOCAL)) {
                setLocalPath(jsonObject.optString(LOCAL));
            }
            if (jsonObject.has(POSTER)) {
                mSnapshotUrl = jsonObject.optString(POSTER);
            }
            if (jsonObject.has(POSTER_LOCAL)) {
                mSnapshotLocalPath = jsonObject.optString(POSTER_LOCAL);
            }
            if (jsonObject.has(HEIGHT)) {
                mHeight = jsonObject.optInt(HEIGHT);
            }
            if (jsonObject.has(WIDTH)) {
                mWidth = jsonObject.optInt(WIDTH);
            }
            if (jsonObject.has(DURATION)) {
                mDuration = jsonObject.optInt(DURATION);
            }
            if (jsonObject.has(SIZE)) {
                mSize = jsonObject.optLong(SIZE);
            }
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "VideoMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public String getSnapshotLocalPath() {
        return mSnapshotLocalPath;
    }

    public void setSnapshotLocalPath(String snapshotLocalPath) {
        this.mSnapshotLocalPath = snapshotLocalPath;
    }

    public String getSnapshotUrl() {
        return mSnapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        mSnapshotUrl = snapshotUrl;
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

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    private String mSnapshotLocalPath;
    private String mSnapshotUrl;
    private int mHeight;
    private int mWidth;
    private long mSize;
    private int mDuration;
    private String mExtra;

    private static final String URL = "url";
    private static final String LOCAL = "local";
    private static final String POSTER = "poster";
    private static final String POSTER_LOCAL = "snapshotLocalPath";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String SIZE = "size";
    private static final String DURATION = "duration";
    private static final String EXTRA = "extra";
    private static final String DIGEST = "[Video]";
}
