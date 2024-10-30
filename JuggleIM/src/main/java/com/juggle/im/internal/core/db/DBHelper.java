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
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_TABLE);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_USER_INDEX);
        sqLiteDatabase.execSQL(UserInfoSql.SQL_CREATE_GROUP_INDEX);
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
    }

    private final static int version = 3;
}
