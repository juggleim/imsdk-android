package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.FavoriteMessage;
import com.juggle.im.model.GetFavoriteMessageOption;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.GroupMessageReadInfoDetail;
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
        // messages: message list, code: result code, 0 indicates success.
        void onGetLocalMessages(List<Message> messages, int code);
        // messages: message list, timestamp: message timestamp for fetching the next batch, hasMore: whether more messages are available, code: result code, 0 indicates success.
        void onGetRemoteMessages(List<Message> messages, long timestamp, boolean hasMore, int code);
    }

    interface IGetMessagesCallbackV3 {
        /**
         * Result callback.
         * @param messages Message list.
         * @param timestamp Message timestamp for fetching the next batch.
         * @param hasMore Whether more messages are available.
         * @param code Result code. 0 indicates success. When code is not 0, cached local messages are returned in messages if they exist locally.
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
     * Sends a media message by uploading the media first and then sending the message.
     * @param content Media message entity.
     * @param conversation Conversation.
     * @param callback Send callback.
     * @return Message object.
     */
    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             ISendMediaMessageCallback callback);

    Message sendMediaMessage(MediaMessageContent content,
                             Conversation conversation,
                             MessageOptions options,
                             ISendMediaMessageCallback callback);

    /**
     * Resends a message after a send failure. If the message was already sent successfully,
     * the success callback is returned directly.
     *
     * @param message Message object.
     * @param callback Result callback. See {@link ISendMessageCallback}.
     */
    Message resendMessage(Message message,
                          ISendMessageCallback callback);

    /**
     * Resends a media message after a send failure. If the message was already sent successfully,
     * the success callback is returned directly.
     *
     * @param message Message object.
     * @param callback Result callback. See {@link ISendMediaMessageCallback}.
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
     * Gets the first unread message in a conversation.
     *
     * @param conversation Conversation identifier.
     * @param callback Callback for downloading the file. See {@link IDownloadMediaMessageCallback}.
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

    List<Message> searchMessageInConversation(
            Conversation conversation,
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
            JIMConst.PullDirection direction,
            List<String> contentTypes,
            List<String> senderUserIds);

    /**
     * Searches conversations by keywords in messages.
     *
     * @param options Search options.
     * @param callback Result callback. See {@link ISearchConversationWithMessageContentCallback}.
     */
    void searchConversationsWithMessageContent(MessageQueryOptions options, ISearchConversationWithMessageContentCallback callback);

    /**
     * Downloads a media file.
     *
     * @param messageId Media message, such as FileMessage, SightMessage, GIFMessage, or HQVoiceMessage.
     * @param callback Callback for downloading the file. See {@link IDownloadMediaMessageCallback}.
     */
    void downloadMediaMessage(
            final String messageId, final IDownloadMediaMessageCallback callback);

    void cancelDownloadMediaMessage(String messageId);

    /**
     * Deletes messages in the same conversation in batches by message ID.
     * @param conversation Conversation identifier.
     * @param messageIds Message ID list.
     * @param callback Result callback.
     */
    void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, ISimpleCallback callback);

    /**
     * Deletes messages in the same conversation in batches by local unique message number.
     * @param conversation Conversation identifier.
     * @param clientMsgNos Local unique message number list.
     * @param callback Result callback.
     */
    void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, ISimpleCallback callback);

    /**
     * Deletes messages in the same conversation in batches by message ID.
     * @param conversation Conversation identifier.
     * @param messageIds Message ID list.
     * @param forAllUsers Whether to delete the messages for all users in the conversation.
     * @param callback Result callback.
     */
    void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, boolean forAllUsers, ISimpleCallback callback);

    /**
     * Deletes messages in the same conversation in batches by local unique message number.
     * @param conversation Conversation identifier.
     * @param clientMsgNos Local unique message number list.
     * @param forAllUsers Whether to delete the messages for all users in the conversation.
     * @param callback Result callback.
     */
    void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, boolean forAllUsers, ISimpleCallback callback);

    /**
     * Clears all messages before the specified time in a conversation. Pass 0 for startTime to use the current time.
     * @param conversation Conversation identifier.
     * @param startTime Start time. Pass 0 to use the current time.
     * @param callback Result callback.
     */
    void clearMessages(Conversation conversation, long startTime, ISimpleCallback callback);

    /**
     * Clears all messages before the specified time in a conversation. Pass 0 for startTime to use the current time.
     * @param conversation Conversation identifier.
     * @param startTime Start time. Pass 0 to use the current time.
     * @param forAllUsers Whether to clear messages for all users in the conversation.
     * @param callback Result callback.
     */
    void clearMessages(Conversation conversation, long startTime, boolean forAllUsers, ISimpleCallback callback);

    /**
     * Physically deletes all messages before the specified time. Only local messages are deleted,
     * which can be used to free local storage space.
     * @param timestamp Timestamp. Pass 0 to use the current time.
     * @param conversationTypes Conversation type list to delete. Pass null to delete messages for all conversation types.
     */
    void purgeMessages(long timestamp, List<Conversation.ConversationType> conversationTypes);

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

    /// Gets messages. Results are sorted by message time in ascending order, with older messages first and newer messages later. This API always invokes the callback twice: first with locally cached messages, which may have gaps, and then with remote messages.
    void getMessages(Conversation conversation,
                     JIMConst.PullDirection direction,
                     GetMessageOptions options,
                     IGetMessagesCallbackV2 callback);

    /**
     * Gets messages. Results are sorted by message time in ascending order, with older messages
     * first and newer messages later. When messages are missing and the network has a problem,
     * locally cached messages are returned.
     * @param conversation Conversation object.
     * @param direction Fetch direction.
     * @param options Message fetch options.
     * @param callback Callback.
     */
    void getMessages(Conversation conversation,
                     JIMConst.PullDirection direction,
                     GetMessageOptions options,
                     IGetMessagesCallbackV3 callback);

    void sendReadReceipt(Conversation conversation,
                         List<String> messageIds,
                         ISendReadReceiptCallback callback);

    /**
     * Gets the read status of a group message.
     * @param conversation Conversation containing the message.
     * @param messageId ID of the group message to query.
     * @param callback Result callback.
     */
    void getGroupMessageReadInfoDetail(Conversation conversation,
                                       String messageId,
                                       JIMConst.IResultCallback<GroupMessageReadInfoDetail> callback);

    /**
     * Gets the read time of a one-to-one chat message. Use getGroupMessageReadInfoDetail for group message read status.
     * @param clientMsgNo Local unique message number.
     */
    long getMessageReadTime(long clientMsgNo);

    /**
     * Gets the list of merged messages.
     * @param containerMsgId Merged message ID.
     * @param callback Result callback.
     */
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
     * Adds a message reaction.
     * @param messageId Message ID.
     * @param conversation Conversation that the message belongs to.
     * @param reactionId Reaction ID.
     * @param callback Result callback.
     */
    void addMessageReaction(String messageId,
                            Conversation conversation,
                            String reactionId,
                            ISimpleCallback callback);

    /**
     * Deletes a message reaction.
     * @param messageId Message ID.
     * @param conversation Conversation that the message belongs to.
     * @param reactionId Reaction ID.
     * @param callback Result callback.
     */
    void removeMessageReaction(String messageId,
                               Conversation conversation,
                               String reactionId,
                               ISimpleCallback callback);

    /**
     * Gets message reactions in batches. Messages must belong to the same conversation.
     * @param messageIdList Message ID list.
     * @param conversation Conversation that the messages belong to.
     * @param callback Result callback.
     */
    void getMessagesReaction(List<String> messageIdList,
                             Conversation conversation,
                             IMessageReactionListCallback callback);

    /**
     * Gets cached message reactions. Cached data may not be the latest version and can be used
     * to render immediately for a better user experience.
     * @param messageIdList Message ID list.
     * @return Message reaction list.
     */
    List<MessageReaction> getCachedMessagesReaction(List<String> messageIdList);

    /**
     * Uploads an image.
     * @param localPath Image path.
     * @param callback Returns the remote image URL on success.
     */
    void uploadImage(String localPath, JIMConst.IResultCallback<String> callback);

    /**
     * Sets global message mute.
     *
     * @param isMute Whether to mute.
     * @param periods Mute time periods. If empty, it is treated as all-day mute.
     * @param callback Result callback.
     */
    void setMute(boolean isMute, List<TimePeriod> periods, ISimpleCallback callback);

    void getMuteStatus(IGetMuteStatusCallback callback);

    void setMessageState(long clientMsgNo, Message.MessageState state);

    /**
     * Sets a message as top.
     * @param messageId Message ID.
     * @param conversation Identifier of the conversation that the message belongs to.
     * @param isTop Whether to set as top.
     * @param callback Result callback.
     */
    void setTop(String messageId, Conversation conversation, boolean isTop, ISimpleCallback callback);

    /**
     * Gets the top message.
     * @param conversation Conversation identifier.
     * @param callback Result callback.
     */
    void getTopMessage(Conversation conversation, IGetTopMessageCallback callback);

    /**
     * Adds messages to favorites.
     * @param messageIdList List of message IDs to add to favorites.
     * @param callback Result callback.
     */
    void addFavorite(List<String> messageIdList, ISimpleCallback callback);

    /**
     * Removes messages from favorites.
     * @param messageIdList List of message IDs to remove from favorites.
     * @param callback Result callback.
     */
    void removeFavorite(List<String> messageIdList, ISimpleCallback callback);

    /**
     * Gets favorite messages.
     * @param option Query options.
     * @param callback Result callback.
     */
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

    void addStreamMessageListener(String key, IStreamMessageListener listener);

    void removeStreamMessageListener(String key);

    void setMessageUploadProvider(IMessageUploadProvider uploadProvider);

    interface IMessageListener {
        void onMessageReceive(Message message);

        void onMessageRecall(Message message);

        void onMessageDelete(Conversation conversation, List<Long> clientMsgNos);

        // When senderId has a value, only messages sent by this user are cleared.
        void onMessageClear(Conversation conversation, long timestamp, String senderId);

        // Callback for message updates.
        void onMessageUpdate(Message message);

        // Callback for added message reactions.
        void onMessageReactionAdd(Conversation conversation, MessageReaction reaction);

        // Callback for deleted message reactions.
        void onMessageReactionRemove(Conversation conversation, MessageReaction reaction);

        // Callback for setting a message as top.
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
         * Callback for message destroy time updates. This usually occurs in scenarios such as burn-after-reading.
         * @param messageId Message ID.
         * @param conversation Conversation containing the message.
         * @param destroyTime Updated destroy time.
         */
        void onMessageDestroyTimeUpdate(String messageId, Conversation conversation, long destroyTime);
    }

    interface IMessagePreprocessor {
        /**
         * Callback for message encryption.
         * Callback timing: after the message is stored in the database and before it is sent.
         * @param content Message content to send, already serialized into a byte array.
         * @param conversation Conversation containing the message.
         * @param contentType Message type.
         * @return Processed message content.
         */
        byte[] encryptMessageContent(byte[] content, Conversation conversation, String contentType);

        /**
         * Callback for message decryption.
         * Callback timing: after the message is received and before it is stored in the database.
         * @param content Received message content as a byte array, not yet deserialized.
         * @param conversation Conversation containing the message.
         * @param contentType Message type.
         * @return Processed message content.
         */
        byte[] decryptMessageContent(byte[] content, Conversation conversation, String contentType);
    }

    interface IStreamMessageListener {
        /**
         * Callback for appending a streaming message fragment.
         * @param messageId Message ID of the streaming message.
         * @param content Appended fragment content. Developers can append content to the end of JStreamTextMessage content in the UI.
         */
        void onStreamTextMessageAppend(String messageId, String content);

        /**
         * Callback for streaming message completion.
         * @param message Completed streaming message. Developers can find the corresponding streaming message in the UI by messageId and refresh the UI.
         */
        void onStreamTextMessageComplete(Message message);
    }
}
