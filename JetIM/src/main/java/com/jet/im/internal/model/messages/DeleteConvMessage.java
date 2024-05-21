package com.jet.im.internal.model.messages;

import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;
import com.jet.im.internal.util.LoggerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeleteConvMessage extends MessageContent {
    public DeleteConvMessage() {
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
            LoggerUtils.e("DeleteConvMessage decode data is null");
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
            LoggerUtils.e("DeleteConvMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:delconvers";

    public List<Conversation> getConversations() {
        return mConversations;
    }

    private List<Conversation> mConversations;

    private static final String CONVERSATIONS = "conversations";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
}
