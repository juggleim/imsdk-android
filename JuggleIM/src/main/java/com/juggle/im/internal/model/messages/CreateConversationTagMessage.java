package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.ConversationTagInfo;
import com.juggle.im.model.MessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CreateConversationTagMessage extends MessageContent {
    public CreateConversationTagMessage() {
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
            JLogger.e("MSG-Decode", "CreateConversationTagMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(TAGS)) {
                List<ConversationTagInfo> tagList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.optJSONArray(TAGS);
                if (jsonArray == null) {
                    return;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    if (object == null) {
                        continue;
                    }
                    ConversationTagInfo info = new ConversationTagInfo();
                    info.setTagId(object.optString(TAG));
                    info.setName(object.optString(TAG_NAME));
                    info.setType(ConversationTagInfo.TagType.USER);
                    tagList.add(info);
                }
                mTagList = tagList;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "CreateConversationTagMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public List<ConversationTagInfo> getTagList() {
        return mTagList;
    }

    private List<ConversationTagInfo> mTagList;
    private Boolean mIsAdd;
    public static final String CONTENT_TYPE = "jg:createconvertags";
    private static final String TAGS = "tags";
    private static final String TAG = "tag";
    private static final String TAG_NAME = "tag_name";
}
