package com.jet.im.internal.core.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jet.im.utils.LoggerUtils;

import java.io.File;

public class DBManager {
    public boolean openIMDB(Context context, String appKey, String userId) {
        String path = getOrCreateDbPath(context, appKey, userId);
        LoggerUtils.d("open db, path is " + path);
        closeDB();
        if (!TextUtils.isEmpty(path)) {
            mDBHelper = new DBHelper(context, path);
            mDb = mDBHelper.getWritableDatabase();
        }
        return true;
    }

    public void closeDB() {
        if (mDBHelper != null) {
            mDb = null;
            mDBHelper.close();
        }
        mDBHelper = null;
    }

    public long getConversationSyncTime() {
        long result = 0;
        String[] args = new String[]{ProfileSql.CONVERSATION_TIME};
        Cursor cursor = rawQuery(ProfileSql.SQL_GET_VALUE, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = CursorHelper.readLong(cursor, ProfileSql.COLUMN_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    public long getMessageSendSyncTime() {
        long result = 0;
        String[] args = new String[]{ProfileSql.SEND_TIME};
        Cursor cursor = rawQuery(ProfileSql.SQL_GET_VALUE, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = CursorHelper.readLong(cursor, ProfileSql.COLUMN_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    public long getMessageReceiveSyncTime() {
        long result = 0;
        String[] args = new String[]{ProfileSql.RECEIVE_TIME};
        Cursor cursor = rawQuery(ProfileSql.SQL_GET_VALUE, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = CursorHelper.readLong(cursor, ProfileSql.COLUMN_VALUE);
            }
            cursor.close();
        }
        return result;
    }

    public void setConversationSyncTime(long time) {
        String[] args = new String[]{ProfileSql.CONVERSATION_TIME, String.valueOf(time)};
        execSQL(ProfileSql.SQL_SET_VALUE, args);
    }

    public void setMessageSendSyncTime(long time) {
        String[] args = new String[]{ProfileSql.SEND_TIME, String.valueOf(time)};
        execSQL(ProfileSql.SQL_SET_VALUE, args);
    }

    public void setMessageReceiveSyncTime(long time) {
        String[] args = new String[]{ProfileSql.RECEIVE_TIME, String.valueOf(time)};
        execSQL(ProfileSql.SQL_SET_VALUE, args);
    }

    private Cursor rawQuery(String sql, String[] selectionArgs) {
        if (mDb == null) {
            return null;
        }
        return mDb.rawQuery(sql, selectionArgs);
    }

    private void execSQL(String sql, Object[] bindArgs) {
        if (mDb == null) {
            return;
        }
        mDb.execSQL(sql, bindArgs);
    }
    private String getOrCreateDbPath(Context context, String appKey, String userId) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        path = String.format("%s/%s/%s/%s", path, PATH_JET_IM, appKey, userId);
        file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LoggerUtils.e("create db path fail");
            }
        }
        path = String.format("%s/%s", path, DB_NAME);
        return path;
    }

    private DBHelper mDBHelper;
    private SQLiteDatabase mDb;
    private static final String PATH_JET_IM = "jet_im";
    private static final String DB_NAME = "jetimdb";
}
