package com.jet.im.kit.vm;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jet.im.kit.consts.MessageLoadState;
import com.jet.im.kit.consts.ReplyType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.internal.contracts.MessageCollectionContract;
import com.jet.im.kit.internal.contracts.MessageCollectionImpl;
import com.jet.im.kit.internal.contracts.SendbirdChatContract;
import com.jet.im.kit.internal.contracts.SendbirdChatImpl;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.configurations.ChannelConfig;
import com.jet.im.kit.utils.Available;
import com.jet.im.kit.utils.MessageUtils;
import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.UserInfo;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Feedback;
import com.sendbird.android.message.FeedbackRating;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.common.MessagePayloadFilter;
import com.sendbird.android.user.User;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of messages in a channel
 * <p>
 * since 3.0.0
 */
public class ChannelViewModel extends BaseMessageListViewModel {
    @NonNull
    private final String ID_CHANNEL_EVENT_HANDLER = "ID_CHANNEL_EVENT_HANDLER" + System.currentTimeMillis();
    @NonNull
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT" + System.currentTimeMillis();
    @NonNull
    private final MutableLiveData<List<UserInfo>> typingMembers = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<ConversationInfo> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<Message>> messagesDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> hugeGapDetected = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Pair<BaseMessage, SendbirdException>> feedbackSubmitted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Pair<BaseMessage, SendbirdException>> feedbackUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Pair<BaseMessage, SendbirdException>> feedbackDeleted = new MutableLiveData<>();
    @Nullable
    private MessageListParams messageListParams;
    @Nullable
    private MessageCollectionContract collection;
    private boolean needToLoadMessageCache = true;
    @NonNull
    private final SendbirdChatContract sendbirdChatContract;
    @NonNull
    private final ChannelConfig channelConfig;

    /**
     * Class that holds message data in a channel.
     * <p>
     * since 3.0.0
     */
    public static class ChannelMessageData {
        final List<Message> messages;
        final String traceName;

        ChannelMessageData(@Nullable String traceName, @NonNull List<Message> messages) {
            this.traceName = traceName;
            this.messages = messages;
        }

        /**
         * Returns a list of messages for the current channel.
         *
         * @return A list of the latest messages on the current channel
         * since 3.0.0
         */
        @NonNull
        public List<Message> getMessages() {
            return messages;
        }

        /**
         * Returns data indicating how the message list was updated.
         *
         * @return The String that traces the path of the message list
         * since 3.0.0
         */
        @Nullable
        public String getTraceName() {
            return traceName;
        }
    }


    ChannelViewModel(@NonNull Conversation conversation, @Nullable MessageListParams messageListParams, @NonNull ChannelConfig channelConfig) {
        this(conversation, messageListParams, new SendbirdUIKitImpl(), new SendbirdChatImpl(), channelConfig);
    }

    @VisibleForTesting
    ChannelViewModel(@NonNull Conversation conversation, @Nullable MessageListParams messageListParams, @NonNull SendbirdUIKitContract sendbirdUIKitContract, @NonNull SendbirdChatContract sendbirdChatContract, @NonNull ChannelConfig channelConfig) {
        super(conversation, sendbirdUIKitContract);
        this.messageListParams = messageListParams;
        this.sendbirdChatContract = sendbirdChatContract;
        this.channelConfig = channelConfig;

        this.sendbirdChatContract.addChannelHandler(ID_CHANNEL_EVENT_HANDLER, new IMessageManager.IMessageListener() {
            @Override
            public void onMessageReceive(Message message) {
                if (!message.getConversation().equals(conversation)) {
                    return;
                }
                cachedMessages.add(message);
                notifyDataSetChanged("onMessageReceive");
                markAsRead();
                sendReceipt(Collections.singletonList(message));
            }

            @Override
            public void onMessageRecall(Message message) {
                if (!message.getConversation().equals(conversation)) {
                    return;
                }
                cachedMessages.add(message);
                notifyDataSetChanged("onMessageRecall");
            }

            @Override
            public void onMessageDelete(Conversation conversation2, List<Long> clientMsgNos) {
                if (!conversation2.equals(conversation)) {
                    return;
                }
                for (Long a : clientMsgNos) {
                    cachedMessages.deleteByMessageId(a);
                }
                notifyDataSetChanged("onMessageDelete");
            }

            @Override
            public void onMessageClear(Conversation conversation2, long timestamp, String senderId) {
                if (!conversation2.equals(conversation)) {
                    return;
                }
                cachedMessages.clear();
                notifyDataSetChanged("onMessageClear");
            }

            @Override
            public void onMessageUpdate(Message message) {
                if (!message.getConversation().equals(conversation)) {
                    return;
                }
                cachedMessages.add(message);
                notifyDataSetChanged("onMessageUpdate");
            }

            @Override
            public void onMessageReactionAdd(Conversation conversation, MessageReaction reaction) {

            }

            @Override
            public void onMessageReactionRemove(Conversation conversation, MessageReaction reaction) {

            }
        });
    }

    /**
     * Checks if the message with {@code messageId} is in the message list which this view model manages.
     *
     * @param messageId ID of the message you want to check
     * @return {@code true} if the message in in the message list, {@code false} otherwise
     * since 3.0.0
     */
    public boolean hasMessageById(long messageId) {
        return cachedMessages.getById(messageId) != null;
    }

    /**
     * Retrieves message that matches {@code messageId} from the message list which this view model manages.
     *
     * @param messageId ID of the message you want to retrieve
     * @return {@code BaseMessage} that matches {@code messageId}
     * since 3.3.0
     */
    @Nullable
    public Message getMessageById(long messageId) {
        return cachedMessages.getById(messageId);
    }

    /**
     * Retrieves messages created at {@code createdAt} from the message list which this view model manages.
     *
     * @param createdAt The timestamp messages were created
     * @return The list of messages created at {@code createdAt}
     * since 3.0.0
     */
    @NonNull
    public List<Message> getMessagesByCreatedAt(long createdAt) {
        return cachedMessages.getByCreatedAt(createdAt);
    }

    // Do not call loadInitial inside this function.
    private synchronized void initMessageCollection(final long startingPoint) {
        Logger.i(">> ChannelViewModel::initMessageCollection()");
        final ConversationInfo channel = getConversationInfo();
        if (channel == null) return;
        if (this.collection != null) {
            disposeMessageCollection();
        }
        if (this.messageListParams == null) {
            this.messageListParams = createMessageListParams();
        }
        this.messageListParams.setReverse(true);
        this.collection = new MessageCollectionImpl(channel);
        Logger.i(">> ChannelViewModel::initMessageCollection() collection=%s", collection);
    }

    @Override
    void onMessagesUpdated(@NonNull ConversationInfo channel, @NonNull Message message) {
        super.onMessagesUpdated(channel, message);
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> ChannelViewModel::disposeMessageCollection()");
        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }


    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<ConversationInfo> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if huge gaps are detected within the collection this view model managed.
     *
     * @return LiveData holding whether huge gaps are detected
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getHugeGapDetected() {
        return hugeGapDetected;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<String> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns LiveData that can be observed if the messages has been deleted in the collection this view model managed.
     *
     * @return LiveData holding the list of deleted messages
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<Message>> onMessagesDeleted() {
        return messagesDeleted;
    }

    /**
     * Returns parameters required to retrieve the message list from this view model
     *
     * @return {@link MessageListParams} used in this view model
     * since 3.0.0
     */
    @Nullable
    public MessageListParams getMessageListParams() {
        return messageListParams;
    }

    /**
     * Returns LiveData that can be observed for members who are typing in the channel associated with this view model.
     *
     * @return LiveData holding members who are typing
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<UserInfo>> getTypingMembers() {
        return typingMembers;
    }

    /**
     * Returns LiveData that can be observed for the state of loading messages.
     *
     * @return LiveData holding {@link MessageLoadState} for this view model
     * since 3.0.0
     */
    @NonNull
    public LiveData<MessageLoadState> getMessageLoadState() {
        return messageLoadState;
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the message list.
     * When the message list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the message list
     * since 3.0.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }


    /**
     * Returns LiveData that can be observed for the result of submitting feedback.
     *
     * @return The BaseMessage that feedback is submitted.
     * since 3.13.0
     */
    public LiveData<Pair<BaseMessage, SendbirdException>> onFeedbackSubmitted() {
        return feedbackSubmitted;
    }

    /**
     * Returns LiveData that can be observed for the result of updating feedback.
     *
     * @return The BaseMessage that feedback is updated.
     * since 3.13.0
     */
    @NonNull
    public LiveData<Pair<BaseMessage, SendbirdException>> onFeedbackUpdated() {
        return feedbackUpdated;
    }

    /**
     * Returns LiveData that can be observed for the result of deleting feedback.
     *
     * @return The BaseMessage that feedback is deleted.
     * since 3.13.0
     */
    @NonNull
    public LiveData<Pair<BaseMessage, SendbirdException>> onFeedbackDeleted() {
        return feedbackDeleted;
    }
    boolean hasNext = true;
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    boolean hasPrevious = true;

    @Override
    public boolean hasPrevious() {
        return hasPrevious;
    }

    /**
     * Returns the timestamp that is the starting point when the message list is fetched initially.
     *
     * @return The timestamp as the starting point
     * since 3.0.0
     */
    public long getStartingPoint() {
        return Long.MAX_VALUE;
    }

    @UiThread
    private synchronized void notifyChannelDataChanged() {
        Logger.d(">> ChannelViewModel::notifyChannelDataChanged()");
        final ConversationInfo groupChannel = getConversationInfo();
        if (groupChannel == null) return;
        channelUpdated.setValue(groupChannel);
    }

    @Override
    synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> ChannelViewModel::notifyDataSetChanged(), size = %s, action=%s, hasNext=%s", cachedMessages.size(), traceName, hasNext());
        final List<Message> finalMessageList = buildMessageList();
        if (finalMessageList.size() == 0) {
            statusFrame.setValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.setValue(StatusFrameView.Status.NONE);
        }

        messageList.setValue(new ChannelMessageData(traceName, finalMessageList));
    }


    @UiThread
    @NonNull
    @Override
    public List<Message> buildMessageList() {
        return cachedMessages.toList();
    }

    private void removeThreadMessages(@NonNull List<Message> src) {
        final ListIterator<Message> iterator = src.listIterator();
        while (iterator.hasNext()) {
            if (MessageUtils.hasParentMessage(iterator.next())) {
                iterator.remove();
            }
        }
    }


    private void markAsRead() {
        Logger.dev("markAsRead");
        if (mConversationInfo == null || mConversationInfo.getConversation() == null) {
            return;
        }
        JIM.getInstance().getConversationManager().clearUnreadCount(mConversationInfo.getConversation(), new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    private void sendReceipt(List<Message> messages) {
        if (mConversationInfo == null
        || mConversationInfo.getConversation() == null
        || mConversationInfo.getConversation().getConversationType() != Conversation.ConversationType.PRIVATE) {
            return;
        }
        List<String> messageIds = new ArrayList<>();
        for (Message message : messages) {
            if (message.getDirection() == Message.MessageDirection.RECEIVE && !message.isHasRead()) {
                messageIds.add(message.getMessageId());
            }
        }
        if (messageIds.size() == 0) {
            return;
        }
        JIM.getInstance().getMessageManager().sendReadReceipt(mConversationInfo.getConversation(), messageIds, new IMessageManager.ISendReadReceiptCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    @UiThread
    private synchronized void notifyChannelDeleted(@NonNull String channelUrl) {
        channelDeleted.setValue(channelUrl);
    }

    @UiThread
    private synchronized void notifyMessagesDeleted(@NonNull List<Message> deletedMessages) {
        messagesDeleted.setValue(deletedMessages);
    }

    @UiThread
    private synchronized void notifyHugeGapDetected() {
        hugeGapDetected.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        this.sendbirdChatContract.removeChannelHandler(ID_CHANNEL_EVENT_HANDLER);
        disposeMessageCollection();
    }

//    @Override
//    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
//        super.deleteMessage(message, handler);
//        final SendingStatus status = message.getSendingStatus();
//        if (status == SendingStatus.FAILED) {
//            if (collection != null) {
//                collection.removeFailedMessages(Collections.singletonList(message), (requestIds, e) -> {
//                    if (handler != null) handler.onComplete(e);
//                    Logger.i("++ deleted message : %s", message);
//                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
//                    if (message instanceof FileMessage) {
//                        PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
//                    }
//                });
//            }
//        }
//    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @param startingPoint Timestamp that is the starting point when the message list is fetched
     *                      since 3.0.0
     */
    @UiThread
    public synchronized boolean loadInitial(final long startingPoint) {
        Logger.d(">> ChannelViewModel::loadInitial() startingPoint=%s", startingPoint);
        initMessageCollection(startingPoint);
        if (collection == null) {
            Logger.d("-- channel instance is null. an authenticate process must be proceed first");
            return false;
        }

        markAsRead();
        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        cachedMessages.clear();
        GetMessageOptions options = new GetMessageOptions();
        options.setStartTime(startingPoint);
        options.setCount(20);
        JIM.getInstance().getMessageManager().getMessages(conversation, JIMConst.PullDirection.OLDER, options, new IMessageManager.IGetMessagesCallbackV3() {
            @Override
            public void onGetMessages(List<Message> messages, long timestamp, boolean hasMore, int code) {
                cachedMessages.clear();
                cachedMessages.addAll(messages);
                notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
                sendReceipt(messages);
            }
        });

        return true;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     *                   since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<Message> loadPrevious() throws Exception {
        if (!hasPrevious()) return Collections.emptyList();

        final AtomicReference<List<Message>> result = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        Message oldestMessage = cachedMessages.getOldestMessage();
        long startingPoint = 0;
        if (oldestMessage != null) {
            startingPoint = oldestMessage.getTimestamp();
        }
        GetMessageOptions options = new GetMessageOptions();
        options.setStartTime(startingPoint);
        options.setCount(20);
        JIM.getInstance().getMessageManager().getMessages(conversation, JIMConst.PullDirection.OLDER, options, new IMessageManager.IGetMessagesCallbackV3() {
            @Override
            public void onGetMessages(List<Message> messages, long timestamp, boolean hasMore, int code) {
                hasPrevious = hasMore;
                cachedMessages.addAll(messages);
                result.set(messages);
                notifyDataSetChanged(StringSet.ACTION_PREVIOUS);
                lock.countDown();
                sendReceipt(messages);
            }
        });

        lock.await();
        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        return result.get();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the next.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     *                   since 3.0.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<Message> loadNext() throws Exception {
        if (!hasNext() || collection == null) return Collections.emptyList();

        final AtomicReference<List<Message>> result = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
        Message oldestMessage = cachedMessages.getLatestMessage();
        long startingPoint = 0;
        if (oldestMessage != null) {
            startingPoint = oldestMessage.getTimestamp();
        }
        GetMessageOptions options = new GetMessageOptions();
        options.setStartTime(startingPoint);
        options.setCount(20);
        JIM.getInstance().getMessageManager().getMessages(conversation, JIMConst.PullDirection.NEWER, options, new IMessageManager.IGetMessagesCallbackV3() {
            @Override
            public void onGetMessages(List<Message> messages, long timestamp, boolean hasMore, int code) {
                hasNext = hasMore;
                cachedMessages.addAll(messages);
                result.set(messages);
                notifyDataSetChanged(StringSet.ACTION_NEXT);
                lock.countDown();
                sendReceipt(messages);
            }
        });
        
        lock.await();
        messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        return result.get();
    }

    /**
     * Creates params for the message list when loading the message list.
     *
     * @return {@link MessageListParams} to be used when loading the message list
     * since 3.0.0
     */
    @NonNull
    public MessageListParams createMessageListParams() {
        final MessageListParams messageListParams = new MessageListParams();
        messageListParams.setReverse(true);
        if (channelConfig.getReplyType() != ReplyType.NONE) {
            messageListParams.setReplyType(com.sendbird.android.message.ReplyType.ONLY_REPLY_TO_CHANNEL);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), true, true));
        } else {
            messageListParams.setReplyType(com.sendbird.android.message.ReplyType.NONE);
            messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), false, true));
        }
        return messageListParams;
    }

    /**
     * Submits feedback for the message.
     *
     * @param message The message for feedback.
     * @param rating  The rating for the message.
     * @param comment The comment for the message.
     *                since 3.13.0
     */
    public void submitFeedback(@NonNull BaseMessage message, @NonNull FeedbackRating rating, @Nullable String comment) {
        // If using BaseMessage without copying it, the properties of the message are updated immediately when updating the feedback,
        // so the UI is not updated because the changes are not caught in the diff callback.
        BaseMessage copiedMessage = BaseMessage.clone(message);
        if (copiedMessage == null) return;

        Feedback currentFeedback = copiedMessage.getMyFeedback();
        if (currentFeedback == null) {
            copiedMessage.submitFeedback(rating, comment, (feedback, e) -> {
                feedbackSubmitted.postValue(Pair.create(copiedMessage, e));
            });
        } else {
            copiedMessage.updateFeedback(rating, comment, (feedback, e) -> {
                feedbackUpdated.postValue(Pair.create(copiedMessage, e));
            });
        }
    }

    /**
     * Removes feedback for the message.
     *
     * @param message The message for removing feedback.
     *                since 3.13.0
     */
    public void removeFeedback(@NonNull BaseMessage message) {
        // If using BaseMessage without copying it, the properties of the message are updated immediately when updating the feedback,
        // so the UI is not updated because the changes are not caught in the diff callback.
        BaseMessage copiedMessage = BaseMessage.clone(message);
        if (copiedMessage == null) return;

        copiedMessage.deleteFeedback(e -> {
            feedbackDeleted.postValue(Pair.create(copiedMessage, e));
        });
    }


    @VisibleForTesting
    @NonNull
    MessageCollectionContract createSyncMessageCollection(ConversationInfo channel) {
        return new MessageCollectionImpl(channel);
    }

    @TestOnly
    @NonNull
    String getChannelHandlerIdentifier() {
        return ID_CHANNEL_EVENT_HANDLER;
    }

    @TestOnly
    @NonNull
    String getConnectionHandlerIdentifier() {
        return CONNECTION_HANDLER_ID;
    }

    @TestOnly
    boolean isNeedToLoadMessageCache() {
        return needToLoadMessageCache;
    }

    @TestOnly
    @Nullable
    MessageCollectionContract getCollection() {
        return collection;
    }
}
