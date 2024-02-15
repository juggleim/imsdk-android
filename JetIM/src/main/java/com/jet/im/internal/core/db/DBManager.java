package com.jet.im.internal.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jet.im.JetIMConst;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
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
            mDBHelper = null;
        }
    }

    public boolean isOpen() {
        return mDb != null;
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

    public List<ConversationInfo> getConversationInfoList() {
        Cursor cursor = rawQuery(ConversationSql.SQL_GET_CONVERSATIONS, null);
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<ConversationInfo> list = conversationListFromCursor(cursor);
        cursor.close();
        return list;
    }

    public List<ConversationInfo> getConversationInfoList(int[] conversationTypes, int count, long timestamp, JetIMConst.PullDirection direction) {
        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        String sql = ConversationSql.sqlGetConversationsBy(conversationTypes, count, timestamp, direction);
        Cursor cursor = rawQuery(sql, null);
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<ConversationInfo> list = conversationListFromCursor(cursor);
        cursor.close();
        return list;
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

    public void deleteConversationInfo(Conversation conversation) {
        String[] args = new String[]{conversation.getConversationId()};
        execSQL(ConversationSql.sqlDeleteConversation(conversation.getConversationType().getValue()), args);
    }

    public void setDraft(Conversation conversation, String draft) {
        execSQL(ConversationSql.sqlSetDraft(conversation, draft));
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

    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        int contentTypeSize = contentTypes.size();
        String sql = MessageSql.sqlGetMessagesInConversation(conversation, count, timestamp, direction, contentTypeSize);
        String[] args = new String[contentTypeSize+1];
        args[0] = conversation.getConversationId();
        for (int i = 0; i < contentTypeSize; i ++) {
            args[i+1] = contentTypes.get(i);
        }
        Cursor cursor = rawQuery(sql, args);
        List<Message> list = new ArrayList<>();
        if (cursor == null) {
            return list;
        }
        addMessagesFromCursor(list, cursor);
        cursor.close();
        return list;
    }


    //被删除的消息也能查出来
    public List<Message> getMessagesByMessageIds(List<String> messageIds) {
        List<Message> result = new ArrayList<>();
        if (messageIds.size() == 0) {
            return result;
        }
        String sql = MessageSql.sqlGetMessagesByMessageIds(messageIds.size());
        Cursor cursor = rawQuery(sql, messageIds.toArray(new String[0]));
        if (cursor == null) {
            return result;
        }
        addMessagesFromCursor(result, cursor);
        cursor.close();
        List<Message> messages = new ArrayList<>();
        for (String messageId : messageIds) {
            for (Message message : result) {
                if (messageId.equals(message.getMessageId())) {
                    messages.add(message);
                    break;
                }
            }
        }
        return messages;
    }

    //被删除的消息也能查出来
    public List<Message> getMessagesByClientMsgNos(long[] clientMsgNos) {
        List<Message> result = new ArrayList<>();
        if (clientMsgNos.length == 0) {
            return result;
        }
        String sql = MessageSql.sqlGetMessagesByClientMsgNos(clientMsgNos);
        Cursor cursor = rawQuery(sql, null);
        if (cursor == null) {
            return result;
        }
        addMessagesFromCursor(result, cursor);
        cursor.close();
        List<Message> messages = new ArrayList<>();
        for (long clientMsgNo : clientMsgNos) {
            for (Message message : result) {
                if (clientMsgNo == message.getClientMsgNo()) {
                    messages.add(message);
                    break;
                }
            }
        }
        return messages;
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

    public void updateMessageContent(MessageContent content, String type, String messageId) {
        Object[] args = new Object[3];
        args[0] = new String(content.encode());
        args[1] = type;
        args[2] = messageId;
        execSQL(MessageSql.SQL_UPDATE_MESSAGE_CONTENT, args);
    }

    public void messageSendFail(long clientMsgNo) {
        String sql = MessageSql.sqlMessageSendFail(clientMsgNo);
        execSQL(sql);
    }

    public void deleteMessageByClientMsgNo(long clientMsgNo) {
        String sql = MessageSql.SQL_DELETE_MESSAGE + MessageSql.SQL_CLIENT_MSG_NO_IS + clientMsgNo;
        execSQL(sql);
    }

    public void deleteMessageByMessageId(String messageId) {
        Object[] args = new Object[]{messageId};
        String sql = MessageSql.SQL_DELETE_MESSAGE + MessageSql.SQL_MESSAGE_ID_IS;
        execSQL(sql, args);
    }

    public void clearMessages(Conversation conversation) {
        execSQL(MessageSql.sqlClearMessages(conversation));
    }

    private Cursor rawQuery(String sql, String[] selectionArgs) {
        if (mDb == null) {
            return null;
        }
        return mDb.rawQuery(sql, selectionArgs);
    }

    private void execSQL(String sql) {
        if (mDb == null) {
            return;
        }
        mDb.execSQL(sql);
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

    private void addMessagesFromCursor(@NonNull List<Message> list, @NonNull Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ConcreteMessage message = MessageSql.messageWithCursor(cursor);
            list.add(message);
        }
    }

    private List<ConversationInfo> conversationListFromCursor(@NonNull Cursor cursor) {
        List<ConversationInfo> list = new ArrayList<>();
        List<String> messageIdList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ConcreteConversationInfo info = ConversationSql.conversationInfoWithCursor(cursor);
            String lastMessageId = CursorHelper.readString(cursor, ConversationSql.COL_LAST_MESSAGE_ID);
            list.add(info);
            messageIdList.add(lastMessageId);
        }
        for (int i = 0; i < list.size(); i++) {
            ConversationInfo info = list.get(i);
            String messageId = messageIdList.get(i);
            info.setLastMessage(getMessageWithMessageId(messageId));
        }
        return list;
    }

    private DBHelper mDBHelper;
    private SQLiteDatabase mDb;
    private static final String PATH_JET_IM = "jet_im";
    private static final String DB_NAME = "jetimdb";
}
