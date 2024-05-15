package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ClearHistoryMessage extends MessageContent {
    public ClearHistoryMessage() {
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
            LoggerUtils.e("ClearHistoryMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        LoggerUtils.i("ClearHistoryMsgMessage decode data= " + jsonStr);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CLEAN_TIME)) {
                cleanTime = jsonObject.optLong(CLEAN_TIME);
            }
        } catch (JSONException e) {
            LoggerUtils.e("ClearHistoryMsgMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private long cleanTime;

    public long getCleanTime() {
        return cleanTime;
    }

    public static final String CONTENT_TYPE = "jg:cleanmsg";
    private static final String CLEAN_TIME = "clean_time";
}
