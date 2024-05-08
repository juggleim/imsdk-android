package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.collection.CollectionEventSource;
import com.sendbird.android.collection.GroupChannelContext;
import com.sendbird.android.collection.MessageCollection;
import com.sendbird.android.collection.MessageCollectionInitPolicy;
import com.sendbird.android.collection.MessageContext;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.android.handler.GroupChannelHandler;
import com.sendbird.android.handler.MessageCollectionHandler;
import com.sendbird.android.handler.MessageCollectionInitHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.ReactionEvent;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.params.MessageCollectionCreateParams;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.ThreadMessageListParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.params.common.MessagePayloadFilter;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.internal.queries.MessageThreadListQuery;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.Available;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel preparing and managing data related with the list of threads in a parent message
 *
 * since 3.3.0
 */
public class MessageThreadViewModel extends BaseMessageListViewModel {
    @NonNull
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_MESSAGE_THREAD_CHAT" + System.currentTimeMillis();
    @NonNull
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_MESSAGE_THREAD_CHAT" + System.currentTimeMillis();
    @NonNull
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @NonNull
    private BaseMessage parentMessage;
    @Nullable
    private MessageThreadListQuery messageThreadListQuery;
    @NonNull
    private final MutableLiveData<BaseMessage> parentMessageUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> parentMessageDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Long> threadMessageDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> onReconnected = new MutableLiveData<>();
    @Nullable
    private ThreadMessageListParams messageListParams;
    @Nullable
    private MessageCollection parentMessageCollection;
    @Nullable
    private MessageCollection collection;
    private volatile boolean skipEvent = true;

    /**
     * Constructor
     *
     * @param channelUrl        The URL of a channel this view model is currently associated with
     * @param parentMessage     The parent message required to retrieve the thread list from this view model
     * @param messageListParams Parameters required to retrieve the thread list from this view model
     * since 3.3.0
     */
    public MessageThreadViewModel(@NonNull String channelUrl, @NonNull BaseMessage parentMessage, @Nullable ThreadMessageListParams messageListParams) {
        super(channelUrl);
        this.messageListParams = messageListParams;
        this.parentMessage = parentMessage;
        registerChannelHandler();
        SendbirdChat.addConnectionHandler(CONNECTION_HANDLER_ID, new ConnectionHandler() {
            @Override
            public void onConnected(@NonNull String s) {
            }

            @Override
            public void onDisconnected(@NonNull String s) {
            }

            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                if (channel != null && messageThreadListQuery != null) {
                    // load messages again from the current view-point
                    onReconnected.postValue(true);
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposeMessageCollection();
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_ID);
        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
        worker.shutdown();
    }

    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                GroupChannel.getChannel(getChannelUrl(), (channel, e1) -> {
                    this.channel = channel;
                    if (e1 != null || channel == null) {
                        handler.onAuthenticationFailed();
                    } else {
                        this.memberFinder = new MemberFinder(channel, SendbirdUIKit.getUserMentionConfig());
                        prepareThreadViewModel(channel, e2 -> {
                            if (e2 != null) {
                                handler.onAuthenticationFailed();
                                return;
                            }
                            handler.onAuthenticated();
                        });
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    @Override
    public boolean hasNext() {
        return messageThreadListQuery != null && messageThreadListQuery.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return messageThreadListQuery != null && messageThreadListQuery.hasPrevious();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     *
     * @param startingPoint Timestamp that is the starting point when the thread list is fetched
     * since 3.3.0
     */
    @UiThread
    public synchronized void loadInitial(final long startingPoint) {
        if (this.channel == null) return;
        if (messageListParams == null) this.messageListParams = createMessageListParams();

        this.skipEvent = true;
        this.cachedMessages.clear();
        worker.execute(() -> {
            try {
                // warn: parent message must have entire message properties.
                messageThreadListQuery = new MessageThreadListQuery(this.parentMessage, startingPoint);
                if (startingPoint > 0L) {
                    cachedMessages.addAll(messageThreadListQuery.loadPrevious(messageListParams));
                }
                cachedMessages.addAll(messageThreadListQuery.loadNext(messageListParams));
            } catch (Exception ex) {
                Logger.e(ex);
                SendbirdUIKit.runOnUIThread(() -> statusFrame.setValue(StatusFrameView.Status.ERROR));
                return;
            }
            this.skipEvent = false;
            notifyDataSetChangedOnUiThread(StringSet.ACTION_INIT_FROM_REMOTE);
        });
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.3.0
     */
    @NonNull
    @Override
    @WorkerThread
    public List<BaseMessage> loadPrevious() throws Exception {
        if (messageThreadListQuery == null || messageListParams == null) return Collections.emptyList();
        final List<BaseMessage> result = messageThreadListQuery.loadPrevious(messageListParams);
        cachedMessages.addAll(result);

        notifyDataSetChangedOnUiThread(StringSet.ACTION_PREVIOUS);
        return result;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the next.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.3.0
     */
    @NonNull
    @Override
    @WorkerThread
    public List<BaseMessage> loadNext() throws Exception {
        if (messageThreadListQuery == null || messageListParams == null) return Collections.emptyList();
        final List<BaseMessage> result = messageThreadListQuery.loadNext(messageListParams);
        cachedMessages.addAll(result);

        notifyDataSetChangedOnUiThread(StringSet.ACTION_NEXT);
        return result;
    }

    /**
     * Returns the parent message of thread list.
     *
     * @return The parent message
     * since 3.3.0
     */
    @NonNull
    public BaseMessage getParentMessage() {
        return parentMessage;
    }

    /**
     * Returns the timestamp that is the starting point when the thread list is fetched initially.
     *
     * @return The timestamp as the starting point
     * since 3.3.0
     */
    public long getStartingPoint() {
        return this.messageThreadListQuery != null ? this.messageThreadListQuery.getStartingPoint() : 0L;
    }

    /**
     * Returns LiveData that can be observed if the parent message has been updated.
     *
     * @return LiveData holding the updated parent message
     * since 3.3.0
     */
    @NonNull
    public LiveData<BaseMessage> onParentMessageUpdated() {
        return parentMessageUpdated;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * since 3.3.0
     */
    @NonNull
    public LiveData<GroupChannel> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding whether {@code GroupChannel} has been deleted
     * since 3.3.0
     */
    @NonNull
    public LiveData<Boolean> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns LiveData that can be observed if the parent message has been deleted.
     *
     * @return LiveData holding whether the parent message has been deleted
     * since 3.3.0
     */
    @NonNull
    public LiveData<Boolean> onParentMessageDeleted() {
        return parentMessageDeleted;
    }

    /**
     * Returns LiveData that can be observed if the thread message has been deleted.
     *
     * @return LiveData holding whether the thread message has been deleted
     * since 3.4.0
     */
    @NonNull
    public LiveData<Long> onThreadMessageDeleted() {
        return threadMessageDeleted;
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the thread list.
     * When the thread list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the thread list
     * since 3.3.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    /**
     * Returns LiveData that can be observed if the chat server has been reconnected.
     *
     * @return LiveData holding whether the chat server has been reconnected
     * since 3.3.0
     */
    @NonNull
    public LiveData<Boolean> onReconnected() {
        return onReconnected;
    }

    @Override
    public void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (message != null) {
                cachedMessages.update(message);
                notifyDataSetChanged(StringSet.EVENT_MESSAGE_UPDATED);
            }
            if (handler != null) handler.onComplete(e);
            Logger.i("++ updated message : %s", message);
        });
    }

    @Override
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        super.deleteMessage(message, handler);
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.FAILED) {
            if (parentMessageCollection != null) {
                parentMessageCollection.removeFailedMessages(Collections.singletonList(message), (requestIds, e) -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ deleted message : %s", message);
                    notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
                    if (message instanceof FileMessage) {
                        PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
                    }
                });
            }
        }
    }

    /**
     * Creates params for the thread list when loading the thread list.
     *
     * @return {@link ThreadMessageListParams} to be used when loading the thread list
     * since 3.3.0
     */
    @NonNull
    public ThreadMessageListParams createMessageListParams() {
        final ThreadMessageListParams messageListParams = new ThreadMessageListParams();
        messageListParams.setReverse(true);
        messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), false, false));
        return messageListParams;
    }

    /********************************************************************************************
     *                                      PRIVATE AREA
     *********************************************************************************************/

    private void notifyDataSetChangedOnUiThread(@NonNull String traceName) {
        SendbirdUIKit.runOnUIThread(() -> notifyDataSetChanged(traceName));
    }

    @UiThread
    @Override
    synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.i(">> MessageThreadViewModel::notifyDataSetChanged(), skipEvent=%s traceName=%s ", skipEvent, traceName);
        if (skipEvent) return;
        if (shouldIgnoreEvent(traceName)) {
            Logger.d("-- MessageThreadViewModel::notifyDataSetChanged() event is ignored. traceName=%s", traceName);
            return;
        }
        final List<BaseMessage> finalMessageList = buildMessageList();
        statusFrame.setValue(StatusFrameView.Status.NONE);
        messageList.setValue(new ChannelViewModel.ChannelMessageData(traceName, finalMessageList));
    }

    boolean shouldIgnoreEvent(@NonNull String traceName) {
        MessageCollection collection = this.collection;
        if (collection == null) return true;

        final List<BaseMessage> pendingMessages = new ArrayList<>(filterThreadMessages(collection.getPendingMessages()));
        if (traceName.equals(StringSet.ACTION_PENDING_MESSAGE_ADDED) && pendingMessages.size() == 0) return true;

        final List<BaseMessage> failedMessages = new ArrayList<>(filterThreadMessages(collection.getFailedMessages()));
        if (traceName.equals(StringSet.ACTION_FAILED_MESSAGE_ADDED) && failedMessages.size() == 0) return true;

        return false;
    }

    @UiThread
    @NonNull
    @Override
    public List<BaseMessage> buildMessageList() {
        MessageCollection collection = this.collection;
        if (collection == null) return Collections.emptyList();

        final List<BaseMessage> currentList = this.cachedMessages.toList();
        final List<BaseMessage> pendingMessages = new ArrayList<>(filterThreadMessages(collection.getPendingMessages()));
        final List<BaseMessage> failedMessages = new ArrayList<>(filterThreadMessages(collection.getFailedMessages()));

        // parent message should be added regardless of pending, failed messages.
        if (!hasPrevious() || currentList.size() == 0) {
            currentList.add(parentMessage);
        }

        if (!hasNext()) {
            currentList.addAll(0, pendingMessages);
            currentList.addAll(0, failedMessages);
        }
        return currentList;
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> MessageThreadViewModel::disposeMessageCollection()");
        if (this.parentMessageCollection != null) {
            this.parentMessageCollection.setMessageCollectionHandler(null);
            this.parentMessageCollection.dispose();
        }

        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }

    // Initialize 2 MessageCollections.
    // First, a collection that updates the parent message.
    // If the update of the parent message is successful, then initializes the collection handling pending and failed messages.
    // TODO : This is not a recommended implementation for collection. This collection will be removed after Chat SDK support message thread collection.
    private void prepareThreadViewModel(@NonNull GroupChannel channel, @NonNull OnCompleteHandler handler) {
        this.parentMessageCollection = createParentMessageCollection(channel, parentMessage);
        Logger.d("++ collection = %s", parentMessageCollection);

        this.parentMessageCollection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            volatile boolean isUpdated = false;

            @Override
            public void onCacheResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {
                if (list != null) {
                    final BaseMessage message = findCopiedMessage(list, parentMessage.getMessageId());
                    if (message != null) {
                        parentMessage = message;
                        isUpdated = true;
                    }
                }
            }

            @Override
            public void onApiResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {
                if (list != null) {
                    final BaseMessage message = findCopiedMessage(list, parentMessage.getMessageId());
                    if (message != null) {
                        parentMessage = message;
                        isUpdated = true;
                    }
                }

                // Do it if parent message has been updated.
                if (isUpdated) {
                    MessageThreadViewModel.this.collection = createCollection(channel);
                    MessageThreadViewModel.this.collection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
                        @Override
                        public void onCacheResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {
                        }

                        @Override
                        public void onApiResult(@Nullable List<BaseMessage> list, @Nullable SendbirdException e) {
                            Logger.d("++ refreshParentMessage isUpdated=%s, error message=%s", isUpdated, e != null ? e.getMessage() : "no error");
                            if (isUpdated) {
                                parentMessageUpdated.postValue(parentMessage);
                            }
                            handler.onComplete(isUpdated ? null : e);
                        }
                    });
                } else {
                    handler.onComplete(e);
                }
            }
        });
    }

    @NonNull
    private List<BaseMessage> filterThreadMessages(@NonNull List<BaseMessage> messages) {
        final List<BaseMessage> result = new ArrayList<>();
        for (final BaseMessage message : messages) {
            if (parentMessage.getMessageId() == message.getParentMessageId()) {
                result.add(message);
            }
        }
        return result;
    }

    // In parent message collection, the collection updates parent message updates and channel information updates.
    // The starting point creates a collection with the parent's createdAt, and only receives events for 1 message before and after including the parent message.
    // Update the parent message only if the parent message is included in onMessagesUpdated.
    // Update channel information by receiving onChannelChanged and onChannelDeleted.
    // TODO : This is not a recommended implementation for collection. This collection will be removed after Chat SDK support message thread collection.
    @NonNull
    private synchronized MessageCollection createParentMessageCollection(@NonNull GroupChannel channel, @NonNull BaseMessage parentMessage) {
        final MessageListParams params = new MessageListParams();
        params.setReverse(true);
        params.setReplyType(com.sendbird.android.message.ReplyType.ONLY_REPLY_TO_CHANNEL);
        params.setInclusive(true);
        params.setPreviousResultSize(1);
        params.setNextResultSize(1);
        if (messageListParams != null) {
            params.setMessagePayloadFilter(new MessagePayloadFilter(messageListParams.getMessagePayloadFilter().getIncludeMetaArray(),
                messageListParams.getMessagePayloadFilter().getIncludeReactions(), true, true));
        } else {
            params.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), true, true));
        }
        return SendbirdChat.createMessageCollection(new MessageCollectionCreateParams(channel, params, parentMessage.getCreatedAt(), new MessageCollectionHandler() {
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesAdded(parent collection) from=%s", context.getCollectionEventSource());
            }

            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesUpdated(parent collection) from=%s", context.getCollectionEventSource());

                // check parent message updated
                Logger.d("++ MessageThreadViewModel::onMessagesUpdated() hasNext=%s", parentMessageCollection.getHasNext());
                final BaseMessage updatedParentMessage = findCopiedMessage(messages, parentMessage.getMessageId());
                if (updatedParentMessage != null) {
                    MessageThreadViewModel.this.parentMessage = updatedParentMessage;
                    parentMessageUpdated.postValue(MessageThreadViewModel.this.parentMessage);
                    notifyDataSetChanged(new MessageContext(CollectionEventSource.EVENT_MESSAGE_UPDATED, SendingStatus.SUCCEEDED));
                }
            }

            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesDeleted(parent collection) from=%s", context.getCollectionEventSource());

                // check parent message deleted
                for (final BaseMessage message : messages) {
                    if (parentMessage.getMessageId() == message.getMessageId()) {
                        parentMessageDeleted.postValue(true);
                        break;
                    }
                }
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
                Logger.d(">> MessageThreadViewModel::onChannelUpdated(parent collection) from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());
                channelUpdated.postValue(channel);
            }

            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> MessageThreadViewModel::onChannelDeleted(parent collection) from=%s", context.getCollectionEventSource());
                channelDeleted.postValue(true);
            }

            @Override
            public void onHugeGapDetected() {
                Logger.d(">> MessageThreadViewModel::onHugeGapDetected(parent collection)");
            }
        }));
    }

    // The collection created here is used to receive new messages and manage pending and failed messages.
    // Set the starting point to Long.MAX_VALUE to receive events for newly added messages.
    // At the time of creation, only the two latest messages are stored in the chat cache, so update events for other messages are not received.
    // If hasNext is true in chat SDK, delete event is given to new pending message.
    // TODO : This is not a recommended implementation for collection. This collection will be removed after Chat SDK support message thread collection.
    @NonNull
    private synchronized MessageCollection createCollection(@NonNull GroupChannel channel) {
        final MessageListParams params = new MessageListParams();
        params.setReverse(true);
        params.setReplyType(com.sendbird.android.message.ReplyType.ONLY_REPLY_TO_CHANNEL);
        params.setPreviousResultSize(1);
        params.setNextResultSize(1);
        if (messageListParams != null) {
            params.setMessagePayloadFilter(new MessagePayloadFilter(messageListParams.getMessagePayloadFilter().getIncludeMetaArray(),
                messageListParams.getMessagePayloadFilter().getIncludeReactions(), true, true));
        } else {
            params.setMessagePayloadFilter(new MessagePayloadFilter(true, Available.isSupportReaction(), true, true));
        }
        return SendbirdChat.createMessageCollection(new MessageCollectionCreateParams(channel, params, Long.MAX_VALUE, new MessageCollectionHandler() {
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesAdded(collection) from=%s, size=%s", context.getCollectionEventSource(), messages.size());
                // if the next message that should load more exists, a new message shouldn't add.
                // however, if the message is a pending message, it should add at the bottom.
                if (context.getMessagesSendingStatus() != SendingStatus.PENDING && hasNext()) return;

                // event from changelogs won't handle at this collection.
                if (context.getCollectionEventSource() == CollectionEventSource.MESSAGE_CHANGELOG
                    || context.getCollectionEventSource() == CollectionEventSource.MESSAGE_FILL) return;

                final List<BaseMessage> threadedMessages = filterThreadMessages(messages);
                if (threadedMessages.isEmpty()) return;

                MessageThreadViewModel.this.onMessagesAdded(context, channel, threadedMessages);
            }

            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesUpdated(collection) from=%s, size=%s", context.getCollectionEventSource(), messages.size());
                if (context.getCollectionEventSource() == CollectionEventSource.MESSAGE_CHANGELOG
                    || context.getCollectionEventSource() == CollectionEventSource.MESSAGE_FILL) return;
                final List<BaseMessage> threadMessages = filterThreadMessages(messages);
                if (threadMessages.isEmpty()) return;

                MessageThreadViewModel.this.onMessagesUpdated(context, channel, threadMessages);
            }

            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> MessageThreadViewModel::onMessagesDeleted(collection) from=%s", context.getCollectionEventSource());
                final List<BaseMessage> threadMessages = filterThreadMessages(messages);
                if (threadMessages.isEmpty()) return;

                MessageThreadViewModel.this.onMessagesDeleted(context, channel, threadMessages);
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
                Logger.d(">> MessageThreadViewModel::onChannelUpdated(collection) from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());
            }

            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> MessageThreadViewModel::onChannelDeleted(collection) from=%s", context.getCollectionEventSource());
            }

            @Override
            public void onHugeGapDetected() {
                Logger.d(">> MessageThreadViewModel::onHugeGapDetected(collection)");
            }
        }));
    }

    // Use the channel handler to update messages in the thread list.
    // Additionally, delivery receipt and read receipt update information are also handled here.
    // Don't update the parent message here. Parent messages are handled by the parent message collection.
    // Don't receive new messages. New incoming messages are handled by the collection.
    // TODO : This handler will be replaced by collection after Chat SDK support message thread collection.
    private void registerChannelHandler() {
        // for handling CRUD of thread message only
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_ID, new GroupChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {
            }

            @Override
            public void onReactionUpdated(@NonNull BaseChannel channel, @NonNull ReactionEvent reactionEvent) {
                Logger.d(">> MessageThreadViewModel::onReactionUpdated()");
                if (!isCurrentChannel(channel.getUrl())) return;
                final BaseMessage updatedMessage = cachedMessages.getById(reactionEvent.getMessageId());
                if (updatedMessage != null) {
                    final BaseMessage clone = BaseMessage.clone(updatedMessage);
                    if (clone != null) {
                        clone.applyReactionEvent(reactionEvent);
                        cachedMessages.update(clone);
                        notifyDataSetChanged(StringSet.EVENT_MESSAGE_UPDATED);
                    }
                }
            }

            @Override
            public void onMessageUpdated(@NonNull BaseChannel channel, @NonNull BaseMessage message) {
                Logger.d(">> MessageThreadViewModel::onMessageUpdated()");
                if (!isCurrentChannel(channel.getUrl())) return;
                final BaseMessage updatedMessage = cachedMessages.getById(message.getMessageId());
                if (updatedMessage != null) {
                    cachedMessages.update(message);
                    notifyDataSetChanged(StringSet.EVENT_MESSAGE_UPDATED);
                }
            }

            @Override
            public void onMessageDeleted(@NonNull BaseChannel channel, long msgId) {
                Logger.d(">> MessageThreadViewModel::onMessageDeleted()");
                if (!isCurrentChannel(channel.getUrl())) return;
                threadMessageDeleted.postValue(msgId);
                final BaseMessage deletedMessage = cachedMessages.getById(msgId);
                if (deletedMessage != null) {
                    cachedMessages.deleteByMessageId(msgId);
                    notifyDataSetChanged(new MessageContext(CollectionEventSource.EVENT_MESSAGE_DELETED, SendingStatus.NONE));
                }
            }
        });
    }

    @Nullable
    private BaseMessage findCopiedMessage(@NonNull List<BaseMessage> messages, long messageId) {
        for (BaseMessage message : messages) {
            if (message.getMessageId() == messageId) {
                return BaseMessage.clone(message);
            }
        }
        return null;
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        if (channel == null) return false;
        return channelUrl.equals(channel.getUrl());
    }
}
