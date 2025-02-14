package com.juggle.im.internal.core.db;

import android.database.Cursor;

import com.juggle.im.interfaces.GroupMember;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserInfoSql {
    static UserInfo userInfoWithCursor(Cursor cursor) {
        UserInfo info = new UserInfo();
        info.setUserId(CursorHelper.readString(cursor, COL_USER_ID));
        info.setUserName(CursorHelper.readString(cursor, COL_NAME));
        info.setPortrait(CursorHelper.readString(cursor, COL_PORTRAIT));
        String extra = CursorHelper.readString(cursor, COL_EXTENSION);
        info.setExtra(mapFromString(extra));
        return info;
    }

    static GroupInfo groupInfoWithCursor(Cursor cursor) {
        GroupInfo info = new GroupInfo();
        info.setGroupId(CursorHelper.readString(cursor, COL_GROUP_ID));
        info.setGroupName(CursorHelper.readString(cursor, COL_NAME));
        info.setPortrait(CursorHelper.readString(cursor, COL_PORTRAIT));
        String extra = CursorHelper.readString(cursor, COL_EXTENSION);
        info.setExtra(mapFromString(extra));
        return info;
    }

    static GroupMember groupMemberWithCursor(Cursor cursor) {
        GroupMember member = new GroupMember();
        member.setGroupId(CursorHelper.readString(cursor, COL_GROUP_ID));
        member.setUserId(CursorHelper.readString(cursor, COL_USER_ID));
        member.setGroupDisplayName(CursorHelper.readString(cursor, COL_DISPLAY_NAME));
        String extra = CursorHelper.readString(cursor, COL_EXTENSION);
        member.setExtra(mapFromString(extra));
        return member;
    }

    static String stringFromMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                obj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                return "";
            }
        }
        return obj.toString();
    }

    private static HashMap<String, String> mapFromString(String s) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                String value = jsonObject.getString(key);
                map.put(key, value);
            }
        } catch (JSONException e) {
            map = null;
        }
        return map;
    }

    static final String SQL_CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS user ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "user_id VARCHAR (64),"
            + "name VARCHAR (64),"
            + "portrait TEXT,"
            + "extension TEXT"
            + ")";
    static final String SQL_CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS group_info ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "group_id VARCHAR (64),"
            + "name VARCHAR (64),"
            + "portrait TEXT,"
            + "extension TEXT"
            + ")";
    static final String SQL_CREATE_GROUP_MEMBER_TABLE = "CREATE TABLE IF NOT EXISTS group_member ("
                                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                + "group_id VARCHAR (64),"
                                                + "user_id VARCHAR (64),"
                                                + "display_name VARCHAR (64),"
                                                + "extension TEXT"
                                                + ")";
    static final String SQL_CREATE_USER_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_user ON user(user_id)";
    static final String SQL_CREATE_GROUP_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_group ON group_info(group_id)";
    static final String SQL_CREATE_GROUP_MEMBER_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_group_member ON group_member(group_id, user_id)";
    static final String SQL_GET_USER_INFO = "SELECT * FROM user WHERE user_id = ?";
    static final String SQL_GET_GROUP_INFO = "SELECT * FROM group_info WHERE group_id = ?";
    static final String SQL_GET_GROUP_MEMBER = "SELECT * FROM group_member WHERE group_id = ? AND user_id = ?";
    static final String SQL_INSERT_USER_INFO = "INSERT OR REPLACE INTO user (user_id, name, portrait, extension) VALUES (?, ?, ?, ?)";
    static final String SQL_INSERT_GROUP_INFO = "INSERT OR REPLACE INTO group_info (group_id, name, portrait, extension) VALUES (?, ?, ?, ?)";
    static final String SQL_INSERT_GROUP_MEMBERS = "INSERT OR REPLACE INTO group_member (group_id, user_id, display_name, extension) VALUES ";

    static String sqlInsertGroupMembers(List<GroupMember> members, List<String> whereArgs) {
        StringBuilder sql = new StringBuilder(SQL_INSERT_GROUP_MEMBERS);
        for (int i = 0; i < members.size(); i++) {
            sql.append(CursorHelper.getQuestionMarkPlaceholder(4));
            if (i != members.size() - 1) {
                sql.append(", ");
            }
            GroupMember member = members.get(i);
            whereArgs.add(member.getGroupId());
            whereArgs.add(member.getUserId());
            whereArgs.add(member.getGroupDisplayName());
            whereArgs.add(stringFromMap(member.getExtra()));
        }

        return sql.toString();
    }

    static final String COL_USER_ID = "user_id";
    static final String COL_GROUP_ID = "group_id";
    static final String COL_NAME = "name";
    static final String COL_PORTRAIT = "portrait";
    static final String COL_EXTENSION = "extension";
    static final String COL_DISPLAY_NAME = "display_name";
}
