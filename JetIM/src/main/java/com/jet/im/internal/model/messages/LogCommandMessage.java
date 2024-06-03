package com.jet.im.internal.model.messages;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LogCommandMessage extends MessageContent {
    public LogCommandMessage() {
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
            JLogger.e("MSG-Decode", "LogCommandMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(START_TIME)) {
                mStartTime = jsonObject.optLong(START_TIME);
            }
            if (jsonObject.has(END_TIME)) {
                mEndTime = jsonObject.optLong(END_TIME);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "LogCommandMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private long mStartTime;
    private long mEndTime;

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public static final String CONTENT_TYPE = "jg:logcmd";
    private static final String START_TIME = "start";
    private static final String END_TIME = "end";
}
