package com.juggle.im.internal.core.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.juggle.im.JIMConst;
import com.juggle.im.internal.ContentTypeCenter;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.messages.MergeMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class MessageSql {
    static ConcreteMessage messageWithCursor(Cursor cursor) {
        ConcreteMessage message = new ConcreteMessage();
        int type = CursorHelper.readInt(cursor, COL_CONVERSATION_TYPE);
        String conversationId = CursorHelper.readString(cursor, COL_CONVERSATION_ID);
        Conversation c = new Conversation(Conversation.ConversationType.setValue(type), conversationId);
        message.setConversation(c);
        message.setContentType(CursorHelper.readString(cursor, COL_CONTENT_TYPE));
        message.setClientMsgNo(CursorHelper.readLong(cursor, COL_MESSAGE_ID));
        message.setMessageId(CursorHelper.readString(cursor, COL_MESSAGE_UID));
        message.setClientUid(CursorHelper.readString(cursor, COL_MESSAGE_CLIENT_UID));
        Message.MessageDirection direction = Message.MessageDirection.setValue(CursorHelper.readInt(cursor, COL_DIRECTION));
        message.setDirection(direction);
        Message.MessageState state = Message.MessageState.setValue(CursorHelper.readInt(cursor, COL_STATE));
        message.setState(state);
        boolean hasRead = CursorHelper.readInt(cursor, COL_HAS_READ) != 0;
        message.setHasRead(hasRead);
        message.setTimestamp(CursorHelper.readLong(cursor, COL_TIMESTAMP));
        message.setSenderUserId(CursorHelper.readString(cursor, COL_SENDER));
        String content = CursorHelper.readString(cursor, COL_CONTENT);
        MessageContent messageContent;
        if (content != null) {
            messageContent = ContentTypeCenter.getInstance().getContent(content.getBytes(StandardCharsets.UTF_8), message.getContentType());
            message.setContent(messageContent);
            if (messageContent instanceof MergeMessage) {
                if (TextUtils.isEmpty(((MergeMessage) messageContent).getContainerMsgId())) {
                    ((MergeMessage) messageContent).setContainerMsgId(message.getMessageId());
                }
            }
        }
        message.setSeqNo(CursorHelper.readLong(cursor, COL_SEQ_NO));
        message.setMsgIndex(CursorHelper.readLong(cursor, COL_MESSAGE_INDEX));
        GroupMessageReadInfo info = new GroupMessageReadInfo();
        info.setReadCount(CursorHelper.readInt(cursor, COL_READ_COUNT));
        info.setMemberCount(CursorHelper.readInt(cursor, COL_MEMBER_COUNT));
        message.setGroupMessageReadInfo(info);
        message.setLocalAttribute(CursorHelper.readString(cursor, COL_LOCAL_ATTRIBUTE));
        boolean isDelete = CursorHelper.readInt(cursor, COL_IS_DELETED) != 0;
        message.setDelete(isDelete);
        String mentionInfoStr = CursorHelper.readString(cursor, COL_MENTION_INFO);
        if (!TextUtils.isEmpty(mentionInfoStr)) {
            message.setMentionInfo(new MessageMentionInfo(mentionInfoStr));
        }
        String referMsgId = CursorHelper.readString(cursor, COL_REFER_MSG_ID);
        if (!TextUtils.isEmpty(referMsgId)) {
            message.setReferMsgId(referMsgId);
        }
        return message;
    }

    static ContentValues getMessageInsertCV(Message message) {
        ContentValues cv = new ContentValues();
        if (message == null) {
            return cv;
        }
        long seqNo = 0;
        long msgIndex = 0;
        String clientUid = "";
        if (message instanceof ConcreteMessage) {
            ConcreteMessage c = (ConcreteMessage) message;
            seqNo = c.getSeqNo();
            msgIndex = c.getMsgIndex();
            clientUid = c.getClientUid();
        }
        cv.put(COL_CONVERSATION_TYPE, message.getConversation().getConversationType().getValue());
        cv.put(COL_CONVERSATION_ID, message.getConversation().getConversationId());
        cv.put(COL_CONTENT_TYPE, message.getContentType());
        cv.put(COL_MESSAGE_UID, message.getMessageId());
        cv.put(COL_MESSAGE_CLIENT_UID, clientUid);
        cv.put(COL_DIRECTION, message.getDirection().getValue());
        cv.put(COL_STATE, message.getState().getValue());
        cv.put(COL_HAS_READ, message.isHasRead());
        cv.put(COL_TIMESTAMP, message.getTimestamp());
        cv.put(COL_SENDER, message.getSenderUserId());
        if (message.getContent() != null) {
            cv.put(COL_CONTENT, new String(message.getContent().encode()));
            cv.put(COL_SEARCH_CONTENT, message.getContent().getSearchContent());
        }
        cv.put(COL_SEQ_NO, seqNo);
        cv.put(COL_MESSAGE_INDEX, msgIndex);
        if (message.getLocalAttribute() != null) {
            cv.put(COL_LOCAL_ATTRIBUTE, message.getLocalAttribute());
        }
        if (message.hasMentionInfo()) {
            cv.put(COL_MENTION_INFO, message.getMentionInfo().encodeToJson());
        }
        if (message.getGroupMessageReadInfo() != null) {
            cv.put(COL_READ_COUNT, message.getGroupMessageReadInfo().getReadCount());
            int memberCount = message.getGroupMessageReadInfo().getMemberCount();
            if (memberCount == 0) {
                memberCount = -1;
            }
            cv.put(COL_MEMBER_COUNT, memberCount);
        }
        if (message.hasReferredInfo()) {
            cv.put(COL_REFER_MSG_ID, message.getReferredMessage().getMessageId());
        }
        return cv;
    }

    static ContentValues getMessageUpdateCV(Message message) {
        ContentValues cv = new ContentValues();
        if (message == null) {
            return cv;
        }
        cv.put(COL_CONTENT_TYPE, message.getContentType());
        if (message.getContent() != null) {
            cv.put(COL_CONTENT, new String(message.getContent().encode()));
            cv.put(COL_SEARCH_CONTENT, message.getContent().getSearchContent());
        }
        if (message.getLocalAttribute() != null) {
            cv.put(COL_LOCAL_ATTRIBUTE, message.getLocalAttribute());
        }
        if (message.hasMentionInfo()) {
            cv.put(COL_MENTION_INFO, message.getMentionInfo().encodeToJson());
        }
        if (message.hasReferredInfo()) {
            cv.put(COL_REFER_MSG_ID, message.getReferredMessage().getMessageId());
        }
        return cv;
    }

    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS message ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "conversation_type SMALLINT,"
            + "conversation_id VARCHAR (64),"
            + "type VARCHAR (64),"
            + "message_uid VARCHAR (64),"
            + "client_uid VARCHAR (64),"
            + "direction BOOLEAN,"
            + "state SMALLINT,"
            + "has_read BOOLEAN,"
            + "timestamp INTEGER,"
            + "sender VARCHAR (64),"
            + "content TEXT,"
            + "extra TEXT,"
            + "seq_no INTEGER,"
            + "message_index INTEGER,"
            + "read_count INTEGER DEFAULT 0,"
            + "member_count INTEGER DEFAULT -1,"
            + "is_deleted BOOLEAN DEFAULT 0,"
            + "search_content TEXT,"
            + "local_attribute TEXT,"
            + "mention_info TEXT,"
            + "refer_msg_id VARCHAR (64)"
            + ")";

    static final String TABLE = "message";
    static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_message ON message(message_uid)";
    static final String SQL_GET_MESSAGE_WITH_MESSAGE_ID = "SELECT * FROM message WHERE message_uid = ? AND is_deleted = 0";

    static String sqlGetMessages(
            int count,
            long timestamp,
            JIMConst.PullDirection pullDirection,
            String searchContent,
            Message.MessageDirection direction,
            List<String> contentTypes,
            List<String> senderUserIds,
            List<Message.MessageState> messageStates,
            List<Conversation> conversations,
            List<String> whereArgs
    ) {
        List<String> whereClauses = new ArrayList<>();
        //添加 is_deleted = 0 条件
        whereClauses.add("is_deleted = 0");
        //添加 direction 条件
        if (direction != null) {
            whereClauses.add("direction = ?");
            whereArgs.add(String.valueOf(direction.getValue()));
        }
        //添加 contentTypes 条件
        if (contentTypes != null && !contentTypes.isEmpty()) {
            whereClauses.add("type IN " + CursorHelper.getQuestionMarkPlaceholder(contentTypes.size()));
            whereArgs.addAll(contentTypes);
        }
        //添加 senderUserIds 条件
        if (senderUserIds != null && !senderUserIds.isEmpty()) {
            whereClauses.add("sender IN " + CursorHelper.getQuestionMarkPlaceholder(senderUserIds.size()));
            whereArgs.addAll(senderUserIds);
        }
        //添加 messageStates 条件
        if (messageStates != null && !messageStates.isEmpty()) {
            whereClauses.add("state IN " + CursorHelper.getQuestionMarkPlaceholder(messageStates.size()));
            for (Message.MessageState state : messageStates) {
                whereArgs.add(String.valueOf(state.getValue()));
            }
        }
        //添加 conversations 条件
        if (conversations != null && !conversations.isEmpty()) {
            List<String> conversationClauses = new ArrayList<>();
            for (Conversation conversation : conversations) {
                conversationClauses.add("(conversation_type = ? AND conversation_id = ?)");
                whereArgs.add(String.valueOf(conversation.getConversationType().getValue()));
                whereArgs.add(conversation.getConversationId());
            }
            whereClauses.add("(" + String.join(" OR ", conversationClauses) + ")");
        }
        //添加 timestamp 和 pullDirection 条件
        if (pullDirection != null) {
            whereClauses.add(pullDirection == JIMConst.PullDirection.NEWER ? "timestamp > ?" : "timestamp < ?");
            whereArgs.add(String.valueOf(timestamp));
        }
        //添加 search_content 条件
        if (searchContent != null) {
            whereClauses.add("search_content LIKE ?");
            whereArgs.add("%" + searchContent + "%");
        }
        //合并查询条件
        String whereClause = whereClauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereClauses);
        //返回sql
        return "SELECT * FROM message " + whereClause + " ORDER BY timestamp " + (JIMConst.PullDirection.NEWER == pullDirection ? "ASC" : "DESC") + " LIMIT " + count;
    }

    static String sqlGetLastMessageInConversation(Conversation conversation) {
        String sql = String.format("SELECT * FROM message WHERE conversation_type = '%s' AND conversation_id = '%s' AND is_deleted = 0", conversation.getConversationType().getValue(), conversation.getConversationId());
        sql = sql + SQL_ORDER_BY_TIMESTAMP + SQL_DESC + SQL_LIMIT + 1;
        return sql;
    }

    static String sqlGetMessagesByMessageIds(int count) {
        return "SELECT * FROM message WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlUpdateMessageState(int state, long clientMsgNo) {
        return String.format("UPDATE message SET state = %s WHERE id = %s", state, clientMsgNo);
    }

    static String sqlSetMessagesRead(int count) {
        return "UPDATE message SET has_read = 1 WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlSetGroupReadInfo(int readCount, int memberCount, String messageId) {
        return String.format("UPDATE message SET read_count = %s, member_count = %s WHERE message_uid = '%s'", readCount, memberCount, messageId);
    }

    static String sqlGetMessagesByClientMsgNos(long[] nos) {
        StringBuilder sql = new StringBuilder("SELECT * FROM message WHERE id in (");
        for (int i = 0; i < nos.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(nos[i]);
        }
        sql.append(")");
        return sql.toString();
    }

    static final String SQL_ORDER_BY_TIMESTAMP = " ORDER BY timestamp";
    static final String SQL_DESC = " DESC";
    static final String SQL_LIMIT = " LIMIT ";

    static String sqlUpdateMessageAfterSend(int state, long clientMsgNo, long timestamp, long seqNo) {
        return String.format("UPDATE message SET message_uid = ?, state = %s, timestamp = %s, seq_no = %s WHERE id = %s", state, timestamp, seqNo, clientMsgNo);
    }

    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_ID = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE message_uid = ?";
    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_NO = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE id = ?";

    static String sqlDeleteMessagesByMessageId(int count) {
        return "UPDATE message SET is_deleted = 1 WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlDeleteMessagesByClientMsgNo(int count) {
        return "UPDATE message SET is_deleted = 1 WHERE id in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlClearMessages(Conversation conversation, long startTime, String senderId) {
        String sql = String.format("UPDATE message SET is_deleted = 1 WHERE conversation_type = %s AND conversation_id = '%s' AND timestamp <= %s", conversation.getConversationType().getValue(), conversation.getConversationId(), startTime);
        if (!TextUtils.isEmpty(senderId)) {
            sql = sql + String.format(" AND sender = '%s'", senderId);
        }
        return sql;
    }

    static String sqlUpdateLocalAttribute(String messageId, String localAttribute) {
        return String.format("UPDATE message SET local_attribute = '%s' WHERE message_uid = '%s'", localAttribute, messageId);
    }

    static String sqlUpdateLocalAttribute(long clientMsgNo, String localAttribute) {
        return String.format("UPDATE message SET local_attribute = '%s' WHERE id = '%s'", localAttribute, clientMsgNo);
    }

    static String sqlGetLocalAttribute(String messageId) {
        return String.format("SELECT local_attribute FROM message WHERE message_uid = '%s'", messageId);
    }

    static String sqlGetLocalAttribute(long clientMsgNo) {
        return String.format("SELECT local_attribute FROM message WHERE id = '%s'", clientMsgNo);
    }

    static final String COL_CONVERSATION_TYPE = "conversation_type";
    static final String COL_CONVERSATION_ID = "conversation_id";
    static final String COL_MESSAGE_ID = "id";
    static final String COL_CONTENT_TYPE = "type";
    static final String COL_MESSAGE_UID = "message_uid";
    static final String COL_MESSAGE_CLIENT_UID = "client_uid";
    static final String COL_DIRECTION = "direction";
    static final String COL_STATE = "state";
    static final String COL_HAS_READ = "has_read";
    static final String COL_TIMESTAMP = "timestamp";
    static final String COL_SENDER = "sender";
    static final String COL_CONTENT = "content";
    static final String COL_EXTRA = "extra";
    static final String COL_SEQ_NO = "seq_no";
    static final String COL_MESSAGE_INDEX = "message_index";
    static final String COL_READ_COUNT = "read_count";
    static final String COL_MEMBER_COUNT = "member_count";
    static final String COL_IS_DELETED = "is_deleted";
    static final String COL_SEARCH_CONTENT = "search_content";
    static final String COL_LOCAL_ATTRIBUTE = "local_attribute";
    static final String COL_MENTION_INFO = "mention_info";
    static final String COL_REFER_MSG_ID = "refer_msg_id";
}
