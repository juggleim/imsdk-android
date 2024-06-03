package com.jet.im.internal.model.messages;

import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TopConvMessage extends MessageContent {
    public TopConvMessage() {
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
            JLogger.e("MSG-Decode", "TopConvMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        List<ConcreteConversationInfo> conversations = new ArrayList<>();
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
                    long topUpdatedTime = object.getLong(TOP_UPDATED_TIME);
                    boolean isTop = object.optBoolean(IS_TOP);
                    String conversationId = object.optString(TARGET_ID);

                    ConcreteConversationInfo conversation = new ConcreteConversationInfo();
                    conversation.setConversation(new Conversation(Conversation.ConversationType.setValue(type), conversationId));
                    conversation.setTop(isTop);
                    conversation.setTopTime(topUpdatedTime);

                    conversations.add(conversation);
                }
            }
            mConversations = conversations;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "TopConvMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:topconvers";

    public List<ConcreteConversationInfo> getConversations() {
        return mConversations;
    }

    private List<ConcreteConversationInfo> mConversations;

    private static final String CONVERSATIONS = "conversations";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String IS_TOP = "is_top";
    private static final String TOP_UPDATED_TIME = "top_update_time";
}
