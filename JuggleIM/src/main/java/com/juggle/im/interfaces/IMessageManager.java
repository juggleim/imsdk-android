package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.FavoriteMessage;
import com.juggle.im.model.GetFavoriteMessageOption;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageOptions;
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.SearchConversationsResult;
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

    interface IGetMessagesCallbackV3 {
        /**
         * 结果回调
         * @param messages 消息列表
         * @param timestamp 消息时间戳，拉下一批消息的时候可以使用
         * @param hasMore 是否还有更多消息
         * @param code 结果码，0 为成功。code 不为 0 的时候，如果本地存在缓存消息，则会在 messages 里返回本地消息
         */
        void onGetMessages(List<Message> messages, long timestamp, boolean hasMore, int code);
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

    interface IMessageCallback {
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

    interface IMessageReactionListCallback {
        void onSuccess(List<MessageReaction> reactionList);
        void onError(int errorCode);
    }

    interface IGetMuteStatusCallback {
        void onSuccess(boolean isMute, String timezone, List<TimePeriod> periods);
        void onError(int errorCode);
    }

    interface ISearchConversationWithMessageContentCallback {
        void onComplete(List<SearchConversationsResult> resultList);
    }

    interface IGetTopMessageCallback {
        void onSuccess(Message message, UserInfo operator, long timestamp);
        void onError(int errorCode);
    }

    interface IGetFavoriteMessageCallback {
        void onSuccess(List<FavoriteMessage> messageList, String offset);
        void onError(int errorCode);
    }

    Message sendMessage(MessageContent content,
                        Conversation conversation,
                        ISendMessageCallback callback);

    Message sendMessage(MessageContent content,
                        Conversation conversation,
                        MessageOptions options,
                        ISendMessageCallback callback);

    /**
     * 发送媒体消息（先上传媒体，再发送消息）
     * @param content 媒体消息实体
     * @param conversation 会话
     * @param callback 发送回调
     * @return 消息对象
     */
    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             ISendMediaMessageCallback callback);

    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             MessageOptions options,
                             ISendMediaMessageCallback callback);

    /**
     * 重发消息，用于发送失败后进行重发（如果消息已经发送成功则直接返回成功回调）
     *
     * @param message 消息对象。
     * @param callback 结果回调。参考 {@link ISendMessageCallback}。
     */
    Message resendMessage(Message message,
                          ISendMessageCallback callback);

    /**
     * 重发消息，用于媒体类型消息发送失败后重发（如果消息已经发送成功则直接返回成功回调）
     *
     * @param message 消息对象。
     * @param callback 结果回调。参考 {@link ISendMediaMessageCallback}。
     */
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

    /**
     * 获取会话中第一条未读消息。
     *
     * @param conversation 会话标识。
     * @param callback  下载文件的回调。参考 {@link IDownloadMediaMessageCallback}。
     */
    void getFirstUnreadMessage(Conversation conversation, IGetMessagesCallback callback);

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
     * 根据消息中的关键字搜索会话。
     *
     * @param options 搜索条件。
     * @param callback 结果回调。参考 {@link ISearchConversationWithMessageContentCallback}。
     */
    void searchConversationsWithMessageContent(MessageQueryOptions options, ISearchConversationWithMessageContentCallback callback);

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

    /**
     * 在同一个会话里，根据消息 id 批量删除消息
     * @param conversation 会话标识
     * @param messageIds 消息 id 列表
     * @param callback 结果回调
     */
    void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, ISimpleCallback callback);

    /**
     * 在同一个会话里，根据本端消息唯一编号批量删除消息
     * @param conversation 会话标识
     * @param clientMsgNos 本端消息唯一编号列表
     * @param callback 结果回调
     */
    void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, ISimpleCallback callback);

    /**
     * 在同一个会话里，根据消息 id 批量删除消息
     * @param conversation 会话标识
     * @param messageIds 消息 id 列表
     * @param forAllUsers 是否对会话里所有用户删除消息
     * @param callback 结果回调
     */
    void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, boolean forAllUsers, ISimpleCallback callback);

    /**
     * 在同一个会话里，根据本端消息唯一编号批量删除消息
     * @param conversation 会话标识
     * @param clientMsgNos 本端消息唯一编号列表
     * @param forAllUsers 是否对会话里所有用户删除消息
     * @param callback 结果回调
     */
    void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, boolean forAllUsers, ISimpleCallback callback);

    /**
     * 清空会话中指定时间之前的所有消息，startTime 传 0 表示当前时间
     * @param conversation 会话标识
     * @param startTime 开始时间，传 0 表示当前时间
     * @param callback 结果回调
     */
    void clearMessages(Conversation conversation, long startTime, ISimpleCallback callback);

    /**
     * 清空会话中指定时间之前的所有消息，startTime 传 0 表示当前时间
     * @param conversation 会话标识
     * @param startTime 开始时间，传 0 表示当前时间
     * @param forAllUsers 是否对会话里所有用户清除消息
     * @param callback 结果回调
     */
    void clearMessages(Conversation conversation, long startTime, boolean forAllUsers, ISimpleCallback callback);

    void recallMessage(String messageId, Map<String, String> extras, IRecallMessageCallback callback);

    void updateMessage(String messageId, MessageContent content, Conversation conversation, IMessageCallback callback);

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

    /**
     * 获取消息，结果按照消息时间正序排列（旧的在前，新的在后）。当消息有缺失并且网络有问题的时候，返回本地缓存的消息。
     * @param conversation 会话对象
     * @param direction 拉取方向
     * @param options 获取消息选项
     * @param callback 回调
     */
    void getMessages(Conversation conversation,
                     JIMConst.PullDirection direction,
                     GetMessageOptions options,
                     IGetMessagesCallbackV3 callback);

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
     * 添加消息回应
     * @param messageId 消息 id
     * @param conversation 消息所属会话
     * @param reactionId 回应 id
     * @param callback 结果回调
     */
    void addMessageReaction(String messageId,
                            Conversation conversation,
                            String reactionId,
                            ISimpleCallback callback);

    /**
     * 删除消息回应
     * @param messageId 消息 id
     * @param conversation 消息所属会话
     * @param reactionId 回应 id
     * @param callback 结果回调
     */
    void removeMessageReaction(String messageId,
                               Conversation conversation,
                               String reactionId,
                               ISimpleCallback callback);

    /**
     * 批量获取消息回应（消息必须属于同一个会话）
     * @param messageIdList 消息 id 列表
     * @param conversation 消息所属会话
     * @param callback 结果回调
     */
    void getMessagesReaction(List<String> messageIdList,
                             Conversation conversation,
                             IMessageReactionListCallback callback);

    /**
     * 获取缓存的消息回应（缓存的数据不一定是最新版本，可用于第一时间渲染，优化用户体验）
     * @param messageIdList 消息 id 列表
     * @return 消息回应列表
     */
    List<MessageReaction> getCachedMessagesReaction(List<String> messageIdList);

    /**
     * 上传图片
     * @param localPath 图片路径
     * @param callback 成功的时候回调图片的远端 url
     */
    void uploadImage(String localPath, JIMConst.IResultCallback<String> callback);

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

    /**
     * 设置置顶
     * @param messageId 消息 id
     * @param conversation 消息所属会话标识
     * @param isTop 是否置顶
     * @param callback 结果回调
     */
    void setTop(String messageId, Conversation conversation, boolean isTop, ISimpleCallback callback);

    /**
     * 获取置顶消息
     * @param conversation 会话标识
     * @param callback 结果回调
     */
    void getTopMessage(Conversation conversation, IGetTopMessageCallback callback);

    void addFavorite(List<String> messageIdList, ISimpleCallback callback);

    void removeFavorite(List<String> messageIdList, ISimpleCallback callback);

    void getFavorite(GetFavoriteMessageOption option, IGetFavoriteMessageCallback callback);

    void registerContentType(Class<? extends MessageContent> messageContentClass);

    void addListener(String key, IMessageListener listener);

    void removeListener(String key);

    void addSyncListener(String key, IMessageSyncListener listener);

    void removeSyncListener(String key);

    void addReadReceiptListener(String key, IMessageReadReceiptListener listener);

    void removeReadReceiptListener(String key);

    void addDestroyListener(String key, IMessageDestroyListener listener);

    void removeDestroyListener(String key);

    void setPreprocessor(IMessagePreprocessor preprocessor);

    void setMessageUploadProvider(IMessageUploadProvider uploadProvider);

    interface IMessageListener {
        void onMessageReceive(Message message);

        void onMessageRecall(Message message);

        void onMessageDelete(Conversation conversation, List<Long> clientMsgNos);

        //当 senderId 有值时，表示只清空这个用户发送的消息
        void onMessageClear(Conversation conversation, long timestamp, String senderId);

        //消息修改的回调
        void onMessageUpdate(Message message);

        //新增消息回应的回调
        void onMessageReactionAdd(Conversation conversation, MessageReaction reaction);

        //删除消息回应的回调
        void onMessageReactionRemove(Conversation conversation, MessageReaction reaction);

        //消息置顶的回调
        void onMessageSetTop(Message message, UserInfo operator, boolean isTop);
    }

    interface IMessageSyncListener {
        void onMessageSyncComplete();
    }

    interface IMessageReadReceiptListener {
        void onMessagesRead(Conversation conversation, List<String> messageIds);

        void onGroupMessagesRead(Conversation conversation, Map<String, GroupMessageReadInfo> messages);
    }

    interface IMessageDestroyListener {
        /**
         * 消息销毁时间更新回调（一般发生在阅后即焚之类的场景）
         * @param messageId 消息 id
         * @param conversation 所在会话
         * @param destroyTime 更新后的销毁时间
         */
        void onMessageDestroyTimeUpdate(String messageId, Conversation conversation, long destroyTime);
    }

    interface IMessagePreprocessor {
        /**
         * 消息加密的回调
         * 回调时机：消息入库之后，发送之前
         * @param content 待发送的消息内容，已序列化成 byte[]
         * @param conversation 所在会话
         * @param contentType 消息类型
         * @return 处理后的消息内容。
         */
        byte[] encryptMessageContent(byte[] content, Conversation conversation, String contentType);

        /**
         * 消息解密的回调
         * 回调时机：接收到消息，入库之前
         * @param content 接收到的消息内容, byte[] 类型，还没反序列化
         * @param conversation 所在会话
         * @param contentType 消息类型
         * @return 处理后的消息内容。
         */
        byte[] decryptMessageContent(byte[] content, Conversation conversation, String contentType);
    }
}
