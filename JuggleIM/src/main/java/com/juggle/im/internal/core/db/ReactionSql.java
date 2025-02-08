package com.juggle.im.internal.core.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReactionSql {
    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS reaction ("
                                        + "messageId VARCHAR (64) PRIMARY KEY,"
                                        + "reactions TEXT"
                                        + ")";
    static String sqlGetReaction(int count) {
        return "SELECT * FROM reaction WHERE messageId IN " + CursorHelper.getQuestionMarkPlaceholder(count);
    }
    static final String SQL_SET_REACTION = "INSERT OR REPLACE INTO reaction (messageId, reactions) VALUES (?, ?)";

    static MessageReaction reactionWithCursor(Cursor cursor) {
        MessageReaction reaction = new MessageReaction();
        reaction.setMessageId(CursorHelper.readString(cursor, COL_MESSAGE_ID));
        String reactionStr = CursorHelper.readString(cursor, COL_REACTIONS);
        if (reactionStr == null || reactionStr.isEmpty()) {
            return reaction;
        }
        try {
            JSONArray reactions = new JSONArray(reactionStr);
            List<MessageReactionItem> reactionItemList = new ArrayList<>();
            for (int i = 0; i < reactions.length(); i++) {
                JSONObject itemJson = reactions.optJSONObject(i);
                MessageReactionItem reactionItem = new MessageReactionItem();
                reactionItem.setReactionId(itemJson.optString(REACTION_ID));
                JSONArray userInfoListJson = itemJson.optJSONArray(USER_INFO_LIST);
                List<UserInfo> userInfoList = new ArrayList<>();
                if (userInfoListJson != null) {
                    for (int j = 0; j < userInfoListJson.length(); j++) {
                        JSONObject userInfoJson = userInfoListJson.optJSONObject(j);
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(userInfoJson.optString("id"));
                        userInfo.setUserName(userInfoJson.optString("name"));
                        userInfo.setPortrait(userInfoJson.optString("portrait"));
                        userInfoList.add(userInfo);
                    }
                }
                reactionItem.setUserInfoList(userInfoList);
                reactionItemList.add(reactionItem);
            }
            reaction.setItemList(reactionItemList);
        } catch (JSONException e) {
            JLogger.e("DB-Decode", "reactionWithCursor JSONException " + e.getMessage());
        }
        return reaction;
    }

    static String jsonWithReactionItemList(List<MessageReactionItem> items) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (MessageReactionItem item : items) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.putOpt(REACTION_ID, item.getReactionId());
                JSONArray jsonUserList = new JSONArray();
                for (UserInfo userInfo : item.getUserInfoList()) {
                    JSONObject jsonUser = new JSONObject();
                    if (!TextUtils.isEmpty(userInfo.getUserId())) {
                        jsonUser.putOpt("id", userInfo.getUserId());
                    }
                    if (!TextUtils.isEmpty(userInfo.getUserName())) {
                        jsonUser.putOpt("name", userInfo.getUserName());
                    }
                    if (!TextUtils.isEmpty(userInfo.getPortrait())) {
                        jsonUser.putOpt("portrait", userInfo.getPortrait());
                    }
                    jsonUserList.put(jsonUser);
                }
                jsonItem.putOpt(USER_INFO_LIST, jsonUserList);
                jsonArray.put(jsonItem);
            }
        } catch (JSONException e) {
            JLogger.e("DB-Encode", "jsonWithReactionItemList, JSONException " + e.getMessage());
        }
        return jsonArray.toString();
    }

    private static final String COL_MESSAGE_ID = "messageId";
    private static final String COL_REACTIONS = "reactions";
    private static final String REACTION_ID = "reactionId";
    private static final String USER_INFO_LIST = "userInfoList";
}
