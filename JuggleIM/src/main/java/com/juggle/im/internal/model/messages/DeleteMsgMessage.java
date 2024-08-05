package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeleteMsgMessage extends MessageContent {
    public DeleteMsgMessage() {
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
            JLogger.e("MSG-Decode", "DeleteMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        try {
            List<String> msgIdList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(MSG_LIST)) {
                JSONArray jsonArray = jsonObject.optJSONArray(MSG_LIST);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    String conversationId = object.optString(MSG_ID);
                    msgIdList.add(conversationId);
                }
            }
            mMsgIdList = msgIdList;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "DeleteMsgMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    private List<String> mMsgIdList;

    public List<String> getMsgIdList() {
        return mMsgIdList;
    }

    public static final String CONTENT_TYPE = "jg:delmsgs";
    private static final String MSG_LIST = "msgs";
    private static final String MSG_ID = "msg_id";
}
