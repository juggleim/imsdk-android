package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.internal.util.JLogger;

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
            JLogger.e("CleanMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        JLogger.d("ClearHistoryMsgMessage decode data= " + jsonStr);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CLEAN_TIME)) {
                mCleanTime = jsonObject.optLong(CLEAN_TIME);
            }
            if (jsonObject.has(SENDER_ID)) {
                mSenderId = jsonObject.optString(SENDER_ID);
            }
        } catch (JSONException e) {
            JLogger.e("CleanMsgMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private long mCleanTime;
    private String mSenderId;

    public long getCleanTime() {
        return mCleanTime;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public static final String CONTENT_TYPE = "jg:cleanmsg";
    private static final String CLEAN_TIME = "clean_time";
    private static final String SENDER_ID = "sender_id";
}
