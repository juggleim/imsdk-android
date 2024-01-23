package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class VoiceMessage extends MessageContent {
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
        } catch (JSONException e) {
            LoggerUtils.e("VoiceMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("VoiceMessage decode data is null");
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
        } catch (JSONException e) {
            LoggerUtils.e("VoiceMessage decode JSONException " + e.getMessage());
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

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    private String mUrl;
    private int mDuration;
    private static final String URL = "url";
    private static final String DURATION = "duration";
    private static final String DIGEST = "[voice]";
}