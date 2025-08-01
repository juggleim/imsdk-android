package com.juggle.im.internal.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    DBHelper(Context context, String path) {
        super(context, path, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ProfileSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CLIENT_UID_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_TS_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_INDEX);
        sqLiteDatabase.execSQL(ReactionSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(ConversationSql.SQL_ADD_COLUMN_UNREAD_TAG);
        }
        if (oldVersion < 3) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CLIENT_UID_INDEX);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 4) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_INDEX);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 5) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_FLAGS);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 6) {
            try {
                sqLiteDatabase.execSQL(ReactionSql.SQL_CREATE_TABLE);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 7) {
            try {
                sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_TABLE);
                sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_INDEX);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 8) {
            try {
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_TABLE);
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_INDEX);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 9) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_TS_INDEX);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 10) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_LIFE_TIME);
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_LIFE_TIME_AFTER_READ);
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_DESTROY_TIME);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        }
    }

    private final static int version = 10;
}
