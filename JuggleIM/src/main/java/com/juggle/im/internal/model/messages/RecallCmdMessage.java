package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

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
        //不会往外发，也不存本地
        return new byte[0];
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "RecallCmdMessage decode data is null");
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
            JLogger.e("MSG-Decode", "RecallCmdMessage decode JSONException " + e.getMessage());
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
                JLogger.e("MSG-Decode", "RecallCmdMessage decodeExt JSONException " + e.getMessage());
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

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public static final String CONTENT_TYPE = "jg:recall";

    private String mOriginalMessageId;
    private long mOriginalMessageTime;
    private Map<String, String> mExtra;

    private static final String MSG_TIME = "msg_time";
    private static final String MSG_ID = "msg_id";
    private static final String MSG_EXT = "exts";
}
