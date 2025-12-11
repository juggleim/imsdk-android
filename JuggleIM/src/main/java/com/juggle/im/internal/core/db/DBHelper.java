package com.juggle.im.internal.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    DBHelper(Context context, String path) {
        super(context, path, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ProfileSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_INDEX2);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CLIENT_UID_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_TIMESTAMP_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_DESTROY_TIME_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CONVERSATION_SUBCHANNEL_INDEX);
        sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX2);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_INDEX);
        sqLiteDatabase.execSQL(ReactionSql.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_TABLE);
        sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_INDEX2);
        sqLiteDatabase.execSQL(MomentSql.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(ConversationSql.SQL_ADD_COLUMN_UNREAD_TAG);
        }
        if (oldVersion < 3) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CLIENT_UID_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 4) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 5) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_FLAGS);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 6) {
            try {
                sqLiteDatabase.execSQL(ReactionSql.SQL_CREATE_TABLE);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 7) {
            try {
                sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_TABLE);
                sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_MEMBER_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 8) {
            try {
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_TABLE);
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 9) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_CONVERSATION_TS_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 10) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_LIFE_TIME);
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_LIFE_TIME_AFTER_READ);
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_DESTROY_TIME);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 11) {
            try {
                sqLiteDatabase.execSQL(UserInfoSql.SQL_ALTER_USER_ADD_UPDATED_TIME);
                sqLiteDatabase.execSQL(UserInfoSql.SQL_ALTER_GROUP_ADD_UPDATED_TIME);
                sqLiteDatabase.execSQL(UserInfoSql.SQL_ALTER_GROUP_MEMBER_ADD_UPDATED_TIME);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 12) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 13) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_READ_TIME);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 14) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_ALTER_ADD_SUB_CHANNEL);
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX2);
                sqLiteDatabase.execSQL(ConversationSql.SQL_ALTER_CONVERSATION_INFO_ADD_SUB_CHANNEL);
                sqLiteDatabase.execSQL(ConversationSql.SQL_ALTER_CONVERSATION_TAG_ADD_SUB_CHANNEL);
                sqLiteDatabase.execSQL(ConversationSql.SQL_DROP_CONVERSATION_INDEX1);
                sqLiteDatabase.execSQL(ConversationSql.SQL_DROP_CONVERSATION_TAG_INDEX1);
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_INDEX2);
                sqLiteDatabase.execSQL(ConversationSql.SQL_CREATE_TAG_INDEX2);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 15) {
            try {
                sqLiteDatabase.execSQL(MomentSql.SQL_CREATE_TABLE);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 16) {
            try {
                sqLiteDatabase.execSQL(MessageSql.SQL_DROP_INDEX_CONVERSATION);
                sqLiteDatabase.execSQL(MessageSql.SQL_DROP_INDEX_CONVERSATION_TS);
                sqLiteDatabase.execSQL(MessageSql.SQL_DROP_INDEX_DS_CONVERSATION_TS);
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_TIMESTAMP_INDEX);
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_DESTROY_TIME_INDEX);
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_CONVERSATION_SUBCHANNEL_INDEX);
                sqLiteDatabase.execSQL(MessageSql.SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX2);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    }

    private final static int version = 16;
}
