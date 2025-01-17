package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MarkUnreadMessage extends MessageContent {
    public MarkUnreadMessage() {
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
            JLogger.e("MSG-Decode", "MarkUnreadMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        List<Conversation> conversations = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONVERSATIONS)) {
                JSONArray jsonArray = jsonObject.optJSONArray(CONVERSATIONS);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    int type = object.optInt(CHANNEL_TYPE);
                    String conversationId = object.optString(TARGET_ID);
                    Conversation c = new Conversation(Conversation.ConversationType.setValue(type), conversationId);
                    conversations.add(c);
                }
            }
            mConversations = conversations;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "MarkUnreadMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:markunread";

    public List<Conversation> getConversations() {
        return mConversations;
    }

    private List<Conversation> mConversations;

    private static final String CONVERSATIONS = "conversations";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
}
