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

public class UnDisturbConvMessage extends MessageContent {
    public UnDisturbConvMessage() {
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
            JLogger.e("MSG-Decode", "UnDisturbConvMessage decode data is null");
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
                    int unDisturbType = object.optInt(UN_DISTURB_TYPE);
                    String conversationId = object.optString(TARGET_ID);

                    ConcreteConversationInfo conversation = new ConcreteConversationInfo();
                    conversation.setConversation(new Conversation(Conversation.ConversationType.setValue(type), conversationId));
                    conversation.setMute(unDisturbType == 1);

                    conversations.add(conversation);
                }
            }
            mConversations = conversations;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "UnDisturbConvMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:undisturb";

    public List<ConcreteConversationInfo> getConversations() {
        return mConversations;
    }

    private List<ConcreteConversationInfo> mConversations;

    private static final String CONVERSATIONS = "conversations";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String UN_DISTURB_TYPE = "undisturb_type";
}
