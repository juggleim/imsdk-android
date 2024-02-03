package com.jet.im.internal.core.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.jet.im.JetIMConst;
import com.jet.im.internal.ContentTypeCenter;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;

import java.nio.charset.StandardCharsets;
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
        if (content != null) {
            message.setContent(ContentTypeCenter.getInstance().getContent(content.getBytes(StandardCharsets.UTF_8), message.getContentType()));
        }
        message.setMsgIndex(CursorHelper.readLong(cursor, COL_MESSAGE_INDEX));
        return message;
    }

    static Object[] argsWithMessage(Message message) {
        long msgIndex = 0;
        String clientUid = "";
        if (message instanceof ConcreteMessage) {
            ConcreteMessage c = (ConcreteMessage) message;
            msgIndex = c.getMsgIndex();
            clientUid = c.getClientUid();
        }
        byte[] data = message.getContent().encode();
        Object[] args = new Object[12];
        args[0] = message.getConversation().getConversationType().getValue();
        args[1] = message.getConversation().getConversationId();
        args[2] = message.getContentType();
        args[3] = message.getMessageId();
        args[4] = clientUid;
        args[5] = message.getDirection().getValue();
        args[6] = message.getState().getValue();
        args[7] = message.isHasRead();
        args[8] = message.getTimestamp();
        args[9] = message.getSenderUserId();
        args[10] = new String(data);
        args[11] = msgIndex;
        return args;
    }

    static ContentValues getMessageInsertCV(Message message) {
        ContentValues cv = new ContentValues();
        if (message == null) {
            return cv;
        }
        long msgIndex = 0;
        String clientUid = "";
        if (message instanceof ConcreteMessage) {
            ConcreteMessage c = (ConcreteMessage) message;
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
        }
        cv.put(COL_MESSAGE_INDEX, msgIndex);
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
            + "message_index INTEGER,"
            + "is_deleted BOOLEAN DEFAULT 0"
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

    static String sqlGetMessagesByMessageIds(int count) {
        return "SELECT * FROM message WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlGetMessagesByClientMsgNos(long[] nos) {
        StringBuilder sql = new StringBuilder("SELECT * FROM message WHERE id in (");
        for (int i = 0; i<nos.length; i++) {
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
    static final String SQL_INSERT_MESSAGE = "INSERT INTO message (conversation_type, conversation_id, type, message_uid, client_uid, direction, state, has_read, timestamp, sender, content, message_index) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    static String sqlUpdateMessageAfterSend(int state, long clientMsgNo, long timestamp, long messageIndex) {
        return String.format("UPDATE message SET message_uid = ?, state = %s, timestamp = %s, message_index = %s WHERE id = %s", state, timestamp, messageIndex, clientMsgNo);
    }

    static final String SQL_UPDATE_MESSAGE_CONTENT = "UPDATE message SET content = ?, type = ? WHERE message_uid = ?";

    static String sqlMessageSendFail(long clientMsgNo) {
        return String.format("UPDATE message SET state = %s WHERE id = %s", Message.MessageState.FAIL.getValue(), clientMsgNo);
    }
    static final String SQL_DELETE_MESSAGE = "UPDATE message SET is_deleted = 1 WHERE";
    static String sqlClearMessages(Conversation conversation) {
        return String.format("UPDATE message SET is_deleted = 1 WHERE conversation_type = %s AND conversation_id = '%s'", conversation.getConversationType().getValue(), conversation.getConversationId());
    }
    static final String SQL_CLIENT_MSG_NO_IS = " id = ";
    static final String SQL_MESSAGE_ID_IS = " message_uid = ?";

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
    static final String COL_MESSAGE_INDEX = "message_index";
    static final String COL_IS_DELETED = "is_deleted";

}
