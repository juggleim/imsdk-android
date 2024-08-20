package com.juggle.im.model.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.juggle.im.internal.ConstInternal;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;
import com.juggle.im.model.MediaMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class SnapshotPackedVideoMessage extends MediaMessageContent {

    public SnapshotPackedVideoMessage() {
        this.mContentType = "jg:spvideo";
    }

    public static SnapshotPackedVideoMessage messageWithVideo(String videoPath, String snapshotImagePath) {
        SnapshotPackedVideoMessage message = new SnapshotPackedVideoMessage();
        message.setLocalPath(videoPath);
        if (snapshotImagePath != null) {
            Bitmap snapshotImage = BitmapFactory.decodeFile(snapshotImagePath);
            message.setSnapshotImage(snapshotImage);
        }
        return message;
    }

    public static SnapshotPackedVideoMessage messageWithVideo(String videoPath, Bitmap snapshotImage) {
        SnapshotPackedVideoMessage message = new SnapshotPackedVideoMessage();
        message.setLocalPath(videoPath);
        message.setSnapshotImage(snapshotImage);
        return message;
    }

    @Override
    public byte[] encode() {
        String snapshotString = "";
        if (mSnapshotImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mSnapshotImage.compress(Bitmap.CompressFormat.JPEG, ConstInternal.THUMBNAIL_QUALITY, byteArrayOutputStream);
            byte[] snapshotData = byteArrayOutputStream.toByteArray();
            snapshotString = JUtility.base64EncodedStringFrom(snapshotData);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(getUrl())) {
                jsonObject.put(URL, getUrl());
            }
            if (!TextUtils.isEmpty(getLocalPath())) {
                jsonObject.put(LOCAL, getLocalPath());
            }
            if (!TextUtils.isEmpty(snapshotString)) {
                jsonObject.put(POSTER, snapshotString);
            }
            jsonObject.put(HEIGHT, mHeight);
            jsonObject.put(WIDTH, mWidth);
            jsonObject.put(DURATION, mDuration);
            jsonObject.put(SIZE, mSize);
            if (!TextUtils.isEmpty(mName)) {
                jsonObject.put(NAME, mName);
            }
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "SnapshotPackedVideoMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "SnapshotPackedVideoMessage decode data is null");
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
                String snapshotString = jsonObject.optString(POSTER);
                byte[] snapshotData = JUtility.dataWithBase64EncodedString(snapshotString);
                mSnapshotImage = BitmapFactory.decodeByteArray(snapshotData, 0, snapshotData.length);
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
            if (jsonObject.has(NAME)) {
                mName = jsonObject.optString(NAME);
            }
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "SnapshotPackedVideoMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public Bitmap getSnapshotImage() {
        return mSnapshotImage;
    }

    public void setSnapshotImage(Bitmap snapshotImage) {
        this.mSnapshotImage = snapshotImage;
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

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    private Bitmap mSnapshotImage;
    private int mHeight;
    private int mWidth;
    private long mSize;
    private int mDuration;
    private String mName;
    private String mExtra;
    private static final String URL = "sightUrl";
    private static final String LOCAL = "local";
    private static final String POSTER = "content";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String SIZE = "size";
    private static final String DURATION = "duration";
    private static final String NAME = "name";
    private static final String EXTRA = "extra";
    private static final String DIGEST = "[Video]";
}
