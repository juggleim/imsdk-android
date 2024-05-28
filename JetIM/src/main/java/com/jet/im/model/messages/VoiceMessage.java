package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.upload.UploadFileType;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class VoiceMessage extends MediaMessageContent {
    public VoiceMessage() {
        mContentType = "jg:voice";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mUrl)) {
                jsonObject.put(URL, mUrl);
            }
            jsonObject.put(DURATION, mDuration);
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
        } catch (JSONException e) {
            JLogger.e("VoiceMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("VoiceMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(URL)) {
                mUrl = jsonObject.optString(URL);
            }
            if (jsonObject.has(DURATION)) {
                mDuration = jsonObject.optInt(DURATION);
            }
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
        } catch (JSONException e) {
            JLogger.e("VoiceMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    @Override
    public UploadFileType getUploadFileType() {
        return UploadFileType.AUDIO;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
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

    private String mUrl;
    private int mDuration;
    private String mExtra;
    private static final String URL = "url";
    private static final String DURATION = "duration";
    private static final String EXTRA = "extra";
    private static final String DIGEST = "[Voice]";
}
