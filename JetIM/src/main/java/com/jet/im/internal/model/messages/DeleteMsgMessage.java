package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.internal.util.JLogger;

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
            JLogger.e("DeleteMsgMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        JLogger.d("DeleteMsgMessage decode data= " + jsonStr);

        List<String> msgIdList = new ArrayList<>();
        try {
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
            JLogger.e("DeleteMsgMessage decode JSONException " + e.getMessage());
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
