package com.juggle.im.internal.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.model.GroupMember;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JSortTimeCounter;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GetConversationOptions;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.SearchConversationsResult;
import com.juggle.im.model.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DBManager {

    public synchronized boolean openIMDB(Context context, String appKey, String userId) {
        String path = getOrCreateDbPath(context, appKey, userId);
        closeDB();
        if (!TextUtils.isEmpty(path)) {
            mDBHelper = new DBHelper(context, path);
            mDb = mDBHelper.getWritableDatabase();
        }
        mSortTimeCounter = new JSortTimeCounter(context, appKey, userId);
        JLogger.i("DB-Open", "open db, path is " + path + ", result is " + isOpen());
        return true;
    }

    public synchronized void closeDB() {
        JLogger.i("DB-Close", "close db");
        if (mDBHelper != null) {
            mDb = null;
            mDBHelper.close();
            mDBHelper = null;
        }
    }

    public synchronized boolean isOpen() {
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
        List<ConcreteConversationInfo> insertConversations = new ArrayList<>();
        List<ConcreteConversationInfo> updateConversations = new ArrayList<>();
        performTransaction(() -> {
            for (ConcreteConversationInfo info : list) {
                ConcreteConversationInfo dbInfo = getConversationInfo(info.getConversation());
                if (dbInfo != null) {
                    updateConversations.add(info);
                    Object[] args = ConversationSql.argsWithUpdateConcreteConversationInfo(info);
                    execSQL(ConversationSql.SQL_UPDATE_CONVERSATION, args);
                } else {
                    resetSortTime(info);
                    insertConversations.add(info);
                    Object[] args = ConversationSql.argsWithInsertConcreteConversationInfo(info);
                    execSQL(ConversationSql.SQL_INSERT_CONVERSATION, args);
                }
            }
        });
        if (callback != null) {
            callback.onComplete(insertConversations, updateConversations);
        }
    }

    //重设会话sortTime
    private void resetSortTime(ConcreteConversationInfo info) {
        if (info == null || info.getSortTime() != 0) return;
        info.setSortTime(mSortTimeCounter == null ? 0 : mSortTimeCounter.getNextSortTime());
    }

    public List<ConversationInfo> getConversationInfoList() {
        String sql = ConversationSql.SQL_GET_CONVERSATIONS;
        sql = appendConversationOrderSql(sql);
        Cursor cursor = rawQuery(sql, null);
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<ConversationInfo> list = conversationListFromCursor(cursor);
        cursor.close();
        for (ConversationInfo info : list) {
            if (info instanceof ConcreteConversationInfo) {
                checkLastMessage((ConcreteConversationInfo) info);
            }
        }
        return list;
    }

    public List<ConversationInfo> getConversationInfoList(GetConversationOptions options) {
        if (options == null) {
            return new ArrayList<>();
        }
        List<String> argList = new ArrayList<>();
        String sql = ConversationSql.sqlGetConversationsWithOptions(options, argList, mTopConversationsOrderType);
        String[] args = argList.toArray(new String[0]);
        Cursor cursor = rawQuery(sql, args);
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<ConversationInfo> list = conversationListFromCursor(cursor);
        cursor.close();
        for (ConversationInfo info : list) {
            if (info instanceof ConcreteConversationInfo) {
                checkLastMessage((ConcreteConversationInfo) info);
            }
        }
        return list;
    }

    public List<ConversationInfo> getTopConversationInfoList(int[] conversationTypes, int count, long timestamp, JIMConst.PullDirection direction) {
        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        String sql = ConversationSql.sqlGetTopConversationsBy(conversationTypes, count, timestamp, direction, mTopConversationsOrderType);
        Cursor cursor = rawQuery(sql, null);
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<ConversationInfo> list = conversationListFromCursor(cursor);
        cursor.close();
        for (ConversationInfo info : list) {
            if (info instanceof ConcreteConversationInfo) {
                checkLastMessage((ConcreteConversationInfo) info);
            }
        }
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
        checkLastMessage(result);
        return result;
    }

    public void deleteConversationInfo(List<Conversation> conversations) {
        performTransaction(() -> {
            for (Conversation conversation : conversations) {
                String[] args = new String[]{conversation.getConversationId()};
                execSQL(ConversationSql.sqlDeleteConversation(conversation.getConversationType().getValue()), args);
            }
        });
    }

    public void setDraft(Conversation conversation, String draft) {
        execSQL(ConversationSql.sqlSetDraft(conversation), new Object[]{draft == null ? "" : draft});
    }

    public void setMute(Conversation conversation, boolean isMute) {
        execSQL(ConversationSql.sqlSetMute(conversation, isMute));
    }

    public void setTop(Conversation conversation, boolean isTop, long topTime) {
        execSQL(ConversationSql.sqlSetTop(conversation, isTop, topTime));
    }

    public void setUnread(Conversation conversation, boolean isUnread) {
        execSQL(ConversationSql.sqlSetUnread(conversation, isUnread));
    }

    public void clearUnreadTag() {
        execSQL(ConversationSql.sqlClearUnreadTag());
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

    public void updateConversationLastMessageHasRead(Conversation conversation, String messageId, boolean isHasRead) {
        execSQL(ConversationSql.sqlUpdateLastMessageHasRead(conversation, messageId, isHasRead));
    }

    public void updateConversationLastMessageState(Conversation conversation, long clientMsgNo, Message.MessageState state) {
        execSQL(ConversationSql.sqlUpdateLastMessageState(conversation, clientMsgNo, state.getValue()));
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

    public int getUnreadCountWithTypes(int[] conversationTypes) {
        Cursor cursor = rawQuery(ConversationSql.sqlGetUnreadCountWithTypes(conversationTypes), null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = CursorHelper.readInt(cursor, ConversationSql.COL_TOTAL_COUNT);
            }
            cursor.close();
        }
        return count;
    }

    public int getUnreadCountWithTag(String tagId) {
        Cursor cursor = rawQuery(ConversationSql.SQL_GET_UNREAD_COUNT_WITH_TAG, new String[]{tagId});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = CursorHelper.readInt(cursor, ConversationSql.COL_TOTAL_COUNT);
            }
            cursor.close();
        }
        return count;
    }

    public void updateConversationTag(List<ConcreteConversationInfo> conversationInfos) {
        StringBuilder sql = new StringBuilder(ConversationSql.SQL_INSERT_CONVERSATION_TAG);
        List<String> argList = new ArrayList<>();
        performTransaction(() -> {
            for (ConcreteConversationInfo info : conversationInfos) {
                if (info.getTagIdList() != null && !info.getTagIdList().isEmpty()) {
                    execSQL(ConversationSql.SQL_CLEAR_TAG_BY_CONVERSATION, new String[]{String.valueOf(info.getConversation().getConversationType().getValue()), info.getConversation().getConversationId()});
                    for (String tagId : info.getTagIdList()) {
                        sql.append(CursorHelper.getQuestionMarkPlaceholder(3)).append(", ");
                        argList.add(tagId);
                        argList.add(String.valueOf(info.getConversation().getConversationType().getValue()));
                        argList.add(info.getConversation().getConversationId());
                    }
                }
            }
            String[] args = argList.toArray(new String[0]);
            String last2 = sql.substring(sql.length()-2);
            if (last2.equals(", ")) {
                sql.delete(sql.length()-2, sql.length());
                execSQL(sql.toString(), args);
            }
        });
    }

    public void addConversationsToTag(List<Conversation> conversations, String tagId) {
        if (conversations == null || conversations.isEmpty() || TextUtils.isEmpty(tagId)) {
            return;
        }
        List<String> argList = new ArrayList<>();
        String sql = ConversationSql.sqlAddConversationsToTag(conversations, tagId, argList);
        String[] args = argList.toArray(new String[0]);
        try {
            execSQL(sql, args);
        } catch (SQLiteConstraintException e) {
            JLogger.w("DB-Exception", "addConversationsToTag " + e.getMessage());
        }
    }

    public void removeConversationsFromTag(List<Conversation> conversations, String tagId) {
        if (conversations == null || conversations.isEmpty() || TextUtils.isEmpty(tagId)) {
            return;
        }
        performTransaction(() -> {
            for (Conversation conversation : conversations) {
                execSQL(ConversationSql.SQL_REMOVE_CONVERSATION_FROM_TAG, new String[]{String.valueOf(conversation.getConversationType().getValue()), conversation.getConversationId(), tagId});
            }
        });
    }

    public void updateSortTime(Conversation conversation, long sortTime) {
        execSQL(ConversationSql.sqlUpdateSortTime(conversation, sortTime));
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

    public void setTopConversationsOrderType(JIMConst.TopConversationsOrderType type) {
        mTopConversationsOrderType = type;
    }

    public ConcreteMessage getMessageWithMessageId(String messageId, long now) {
        ConcreteMessage message = null;
        if (TextUtils.isEmpty(messageId)) {
            return null;
        }
        String[] args = new String[]{messageId, String.valueOf(now)};
        Cursor cursor = rawQuery(MessageSql.SQL_GET_MESSAGE_WITH_MESSAGE_ID, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                message = getMessageWithCursor(cursor);
            }
            cursor.close();
        }
        return message;
    }

    public ConcreteMessage getMessageWithMessageIdEvenDelete(String messageId) {
        ConcreteMessage message = null;
        if (TextUtils.isEmpty(messageId)) {
            return null;
        }
        String[] args = new String[]{messageId};
        Cursor cursor = rawQuery(MessageSql.SQL_GET_MESSAGE_WITH_MESSAGE_ID_EVEN_DELETE, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                message = getMessageWithCursor(cursor);
            }
            cursor.close();
        }
        return message;
    }

    private ConcreteMessage getMessageWithClientUid(String clientUid) {
        ConcreteMessage message = null;
        if (TextUtils.isEmpty(clientUid)) {
            return null;
        }
        String[] args = new String[]{clientUid};
        Cursor cursor = rawQuery(MessageSql.SQL_GET_MESSAGE_WITH_CLIENT_UID, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                message = getMessageWithCursor(cursor);
            }
            cursor.close();
        }
        return message;
    }

    private ConcreteMessage getMessageWithCursor(Cursor cursor) {
        ConcreteMessage message;
        if (cursor == null) {
            return null;
        }
        message = MessageSql.messageWithCursor(cursor);
        if (TextUtils.isEmpty(message.getReferMsgId())) {
            return message;
        }
        //查询被引用的消息
        ConcreteMessage referMsg = getMessageWithMessageIdEvenDelete(message.getReferMsgId());
        if (referMsg != null) {
            message.setReferredMessage(referMsg);
        }
        return message;
    }

    public List<SearchConversationsResult> searchMessageInConversations(MessageQueryOptions options, long now) {
        List<SearchConversationsResult> resultList = new ArrayList<>();
        List<String> args = new ArrayList<>();
        String sql = MessageSql.sqlSearchMessageInConversations(options, now, args);
        //执行查询
        Cursor cursor = rawQuery(sql, args.toArray(new String[0]));
        if (cursor == null) {
            return resultList;
        }
        //解析查询结果
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int type = CursorHelper.readInt(cursor, MessageSql.COL_CONVERSATION_TYPE);
            String conversationId = CursorHelper.readString(cursor, MessageSql.COL_CONVERSATION_ID);
            Conversation c = new Conversation(Conversation.ConversationType.setValue(type), conversationId);
            ConversationInfo info = new ConcreteConversationInfo();
            info.setConversation(c);

            SearchConversationsResult result = new SearchConversationsResult();
            result.setConversationInfo(info);
            result.setMatchedCount(CursorHelper.readInt(cursor, "match_count"));
            resultList.add(result);
        }
        cursor.close();

        if (!resultList.isEmpty()) {
            for (SearchConversationsResult result : resultList) {
                ConversationInfo info = getConversationInfo(result.getConversationInfo().getConversation());
                result.setConversationInfo(info);
            }
        }

        //返回查询结果
        return resultList;
    }

    public List<Message> getMessages(
            int count,
            long timestamp,
            JIMConst.PullDirection pullDirection,
            String searchContent,
            Message.MessageDirection direction,
            List<String> contentTypes,
            List<String> senderUserIds,
            List<Message.MessageState> messageStates,
            List<Conversation> conversations,
            List<Conversation.ConversationType> conversationTypes,
            long now
    ) {
        List<Message> result = new ArrayList<>();
        if (count < 1) return result;
        if (timestamp == 0) {
            timestamp = Long.MAX_VALUE;
        }
        //处理sql及查询条件
        List<String> whereArgs = new ArrayList<>();
        String sql = MessageSql.sqlGetMessages(count, timestamp, pullDirection, searchContent, direction, contentTypes, senderUserIds, messageStates, conversations, conversationTypes, now, whereArgs);
        //执行查询
        Cursor cursor = rawQuery(sql, whereArgs.toArray(new String[0]));
        if (cursor == null) {
            return result;
        }
        //解析查询结果
        addMessagesFromCursor(result, cursor);
        cursor.close();
        //按需反转结果列表
        if (JIMConst.PullDirection.OLDER == pullDirection) {
            Collections.reverse(result);
        }
        //返回查询结果
        return result;
    }

    //被删除的消息也能查出来
    public List<Message> getMessagesByMessageIds(List<String> messageIds) {
        List<Message> result = new ArrayList<>();
        if (messageIds.isEmpty()) {
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
        if (messageIds.isEmpty()) {
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

    //从消息表中获取会话中最新一条消息
    public Message getLastMessage(Conversation conversation, long now) {
        String sql = MessageSql.sqlGetLastMessageInConversation(conversation);
        String[] args = new String[]{String.valueOf(now)};
        Cursor cursor = rawQuery(sql, args);
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
        execSQL(MessageSql.sqlUpdateLocalAttribute(messageId), new Object[]{attribute == null ? "" : attribute});
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
        execSQL(MessageSql.sqlUpdateLocalAttribute(clientMsgNo), new Object[]{attribute == null ? "" : attribute});
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

    public void insertMessages(List<ConcreteMessage> list) {
        performTransaction(() -> {
            for (ConcreteMessage message : list) {
                ConcreteMessage m = null;
                //messageId 排重
                if (!TextUtils.isEmpty(message.getMessageId())) {
                    m = getMessageWithMessageId(message.getMessageId(), 0);
                }
                //clientUid 排重
                if (m == null && !TextUtils.isEmpty(message.getClientUid())) {
                    m = getMessageWithClientUid(message.getClientUid());
                }
                if (m != null) {
                    message.setClientMsgNo(m.getClientMsgNo());
                    message.setExisted(true);
                } else {
                    ContentValues cv = MessageSql.getMessageInsertCV(message);
                    long clientMsgNo = insert(MessageSql.TABLE, cv);
                    message.setClientMsgNo(clientMsgNo);
                }
            }
        });
    }

    public void setMessageFlags(String messageId, int flags) {
        String sql = MessageSql.SQL_SET_MESSAGE_FLAGS;
        Object[] args = new Object[]{flags, messageId};
        execSQL(sql, args);
    }

    public void updateDestroyTimeWithMessageId(String messageId, long destroyTime) {
        String sql = MessageSql.SQL_UPDATE_DESTROY_TIME;
        Object[] args = new Object[]{destroyTime, messageId};
        execSQL(sql, args);
    }

    public void updateMessage(ConcreteMessage message) {
        performTransaction(() -> {
            ContentValues cv = MessageSql.getMessageUpdateCV(message);
            update(message.getClientMsgNo(), MessageSql.TABLE, cv);
        });
    }

    public void updateMessageAfterSend(long clientMsgNo,
                                       String msgId,
                                       long timestamp,
                                       long seqNo,
                                       int groupMemberCount) {
        Object[] args = new Object[]{msgId, timestamp};
        String sql = MessageSql.sqlUpdateMessageAfterSend(Message.MessageState.SENT.getValue(), clientMsgNo, timestamp, seqNo, groupMemberCount);
        execSQL(sql, args);
    }

    public void updateMessageAfterSendWithClientUid(String clientUid, String messageId, long timestamp, long seqNo, int groupMemberCount) {
        Object[] args = new Object[]{messageId, timestamp, clientUid};
        String sql = MessageSql.sqlUpdateMessageAfterSendWithClientUid(Message.MessageState.SENT.getValue(), timestamp, seqNo, groupMemberCount);
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

    public void setMessageState(long clientMsgNo, Message.MessageState state) {
        execSQL(MessageSql.sqlUpdateMessageState(state.getValue(), clientMsgNo));
    }

    public void setMessagesRead(List<String> messageIds) {
        String[] args = messageIds.toArray(new String[0]);
        execSQL(MessageSql.sqlSetMessagesRead(messageIds.size()), args);
    }

    public void setGroupMessageReadInfo(Map<String, GroupMessageReadInfo> messages) {
        performTransaction(() -> {
            for (Map.Entry<String, GroupMessageReadInfo> entry : messages.entrySet()) {
                execSQL(MessageSql.sqlSetGroupReadInfo(entry.getValue().getReadCount(), entry.getValue().getMemberCount(), entry.getKey()));
            }
        });
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

    public void clearChatroomMessageExclude(List<String> chatroomIds) {
        String[] args = chatroomIds.toArray(new String[0]);
        execSQL(MessageSql.sqlClearChatroomMessagesExclude(chatroomIds.size()), args);
    }

    public void clearChatroomMessage(String chatroomId) {
        String[] args = new String[1];
        args[0] = chatroomId;
        execSQL(MessageSql.SQL_CLEAR_CHATROOM_MESSAGES_IN, args);
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

    public void insertUserInfoList(List<UserInfo> userInfoList) {
        performTransaction(() -> {
            for (UserInfo info : userInfoList) {
                UserInfo old = getUserInfo(info.getUserId());
                if (old == null || info.getUpdatedTime() > old.getUpdatedTime()) {
                    String extra = UserInfoSql.stringFromMap(info.getExtra());
                    String[] args = new String[]{info.getUserId(), info.getUserName(), info.getPortrait(), extra, String.valueOf(info.getUpdatedTime())};
                    execSQL(UserInfoSql.SQL_INSERT_USER_INFO, args);
                }
            }
        });
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

    public void insertGroupInfoList(List<GroupInfo> groupInfoList) {
        performTransaction(() -> {
            for (GroupInfo info : groupInfoList) {
                GroupInfo old = getGroupInfo(info.getGroupId());
                if (old == null || info.getUpdatedTime() > old.getUpdatedTime()) {
                    String extra = UserInfoSql.stringFromMap(info.getExtra());
                    String[] args = new String[]{info.getGroupId(), info.getGroupName(), info.getPortrait(), extra, String.valueOf(info.getUpdatedTime())};
                    execSQL(UserInfoSql.SQL_INSERT_GROUP_INFO, args);
                }
            }
        });
    }

    public GroupMember getGroupMember(String groupId, String userId) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return null;
        }
        GroupMember member = null;
        String[] args = new String[]{groupId, userId};
        Cursor cursor = rawQuery(UserInfoSql.SQL_GET_GROUP_MEMBER, args);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                member = UserInfoSql.groupMemberWithCursor(cursor);
            }
            cursor.close();
        }
        return member;
    }

    public void insertGroupMembers(List<GroupMember> members) {
        if (members == null || members.isEmpty()) {
            return;
        }
        performTransaction(() -> {
            for (GroupMember member : members) {
                GroupMember old = getGroupMember(member.getGroupId(), member.getUserId());
                if (old == null || member.getUpdatedTime() > old.getUpdatedTime()) {
                    String extra = UserInfoSql.stringFromMap(member.getExtra());
                    String[] args = new String[]{member.getGroupId(), member.getUserId(), member.getGroupDisplayName(), extra, String.valueOf(member.getUpdatedTime())};
                    execSQL(UserInfoSql.SQL_INSERT_GROUP_MEMBER, args);
                }
            }
        });
    }

    public List<MessageReaction> getMessageReactions(List<String> messageIds) {
        List<MessageReaction> result = new ArrayList<>();
        if (messageIds == null || messageIds.isEmpty()) {
            return result;
        }
        String sql = ReactionSql.sqlGetReaction(messageIds.size());
        String[] args = messageIds.toArray(new String[0]);
        Cursor cursor = rawQuery(sql, args);
        if (cursor != null) {
            addReactionsFromCursor(result, cursor);
            cursor.close();
        }
        return result;
    }

    public void setMessageReactions(List<MessageReaction> reactions) {
        performTransaction(() -> {
            for (MessageReaction reaction : reactions) {
                if (!TextUtils.isEmpty(reaction.getMessageId())
                && reaction.getItemList() != null) {
                    if (!reaction.getItemList().isEmpty()) {
                        String itemListJson = ReactionSql.jsonWithReactionItemList(reaction.getItemList());
                        String[] args = new String[]{reaction.getMessageId(), itemListJson};
                        execSQL(ReactionSql.SQL_SET_REACTION, args);
                    } else {
                        execSQL(ReactionSql.SQL_DELETE_REACTION, new String[]{reaction.getMessageId()});
                    }
                }
            }
        });
    }

    private void checkLastMessage(ConcreteConversationInfo info) {
        if (info == null) {
            return;
        }
        boolean needUpdate = false;
        long timeDifference = JIM.getInstance().getTimeDifference();
        long now = System.currentTimeMillis() + timeDifference;
        // 当 lastMessage 存在的时候，检查它是否被删除或者过期了。不存在的时候不做处理
        if (info.getLastMessage() instanceof ConcreteMessage) {
            ConcreteMessage conversationLastMessage = (ConcreteMessage) info.getLastMessage();
            ConcreteMessage lastMessage = getMessageWithClientUid(conversationLastMessage.getClientUid());
            if (lastMessage != null) {
                if (lastMessage.isDelete() || (lastMessage.getDestroyTime() > 0 && lastMessage.getDestroyTime() <= now)) {
                    needUpdate = true;
                } else {
                    info.getLastMessage().setDestroyTime(lastMessage.getDestroyTime());
                    info.getLastMessage().setLifeTimeAfterRead(lastMessage.getLifeTimeAfterRead());
                }
            }
        }
        if (needUpdate) {
            Message newLast = getLastMessage(info.getConversation(), now);
            info.setLastMessage(newLast);
        }
    }

    private synchronized Cursor rawQuery(String sql, String[] selectionArgs) {
        if (mDb == null) {
            return null;
        }
        long start = System.currentTimeMillis();
        Cursor c = mDb.rawQuery(sql, selectionArgs);
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (duration > DB_DURATION) {
            JLogger.w("DB-Duration", "rawQuery lasts for " + duration + "ms, sql is " + sql);
        }
        return c;
    }

    private synchronized void execSQL(String sql) {
        if (mDb == null) {
            return;
        }
        long start = System.currentTimeMillis();
        mDb.execSQL(sql);
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (duration > DB_DURATION) {
            JLogger.w("DB-Duration", "execSQL lasts for " + duration + "ms, sql is " + sql);
        }
    }

    private synchronized void execSQL(String sql, Object[] bindArgs) {
        if (mDb == null) {
            return;
        }
        long start = System.currentTimeMillis();
        mDb.execSQL(sql, bindArgs);
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (duration > DB_DURATION) {
            JLogger.w("DB-Duration", "execSQL lasts for " + duration + "ms, sql is " + sql);
        }
    }

    private synchronized long insert(String table, ContentValues cv) {
        if (mDb == null) {
            return -1;
        }
        long start = System.currentTimeMillis();
        long result = mDb.insertWithOnConflict(table, "", cv, SQLiteDatabase.CONFLICT_IGNORE);
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (duration > DB_DURATION) {
            JLogger.w("DB-Duration", "insert lasts for " + duration + "ms, table is " + table);
        }
        return result;
    }

    //执行事务
    private synchronized boolean performTransaction(TransactionOperation operation) {
        if (mDb == null) return false;

        boolean success = false;
        try {
            long start = System.currentTimeMillis();
            mDb.beginTransaction();
            operation.execute();
            mDb.setTransactionSuccessful();
            success = true;
            long end = System.currentTimeMillis();
            long duration = end - start;
            if (duration > DB_DURATION) {
                JLogger.w("DB-Duration", "performTransaction lasts for " + duration + "ms");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }
        return success;
    }

    private synchronized long update(long msgClientNo, String table, ContentValues cv) {
        if (mDb == null) {
            return -1;
        }
        String whereCase = MessageSql.COL_MESSAGE_ID + " = ?";
        String[] whereArgs = {String.valueOf(msgClientNo)};
        long start = System.currentTimeMillis();
        long result = mDb.updateWithOnConflict(table, cv, whereCase, whereArgs, SQLiteDatabase.CONFLICT_IGNORE);
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (duration > DB_DURATION) {
            JLogger.w("DB-Duration", "update lasts for " + duration + "ms, table is " + table);
        }

        return result;
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
            ConcreteMessage message = getMessageWithCursor(cursor);
            list.add(message);
        }
    }

    private void addReactionsFromCursor(List<MessageReaction> list, Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            MessageReaction reaction = ReactionSql.reactionWithCursor(cursor);
            list.add(reaction);
        }
    }

    private void addConcreteMessagesFromCursor(@NonNull List<ConcreteMessage> list, @NonNull Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ConcreteMessage message = getMessageWithCursor(cursor);
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

    private String appendConversationOrderSql(String originSql) {
        if (mTopConversationsOrderType == JIMConst.TopConversationsOrderType.ORDER_BY_TOP_TIME) {
            originSql = originSql + ConversationSql.SQL_ORDER_BY_TOP_TOPTIME_TIME;
        } else {
            originSql = originSql + ConversationSql.SQL_ORDER_BY_TOP_TIME;
        }
        return originSql;
    }

    private DBHelper mDBHelper;
    private SQLiteDatabase mDb;
    private JIMConst.TopConversationsOrderType mTopConversationsOrderType = JIMConst.TopConversationsOrderType.ORDER_BY_TOP_TIME;
    private JSortTimeCounter mSortTimeCounter;
    private static final String PATH_JET_IM = "jet_im";
    private static final String DB_NAME = "jetimdb";
    private static final long DB_DURATION = 500;

    private interface TransactionOperation {
        void execute() throws Exception;
    }
}
