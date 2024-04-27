package com.jet.im.model;

import com.jet.im.utils.LoggerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageMentionInfo {
    public enum MentionType {
        DEFAULT(0),
        /// @ 所有人
        ALL(1),
        /// @ 指定用户
        SOMEONE(2),
        /// @ 所有人和指定用户
        ALL_AND_SOMEONE(3);

        MentionType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static MentionType setValue(int value) {
            for (MentionType m : MentionType.values()) {
                if (value == m.mValue) {
                    return m;
                }
            }
            return DEFAULT;
        }
        private final int mValue;
    }

    public String encodeToJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt(MENTION_TYPE, mType.getValue());
            if (mTargetUsers != null && mTargetUsers.size() > 0) {
                JSONArray jsonUsers = new JSONArray();
                for (UserInfo user : mTargetUsers) {
                    JSONObject jsonUser = new JSONObject();
                    jsonUser.putOpt(USER_ID, user.getUserId());
                    jsonUser.putOpt(NAME, user.getUserName());
                    jsonUser.putOpt(PORTRAIT, user.getPortrait());
                    jsonUsers.put(jsonUser);
                }
                jsonObject.putOpt(TARGET_USERS, jsonUsers);
            }
        } catch (JSONException e) {
            LoggerUtils.e("MessageMentionInfo encodeToJson JSONException " + e.getMessage());
        }
        return jsonObject.toString();
    }

    public MessageMentionInfo(String json) {
        if (json == null || json.length() == 0) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            mType = MentionType.setValue(jsonObject.optInt(MENTION_TYPE));
            JSONArray jsonUsers = jsonObject.optJSONArray(TARGET_USERS);
            if (jsonUsers != null) {
                List<UserInfo> users = new ArrayList<>();
                for (int i = 0; i < jsonUsers.length(); i++) {
                    JSONObject jsonUser = jsonUsers.optJSONObject(i);
                    UserInfo user = new UserInfo();
                    user.setUserId(jsonUser.optString(USER_ID));
                    user.setUserName(jsonUser.optString(NAME));
                    user.setPortrait(jsonUser.optString(PORTRAIT));
                    users.add(user);
                }
                mTargetUsers = users;
            }
        } catch (JSONException e) {
            LoggerUtils.e("MessageMentionInfo decode JSONException " + e.getMessage());
        }
    }

    public MessageMentionInfo() {
    }

    public MentionType getType() {
        return mType;
    }

    public void setType(MentionType type) {
        mType = type;
    }

    public List<UserInfo> getTargetUsers() {
        return mTargetUsers;
    }

    public void setTargetUsers(List<UserInfo> targetUsers) {
        mTargetUsers = targetUsers;
    }

    private MentionType mType;
    private List<UserInfo> mTargetUsers;

    private static final String MENTION_TYPE = "mention_type";
    private static final String TARGET_USERS = "target_users";
    private static final String USER_ID = "id";
    private static final String NAME = "name";
    private static final String PORTRAIT = "portrait";
}
