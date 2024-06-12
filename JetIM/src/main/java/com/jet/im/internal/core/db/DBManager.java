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
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.GroupMessageReadInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DBManager {
    public boolean openIMDB(Context context, String appKey, String userId) {
        String path = getOrCreateDbPath(context, appKey, userId);
        closeDB();
        if (!TextUtils.isEmpty(path)) {
            mDBHelper = new DBHelper(context, path);
            mDb = mDBHelper.getWritableDatabase();
        }
        JLogger.i("DB-Open", "open db, path is " + path + ", result is " + isOpen());
        return true;
    }

    public void closeDB() {
        JLogger.i("DB-Close", "close db");
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

    public interface IDbInsertConversationsCallback {
        void onComplete(List<ConcreteConversationInfo> insertList, List<ConcreteConversationInfo> updateList);
    }

    public void insertConversations(List<ConcreteConversationInfo> list, IDbInsertConversationsCallback callback) {
        if (mDb == null) {
            return;
        }
        List<ConcreteConversationInfo> insertConversations = new ArrayList<>();
        List<ConcreteConversationInfo> updateConversations = new ArrayList<>();
        mDb.beginTransaction();
        for (ConcreteConversationInfo info : list) {
            ConcreteConversationInfo dbInfo = getConversationInfo(info.getConversation());
            if (dbInfo != null) {
                updateConversations.add(info);
                Object[] args = ConversationSql.argsWithUpdateConcreteConversationInfo(info);
                execSQL(ConversationSql.SQL_UPDATE_CONVERSATION, args);
            } else {
                insertConversations.add(info);
                Object[] args = ConversationSql.argsWithInsertConcreteConversationInfo(info);
                execSQL(ConversationSql.SQL_INSERT_CONVERSATION, args);
            }
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        if (callback != null) {
            callback.onComplete(insertConversations, updateConversations);
        }
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

    public List<ConversationInfo> getTopConversationInfoList(int[] conversationTypes, int count, long timestamp, JetIMConst.PullDirection direction) {
        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        String sql = ConversationSql.sqlGetTopConversationsBy(conversationTypes, count, timestamp, direction);
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
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = ConversationSql.conversationInfoWithCursor(cursor);
            }
            cursor.close();
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

    public void setMute(Conversation conversation, boolean isMute) {
        execSQL(ConversationSql.sqlSetMute(conversation, isMute));
    }

    public void setTop(Conversation conversation, boolean isTop, long topTime) {
        execSQL(ConversationSql.sqlSetTop(conversation, isTop, topTime));
    }

    public void setMentionInfo(Conversation conversation, String mentionInfoJson) {
        execSQL(ConversationSql.sqlSetMention(conversation, mentionInfoJson));
    }

    public void clearMentionInfo() {
        execSQL(ConversationSql.SQL_CLEAR_MENTION_INFO);
    }

    public void clearUnreadCount(Conversation conversation, long msgIndex) {
        execSQL(ConversationSql.sqlClearUnreadCount(conversation, msgIndex));
    }

    public void clearTotalUnreadCount() {
        execSQL(ConversationSql.sqlClearTotalUnreadCount());
    }

    public int getTotalUnreadCount() {
        Cursor cursor = rawQuery(ConversationSql.SQL_GET_TOTAL_UNREAD_COUNT, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = CursorHelper.readInt(cursor, ConversationSql.COL_TOTAL_COUNT);
            }
            cursor.close();
        }
        return count;
    }

    public void updateSortTime(Conversation conversation, long sortTime) {
        execSQL(ConversationSql.sqlUpdateSortTime(conversation, sortTime));
    }

    public void updateLastMessage(ConcreteMessage message) {
        String sql = ConversationSql.SQL_UPDATE_LAST_MESSAGE;
        boolean isUpdateSortTime = true;
        if (message.getDirection() == Message.MessageDirection.SEND
                && (message.getFlags() & MessageContent.MessageFlag.IS_BROADCAST.getValue()) != 0) {
            isUpdateSortTime = false;
        }
        if (isUpdateSortTime) {
            sql = sql + ConversationSql.SQL_TIMESTAMP_EQUALS_QUESTION;
        }
        if (Message.MessageDirection.RECEIVE == message.getDirection()) {
            sql = sql + ConversationSql.SQL_LAST_MESSAGE_EQUALS_QUESTION;
        }
        sql = sql + ConversationSql.SQL_WHERE_CONVERSATION_IS;
        Object[] args = ConversationSql.argsWithUpdateLastMessage(message, isUpdateSortTime, true);
        execSQL(sql, args);
    }

    public void updateLastMessageWithoutIndex(ConcreteMessage message) {
        String sql = ConversationSql.SQL_UPDATE_LAST_MESSAGE;
        sql = sql + ConversationSql.SQL_WHERE_CONVERSATION_IS;
        Object[] args = ConversationSql.argsWithUpdateLastMessage(message, false, false);
        execSQL(sql, args);
    }

    public void clearLastMessage(Conversation conversation) {
        String sql = ConversationSql.SQL_CLEAR_LAST_MESSAGE + ConversationSql.SQL_WHERE_CONVERSATION_IS;
        Object[] args = new Object[]{conversation.getConversationType().getValue(), conversation.getConversationId()};
        execSQL(sql, args);
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
        String[] args = new String[contentTypeSize + 1];
        args[0] = conversation.getConversationId();
        for (int i = 0; i < contentTypeSize; i++) {
            args[i + 1] = contentTypes.get(i);
        }
        Cursor cursor = rawQuery(sql, args);
        List<Message> list = new ArrayList<>();
        if (cursor == null) {
            return list;
        }
        addMessagesFromCursor(list, cursor);
        if (direction == JetIMConst.PullDirection.OLDER) {
            Collections.reverse(list);
        }
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
    public List<ConcreteMessage> getConcreteMessagesByMessageIds(List<String> messageIds) {
        List<ConcreteMessage> result = new ArrayList<>();
        if (messageIds.size() == 0) {
            return result;
        }
        String sql = MessageSql.sqlGetMessagesByMessageIds(messageIds.size());
        Cursor cursor = rawQuery(sql, messageIds.toArray(new String[0]));
        if (cursor == null) {
            return result;
        }
        addConcreteMessagesFromCursor(result, cursor);
        cursor.close();
        List<ConcreteMessage> messages = new ArrayList<>();
        for (String messageId : messageIds) {
            for (ConcreteMessage message : result) {
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

    public List<Message> searchMessage(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        List<Message> result = new ArrayList<>();
        if (TextUtils.isEmpty(searchContent) || count < 1) return result;

        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        if (direction == null) {
            direction = JetIMConst.PullDirection.OLDER;
        }
        if (contentTypes == null) {
            contentTypes = new ArrayList<>();
        }

        int contentTypeSize = contentTypes.size();
        String sql = MessageSql.sqlSearchMessage(conversation, searchContent, count, timestamp, direction, contentTypeSize);
        String[] args = new String[contentTypeSize];
        for (int i = 0; i < contentTypeSize; i++) {
            args[i] = contentTypes.get(i);
        }
        Cursor cursor = rawQuery(sql, args);
        if (cursor == null) {
            return result;
        }
        addMessagesFromCursor(result, cursor);
        if (JetIMConst.PullDirection.OLDER == direction) {
            Collections.reverse(result);
        }
        cursor.close();
        return result;
    }

    //从消息表中获取会话中最新一条消息
    public Message getLastMessage(Conversation conversation) {
        String sql = MessageSql.sqlGetLastMessageInConversation(conversation);
        Cursor cursor = rawQuery(sql, null);
        List<Message> list = new ArrayList<>();
        if (cursor == null) {
            return null;
        }
        addMessagesFromCursor(list, cursor);
        cursor.close();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public void updateLocalAttribute(String messageId, String attribute) {
        if (TextUtils.isEmpty(messageId)) return;
        execSQL(MessageSql.sqlUpdateLocalAttribute(messageId, attribute == null ? "" : attribute));
    }

    public String getLocalAttribute(String messageId) {
        if (TextUtils.isEmpty(messageId)) return "";
        String sql = MessageSql.sqlGetLocalAttribute(messageId);
        Cursor cursor = rawQuery(sql, null);
        String result = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = CursorHelper.readString(cursor, MessageSql.COL_LOCAL_ATTRIBUTE);
            }
            cursor.close();
        }
        return result;
    }

    public void updateLocalAttribute(long clientMsgNo, String attribute) {
        execSQL(MessageSql.sqlUpdateLocalAttribute(clientMsgNo, attribute == null ? "" : attribute));
    }

    public String getLocalAttribute(long clientMsgNo) {
        String sql = MessageSql.sqlGetLocalAttribute(clientMsgNo);
        Cursor cursor = rawQuery(sql, null);
        String result = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = CursorHelper.readString(cursor, MessageSql.COL_LOCAL_ATTRIBUTE);
            }
            cursor.close();
        }
        return result;
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
            ConcreteMessage m = null;
            if (!TextUtils.isEmpty(message.getMessageId())) {
                m = getMessageWithMessageId(message.getMessageId());
            }
            if (m != null) {
                message.setClientMsgNo(m.getClientMsgNo());
                message.setExisted(true);
            } else {
                long clientMsgNo = insertMessage(message);
                message.setClientMsgNo(clientMsgNo);
            }
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public void updateMessageAfterSend(long clientMsgNo,
                                       String msgId,
                                       long timestamp,
                                       long seqNo) {
        Object[] args = new Object[]{msgId};
        String sql = MessageSql.sqlUpdateMessageAfterSend(Message.MessageState.SENT.getValue(), clientMsgNo, timestamp, seqNo);
        execSQL(sql, args);
    }

    public void updateMessageContentWithMessageId(MessageContent content, String type, String messageId) {
        Object[] args = new Object[4];
        if (content != null) {
            args[0] = new String(content.encode());
            args[2] = content.getSearchContent();
        } else {
            args[0] = "";
            args[2] = "";
        }
        args[1] = type;
        args[3] = messageId;
        execSQL(MessageSql.SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_ID, args);
    }

    public void updateMessageContentWithClientMsgNo(MessageContent content, String type, long clientMsgNo) {
        Object[] args = new Object[4];
        if (content != null) {
            args[0] = new String(content.encode());
            args[2] = content.getSearchContent();
        } else {
            args[0] = "";
            args[2] = "";
        }
        args[1] = type;
        args[3] = clientMsgNo;
        execSQL(MessageSql.SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_NO, args);
    }

    public void messageSendFail(long clientMsgNo) {
        String sql = MessageSql.sqlMessageSendFail(clientMsgNo);
        execSQL(sql);
    }

    public void setMessageState(long clientMsgNo, Message.MessageState state) {
        execSQL(MessageSql.sqlUpdateMessageState(state.getValue(), clientMsgNo));
    }

    public void setMessagesRead(List<String> messageIds) {
        String[] args = messageIds.toArray(new String[0]);
        execSQL(MessageSql.sqlSetMessagesRead(messageIds.size()), args);
    }

    public void setGroupMessageReadInfo(Map<String, GroupMessageReadInfo> messages) {
        if (mDb == null) {
            return;
        }
        mDb.beginTransaction();
        for (Map.Entry<String, GroupMessageReadInfo> entry : messages.entrySet()) {
            mDb.execSQL(MessageSql.sqlSetGroupReadInfo(entry.getValue().getReadCount(), entry.getValue().getMemberCount(), entry.getKey()));
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public void deleteMessageByClientMsgNo(long clientMsgNo) {
        String sql = MessageSql.SQL_DELETE_MESSAGE + MessageSql.SQL_CLIENT_MSG_NO_IS + clientMsgNo;
        execSQL(sql);
    }

    public void deleteMessageByClientMsgNo(List<Long> clientMsgNos) {
        Long[] args = clientMsgNos.toArray(new Long[0]);
        execSQL(MessageSql.sqlDeleteMessagesByClientMsgNo(clientMsgNos.size()), args);
    }

    public void deleteMessagesByMessageIds(List<String> messageIds) {
        String[] args = messageIds.toArray(new String[0]);
        execSQL(MessageSql.sqlDeleteMessagesByMessageId(messageIds.size()), args);
    }

    public void clearMessages(Conversation conversation, long startTime, String senderId) {
        execSQL(MessageSql.sqlClearMessages(conversation, startTime, senderId));
    }

    public UserInfo getUserInfo(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        UserInfo info = null;
        String[] args = new String[]{userId};
        Cursor cursor = rawQuery(UserInfoSql.SQL_GET_USER_INFO, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                info = UserInfoSql.userInfoWithCursor(cursor);
            }
            cursor.close();
        }
        return info;
    }

    public GroupInfo getGroupInfo(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        GroupInfo info = null;
        String[] args = new String[]{groupId};
        Cursor cursor = rawQuery(UserInfoSql.SQL_GET_GROUP_INFO, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                info = UserInfoSql.groupInfoWithCursor(cursor);
            }
            cursor.close();
        }
        return info;
    }

    public void insertUserInfoList(List<UserInfo> userInfoList) {
        mDb.beginTransaction();
        for (UserInfo info : userInfoList) {
            String extra = UserInfoSql.stringFromMap(info.getExtra());
            String[] args = new String[]{info.getUserId(), info.getUserName(), info.getPortrait(), extra};
            execSQL(UserInfoSql.SQL_INSERT_USER_INFO, args);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public void insertGroupInfoList(List<GroupInfo> groupInfoList) {
        mDb.beginTransaction();
        for (GroupInfo info : groupInfoList) {
            String extra = UserInfoSql.stringFromMap(info.getExtra());
            String[] args = new String[]{info.getGroupId(), info.getGroupName(), info.getPortrait(), extra};
            execSQL(UserInfoSql.SQL_INSERT_GROUP_INFO, args);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
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
                JLogger.e("DB-Open", "create db path fail");
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

    private void addConcreteMessagesFromCursor(@NonNull List<ConcreteMessage> list, @NonNull Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ConcreteMessage message = MessageSql.messageWithCursor(cursor);
            list.add(message);
        }
    }

    private List<ConversationInfo> conversationListFromCursor(@NonNull Cursor cursor) {
        List<ConversationInfo> list = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ConcreteConversationInfo info = ConversationSql.conversationInfoWithCursor(cursor);
            list.add(info);
        }
        return list;
    }

    private DBHelper mDBHelper;
    private SQLiteDatabase mDb;
    private static final String PATH_JET_IM = "jet_im";
    private static final String DB_NAME = "jetimdb";
}
