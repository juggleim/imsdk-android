package com.juggle.im.internal.model.messages;

import android.text.TextUtils;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TagAddConvMessage extends MessageContent {
    public TagAddConvMessage() {
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
            JLogger.e("MSG-Decode", "TagAddConvMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(TAG)) {
                mTagId = jsonObject.optString(TAG);
            }
            if (jsonObject.has(TAG_NAME)) {
                mTagName = jsonObject.optString(TAG_NAME);
            }
            if (jsonObject.has(CONVERS)) {
                List<Conversation> conversations = new ArrayList<>();
                JSONArray jsonArray = jsonObject.optJSONArray(CONVERS);
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
                    String subChannel = object.optString(SUB_CHANNEL);
                    if (!TextUtils.isEmpty(subChannel)) {
                        c.setSubChannel(subChannel);
                    }
                    conversations.add(c);
                }
                mConversations = conversations;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "TagAddConvMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public String getTagId() {
        return mTagId;
    }

    public String getTagName() {
        return mTagName;
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    public static final String CONTENT_TYPE = "jg:tagaddconvers";
    private String mTagId;
    private String mTagName;
    private List<Conversation> mConversations;

    private static final String TAG = "tag";
    private static final String TAG_NAME = "tag_name";
    private static final String CONVERS = "convers";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String TARGET_ID = "target_id";
    private static final String SUB_CHANNEL = "sub_channel";
}
