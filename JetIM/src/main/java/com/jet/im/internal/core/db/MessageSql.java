package com.jet.im.internal.core.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.internal.ContentTypeCenter;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.GroupMessageReadInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.MessageMentionInfo;
import com.jet.im.model.MessageOptions;
import com.jet.im.model.MessageReferredInfo;

import java.nio.charset.StandardCharsets;

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
        MessageContent messageContent = null;
        if (content != null) {
            messageContent = ContentTypeCenter.getInstance().getContent(content.getBytes(StandardCharsets.UTF_8), message.getContentType());
            message.setContent(messageContent);
        }
        message.setSeqNo(CursorHelper.readLong(cursor, COL_SEQ_NO));
        message.setMsgIndex(CursorHelper.readLong(cursor, COL_MESSAGE_INDEX));
        GroupMessageReadInfo info = new GroupMessageReadInfo();
        info.setReadCount(CursorHelper.readInt(cursor, COL_READ_COUNT));
        info.setMemberCount(CursorHelper.readInt(cursor, COL_MEMBER_COUNT));
        message.setGroupMessageReadInfo(info);
        message.setLocalAttribute(CursorHelper.readString(cursor, COL_LOCAL_ATTRIBUTE));
        message.setMessageOptions(new MessageOptions());
        String mentionInfoStr = CursorHelper.readString(cursor, COL_MENTION_INFO);
        if (!TextUtils.isEmpty(mentionInfoStr)) {
            message.getMessageOptions().setMentionInfo(new MessageMentionInfo(mentionInfoStr));
        }
        String referMsgId = CursorHelper.readString(cursor, COL_REFER_MSG_ID);
        String referSenderId = CursorHelper.readString(cursor, COL_REFER_SENDER_ID);
        if (!TextUtils.isEmpty(referMsgId) && !TextUtils.isEmpty(referSenderId)) {
            MessageReferredInfo referredInfo = new MessageReferredInfo();
            referredInfo.setMessageId(referMsgId);
            referredInfo.setSenderId(referSenderId);
            message.getMessageOptions().setReferredInfo(referredInfo);
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
            cv.put(COL_MENTION_INFO, message.getMessageOptions().getMentionInfo().encodeToJson());
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
            cv.put(COL_REFER_MSG_ID, message.getMessageOptions().getReferredInfo().getMessageId());
            cv.put(COL_REFER_SENDER_ID, message.getMessageOptions().getReferredInfo().getSenderId());
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
            + "refer_msg_id VARCHAR (64),"
            + "refer_sender_id VARCHAR (64)"
            + ")";

    static final String TABLE = "message";
    static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_message ON message(message_uid)";
    static final String SQL_GET_MESSAGE_WITH_MESSAGE_ID = "SELECT * FROM message WHERE message_uid = ? AND is_deleted = 0";
    static final String SQL_AND_TYPE_IN = " AND type in ";

    static String sqlGetMessagesInConversation(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction, int size) {
        String sql = String.format("SELECT * FROM message WHERE conversation_type = %s AND conversation_id = ? AND is_deleted = 0", conversation.getConversationType().getValue());
        if (direction == JetIMConst.PullDirection.NEWER) {
            sql = sql + SQL_AND_GREATER_THAN + timestamp;
        } else {
            sql = sql + SQL_AND_LESS_THAN + timestamp;
        }
        if (size > 0) {
            sql = sql + SQL_AND_TYPE_IN + CursorHelper.getQuestionMarkPlaceholder(size);
        }
        sql = sql + SQL_ORDER_BY_TIMESTAMP;
        if (direction == JetIMConst.PullDirection.NEWER) {
            sql = sql + SQL_ASC;
        } else {
            sql = sql + SQL_DESC;
        }
        sql = sql + SQL_LIMIT + count;
        return sql;
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

    static final String SQL_AND_GREATER_THAN = " AND timestamp > ";
    static final String SQL_AND_LESS_THAN = " AND timestamp < ";
    static final String SQL_ORDER_BY_TIMESTAMP = " ORDER BY timestamp";
    static final String SQL_ASC = " ASC";
    static final String SQL_DESC = " DESC";
    static final String SQL_LIMIT = " LIMIT ";

    static String sqlUpdateMessageAfterSend(int state, long clientMsgNo, long timestamp, long seqNo) {
        return String.format("UPDATE message SET message_uid = ?, state = %s, timestamp = %s, seq_no = %s WHERE id = %s", state, timestamp, seqNo, clientMsgNo);
    }

    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_ID = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE message_uid = ?";
    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_NO = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE id = ?";

    static String sqlMessageSendFail(long clientMsgNo) {
        return String.format("UPDATE message SET state = %s WHERE id = %s", Message.MessageState.FAIL.getValue(), clientMsgNo);
    }

    static final String SQL_DELETE_MESSAGE = "UPDATE message SET is_deleted = 1 WHERE";

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

    static final String SQL_CLIENT_MSG_NO_IS = " id = ";
    static final String SQL_MESSAGE_ID_IS = " message_uid = ?";

    static String sqlSearchMessage(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, int size) {
        String sql;
        if (conversation == null) {
            sql = String.format("SELECT * FROM message WHERE search_content LIKE '%%%s%%' AND is_deleted = 0", searchContent);
        } else {
            sql = String.format("SELECT * FROM message WHERE search_content LIKE '%%%s%%' AND is_deleted = 0 AND conversation_type = %s AND conversation_id = %s", searchContent, conversation.getConversationType().getValue(), conversation.getConversationId());
        }
        if (JetIMConst.PullDirection.NEWER == direction) {
            sql = sql + SQL_AND_GREATER_THAN + timestamp;
        } else {
            sql = sql + SQL_AND_LESS_THAN + timestamp;
        }
        if (size > 0) {
            sql = sql + SQL_AND_TYPE_IN + CursorHelper.getQuestionMarkPlaceholder(size);
        }
        sql = sql + SQL_ORDER_BY_TIMESTAMP;
        if (JetIMConst.PullDirection.NEWER == direction) {
            sql = sql + SQL_ASC;
        } else {
            sql = sql + SQL_DESC;
        }
        sql = sql + SQL_LIMIT + count;
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
    static final String COL_REFER_SENDER_ID = "refer_sender_id";
}
