package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TextMessage extends MessageContent {

    public TextMessage(String content) {
        this();
        this.mContent = content;
    }

    public TextMessage() {
        this.mContentType = "jg:text";
    }


    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mContent)) {
                jsonObject.put(CONTENT, mContent);
            }
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "TextMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "TextMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONTENT)) {
                mContent = jsonObject.optString(CONTENT);
            }
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "TextMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        if (!TextUtils.isEmpty(mContent)) {
            return mContent;
        }
        return "";
    }

    public String getContent() {
        return mContent;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    @Override
    public String getSearchContent() {
        return TextUtils.isEmpty(mContent) ? "" : mContent;
    }

    private String mContent;
    private String mExtra;
    private static final String CONTENT = "content";
    private static final String EXTRA = "extra";
}
