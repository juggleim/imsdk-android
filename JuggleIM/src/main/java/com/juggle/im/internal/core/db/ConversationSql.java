package com.juggle.im.internal.core.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.juggle.im.JIMConst;
import com.juggle.im.internal.ContentTypeCenter;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationMentionInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageMentionInfo;

import java.nio.charset.StandardCharsets;

class ConversationSql {

    static ConcreteConversationInfo conversationInfoWithCursor(Cursor cursor) {
        ConcreteConversationInfo info = new ConcreteConversationInfo();
        int type = CursorHelper.readInt(cursor, COL_CONVERSATION_TYPE);
        String id = CursorHelper.readString(cursor, COL_CONVERSATION_ID);
        Conversation c = new Conversation(Conversation.ConversationType.setValue(type), id);
        info.setConversation(c);
        info.setDraft(CursorHelper.readString(cursor, COL_DRAFT));
        info.setSortTime(CursorHelper.readLong(cursor, COL_TIMESTAMP));
        info.setLastReadMessageIndex(CursorHelper.readLong(cursor, COL_LAST_READ_MESSAGE_INDEX));
        info.setLastMessageIndex(CursorHelper.readLong(cursor, COL_LAST_MESSAGE_INDEX));
        boolean isTop = CursorHelper.readInt(cursor, COL_IS_TOP) != 0;
        info.setTop(isTop);
        info.setTopTime(CursorHelper.readLong(cursor, COL_TOP_TIME));
        boolean isMute = CursorHelper.readInt(cursor, COL_MUTE) != 0;
        info.setMute(isMute);
        String mentionInfoJson = CursorHelper.readString(cursor, COL_MENTION_INFO);
        if (!TextUtils.isEmpty(mentionInfoJson)) {
            info.setMentionInfo(new ConversationMentionInfo(mentionInfoJson));
        }
        int unreadCount = (int) (info.getLastMessageIndex() - info.getLastReadMessageIndex());
        info.setUnreadCount(unreadCount);

        String lastMessageId = CursorHelper.readString(cursor, COL_LAST_MESSAGE_ID);
        String lastMessageClientUid = CursorHelper.readString(cursor, COL_LAST_MESSAGE_CLIENT_UID);
        long lastMessageClientMsgNo = CursorHelper.readLong(cursor, COL_LAST_MESSAGE_CLIENT_MSG_NO);
        if (TextUtils.isEmpty(lastMessageId) && TextUtils.isEmpty(lastMessageClientUid) && lastMessageClientMsgNo <= 0) {
            info.setLastMessage(null);
        } else {
            ConcreteMessage lastMessage = new ConcreteMessage();
            lastMessage.setConversation(c);
            lastMessage.setContentType(CursorHelper.readString(cursor, COL_LAST_MESSAGE_TYPE));
            lastMessage.setMessageId(lastMessageId);
            lastMessage.setClientUid(lastMessageClientUid);
            lastMessage.setClientMsgNo(lastMessageClientMsgNo);
            Message.MessageDirection direction = Message.MessageDirection.setValue(CursorHelper.readInt(cursor, COL_LAST_MESSAGE_DIRECTION));
            lastMessage.setDirection(direction);
            Message.MessageState state = Message.MessageState.setValue(CursorHelper.readInt(cursor, COL_LAST_MESSAGE_STATE));
            lastMessage.setState(state);
            boolean hasRead = CursorHelper.readInt(cursor, COL_LAST_MESSAGE_HAS_READ) != 0;
            lastMessage.setHasRead(hasRead);
            lastMessage.setTimestamp(CursorHelper.readLong(cursor, COL_LAST_MESSAGE_TIMESTAMP));
            lastMessage.setSenderUserId(CursorHelper.readString(cursor, COL_LAST_MESSAGE_SENDER));
            String content = CursorHelper.readString(cursor, COL_LAST_MESSAGE_CONTENT);
            if (content != null) {
                lastMessage.setContent(ContentTypeCenter.getInstance().getContent(content.getBytes(StandardCharsets.UTF_8), lastMessage.getContentType()));
            }
            String mentionInfoStr = CursorHelper.readString(cursor, COL_LAST_MESSAGE_MENTION_INFO);
            if (!TextUtils.isEmpty(mentionInfoStr)) {
                lastMessage.setMentionInfo(new MessageMentionInfo(mentionInfoStr));
            }
            lastMessage.setSeqNo(CursorHelper.readLong(cursor, COL_LAST_MESSAGE_SEQ_NO));
            lastMessage.setMsgIndex(CursorHelper.readLong(cursor, COL_LAST_MESSAGE_INDEX));
            info.setLastMessage(lastMessage);
        }
        boolean hasUnread = CursorHelper.readInt(cursor, COL_UNREAD_TAG) != 0;
        info.setUnread(hasUnread);
        return info;
    }

    static Object[] argsWithUpdateConcreteConversationInfo(ConcreteConversationInfo info) {
        ConcreteMessage lastMessage = (ConcreteMessage) info.getLastMessage();
        Object[] args = new Object[22];

        args[0] = info.getSortTime();
        args[1] = lastMessage == null ? null : lastMessage.getMessageId();
        args[2] = info.getLastReadMessageIndex();
        args[3] = info.getLastMessageIndex();
        args[4] = info.isTop();
        args[5] = info.getTopTime();
        args[6] = info.isMute();
        if (info.getMentionInfo() != null) {
            args[7] = info.getMentionInfo().encodeToJson();
        } else {
            args[7] = "";
        }
        args[8] = lastMessage == null ? null : lastMessage.getContentType();
        args[9] = lastMessage == null ? null : lastMessage.getClientUid();
        args[10] = lastMessage == null ? 0 : lastMessage.getClientMsgNo();
        args[11] = (lastMessage == null || lastMessage.getDirection() == null) ? 0 : lastMessage.getDirection().getValue();
        args[12] = (lastMessage == null || lastMessage.getState() == null) ? 0 : lastMessage.getState().getValue();
        args[13] = lastMessage == null ? 0 : lastMessage.isHasRead();
        args[14] = lastMessage == null ? 0 : lastMessage.getTimestamp();
        args[15] = lastMessage == null ? null : lastMessage.getSenderUserId();
        if (lastMessage != null && lastMessage.getContent() != null) {
            args[16] = new String(lastMessage.getContent().encode());
        } else {
            args[16] = "";
        }
        if (lastMessage != null && lastMessage.hasMentionInfo()) {
            args[17] = lastMessage.getMentionInfo().encodeToJson();
        } else {
            args[17] = "";
        }
        args[18] = lastMessage == null ? 0 : lastMessage.getSeqNo();
        args[19] = info.hasUnread();
        args[20] = info.getConversation().getConversationType().getValue();
        args[21] = info.getConversation().getConversationId();
        return args;
    }

    static Object[] argsWithInsertConcreteConversationInfo(ConcreteConversationInfo info) {
        ConcreteMessage lastMessage = (ConcreteMessage) info.getLastMessage();
        Object[] args = new Object[22];
        args[0] = info.getConversation().getConversationType().getValue();
        args[1] = info.getConversation().getConversationId();
        args[2] = info.getSortTime();
        args[3] = lastMessage == null ? null : lastMessage.getMessageId();
        args[4] = info.getLastReadMessageIndex();
        args[5] = info.getLastMessageIndex();
        args[6] = info.isTop();
        args[7] = info.getTopTime();
        args[8] = info.isMute();
        if (info.getMentionInfo() != null) {
            args[9] = info.getMentionInfo().encodeToJson();
        } else {
            args[9] = "";
        }
        args[10] = lastMessage == null ? null : lastMessage.getContentType();
        args[11] = lastMessage == null ? null : lastMessage.getClientUid();
        args[12] = lastMessage == null ? 0 : lastMessage.getClientMsgNo();
        args[13] = (lastMessage == null || lastMessage.getDirection() == null) ? 0 : lastMessage.getDirection().getValue();
        args[14] = (lastMessage == null || lastMessage.getState() == null) ? 0 : lastMessage.getState().getValue();
        args[15] = lastMessage == null ? 0 : lastMessage.isHasRead();
        args[16] = lastMessage == null ? 0 : lastMessage.getTimestamp();
        args[17] = lastMessage == null ? null : lastMessage.getSenderUserId();
        if (lastMessage != null && lastMessage.getContent() != null) {
            args[18] = new String(lastMessage.getContent().encode());
        } else {
            args[18] = "";
        }
        if (lastMessage != null && lastMessage.hasMentionInfo()) {
            args[19] = lastMessage.getMentionInfo().encodeToJson();
        } else {
            args[19] = "";
        }
        args[20] = lastMessage == null ? 0 : lastMessage.getSeqNo();
        args[21] = info.hasUnread();
        return args;
    }

    static Object[] argsWithUpdateLastMessage(ConcreteMessage message, boolean isUpdateSortTime, boolean isUpdateLastIndex) {
        int count = 14;
        if (isUpdateSortTime) {
            count++;
        }
        if (isUpdateLastIndex && Message.MessageDirection.RECEIVE == message.getDirection()) {
            count++;
        }
        Object[] args = new Object[count];
        int i = 0;
        if (TextUtils.isEmpty(message.getMessageId())) {
            args[i++] = "";
        } else {
            args[i++] = message.getMessageId();
        }
        args[i++] = message.getContentType();
        args[i++] = message.getClientUid();
        args[i++] = message.getClientMsgNo();
        args[i++] = message.getDirection().getValue();
        args[i++] = message.getState().getValue();
        args[i++] = message.isHasRead();
        args[i++] = message.getTimestamp();
        args[i++] = message.getSenderUserId();
        if (message.getContent() != null) {
            args[i++] = new String(message.getContent().encode());
        } else {
            args[i++] = "";
        }
        if (message.hasMentionInfo()) {
            args[i++] = message.getMentionInfo().encodeToJson();
        } else {
            args[i++] = "";
        }
        args[i++] = message.getSeqNo();
        if (isUpdateSortTime) {
            args[i++] = message.getTimestamp();
        }
        if (isUpdateLastIndex && Message.MessageDirection.RECEIVE == message.getDirection()) {
            args[i++] = message.getMsgIndex();
        }
        args[i++] = message.getConversation().getConversationType().getValue();
        args[i] = message.getConversation().getConversationId();
        return args;
    }

    static String sqlGetConversation(int type) {
        return String.format("SELECT * FROM conversation_info WHERE conversation_type = %s AND conversation_id = ?", type);
    }

    static String sqlDeleteConversation(int type) {
        return String.format("DELETE FROM conversation_info WHERE conversation_type = %s AND conversation_id = ?", type);
    }

    static String sqlSetDraft(Conversation conversation) {
        return String.format("UPDATE conversation_info SET draft = ? WHERE conversation_type = %s AND conversation_id = '%s'", conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlClearUnreadCount(Conversation conversation, long msgIndex) {
        return String.format("UPDATE conversation_info SET last_read_message_index = %s WHERE conversation_type = %s AND conversation_id = '%s'", msgIndex, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlClearTotalUnreadCount() {
        return "UPDATE conversation_info SET last_read_message_index = last_message_index";
    }

    static String sqlUpdateLastMessageHasRead(Conversation conversation, String messageId, boolean isHasRead) {
        return String.format("UPDATE conversation_info SET last_message_has_read = %s WHERE conversation_type = %s AND conversation_id = '%s' AND last_message_id = '%s'", isHasRead ? 1 : 0, conversation.getConversationType().getValue(), conversation.getConversationId(), messageId);
    }

    static String sqlUpdateLastMessageState(Conversation conversation, long clientMsgNo, int state) {
        return String.format("UPDATE conversation_info SET last_message_state = %s WHERE conversation_type = %s AND conversation_id = '%s' AND last_message_client_msg_no = %s", state, conversation.getConversationType().getValue(), conversation.getConversationId(), clientMsgNo);
    }

    static final String SQL_GET_TOTAL_UNREAD_COUNT = "SELECT SUM(CASE WHEN last_message_index = last_read_message_index AND unread_tag = 1 THEN 1 WHEN last_message_index - last_read_message_index > 0 THEN last_message_index - last_read_message_index ELSE 0 END) AS total_count FROM conversation_info WHERE mute = 0";

    static String sqlGetUnreadCountWithTypes(int[] conversationTypes) {
        return SQL_GET_TOTAL_UNREAD_COUNT + sqlAndConversationTypeIn(conversationTypes);
    }

    static String sqlSetMute(Conversation conversation, boolean isMute) {
        return String.format("UPDATE conversation_info SET mute = %s WHERE conversation_type = %s AND conversation_id = '%s'", isMute ? 1 : 0, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlSetTop(Conversation conversation, boolean isTop, long topTime) {
        return String.format("UPDATE conversation_info SET is_top = %s, top_time = %s WHERE conversation_type = %s AND conversation_id = '%s'", isTop ? 1 : 0, isTop ? topTime : 0, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlSetUnread(Conversation conversation, boolean isUnread) {
        return String.format("UPDATE conversation_info SET unread_tag = %s WHERE conversation_type = %s AND conversation_id = '%s'", isUnread ? 1 : 0, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlClearUnreadTag() {
        return "UPDATE conversation_info SET unread_tag = 0";
    }

    static String sqlSetTopTime(Conversation conversation, long time) {
        return String.format("UPDATE conversation_info SET top_time = %s WHERE conversation_type = %s AND conversation_id = '%s'", time, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlSetMention(Conversation conversation, String mentionInfoJson) {
        return String.format("UPDATE conversation_info SET mention_info = '%s' WHERE conversation_type = %s AND conversation_id = '%s'", mentionInfoJson, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static String sqlUpdateSortTime(Conversation conversation, long sortTime) {
        return String.format("UPDATE conversation_info SET timestamp = %s WHERE conversation_type = %s AND conversation_id = '%s'", sortTime, conversation.getConversationType().getValue(), conversation.getConversationId());
    }

    static final String SQL_CLEAR_MENTION_INFO = "UPDATE conversation_info SET mention_info = NULL";

    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS conversation_info ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "conversation_type SMALLINT,"
            + "conversation_id VARCHAR (64),"
            + "draft TEXT,"
            + "timestamp INTEGER,"
            + "last_message_id VARCHAR (64),"
            + "last_read_message_index INTEGER,"
            + "last_message_index INTEGER,"
            + "is_top BOOLEAN,"
            + "top_time INTEGER,"
            + "mute BOOLEAN,"
            + "mention_info VARCHAR (64),"
            + "last_message_type VARCHAR (64),"
            + "last_message_client_uid VARCHAR (64),"
            + "last_message_client_msg_no INTEGER,"
            + "last_message_direction BOOLEAN,"
            + "last_message_state SMALLINT,"
            + "last_message_has_read BOOLEAN,"
            + "last_message_timestamp INTEGER,"
            + "last_message_sender VARCHAR (64),"
            + "last_message_content TEXT,"
            + "last_message_mention_info TEXT,"
            + "last_message_seq_no INTEGER,"
            + "unread_tag BOOLEAN"
            + ")";
    static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_conversation ON conversation_info(conversation_type, conversation_id)";
    static final String SQL_ADD_COLUMN_UNREAD_TAG = "ALTER TABLE conversation_info ADD COLUMN unread_tag BOOLEAN";
    static final String SQL_INSERT_CONVERSATION = "INSERT OR REPLACE INTO conversation_info"
            + "(conversation_type, conversation_id, timestamp, last_message_id,"
            + "last_read_message_index, last_message_index, is_top, top_time, mute, mention_info,"
            + "last_message_type, last_message_client_uid, last_message_client_msg_no, last_message_direction, last_message_state,"
            + "last_message_has_read, last_message_timestamp, last_message_sender, last_message_content, last_message_mention_info,"
            + "last_message_seq_no, unread_tag) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static final String SQL_UPDATE_CONVERSATION = "UPDATE conversation_info SET timestamp=?, last_message_id=?, last_read_message_index=?, "
            + "last_message_index=?, is_top=?, top_time=?, mute=?, mention_info=?, last_message_type=?,  "
            + "last_message_client_uid=?, last_message_client_msg_no=?, last_message_direction=?, last_message_state=?, "
            + "last_message_has_read=?, last_message_timestamp=?, last_message_sender=?, "
            + "last_message_content=?, last_message_mention_info=?, last_message_seq_no=?, unread_tag=? WHERE conversation_type = ? "
            + "AND conversation_id = ?";
    static final String SQL_GET_CONVERSATIONS = "SELECT * FROM conversation_info";
    static final String SQL_ORDER_BY_TOP_TOPTIME_TIME = " ORDER BY is_top DESC, top_time DESC, timestamp DESC";
    static final String SQL_ORDER_BY_TOP_TIME = " ORDER BY is_top DESC, timestamp DESC";
    static final String SQL_UPDATE_LAST_MESSAGE = "UPDATE conversation_info SET last_message_id=?, last_message_type=?,"
            + "last_message_client_uid=?, last_message_client_msg_no=?, "
            + "last_message_direction=?, last_message_state=?, last_message_has_read=?, last_message_timestamp=?, "
            + "last_message_sender=?, last_message_content=?, last_message_mention_info=?, last_message_seq_no=?";

    static final String SQL_CLEAR_LAST_MESSAGE = "UPDATE conversation_info SET "
            + "last_message_id=NULL, last_message_type=NULL,"
            + "last_message_client_uid=NULL, last_message_client_msg_no=0, last_message_seq_no=0,"
            + "last_message_direction=0, last_message_state=0, last_message_has_read=0, last_message_timestamp=0, "
            + "last_message_sender=NULL, last_message_content=NULL, last_message_mention_info=NULL, "
            + "mention_info=NULL";
    static final String SQL_TIMESTAMP_EQUALS_QUESTION = ", timestamp=?";
    static final String SQL_LAST_MESSAGE_EQUALS_QUESTION = ", last_message_index=?";
    static final String SQL_WHERE_CONVERSATION_IS = " WHERE conversation_type = ? AND conversation_id = ?";

    static String sqlGetConversationsBy(int[] conversationTypes, int count, long timestamp, JIMConst.PullDirection direction, JIMConst.TopConversationsOrderType type) {
        StringBuilder sql = new StringBuilder("SELECT * FROM conversation_info WHERE");
        if (direction == JIMConst.PullDirection.OLDER) {
            sql.append(" timestamp < ").append(timestamp);
        } else {
            sql.append(" timestamp > ").append(timestamp);
        }
        sql.append(sqlAndConversationTypeIn(conversationTypes));
        if (type == JIMConst.TopConversationsOrderType.ORDER_BY_TOP_TIME) {
            sql.append(" ORDER BY is_top DESC, top_time DESC, timestamp DESC");
        } else {
            sql.append(" ORDER BY is_top DESC, timestamp DESC");
        }
        sql.append(" LIMIT ").append(count);
        return sql.toString();
    }

    static String sqlGetTopConversationsBy(int[] conversationTypes, int count, long timestamp, JIMConst.PullDirection direction, JIMConst.TopConversationsOrderType type) {
        StringBuilder sql = new StringBuilder("SELECT * FROM conversation_info WHERE");
        sql.append(" is_top = 1 AND ");
        if (direction == JIMConst.PullDirection.OLDER) {
            sql.append(" timestamp < ").append(timestamp);
        } else {
            sql.append(" timestamp > ").append(timestamp);
        }
        sql.append(sqlAndConversationTypeIn(conversationTypes));
        if (type == JIMConst.TopConversationsOrderType.ORDER_BY_TOP_TIME) {
            sql.append(" ORDER BY top_time DESC");
        } else {
            sql.append(" ORDER BY timestamp DESC");
        }
        sql.append(" LIMIT ").append(count);
        return sql.toString();
    }

    private static String sqlAndConversationTypeIn(int[] conversationTypes) {
        StringBuilder sql = new StringBuilder();
        if (conversationTypes != null && conversationTypes.length > 0) {
            sql.append(" AND conversation_type in (");
            for (int i = 0; i < conversationTypes.length; i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(conversationTypes[i]);
            }
            sql.append(")");
        }
        return sql.toString();
    }

    static final String COL_CONVERSATION_TYPE = "conversation_type";
    static final String COL_CONVERSATION_ID = "conversation_id";
    static final String COL_DRAFT = "draft";
    static final String COL_TIMESTAMP = "timestamp";
    static final String COL_LAST_MESSAGE_ID = "last_message_id";
    static final String COL_LAST_READ_MESSAGE_INDEX = "last_read_message_index";
    static final String COL_LAST_MESSAGE_INDEX = "last_message_index";
    static final String COL_IS_TOP = "is_top";
    static final String COL_TOP_TIME = "top_time";
    static final String COL_MUTE = "mute";
    static final String COL_MENTION_INFO = "mention_info";
    static final String COL_LAST_MESSAGE_TYPE = "last_message_type";
    static final String COL_LAST_MESSAGE_CLIENT_UID = "last_message_client_uid";
    static final String COL_LAST_MESSAGE_CLIENT_MSG_NO = "last_message_client_msg_no";
    static final String COL_LAST_MESSAGE_DIRECTION = "last_message_direction";
    static final String COL_LAST_MESSAGE_STATE = "last_message_state";
    static final String COL_LAST_MESSAGE_HAS_READ = "last_message_has_read";
    static final String COL_LAST_MESSAGE_TIMESTAMP = "last_message_timestamp";
    static final String COL_LAST_MESSAGE_SENDER = "last_message_sender";
    static final String COL_LAST_MESSAGE_CONTENT = "last_message_content";
    static final String COL_LAST_MESSAGE_SEQ_NO = "last_message_seq_no";
    static final String COL_LAST_MESSAGE_MENTION_INFO = "last_message_mention_info";
    static final String COL_TOTAL_COUNT = "total_count";
    static final String COL_UNREAD_TAG = "unread_tag";
}
