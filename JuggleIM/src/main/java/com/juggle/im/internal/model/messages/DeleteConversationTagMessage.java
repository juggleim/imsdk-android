package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeleteConversationTagMessage extends MessageContent {
    public DeleteConversationTagMessage() {
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
            JLogger.e("MSG-Decode", "DeleteConversationTagMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(TAGS)) {
                List<String> tagIdList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.optJSONArray(TAGS);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    String tagId = object.optString(TAG);
                    tagIdList.add(tagId);
                }
                mTagIdList = tagIdList;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "DeleteConversationTagMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public List<String> getTagIdList() {
        return mTagIdList;
    }

    private List<String> mTagIdList;

    public static final String CONTENT_TYPE = "jg:delconvertags";
    private static final String TAGS = "tags";
    private static final String TAG = "tag";
}
