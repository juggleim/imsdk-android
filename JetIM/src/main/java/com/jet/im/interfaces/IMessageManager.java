package com.jet.im.interfaces;

import com.jet.im.JetIMConst;
import com.jet.im.model.Conversation;
import com.jet.im.model.GroupMessageReadInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.UserInfo;

import java.util.List;
import java.util.Map;

public interface IMessageManager {
    interface ISendMessageCallback {
        void onSuccess(Message message);

        void onError(Message message, int errorCode);
    }

    interface IGetLocalAndRemoteMessagesCallback {
        void onGetLocalList(List<Message> messages, boolean hasRemote);

        void onGetRemoteList(List<Message> messages);

        void onGetRemoteListError(int errorCode);
    }

    interface IGetMessagesCallback {
        void onSuccess(List<Message> messages);

        void onError(int errorCode);
    }

    interface IRecallMessageCallback {
        void onSuccess(Message message);

        void onError(int errorCode);
    }

    interface ISendReadReceiptCallback {
        void onSuccess();

        void onError(int errorCode);
    }

    interface IGetGroupMessageReadDetailCallback {
        void onSuccess(List<UserInfo> readMembers, List<UserInfo> unreadMembers);

        void onError(int errorCode);
    }

    Message sendMessage(MessageContent content,
                        Conversation conversation,
                        ISendMessageCallback callback);

    Message resendMessage(Message message,
                          ISendMessageCallback callback);

    Message saveMessage(MessageContent content, Conversation conversation);

    List<Message> getMessages(Conversation conversation,
                              int count,
                              long timestamp,
                              JetIMConst.PullDirection direction);

    List<Message> getMessages(Conversation conversation,
                              int count,
                              long timestamp,
                              JetIMConst.PullDirection direction,
                              List<String> contentTypes);

    List<Message> getMessagesByMessageIds(List<String> messageIdList);

    void getMessagesByMessageIds(Conversation conversation,
                                 List<String> messageIds,
                                 IGetMessagesCallback callback);

    List<Message> getMessagesByClientMsgNos(long[] clientMsgNos);

    List<Message> searchMessage(
            String searchContent,
            int count,
            long timestamp,
            JetIMConst.PullDirection direction);

    List<Message> searchMessage(
            String searchContent,
            int count,
            long timestamp,
            JetIMConst.PullDirection direction,
            List<String> contentTypes);

    void deleteMessageByClientMsgNo(long clientMsgNo);

    void deleteMessageByMessageId(String messageId);

    void clearMessages(Conversation conversation);

    void recallMessage(String messageId, IRecallMessageCallback callback);

    void getRemoteMessages(Conversation conversation,
                           int count,
                           long startTime,
                           JetIMConst.PullDirection direction,
                           IGetMessagesCallback callback);

    void getLocalAndRemoteMessages(Conversation conversation,
                                   int count,
                                   long startTime,
                                   JetIMConst.PullDirection direction,
                                   IGetLocalAndRemoteMessagesCallback callback);

    void sendReadReceipt(Conversation conversation,
                         List<String> messageIds,
                         ISendReadReceiptCallback callback);

    void getGroupMessageReadDetail(Conversation conversation,
                                   String messageId,
                                   IGetGroupMessageReadDetailCallback callback);

    void getMergedMessageList(String messageId,
                              IGetMessagesCallback callback);

    void setLocalAttribute(String messageId, String attribute);

    String getLocalAttribute(String messageId);

    void setMessageState(long clientMsgNo, Message.MessageState state);

    void registerContentType(Class<? extends MessageContent> messageContentClass);

    void addListener(String key, IMessageListener listener);

    void removeListener(String key);

    void addSyncListener(String key, IMessageSyncListener listener);

    void removeSyncListener(String key);

    void addReadReceiptListener(String key, IMessageReadReceiptListener listener);

    void removeReadReceiptListener(String key);

    interface IMessageListener {
        void onMessageReceive(Message message);

        void onMessageRecall(Message message);
    }

    interface IMessageSyncListener {
        void onMessageSyncComplete();
    }

    interface IMessageReadReceiptListener {
        void onMessagesRead(Conversation conversation, List<String> messageIds);

        void onGroupMessagesRead(Conversation conversation, Map<String, GroupMessageReadInfo> messages);
    }
}
