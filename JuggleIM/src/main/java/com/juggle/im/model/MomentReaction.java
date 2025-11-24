package com.juggle.im.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MomentReaction {
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        if (mKey != null) {
            json.put("key", mKey);
        }

        JSONArray userJsonArray = new JSONArray();
        if (mUserList != null && !mUserList.isEmpty()) {
            for (UserInfo user : mUserList) {
                if (user != null) {
                    userJsonArray.put(user.toJson());
                }
            }
        }
        json.put("userArray", userJsonArray);

        return json;
    }

    public static List<MomentReaction> mergeReactionListWithJson(JSONArray jsonArray) {
        List<MomentReaction> reactionModels = new ArrayList<>();
        if (jsonArray == null || jsonArray.length() == 0) {
            return reactionModels;
        }

        Map<String, List<UserInfo>> keyUserMap = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject reactionJson = jsonArray.optJSONObject(i);
            if (reactionJson == null) {
                continue;
            }

            String key = reactionJson.optString("key");
            if (key.isEmpty()) {
                continue;
            }

            List<UserInfo> userList = keyUserMap.get(key);
            if (userList == null) {
                userList = new ArrayList<>();
                keyUserMap.put(key, userList);
            }

            JSONObject userJson = reactionJson.optJSONObject("user_info");
            UserInfo userInfo = UserInfo.fromJson(userJson);
            if (userInfo != null) {
                userList.add(userInfo);
            }
        }

        for (Map.Entry<String, List<UserInfo>> entry : keyUserMap.entrySet()) {
            MomentReaction reaction = new MomentReaction();
            reaction.setKey(entry.getKey()); // 设置 key
            reaction.setUserList(entry.getValue()); // 设置分组后的用户列表
            reactionModels.add(reaction);
        }

        return reactionModels;
    }

    public List<UserInfo> getUserList() {
        return mUserList;
    }

    public void setUserList(List<UserInfo> userList) {
        mUserList = userList;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    private String mKey;
    private List<UserInfo> mUserList;
}
