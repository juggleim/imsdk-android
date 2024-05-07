package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class RecallInfoMessage extends MessageContent {
    public RecallInfoMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mExtra)) {
                jsonObject.put(EXTRA, mExtra);
            }
        } catch (JSONException e) {
            LoggerUtils.e("RecallInfoMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("RecallInfoMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(EXTRA)) {
                mExtra = jsonObject.optString(EXTRA);
            }
        } catch (JSONException e) {
            LoggerUtils.e("RecallCmdMessage decode JSONException " + e.getMessage());
        }
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }
    public static final String CONTENT_TYPE = "jg:recallinfo";

    private String mExtra;

    private static final String EXTRA = "extra";

}
