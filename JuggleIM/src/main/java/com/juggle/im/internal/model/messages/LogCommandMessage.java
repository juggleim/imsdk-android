package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

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
            if (jsonObject.has(PLATFORM)) {
                mPlatform = jsonObject.optString(PLATFORM);
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
    private String mPlatform;

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public static final String CONTENT_TYPE = "jg:logcmd";
    private static final String START_TIME = "start";
    private static final String END_TIME = "end";
    private static final String PLATFORM = "platform";
}
