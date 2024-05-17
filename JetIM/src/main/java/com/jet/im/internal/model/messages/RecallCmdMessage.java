package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecallCmdMessage extends MessageContent {
    public RecallCmdMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        //不会往外发
        return new byte[0];
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("RecallCmdMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(MSG_TIME)) {
                mOriginalMessageTime = jsonObject.optLong(MSG_TIME);
            }
            if (jsonObject.has(MSG_ID)) {
                mOriginalMessageId = jsonObject.optString(MSG_ID);
            }
            decodeExt(jsonObject);
        } catch (JSONException e) {
            LoggerUtils.e("RecallCmdMessage decode JSONException " + e.getMessage());
        }
    }

    private void decodeExt(JSONObject jsonObject) {
        if (!jsonObject.has(MSG_EXT)) {
            return;
        }
        JSONObject extJsonObject = jsonObject.optJSONObject(MSG_EXT);
        if (extJsonObject == null) return;

        mExtra = new HashMap<>();
        for (Iterator<String> it = extJsonObject.keys(); it.hasNext(); ) {
            try {
                String key = it.next();
                String value = extJsonObject.getString(key);
                mExtra.put(key, value);
            } catch (JSONException e) {
                LoggerUtils.e("RecallCmdMessage decodeExt JSONException " + e.getMessage());
            }
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public String getOriginalMessageId() {
        return mOriginalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        mOriginalMessageId = originalMessageId;
    }

    public long getOriginalMessageTime() {
        return mOriginalMessageTime;
    }

    public void setOriginalMessageTime(long originalMessageTime) {
        mOriginalMessageTime = originalMessageTime;
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public void setExtra(Map<String, String> extra) {
        mExtra = extra;
    }

    public static final String CONTENT_TYPE = "jg:recall";

    private String mOriginalMessageId;
    private long mOriginalMessageTime;
    private Map<String, String> mExtra;

    private static final String MSG_TIME = "msg_time";
    private static final String MSG_ID = "msg_id";
    private static final String MSG_EXT = "exts";
}
