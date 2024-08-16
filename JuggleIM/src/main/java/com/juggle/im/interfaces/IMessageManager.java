package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageOptions;
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;

import java.util.List;
import java.util.Map;

public interface IMessageManager {
    interface ISimpleCallback {
        void onSuccess();

        void onError(int errorCode);
    }

    interface ISendMessageCallback {
        void onSuccess(Message message);

        void onError(Message message, int errorCode);
    }

    interface ISendMediaMessageCallback {
        void onProgress(int progress, Message message);

        void onSuccess(Message message);

        void onError(Message message, int errorCode);

        void onCancel(Message message);
    }

    interface IDownloadMediaMessageCallback {
        void onProgress(int progress, Message message);

        void onSuccess(Message message);

        void onError(int errorCode);

        void onCancel(Message message);
    }

    interface IGetLocalAndRemoteMessagesCallback {
        void onGetLocalList(List<Message> messages, boolean hasRemote);

        void onGetRemoteList(List<Message> messages);

        void onGetRemoteListError(int errorCode);
    }

    interface IGetMessagesCallbackV2 {
        //messages: 消息列表，code: 结果码，0 为成功
        void onGetLocalMessages(List<Message> messages, int code);
        //messages: 消息列表，timestamp: 消息时间戳，拉下一批消息的时候可以使用，hasMore: 是否还有更多消息，code: 结果码，0 为成功
        void onGetRemoteMessages(List<Message> messages, long timestamp, boolean hasMore, int code);
    }

    interface IGetMessagesCallback {
        void onSuccess(List<Message> messages);

        void onError(int errorCode);
    }

    interface IGetMessagesWithFinishCallback {
        void onSuccess(List<Message> messages, boolean isFinished);

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

    interface IBroadcastMessageCallback {
        void onProgress(Message message, int errorCode, int processCount, int totalCount);

        void onComplete();
    }

    interface IGetMuteStatusCallback {
        void onSuccess(boolean isMute, String timezone, List<TimePeriod> periods);
        void onError(int errorCode);
    }

    Message sendMessage(MessageContent content,
                        Conversation conversation,
                        ISendMessageCallback callback);

    Message sendMessage(MessageContent content,
                        Conversation conversation,
                        MessageOptions options,
                        ISendMessageCallback callback);

    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             ISendMediaMessageCallback callback);

    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             MessageOptions options,
                             ISendMediaMessageCallback callback);

    Message resendMessage(Message message,
                          ISendMessageCallback callback);

    Message resendMediaMessage(Message message,
                               ISendMediaMessageCallback callback);

    Message saveMessage(MessageContent content, Conversation conversation);

    Message saveMessage(MessageContent content, Conversation conversation, MessageOptions options);

    List<Message> getMessages(Conversation conversation,
                              int count,
                              long timestamp,
                              JIMConst.PullDirection direction);

    List<Message> getMessages(Conversation conversation,
                              int count,
                              long timestamp,
                              JIMConst.PullDirection direction,
                              List<String> contentTypes);

    List<Message> getMessages(int count, long timestamp, JIMConst.PullDirection direction, MessageQueryOptions messageQueryOptions);

    List<Message> getMessagesByMessageIds(List<String> messageIdList);

    void getMessagesByMessageIds(Conversation conversation,
                                 List<String> messageIds,
                                 IGetMessagesCallback callback);

    List<Message> getMessagesByClientMsgNos(long[] clientMsgNos);

    List<Message> searchMessage(
            String searchContent,
            int count,
            long timestamp,
            JIMConst.PullDirection direction);

    List<Message> searchMessage(
            String searchContent,
            int count,
            long timestamp,
            JIMConst.PullDirection direction,
            List<String> contentTypes);

    List<Message> searchMessageInConversation(
            Conversation conversation,
            String searchContent,
            int count,
            long timestamp,
            JIMConst.PullDirection direction);


    /**
     * 下载多媒体文件。
     *
     * @param messageId 媒体消息（FileMessage，SightMessage，GIFMessage, HQVoiceMessage等）。
     * @param callback  下载文件的回调。参考 {@link IDownloadMediaMessageCallback}。
     */
    void downloadMediaMessage(
            final String messageId, final IDownloadMediaMessageCallback callback);

    void cancelDownloadMediaMessage(String messageId);

    List<Message> searchMessageInConversation(
            Conversation conversation,
            String searchContent,
            int count,
            long timestamp,
            JIMConst.PullDirection direction,
            List<String> contentTypes);

    void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, ISimpleCallback callback);

    void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, ISimpleCallback callback);

    void clearMessages(Conversation conversation, long startTime, ISimpleCallback callback);

    void recallMessage(String messageId, Map<String, String> extras, IRecallMessageCallback callback);

    void getRemoteMessages(Conversation conversation,
                           int count,
                           long startTime,
                           JIMConst.PullDirection direction,
                           IGetMessagesWithFinishCallback callback);

    void getLocalAndRemoteMessages(Conversation conversation,
                                   int count,
                                   long startTime,
                                   JIMConst.PullDirection direction,
                                   IGetLocalAndRemoteMessagesCallback callback);

    /// 获取消息，结果按照消息时间正序排列（旧的在前，新的在后）。该接口必定回调两次，先回调本地的缓存消息（有可能存在缺失），再回调远端的消息。
    void getMessages(Conversation conversation,
                     JIMConst.PullDirection direction,
                     GetMessageOptions options,
                     IGetMessagesCallbackV2 callback);

    void sendReadReceipt(Conversation conversation,
                         List<String> messageIds,
                         ISendReadReceiptCallback callback);

    void getGroupMessageReadDetail(Conversation conversation,
                                   String messageId,
                                   IGetGroupMessageReadDetailCallback callback);

    void getMergedMessageList(String containerMsgId,
                              IGetMessagesCallback callback);

    void getMentionMessageList(Conversation conversation,
                               int count,
                               long time,
                               JIMConst.PullDirection direction,
                               IGetMessagesWithFinishCallback callback);

    void setLocalAttribute(String messageId, String attribute);

    String getLocalAttribute(String messageId);

    void setLocalAttribute(long clientMsgNo, String attribute);

    String getLocalAttribute(long clientMsgNo);

    void broadcastMessage(MessageContent content,
                          List<Conversation> conversations,
                          IBroadcastMessageCallback callback);

    /**
     * 设置消息全局免打扰。
     *
     * @param isMute 是否免打扰
     * @param periods 免打扰的时间段，如果为空则视为全天免打扰
     * @param callback  结果回调
     */
    void setMute(boolean isMute, List<TimePeriod> periods, ISimpleCallback callback);

    void getMuteStatus(IGetMuteStatusCallback callback);

    void setMessageState(long clientMsgNo, Message.MessageState state);

    void registerContentType(Class<? extends MessageContent> messageContentClass);

    void addListener(String key, IMessageListener listener);

    void removeListener(String key);

    void addSyncListener(String key, IMessageSyncListener listener);

    void removeSyncListener(String key);

    void addReadReceiptListener(String key, IMessageReadReceiptListener listener);

    void removeReadReceiptListener(String key);

    void setMessageUploadProvider(IMessageUploadProvider uploadProvider);

    interface IMessageListener {
        void onMessageReceive(Message message);

        void onMessageRecall(Message message);

        void onMessageDelete(Conversation conversation, List<Long> clientMsgNos);

        //当 senderId 有值时，表示只清空这个用户发送的消息
        void onMessageClear(Conversation conversation, long timestamp, String senderId);
    }

    interface IMessageSyncListener {
        void onMessageSyncComplete();
    }

    interface IMessageReadReceiptListener {
        void onMessagesRead(Conversation conversation, List<String> messageIds);

        void onGroupMessagesRead(Conversation conversation, Map<String, GroupMessageReadInfo> messages);
    }
}
