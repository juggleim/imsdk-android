package com.jet.im.internal.core.db;

import android.database.Cursor;

import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.model.Conversation;

class ConversationSql {

    static ConcreteConversationInfo conversationInfoWithCursor(Cursor cursor) {
        ConcreteConversationInfo info = new ConcreteConversationInfo();
        int type = CursorHelper.readInt(cursor, COL_CONVERSATION_TYPE);
        String id = CursorHelper.readString(cursor, COL_CONVERSATION_ID);
        Conversation c = new Conversation(Conversation.ConversationType.setValue(type), id);
        info.setConversation(c);
        info.setDraft(CursorHelper.readString(cursor, COL_DRAFT));
        info.setUpdateTime(CursorHelper.readLong(cursor, COL_TIMESTAMP));
        info.setLastReadMessageIndex(CursorHelper.readLong(cursor, COL_LAST_READ_MESSAGE_INDEX));
        boolean isTop = CursorHelper.readInt(cursor, COL_IS_TOP) != 0;
        info.setTop(isTop);
        info.setTopTime(CursorHelper.readLong(cursor, COL_TOP_TIME));
        boolean isMute = CursorHelper.readInt(cursor, COL_MUTE) != 0;
        info.setMute(isMute);
        return info;
    }

    static Object[] argsWithConcreteConversationInfo(ConcreteConversationInfo info) {
        Object[] args = new Object[9];
        args[0] = info.getConversation().getConversationType().getValue();
        args[1] = info.getConversation().getConversationId();
        args[2] = info.getUpdateTime();
        args[3] = info.getLastMessage().getMessageId();
        args[4] = info.getLastReadMessageIndex();
        args[5] = info.isTop();
        args[6] = info.getTopTime();
        args[7] = info.isMute();
        args[8] = "0";
        return args;
    }
    static String sqlGetConversation(int type) {
        return String.format("SELECT * FROM conversation_info WHERE conversation_type = %s AND conversation_id = ?", type);
    }

    static String sqlDeleteConversation(int type) {
        return String.format("DELETE FROM conversation_info WHERE conversation_type = %s AND conversation_id = ?", type);
    }

    static String sqlSetDraft(Conversation conversation, String draft) {
        return String.format("UPDATE conversation_info SET draft = '%s' WHERE conversation_type = %s AND conversation_id = '%s'", draft, conversation.getConversationType().getValue(), conversation.getConversationId());
    }
    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS conversation_info ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "conversation_type SMALLINT,"
            + "conversation_id VARCHAR (64),"
            + "draft TEXT,"
            + "timestamp INTEGER,"
            + "last_message_id VARCHAR (64),"
            + "last_read_message_index INTEGER,"
            + "is_top BOOLEAN,"
            + "top_time INTEGER,"
            + "mute BOOLEAN,"
            + "last_mention_message_id VARCHAR (64)"
            + ")";
    static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_conversation ON conversation_info(conversation_type, conversation_id)";
    static final String SQL_INSERT_CONVERSATION = "INSERT OR REPLACE INTO conversation_info"
            + "(conversation_type, conversation_id, timestamp, last_message_id,"
            + "last_read_message_index, is_top, top_time, mute, last_mention_message_id)"
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static final String SQL_GET_CONVERSATIONS = "SELECT * FROM conversation_info ORDER BY timestamp DESC";
    static final String COL_CONVERSATION_TYPE = "conversation_type";
    static final String COL_CONVERSATION_ID = "conversation_id";
    static final String COL_DRAFT = "draft";
    static final String COL_TIMESTAMP = "timestamp";
    static final String COL_LAST_MESSAGE_ID = "last_message_id";
    static final String COL_LAST_READ_MESSAGE_INDEX = "last_read_message_index";
    static final String COL_IS_TOP = "is_top";
    static final String COL_TOP_TIME = "top_time";
    static final String COL_MUTE = "mute";
    static final String COL_LAST_MENTION_MESSAGE_ID = "last_mention_message_id";
}
