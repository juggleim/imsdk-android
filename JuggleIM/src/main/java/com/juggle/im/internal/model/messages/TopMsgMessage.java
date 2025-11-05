package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TopMsgMessage extends MessageContent {
    public TopMsgMessage() {
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
            JLogger.e("MSG-Decode", "TopMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int action = jsonObject.optInt(ACTION);
            mIsTop = action == 0;
            mMessageId = jsonObject.optString(MESSAGE_ID);
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "TopMsgMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public static final String CONTENT_TYPE = "jg:topmsg";

    public boolean isTop() {
        return mIsTop;
    }

    public String getMessageId() {
        return mMessageId;
    }

    private boolean mIsTop;
    private String mMessageId;

    private static final String ACTION = "action";
    private static final String MESSAGE_ID = "msg_id";
}
