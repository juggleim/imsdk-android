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

public class ThumbnailPackedImageMessage extends MediaMessageContent {

    public ThumbnailPackedImageMessage() {
        this.mContentType = "jg:tpimg";
    }

    public static ThumbnailPackedImageMessage messageWithImage(String imagePath) {
        ThumbnailPackedImageMessage message = new ThumbnailPackedImageMessage();
        message.setLocalPath(imagePath);
        if (imagePath != null) {
            Bitmap image = BitmapFactory.decodeFile(imagePath);
            message.setOriginalImage(image);
            message.setThumbnailImage(generateThumbnail(image));
        }
        return message;
    }

    public static ThumbnailPackedImageMessage messageWithImage(Bitmap image) {
        ThumbnailPackedImageMessage message = new ThumbnailPackedImageMessage();
        message.setOriginalImage(image);
        message.setThumbnailImage(generateThumbnail(image));
        return message;
    }

    public static Bitmap generateThumbnail(Bitmap original) {
        return JUtility.generateThumbnail(original, ConstInternal.THUMBNAIL_WIDTH, ConstInternal.THUMBNAIL_HEIGHT);
    }

    @Override
    public byte[] encode() {
        String thumbnailString = "";
        if (mThumbnailImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mThumbnailImage.compress(Bitmap.CompressFormat.JPEG, ConstInternal.THUMBNAIL_QUALITY, byteArrayOutputStream);
            byte[] thumbnailData = byteArrayOutputStream.toByteArray();
            thumbnailString = JUtility.base64EncodedStringFrom(thumbnailData);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(getUrl())) {
                jsonObject.put(URL, getUrl());
            }
            if (!TextUtils.isEmpty(getUrl())) {
                jsonObject.put(LOCAL, getLocalPath());
            }
            if (!TextUtils.isEmpty(thumbnailString)) {
                jsonObject.put(THUMBNAIL, thumbnailString);
            }
            jsonObject.put(HEIGHT, mHeight);
            jsonObject.put(WIDTH, mWidth);
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
            jsonObject.put(SIZE, mSize);
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "ThumbnailPackedImageMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "ThumbnailPackedImageMessage decode data is null");
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
            if (jsonObject.has(THUMBNAIL)) {
                String thumbnailString = jsonObject.optString(THUMBNAIL);
                byte[] thumbnailData = JUtility.dataWithBase64EncodedString(thumbnailString);
                mThumbnailImage = BitmapFactory.decodeByteArray(thumbnailData, 0, thumbnailData.length);
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
            JLogger.e("MSG-Decode", "ThumbnailPackedImageMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public Bitmap getThumbnailImage() {
        return mThumbnailImage;
    }

    public void setThumbnailImage(Bitmap thumbnailImage) {
        this.mThumbnailImage = thumbnailImage;
    }

    public Bitmap getOriginalImage() {
        return mOriginalImage;
    }

    public void setOriginalImage(Bitmap originalImage) {
        this.mOriginalImage = originalImage;
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

    private Bitmap mThumbnailImage;
    private Bitmap mOriginalImage;
    private int mHeight;
    private int mWidth;
    private String mExtra;
    private long mSize;
    private static final String URL = "imageUri";
    private static final String LOCAL = "local";
    private static final String THUMBNAIL = "content";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String EXTRA = "extra";
    private static final String SIZE = "size";
    private static final String DIGEST = "[Image]";
}
