package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GroupReadNtfMessage extends MessageContent {

    public GroupReadNtfMessage() {
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
            JLogger.e("MSG-Decode", "GroupReadNtfMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        Map<String, GroupMessageReadInfo> messages = new HashMap<>();

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(MSGS)) {
                JSONArray jsonArray = jsonObject.optJSONArray(MSGS);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        return;
                    }
                    String messageId = null;
                    if (object.has(MSG_ID)) {
                        messageId = object.optString(MSG_ID);
                    }
                    if (messageId == null) {
                        return;
                    }
                    GroupMessageReadInfo info = new GroupMessageReadInfo();
                    if (object.has(READ_COUNT)) {
                        info.setReadCount(object.optInt(READ_COUNT));
                    }
                    if (object.has(MEMBER_COUNT)) {
                        info.setMemberCount(object.optInt(MEMBER_COUNT));
                    }
                    messages.put(messageId, info);
                }
                mMessages = messages;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "GroupReadNtfMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public static final String CONTENT_TYPE = "jg:grpreadntf";

    public Map<String, GroupMessageReadInfo> getMessages() {
        return mMessages;
    }

    private Map<String, GroupMessageReadInfo> mMessages;

    private static final String MSGS = "msgs";
    private static final String MSG_ID = "msg_id";
    private static final String READ_COUNT = "read_count";
    private static final String MEMBER_COUNT = "member_count";
}
