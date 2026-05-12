package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class UserStatusChangeMessage extends MessageContent {
    public UserStatusChangeMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        //不会往外发，也不入库
        return new byte[0];
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "UserStatusChangeMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            mIsOnline = jsonObject.optBoolean("is_online");
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "UserStatusChangeMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_STATUS.getValue();
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public static final String CONTENT_TYPE = "jg:onlinechg";
    private boolean mIsOnline;
}
