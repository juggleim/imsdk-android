package com.jet.im.internal.model.messages;

import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class RecallCmdMessage extends MessageContent {
    public RecallCmdMessage() {
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
            LoggerUtils.e("RecallCmdMessage decode data is null");
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
            if (jsonObject.has(SENDER_ID)) {
                mSenderId = jsonObject.optString(SENDER_ID);
            }
            if (jsonObject.has(RECEIVER_ID)) {
                mReceiverId = jsonObject.optString(RECEIVER_ID);
            }
            if (jsonObject.has(CHANNEL_TYPE)) {
                int type = jsonObject.optInt(CHANNEL_TYPE);
                mConversationType = Conversation.ConversationType.setValue(type);
            }
        } catch (JSONException e) {
            LoggerUtils.e("RecallCmdMessage decode JSONException " + e.getMessage());
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

    public void setOriginalMessageTime(long originalMessageTime) {
        mOriginalMessageTime = originalMessageTime;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    public String getReceiverId() {
        return mReceiverId;
    }

    public void setReceiverId(String receiverId) {
        mReceiverId = receiverId;
    }

    public Conversation.ConversationType getConversationType() {
        return mConversationType;
    }

    public void setConversationType(Conversation.ConversationType conversationType) {
        mConversationType = conversationType;
    }

    public static final String CONTENT_TYPE = "jg:recall";

    private String mOriginalMessageId;
    private long mOriginalMessageTime;
    private String mSenderId;
    private String mReceiverId;//原消息的接收者 id，群聊时为 groupId
    private Conversation.ConversationType mConversationType;

    private static final String MSG_TIME = "msg_time";
    private static final String MSG_ID = "msg_id";
    private static final String SENDER_ID = "sender_id";
    private static final String RECEIVER_ID = "receiver_id";
    private static final String CHANNEL_TYPE = "channel_type";
}
