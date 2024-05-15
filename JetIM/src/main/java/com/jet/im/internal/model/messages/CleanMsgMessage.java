package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class CleanMsgMessage extends MessageContent {
    public CleanMsgMessage() {
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
            LoggerUtils.e("CleanMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        LoggerUtils.d("ClearHistoryMsgMessage decode data= " + jsonStr);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CLEAN_TIME)) {
                mCleanTime = jsonObject.optLong(CLEAN_TIME);
            }
        } catch (JSONException e) {
            LoggerUtils.e("CleanMsgMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private long mCleanTime;

    public long getCleanTime() {
        return mCleanTime;
    }

    public static final String CONTENT_TYPE = "jg:cleanmsg";
    private static final String CLEAN_TIME = "clean_time";
}
