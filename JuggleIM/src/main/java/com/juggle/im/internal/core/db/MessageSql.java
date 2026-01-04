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
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.messages.MergeMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class MessageSql {
    static ConcreteMessage messageWithCursor(Cursor cursor) {
        ConcreteMessage message = new ConcreteMessage();
        int type = CursorHelper.readInt(cursor, COL_CONVERSATION_TYPE);
        String conversationId = CursorHelper.readString(cursor, COL_CONVERSATION_ID);
        String subChannel = CursorHelper.readString(cursor, COL_SUB_CHANNEL);
        if (subChannel == null) {
            subChannel = "";
        }
        Conversation c = new Conversation(Conversation.ConversationType.setValue(type), conversationId);
        c.setSubChannel(subChannel);
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
        message.setFlags(CursorHelper.readInt(cursor, COL_FLAGS));
        message.setEdit((message.getFlags() & MessageContent.MessageFlag.IS_MODIFIED.getValue()) != 0);
        message.setLifeTime(CursorHelper.readLong(cursor, COL_LIFE_TIME));
        message.setLifeTimeAfterRead(CursorHelper.readLong(cursor, COL_LIFE_TIME_AFTER_READ));
        message.setDestroyTime(CursorHelper.readLong(cursor, COL_DESTROY_TIME));
        message.setReadTime(CursorHelper.readLong(cursor, COL_READ_TIME));
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
        int flags = 0;
        long lifeTime = 0;
        long readTime = 0;

        if (message instanceof ConcreteMessage) {
            ConcreteMessage c = (ConcreteMessage) message;
            seqNo = c.getSeqNo();
            msgIndex = c.getMsgIndex();
            clientUid = c.getClientUid();
            flags = c.getFlags();
            lifeTime = c.getLifeTime();
            readTime = c.getReadTime();
        }
        cv.put(COL_CONVERSATION_TYPE, message.getConversation().getConversationType().getValue());
        cv.put(COL_CONVERSATION_ID, message.getConversation().getConversationId());
        cv.put(COL_SUB_CHANNEL, message.getConversation().getSubChannel());
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
        cv.put(COL_FLAGS, flags);
        cv.put(COL_IS_DELETED, message.isDelete());
        cv.put(COL_LIFE_TIME, lifeTime);
        cv.put(COL_LIFE_TIME_AFTER_READ, message.getLifeTimeAfterRead());
        cv.put(COL_DESTROY_TIME, message.getDestroyTime());
        cv.put(COL_READ_TIME, readTime);
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
            + "member_count INTEGER DEFAULT 0,"
            + "is_deleted BOOLEAN DEFAULT 0,"
            + "search_content TEXT,"
            + "local_attribute TEXT,"
            + "mention_info TEXT,"
            + "refer_msg_id VARCHAR (64),"
            + "flags INTEGER,"
            + "life_time INTEGER DEFAULT 0,"
            + "life_time_after_read INTEGER DEFAULT 0,"
            + "destroy_time INTEGER DEFAULT 0,"
            + "read_time INTEGER,"
            + "subchannel VARCHAR (64) DEFAULT ''"
            + ")";

    static final String TABLE = "message";
    static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_message ON message(message_uid)";
    static final String SQL_CREATE_CLIENT_UID_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_message_client_uid ON message(client_uid)";
    static final String SQL_CREATE_DESTROY_TIME_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_destroy_time ON message(destroy_time)";
    static final String SQL_CREATE_TIMESTAMP_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_timestamp ON message(timestamp)";
    static final String SQL_CREATE_CONVERSATION_SUBCHANNEL_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_conversation_subchannel ON message(conversation_type, conversation_id, subchannel)";
    static final String SQL_DROP_INDEX_CONVERSATION = "DROP INDEX IF EXISTS idx_message_conversation";
    static final String SQL_DROP_INDEX_CONVERSATION_TS = "DROP INDEX IF EXISTS idx_message_conversation_ts";
    static final String SQL_DROP_INDEX_DS_CONVERSATION_TS = "DROP INDEX IF EXISTS idx_message_ds_conversation_ts";
    static final String SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX2 = "CREATE INDEX IF NOT EXISTS idx_message_ds_conversation_ts2 ON message(destroy_time, conversation_type, conversation_id, subchannel, timestamp)";
    static final String SQL_CREATE_STATE_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_state ON message(state)";
    static final String SQL_ALTER_ADD_FLAGS = "ALTER TABLE message ADD COLUMN flags INTEGER";
    static final String SQL_ALTER_ADD_LIFE_TIME = "ALTER TABLE message ADD COLUMN life_time INTEGER DEFAULT 0";
    static final String SQL_ALTER_ADD_LIFE_TIME_AFTER_READ = "ALTER TABLE message ADD COLUMN life_time_after_read INTEGER DEFAULT 0";
    static final String SQL_ALTER_ADD_DESTROY_TIME = "ALTER TABLE message ADD COLUMN destroy_time INTEGER DEFAULT 0";
    static final String SQL_ALTER_ADD_READ_TIME = "ALTER TABLE message ADD COLUMN read_time INTEGER";
    static final String SQL_ALTER_ADD_SUB_CHANNEL = "ALTER TABLE message ADD COLUMN subchannel VARCHAR (64) DEFAULT ''";
    static final String SQL_GET_MESSAGE_WITH_MESSAGE_ID = "SELECT * FROM message WHERE message_uid = ? AND is_deleted = 0  AND (destroy_time = 0 OR destroy_time > ?)";
    static final String SQL_GET_MESSAGE_WITH_MESSAGE_ID_EVEN_DELETE = "SELECT * FROM message WHERE message_uid = ?";
    static final String SQL_GET_MESSAGE_WITH_CLIENT_UID = "SELECT * FROM message WHERE client_uid = ?";
    static final String SQL_SEARCH_MESSAGE_IN_CONVERSATIONS = "SELECT conversation_type, conversation_id, subchannel, count(*) AS match_count FROM message ";
    static final String SQL_SET_MESSAGE_FLAGS = "UPDATE message SET flags = ? WHERE message_uid = ?";
    static final String SQL_UPDATE_DESTROY_TIME = "UPDATE message SET destroy_time = ? WHERE message_uid = ?";

    //deprecated
    static final String SQL_CREATE_MESSAGE_CONVERSATION_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_conversation ON message(conversation_type, conversation_id)";
    static final String SQL_CREATE_MESSAGE_CONVERSATION_TS_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_conversation_ts ON message(conversation_type, conversation_id, timestamp)";
    static final String SQL_CREATE_MESSAGE_DT_CONVERSATION_TS_INDEX = "CREATE INDEX IF NOT EXISTS idx_message_ds_conversation_ts ON message(destroy_time, conversation_type, conversation_id, timestamp)";

    static String sqlSearchMessageInConversations(MessageQueryOptions options, long now, List<String> whereArgs) {
        List<String> whereClauses = new ArrayList<>();
        //添加 is_deleted = 0 条件
        whereClauses.add("is_deleted = 0");
        whereClauses.add("(destroy_time = 0 OR destroy_time > ?)");
        whereArgs.add(String.valueOf(now));
        if (options != null) {
            //添加 conversations 条件
            if (options.getConversations() != null && !options.getConversations().isEmpty()) {
                List<String> conversationClauses = new ArrayList<>();
                for (Conversation conversation : options.getConversations()) {
                    conversationClauses.add("(conversation_type = ? AND conversation_id = ? AND subchannel = ?)");
                    whereArgs.add(String.valueOf(conversation.getConversationType().getValue()));
                    whereArgs.add(conversation.getConversationId());
                    whereArgs.add(conversation.getSubChannel());
                }
                whereClauses.add("(" + String.join(" OR ", conversationClauses) + ")");
            }
            //添加 direction 条件
            if (options.getDirection() != null) {
                whereClauses.add("direction = ?");
                whereArgs.add(String.valueOf(options.getDirection().getValue()));
            }
            //添加 contentTypes 条件
            if (options.getContentTypes() != null && !options.getContentTypes().isEmpty()) {
                whereClauses.add("type IN " + CursorHelper.getQuestionMarkPlaceholder(options.getContentTypes().size()));
                whereArgs.addAll(options.getContentTypes());
            }
            //添加 senderUserIds 条件
            if (options.getSenderUserIds() != null && !options.getSenderUserIds().isEmpty()) {
                whereClauses.add("sender IN " + CursorHelper.getQuestionMarkPlaceholder(options.getSenderUserIds().size()));
                whereArgs.addAll(options.getSenderUserIds());
            }
            //添加 messageStates 条件
            if (options.getStates() != null && !options.getStates().isEmpty()) {
                whereClauses.add("state IN " + CursorHelper.getQuestionMarkPlaceholder(options.getStates().size()));
                for (Message.MessageState state : options.getStates()) {
                    whereArgs.add(String.valueOf(state.getValue()));
                }
            }
            //添加 conversationTypes 条件
            if (options.getConversationTypes() != null && !options.getConversationTypes().isEmpty()) {
                whereClauses.add("conversation_type IN " + CursorHelper.getQuestionMarkPlaceholder(options.getConversationTypes().size()));
                for (Conversation.ConversationType type : options.getConversationTypes()) {
                    whereArgs.add(String.valueOf(type.getValue()));
                }
            }
            //添加 search_content 条件
            if (options.getSearchContent() != null) {
                whereClauses.add("search_content LIKE ?");
                whereArgs.add("%" + options.getSearchContent() + "%");
            }
        }
        //合并查询条件
        String whereClause = whereClauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereClauses);
        //返回sql
        return SQL_SEARCH_MESSAGE_IN_CONVERSATIONS + whereClause + " GROUP BY conversation_type, conversation_id, subchannel" + " ORDER BY timestamp DESC";
    }

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
            List<Conversation.ConversationType> conversationTypes,
            long now,
            List<String> whereArgs
    ) {
        List<String> whereClauses = new ArrayList<>();
        //添加 is_deleted = 0 条件
        whereClauses.add("is_deleted = 0");
        whereClauses.add("(destroy_time = 0 OR destroy_time > ?)");
        whereArgs.add(String.valueOf(now));
        //添加 conversations 条件
        if (conversations != null && !conversations.isEmpty()) {
            List<String> conversationClauses = new ArrayList<>();
            for (Conversation conversation : conversations) {
                conversationClauses.add("(conversation_type = ? AND conversation_id = ? AND subchannel = ?)");
                whereArgs.add(String.valueOf(conversation.getConversationType().getValue()));
                whereArgs.add(conversation.getConversationId());
                whereArgs.add(conversation.getSubChannel());
            }
            whereClauses.add("(" + String.join(" OR ", conversationClauses) + ")");
        }
        //添加 timestamp 和 pullDirection 条件
        if (pullDirection != null) {
            whereClauses.add(pullDirection == JIMConst.PullDirection.NEWER ? "timestamp > ?" : "timestamp < ?");
            whereArgs.add(String.valueOf(timestamp));
        }
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
        //添加 conversationTypes 条件
        if (conversationTypes != null && !conversationTypes.isEmpty()) {
            whereClauses.add("conversation_type IN " + CursorHelper.getQuestionMarkPlaceholder(conversationTypes.size()));
            for (Conversation.ConversationType type : conversationTypes) {
                whereArgs.add(String.valueOf(type.getValue()));
            }
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
        String subChannel = conversation.getSubChannel();
        if (subChannel == null) {
            subChannel = "";
        }
        String sql = String.format("SELECT * FROM message WHERE is_deleted = 0 AND (destroy_time = 0 OR destroy_time > ?) AND conversation_type = '%s' AND conversation_id = '%s' AND subchannel = '%s'", conversation.getConversationType().getValue(), conversation.getConversationId(), subChannel);
        sql = sql + SQL_ORDER_BY_TIMESTAMP + SQL_DESC + SQL_LIMIT + 1;
        return sql;
    }

    static String sqlGetMessagesByMessageIds(int count) {
        return "SELECT * FROM message WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlUpdateMessageState(int state, long clientMsgNo) {
        return String.format("UPDATE message SET state = %s WHERE id = %s", state, clientMsgNo);
    }

    static String sqlSetMessagesRead(int count, long readTime) {
        return "UPDATE message SET has_read = 1, read_time = ? WHERE message_uid in " + CursorHelper.getQuestionMarkPlaceholder(count);
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

    static final String SQL_BATCH_SET_STATE_FAIL = "UPDATE message set state = 3 WHERE state = 1 OR state = 4";
    static final String SQL_ORDER_BY_TIMESTAMP = " ORDER BY timestamp";
    static final String SQL_DESC = " DESC";
    static final String SQL_LIMIT = " LIMIT ";

    static String sqlUpdateMessageAfterSend(int state, long clientMsgNo, long timestamp, long seqNo, int groupMemberCount) {
        return String.format("UPDATE message SET message_uid = ?, state = %s, timestamp = %s, seq_no = %s, member_count = %s, destroy_time = CASE WHEN life_time != 0 THEN ? + life_time ELSE destroy_time END WHERE id = %s", state, timestamp, seqNo, groupMemberCount, clientMsgNo);
    }

    static String sqlUpdateMessageAfterSendWithClientUid(int state, long timestamp, long seqNo, int groupMemberCount) {
        return String.format("UPDATE message SET message_uid = ?, state = %s, timestamp = %s, seq_no = %s, member_count = %s, destroy_time = CASE WHEN life_time != 0 THEN ? + life_time ELSE destroy_time END WHERE client_uid = ?", state, timestamp, seqNo, groupMemberCount);
    }

    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_ID = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE message_uid = ?";
    static final String SQL_UPDATE_MESSAGE_CONTENT_WITH_MESSAGE_NO = "UPDATE message SET content = ?, type = ?, search_content = ? WHERE id = ?";

    static String sqlDeleteMessagesByMessageId(int count) {
        return "UPDATE message SET is_deleted = 1 WHERE message_uid IN " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlDeleteMessagesByClientMsgNo(int count) {
        return "UPDATE message SET is_deleted = 1 WHERE id IN " + CursorHelper.getQuestionMarkPlaceholder(count);
    }

    static String sqlClearMessages(Conversation conversation, long startTime, String senderId) {
        String subChannel = conversation.getSubChannel();
        if (subChannel == null) {
            subChannel = "";
        }
        String sql = String.format("UPDATE message SET is_deleted = 1 WHERE conversation_type = %s AND conversation_id = '%s' AND subchannel = '%s' AND timestamp <= %s", conversation.getConversationType().getValue(), conversation.getConversationId(), subChannel, startTime);
        if (!TextUtils.isEmpty(senderId)) {
            sql = sql + String.format(" AND sender = '%s'", senderId);
        }
        return sql;
    }

    static String sqlClearChatroomMessagesExclude(int count) {
        return "DELETE FROM message WHERE conversation_type = 3 AND conversation_id NOT IN " + CursorHelper.getQuestionMarkPlaceholder(count);
    }
    static final String SQL_CLEAR_CHATROOM_MESSAGES_IN = "DELETE FROM message WHERE conversation_type = 3 AND conversation_id = ?";

    static String sqlUpdateLocalAttribute(String messageId) {
        return String.format("UPDATE message SET local_attribute = ? WHERE message_uid = '%s'", messageId);
    }

    static String sqlUpdateLocalAttribute(long clientMsgNo) {
        return String.format("UPDATE message SET local_attribute = ? WHERE id = '%s'", clientMsgNo);
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
    static final String COL_FLAGS = "flags";
    static final String COL_LIFE_TIME = "life_time";
    static final String COL_LIFE_TIME_AFTER_READ = "life_time_after_read";
    static final String COL_DESTROY_TIME = "destroy_time";
    static final String COL_READ_TIME = "read_time";
    static final String COL_SUB_CHANNEL = "subchannel";
}
