package com.jet.im.kit.model.message;

import android.util.Log;

import com.jet.im.kit.utils.TextUtils;
import com.juggle.im.JIM;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GroupNotifyMessage extends MessageContent {
    public GroupNotifyMessage() {
        mContentType = "jgd:grpntf";
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_SAVE.getValue();
    }

    @Override
    public byte[] encode() {
        JSONArray membersJson = new JSONArray();
        for (UserInfo userInfo : mMembers) {
            JSONObject member = jsonFromUserInfo(userInfo);
            membersJson.put(member);
        }
        JSONObject content = new JSONObject();
        try {
            content.put(MEMBERS, membersJson);
            content.put(TYPE, mType.getValue());
            content.put(NAME, mName);
            if (mOperator != null) {
                JSONObject operatorJson = jsonFromUserInfo(mOperator);
                content.put(OPERATOR, operatorJson);
            }
        } catch (JSONException e) {
            Log.e("GroupNotifyMessage", "encode JSONException " + e.getMessage());
        }
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            Log.e("GroupNotifyMessage", "decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            mType = GroupNotifyType.from(jsonObject.optInt(TYPE));
            JSONArray membersJson = jsonObject.optJSONArray(MEMBERS);
            if (membersJson != null) {
                List<UserInfo> members = new ArrayList<>();
                for (int i = 0; i < membersJson.length(); i++) {
                    UserInfo member = userInfoFromJson(membersJson.getJSONObject(i));
                    members.add(member);
                }
                mMembers = members;
            }
            JSONObject operatorJson = jsonObject.optJSONObject(OPERATOR);
            if (operatorJson != null) {
                mOperator = userInfoFromJson(operatorJson);
            }
            mName = jsonObject.optString(NAME);
        } catch (JSONException e) {
            Log.e("GroupNotifyMessage", "decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public String description() {
        boolean isSender = TextUtils.isNotEmpty(mOperator.getUserId())
                && mOperator.getUserId().equals(JIM.getInstance().getCurrentUserId());
        String sender = isSender ? "你" : mOperator.getUserName();
        StringBuilder userList = new StringBuilder();
        for (UserInfo member : mMembers) {
            userList.append(member.getUserName()).append(", ");
        }
        String newOwner = "";
        boolean isOwner = false;
        if (mType == GroupNotifyType.CHANGE_OWNER) {
            if (!mMembers.isEmpty()) {
                UserInfo member = mMembers.get(0);
                if (member.getUserId().equals(JIM.getInstance().getCurrentUserId())) {
                    isOwner = true;
                }
                newOwner = isOwner ? "你" : member.getUserName();
            }
        }

        int l = userList.length();
        if (l > 2) {
            userList.delete(l-2, l-1);
        }
        String ul = userList.toString();
        switch (mType) {
            case ADD_MEMBER:
                return sender + " 邀请 " + ul + " 加入群聊";
            case REMOVE_MEMBER:
                return sender + " 将 " + ul + " 移除群聊";
            case RENAME:
                return sender + " 修改群名称为 " + mName;
            case CHANGE_OWNER:
                return newOwner + " 已成为新群主";
            case JOIN:
                return sender + " 加入群聊";
            default:
                return "";
        }
    }

    public enum GroupNotifyType {
        OTHER(0),
        ADD_MEMBER(1),
        REMOVE_MEMBER(2),
        RENAME(3),
        CHANGE_OWNER(4),
        JOIN(5);

        final int mValue;

        GroupNotifyType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static GroupNotifyType from(int value) {
            for (GroupNotifyType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    public GroupNotifyType getType() {
        return mType;
    }

    public void setType(GroupNotifyType type) {
        mType = type;
    }

    public List<UserInfo> getMembers() {
        return mMembers;
    }

    public void setMembers(List<UserInfo> members) {
        mMembers = members;
    }

    public UserInfo getOperator() {
        return mOperator;
    }

    public void setOperator(UserInfo operator) {
        mOperator = operator;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    private UserInfo userInfoFromJson(JSONObject jsonObject) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(jsonObject.optString(USER_ID));
        userInfo.setUserName(jsonObject.optString(NICKNAME));
        userInfo.setPortrait(jsonObject.optString(AVATAR));
        return userInfo;
    }

    private JSONObject jsonFromUserInfo(UserInfo userInfo) {
        JSONObject result = new JSONObject();
        try {
            result.put(USER_ID, userInfo.getUserId());
            if (TextUtils.isNotEmpty(userInfo.getUserName())) {
                result.put(NICKNAME, userInfo.getUserName());
            }
            if (TextUtils.isNotEmpty(userInfo.getPortrait())) {
                result.put(AVATAR, userInfo.getPortrait());
            }
        } catch (JSONException e) {
            Log.e("GroupNotifyMessage", "jsonFromUserInfo JSONException " + e.getMessage());
        }
        return result;
    }

    private GroupNotifyType mType;
    private List<UserInfo> mMembers = new ArrayList<>();
    private UserInfo mOperator;
    private String mName;

    private static final String MEMBERS = "members";
    private static final String USER_ID = "user_id";
    private static final String AVATAR = "avatar";
    private static final String NICKNAME = "nickname";
    private static final String TYPE = "type";
    private static final String OPERATOR = "operator";
    private static final String NAME = "name";
    private static final String DIGEST = "[群通知]";
}
