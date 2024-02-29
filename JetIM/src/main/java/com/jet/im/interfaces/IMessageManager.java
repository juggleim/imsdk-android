package com.jet.im.interfaces;

import com.jet.im.JetIMConst;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;

import java.util.List;

public interface IMessageManager {
    interface ISendMessageCallback {
        void onSuccess(Message message);
        void onError(Message message, int errorCode);
    }

    interface IGetMessagesCallback {
        void onSuccess(List<Message> messages);
        void onError(int errorCode);
    }

    interface IRecallMessageCallback {
        void onSuccess(Message message);
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

    List<Message> getMessagesByClientMsgNos(long[] clientMsgNos);

    void deleteMessageByClientMsgNo(long clientMsgNo);

    void deleteMessageByMessageId(String messageId);

    void clearMessages(Conversation conversation);

    void recallMessage(String messageId, IRecallMessageCallback callback);

    void getRemoteMessages(Conversation conversation,
                           int count,
                           long startTime,
                           JetIMConst.PullDirection direction,
                           IGetMessagesCallback callback);

    void registerContentType(Class<? extends MessageContent> messageContentClass);
    void addListener(String key, IMessageListener listener);
    void removeListener(String key);

    void addSyncListener(String key, IMessageSyncListener listener);
    void removeSyncListener(String key);

    interface IMessageListener {
        void onMessageReceive(Message message);
        void onMessageRecall(Message message);
    }

    interface IMessageSyncListener {
        void onMessageSyncComplete();
    }
}
