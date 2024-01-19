package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TextMessage extends MessageContent {

    public TextMessage(String content) {
        this.mContent = content;
    }
    @Override
    public String getContentType() {
        return "jg:text";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mContent)) {
                jsonObject.put(CONTENT, mContent);
            }
        } catch (JSONException e) {
            LoggerUtils.e("TextMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("TextMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONTENT)) {
                mContent = jsonObject.optString(CONTENT);
            }
        } catch (JSONException e) {
            LoggerUtils.e("TextMessage decode JSONException " + e.getMessage());
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

    private String mContent;
    private static final String CONTENT = "content";
}
