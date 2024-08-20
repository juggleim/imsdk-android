package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClearUnreadMessage extends MessageContent {
    public ClearUnreadMessage() {
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
            JLogger.e("MSG-Decode", "ClearUnreadMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        try {
            List<ConcreteConversationInfo> conversations = new ArrayList<>();
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
                    int lastReadIndex = object.optInt(LATEST_READ_INDEX);
                    String conversationId = object.optString(TARGET_ID);

                    ConcreteConversationInfo conversation = new ConcreteConversationInfo();
                    conversation.setConversation(new Conversation(Conversation.ConversationType.setValue(type), conversationId));
                    conversation.setLastReadMessageIndex(lastReadIndex);

                    conversations.add(conversation);
                }
            }
            mConversations = conversations;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "ClearUnreadMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:clearunread";

    public List<ConcreteConversationInfo> getConversations() {
        return mConversations;
    }

    private List<ConcreteConversationInfo> mConversations;

    private static final String CONVERSATIONS = "conversations";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String LATEST_READ_INDEX = "latest_read_index";
}
