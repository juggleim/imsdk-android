package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MsgExSetMessage extends MessageContent {
    public MsgExSetMessage() {
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
            JLogger.e("MSG-Decode", "MsgExSetMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            mOriginalMessageId = jsonObject.optString(MSG_ID);
            List<MessageReactionItem> add = new ArrayList<>();
            List<MessageReactionItem> remove = new ArrayList<>();
            JSONArray jsonArray = jsonObject.optJSONArray(EXTS);
            if (jsonArray == null) {
                return;
            }
            boolean isUpdate = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                int isDelete = object.optInt(IS_DEL);
                String keyJson = object.optString(KEY);
                JSONObject userObject = object.optJSONObject(USER);
                if (userObject == null) {
                    continue;
                }
                UserInfo user = userInfoFromJson(userObject);;
                isUpdate = false;
                if (isDelete != 0) {
                    for (MessageReactionItem loopItem : remove) {
                        if (loopItem.getReactionId().equals(keyJson)) {
                            isUpdate = true;
                            List<UserInfo> userInfoList = loopItem.getUserInfoList();
                            userInfoList.add(user);
                            loopItem.setUserInfoList(userInfoList);
                            break;
                        }
                    }
                    if (!isUpdate) {
                        MessageReactionItem reactionItem = new MessageReactionItem();
                        reactionItem.setReactionId(keyJson);
                        reactionItem.setUserInfoList(Collections.singletonList(user));
                        remove.add(reactionItem);
                    }
                } else {
                    for (MessageReactionItem loopItem : add) {
                        if (loopItem.getReactionId().equals(keyJson)) {
                            isUpdate = true;
                            List<UserInfo> userInfoList = loopItem.getUserInfoList();
                            userInfoList.add(user);
                            loopItem.setUserInfoList(userInfoList);
                            break;
                        }
                    }
                    if (!isUpdate) {
                        MessageReactionItem reactionItem = new MessageReactionItem();
                        reactionItem.setReactionId(keyJson);
                        reactionItem.setUserInfoList(Collections.singletonList(user));
                        add.add(reactionItem);
                    }
                }
            }
            mAddItemList = add;
            mRemoveItemList = remove;
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "MsgExSetMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_STATUS.getValue();
    }

    public String getOriginalMessageId() {
        return mOriginalMessageId;
    }

    public List<MessageReactionItem> getAddItemList() {
        return mAddItemList;
    }

    public List<MessageReactionItem> getRemoveItemList() {
        return mRemoveItemList;
    }

    private UserInfo userInfoFromJson(JSONObject object) {
        UserInfo user = new UserInfo();
        user.setUserId(object.optString(USER_ID));
        user.setUserName(object.optString(NICKNAME));
        user.setPortrait(object.optString(USER_PORTRAIT));
        return user;
    }

    private String mOriginalMessageId;
    private List<MessageReactionItem> mAddItemList;
    private List<MessageReactionItem> mRemoveItemList;

    public static final String CONTENT_TYPE = "jg:msgexset";
    private static final String MSG_ID = "msg_id";
    private static final String EXTS = "exts";
    private static final String IS_DEL = "is_del";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String TIMESTAMP = "timestamp";
    private static final String USER = "user";
    private static final String USER_ID = "user_id";
    private static final String NICKNAME = "nickname";
    private static final String USER_PORTRAIT = "user_portrait";
}
