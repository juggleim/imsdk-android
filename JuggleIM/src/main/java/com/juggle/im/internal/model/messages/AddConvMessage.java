package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AddConvMessage extends MessageContent {
    public AddConvMessage() {
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
            JLogger.e("MSG-Decode", "AddConvMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONVERSATION)) {
                JSONObject convObj = jsonObject.optJSONObject(CONVERSATION);
                if (convObj == null) {
                    return;
                }

                ConcreteConversationInfo conversationInfo = new ConcreteConversationInfo();
                if (convObj.has(CHANNEL_TYPE) && convObj.has(TARGET_ID)) {
                    int type = convObj.optInt(CHANNEL_TYPE);
                    String conversationId = convObj.optString(TARGET_ID);
                    conversationInfo.setConversation(new Conversation(Conversation.ConversationType.setValue(type), conversationId));
                }
                if (convObj.has(SORT_TIME)) {
                    conversationInfo.setSortTime(convObj.optLong(SORT_TIME));
                }
                if (convObj.has(SYNC_TIME)) {
                    conversationInfo.setSyncTime(convObj.optLong(SYNC_TIME));
                }
                if (convObj.has(TARGET_USER_INFO)) {
                    conversationInfo.setTargetUserInfo(decodeUserInfo(convObj.optJSONObject(TARGET_USER_INFO)));
                }
                if (convObj.has(GROUP_INFO)) {
                    conversationInfo.setGroupInfo(decodeGroupInfo(convObj.optJSONObject(GROUP_INFO)));
                }

                mConversationInfo = conversationInfo;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "AddConvMessage decode JSONException " + e.getMessage());
        }
    }

    private UserInfo decodeUserInfo(JSONObject userInfoObj) {
        if (userInfoObj == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        if (userInfoObj.has(USER_ID)) {
            userInfo.setUserId(userInfoObj.optString(USER_ID));
        }
        if (userInfoObj.has(USER_NAME)) {
            userInfo.setUserName(userInfoObj.optString(USER_NAME));
        }
        if (userInfoObj.has(USER_PORTRAIT)) {
            userInfo.setPortrait(userInfoObj.optString(USER_PORTRAIT));
        }
        if (userInfoObj.has(EXT_FIELDS)) {
            userInfo.setExtra(decodeExtFields(userInfoObj.optJSONObject(EXT_FIELDS)));
        }
        return userInfo;
    }

    private GroupInfo decodeGroupInfo(JSONObject groupInfoObj) {
        if (groupInfoObj == null) {
            return null;
        }
        GroupInfo groupInfo = new GroupInfo();
        if (groupInfoObj.has(GROUP_ID)) {
            groupInfo.setGroupId(groupInfoObj.optString(GROUP_ID));
        }
        if (groupInfoObj.has(GROUP_NAME)) {
            groupInfo.setGroupName(groupInfoObj.optString(GROUP_NAME));
        }
        if (groupInfoObj.has(GROUP_PORTRAIT)) {
            groupInfo.setPortrait(groupInfoObj.optString(GROUP_PORTRAIT));
        }
        if (groupInfoObj.has(EXT_FIELDS)) {
            groupInfo.setExtra(decodeExtFields(groupInfoObj.optJSONObject(EXT_FIELDS)));
        }
        return groupInfo;
    }

    private Map<String, String> decodeExtFields(JSONObject extObj) {
        if (extObj == null) {
            return null;
        }
        Map<String, String> extFields = new HashMap<>();
        for (Iterator<String> it = extObj.keys(); it.hasNext(); ) {
            try {
                String key = it.next();
                String value = extObj.getString(key);
                extFields.put(key, value);
            } catch (JSONException e) {
                JLogger.e("MSG-Decode", "AddConvMessage decodeExt JSONException " + e.getMessage());
            }
        }
        return extFields;
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:addconver";

    public ConcreteConversationInfo getConversationInfo() {
        return mConversationInfo;
    }

    private ConcreteConversationInfo mConversationInfo;

    private static final String CONVERSATION = "conversation";
    private static final String TARGET_ID = "target_id";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String SORT_TIME = "sort_time";
    private static final String SYNC_TIME = "sync_time";
    private static final String TARGET_USER_INFO = "target_user_info";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "nickname";
    private static final String USER_PORTRAIT = "user_portrait";
    private static final String EXT_FIELDS = "ext_fields";
    private static final String GROUP_INFO = "group_info";
    private static final String GROUP_ID = "group_id";
    private static final String GROUP_NAME = "group_name";
    private static final String GROUP_PORTRAIT = "group_portrait";
}
