package com.jet.im.internal.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.internal.util.JLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReadNtfMessage  extends MessageContent {
    public ReadNtfMessage() {
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
            JLogger.e("ReadNtfMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        List<String> messageIds = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(MSGS)) {
                JSONArray jsonArray = jsonObject.optJSONArray(MSGS);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    String messageId = object.optString(MSG_ID);
                    messageIds.add(messageId);
                }
            }
            mMessageIds = messageIds;
        } catch (JSONException e) {
            JLogger.e("ReadNtfMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }


    public static final String CONTENT_TYPE = "jg:readntf";

    public List<String> getMessageIds() {
        return mMessageIds;
    }

    private List<String> mMessageIds;

    private static final String MSGS = "msgs";
    private static final String MSG_ID = "msg_id";
}
