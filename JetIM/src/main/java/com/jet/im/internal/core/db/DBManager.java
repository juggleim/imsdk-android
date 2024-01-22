package com.jet.im.internal.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.utils.LoggerUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

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

    public void insertConversations(List<ConcreteConversationInfo> list) {
        if (mDb == null) {
            return;
        }
        mDb.beginTransaction();
        for (ConcreteConversationInfo info : list) {
            Object[] args = ConversationSql.argsWithConcreteConversationInfo(info);
            execSQL(ConversationSql.SQL_INSERT_CONVERSATION, args);
            insertMessage(info.getLastMessage());
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public ConcreteConversationInfo getConversationInfo(Conversation conversation) {
        String[] args = new String[]{conversation.getConversationId()};
        Cursor cursor = rawQuery(ConversationSql.sqlGetConversation(conversation.getConversationType().getValue()), args);
        ConcreteConversationInfo result = null;
        String lastMessageId = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = ConversationSql.conversationInfoWithCursor(cursor);
                lastMessageId = CursorHelper.readString(cursor, ConversationSql.COL_LAST_MESSAGE_ID);
            }
            cursor.close();
        }
        if (result != null) {
            result.setLastMessage(getMessageWithMessageId(lastMessageId));
        }
        return result;
    }

    public ConcreteMessage getMessageWithMessageId(String messageId) {
        ConcreteMessage message = null;
        if (TextUtils.isEmpty(messageId)) {
            return null;
        }
        String[] args = new String[]{messageId};
        Cursor cursor = rawQuery(MessageSql.SQL_GET_MESSAGE_WITH_MESSAGE_ID, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                message = MessageSql.messageWithCursor(cursor);
            }
            cursor.close();
        }
        return message;
    }

    public long insertMessage(Message message) {
        ContentValues cv = MessageSql.getMessageInsertCV(message);
        return insert(MessageSql.TABLE, cv);
    }

    public void insertMessages(List<ConcreteMessage> list) {
        if (mDb == null) {
            return;
        }
        mDb.beginTransaction();
        for (ConcreteMessage message : list) {
            long clientMsgNo = insertMessage(message);
            message.setClientMsgNo(clientMsgNo);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public void updateMessageAfterSend(long clientMsgNo,
                                       String msgId,
                                       long timestamp,
                                       long msgIndex) {
        Object[] args = new Object[]{msgId};
        String sql = MessageSql.sqlUpdateMessageAfterSend(Message.MessageState.SENT.getValue(), clientMsgNo, timestamp, msgIndex);
        execSQL(sql, args);
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

    private long insert(String table, ContentValues cv) {
        if (mDb == null) {
            return -1;
        }
        return mDb.insertWithOnConflict(table, "", cv, SQLiteDatabase.CONFLICT_IGNORE);
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
