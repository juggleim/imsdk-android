package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.ContentTypeCenter;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MsgModifyMessage extends MessageContent {
    public MsgModifyMessage() {
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
            JLogger.e("MSG-Decode", "MsgModifyMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            mOriginalMessageId = jsonObject.optString(MSG_ID);
            mOriginalMessageTime = jsonObject.optLong(MSG_TIME);
            mMessageType = jsonObject.optString(MSG_TYPE);
            String contentString = jsonObject.optString(MSG_CONTENT);

            mMessageContent = ContentTypeCenter.getInstance().getContent(JUtility.dataWithBase64EncodedString(contentString), mMessageType);
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "MsgModifyMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public String getOriginalMessageId() {
        return mOriginalMessageId;
    }

    public long getOriginalMessageTime() {
        return mOriginalMessageTime;
    }

    public String getMessageType() {
        return mMessageType;
    }

    public MessageContent getMessageContent() {
        return mMessageContent;
    }

    public static final String CONTENT_TYPE = "jg:modify";

    private String mOriginalMessageId;
    private long mOriginalMessageTime;
    private String mMessageType;
    private MessageContent mMessageContent;

    private static final String MSG_TIME = "msg_time";
    private static final String MSG_TYPE = "msg_type";
    private static final String MSG_ID = "msg_id";
    private static final String MSG_CONTENT = "msg_content";

}
