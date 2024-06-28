package com.jet.im.internal.model.messages;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ClearTotalUnreadMessage extends MessageContent {
    public ClearTotalUnreadMessage() {
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
            JLogger.e("MSG-Decode", "ClearTotalUnreadMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CLEAR_TIME)) {
                mClearTime = jsonObject.optLong(CLEAR_TIME);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "ClearTotalUnreadMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private long mClearTime;

    public long getClearTime() {
        return mClearTime;
    }

    public static final String CONTENT_TYPE = "jg:cleartotalunread";
    private static final String CLEAR_TIME = "clear_time";
}
