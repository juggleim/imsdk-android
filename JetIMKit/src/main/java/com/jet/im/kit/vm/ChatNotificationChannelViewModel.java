package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.collection.GroupChannelContext;
import com.sendbird.android.collection.MessageCollection;
import com.sendbird.android.collection.MessageCollectionInitPolicy;
import com.sendbird.android.collection.MessageContext;
import com.sendbird.android.collection.Traceable;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.MessageCollectionHandler;
import com.sendbird.android.handler.MessageCollectionInitHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.MessageCollectionCreateParams;
import com.sendbird.android.params.MessageListParams;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.LiveDataEx;
import com.jet.im.kit.model.MessageData;
import com.jet.im.kit.model.MessageList;
import com.jet.im.kit.model.MutableLiveDataEx;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the notification channel.
 *
 * since 3.5.0
 */
public class ChatNotificationChannelViewModel extends BaseViewModel implements OnPagedDataLoader<List<BaseMessage>>, LifecycleEventObserver {
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<BaseMessage>> messagesDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveDataEx<MessageData> notificationList = new MutableLiveDataEx<>();
    @Nullable
    private MessageListParams messageListParams;
    @Nullable
    private MessageCollection collection;
    @NonNull
    private final String channelUrl;
    @Nullable
    private GroupChannel channel;

    private boolean isVisible = false;

    /**
     * Constructor
     *
     * @param channelUrl        The URL of a channel this view model is currently associated with
     * @param messageListParams Parameters required to retrieve the message list from this view model
     * since 3.5.0
     */
    public ChatNotificationChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
        this.channelUrl = channelUrl;
        this.messageListParams = messageListParams;
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.5.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                    ChatNotificationChannelViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.d("-- onCleared ChatNotificationChannelViewModel");
        disposeMessageCollection();
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * since 3.5.0
     */
    @Nullable
    public GroupChannel getChannel() {
        return channel;
    }

    /**
     * Returns parameters required to retrieve the message list from this view model
     *
     * @return {@link MessageListParams} used in this view model
     * since 3.5.0
     */
    @Nullable
    public MessageListParams getMessageListParams() {
        return messageListParams;
    }

    /**
     * Returns URL of GroupChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * since 3.5.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    /**
     * Returns LiveData that can be observed for the list of messages.
     *
     * @return LiveData holding the latest {@link ChannelViewModel.ChannelMessageData}
     * since 3.5.0
     */
    @NonNull
    public LiveDataEx<MessageData> getNotificationList() {
        return notificationList;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * since 3.5.0
     */
    @NonNull
    public LiveData<GroupChannel> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the message list.
     * When the message list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the message list
     * since 3.5.0
     */
    @NonNull
    public MutableLiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted {@code GroupChannel}
     * since 3.5.0
     */
    @NonNull
    public LiveData<String> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getNotificationList()}.
     *
     * @param startingPoint Timestamp that is the starting point when the message list is fetched
     * since 3.5.0
     */
    @UiThread
    public synchronized boolean loadInitial(final long startingPoint) {
        Logger.d(">> ChatNotificationChannelViewModel::loadInitial() startingPoint=%s", startingPoint);
        initMessageCollection(startingPoint);
        if (collection == null) {
            Logger.d("-- channel instance is null. an authenticate process must be proceed first");
            return false;
        }

        collection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> cachedList, @Nullable SendbirdException e) {
                if (e == null && cachedList != null && cachedList.size() > 0) {
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_CACHE);
                }
            }

            @Override
            public void onApiResult(@Nullable List<BaseMessage> apiResultList, @Nullable SendbirdException e) {
                if (e == null && apiResultList != null) {
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                    if (apiResultList.size() > 0) {
                        markAsRead();
                    }
                }
            }
        });
        return true;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getNotificationList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.5.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious() || collection == null) return Collections.emptyList();
        Logger.i(">> ChatNotificationChannelViewModel::loadPrevious()");

        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch lock = new CountDownLatch(1);

        collection.loadPrevious((messages, e) -> {
            try {
                if (e == null) {
                    messages = messages == null ? Collections.emptyList() : messages;
                    result.set(messages);
                    notifyDataSetChanged(StringSet.ACTION_PREVIOUS);
                }
                error.set(e);
            } finally {
                lock.countDown();
            }
        });
        lock.await();

        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the next.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getNotificationList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.5.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadNext() throws Exception {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return collection == null || collection.getHasPrevious();
    }

    @UiThread
    synchronized void notifyDataSetChanged(@NonNull Traceable trace) {
        notifyDataSetChanged(trace.getTraceName());
    }

    @UiThread
    private synchronized void notifyChannelDataChanged() {
        Logger.d(">> ChatNotificationChannelViewModel::notifyChannelDataChanged()");
        channelUpdated.setValue(channel);
    }

    @UiThread
    private synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> ChatNotificationChannelViewModel::notifyDataSetChanged()");
        if (collection == null) return;
        final List<BaseMessage> copiedList = collection.getSucceededMessages();
        if (copiedList.size() == 0) {
            statusFrame.setValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.setValue(StatusFrameView.Status.NONE);
            final MessageList messages = new MessageList();
            messages.addAll(copiedList);
            notificationList.setValue(new MessageData(traceName, messages.toList()));
        }
    }

    @UiThread
    private synchronized void notifyMessagesDeleted(@NonNull List<BaseMessage> deletedMessages) {
        messagesDeleted.setValue(deletedMessages);
    }

    @UiThread
    private synchronized void notifyChannelDeleted(@NonNull String channelUrl) {
        channelDeleted.setValue(channelUrl);
    }

    private synchronized void initMessageCollection(final long startingPoint) {
        Logger.i(">> ChatNotificationChannelViewModel::initMessageCollection()");
        final GroupChannel channel = getChannel();
        if (channel == null) return;
        if (this.collection != null) {
            disposeMessageCollection();
        }
        if (this.messageListParams == null) {
            this.messageListParams = createMessageListParams();
        }
        this.messageListParams.setReverse(true);
        this.collection = SendbirdChat.createMessageCollection(new MessageCollectionCreateParams(channel, this.messageListParams, startingPoint, new MessageCollectionHandler() {
            @UiThread
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChatNotificationChannelViewModel::onMessagesAdded() from=%s", context.getCollectionEventSource());
                if (messages.isEmpty()) return;

                switch (context.getCollectionEventSource()) {
                    case EVENT_MESSAGE_RECEIVED:
                    case EVENT_MESSAGE_SENT:
                    case MESSAGE_FILL:
                        if (isVisible) markAsRead();
                        break;
                }
                notifyDataSetChanged(context);
            }

            @UiThread
            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChatNotificationChannelViewModel::onMessagesUpdated() from=%s", context.getCollectionEventSource());
                notifyDataSetChanged(context);
            }

            @UiThread
            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> ChatNotificationChannelViewModel::onMessagesDeleted() from=%s", context.getCollectionEventSource());
                // Remove the succeeded message from the succeeded message datasource.
                notifyMessagesDeleted(messages);
                notifyDataSetChanged(context);
            }

            @UiThread
            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> ChatNotificationChannelViewModel::onChannelDeleted() from=%s", context.getCollectionEventSource());
                notifyChannelDeleted(channelUrl);
            }

            @UiThread
            @Override
            public void onHugeGapDetected() {
                Logger.d(">> ChatNotificationChannelViewModel::onHugeGapDetected()");
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
                Logger.d(">> ChatNotificationChannelViewModel::onChannelUpdated() from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());
                notifyChannelDataChanged();
            }
        }));
    }

    private synchronized void disposeMessageCollection() {
        Logger.i(">> ChatNotificationChannelViewModel::disposeMessageCollection()");
        if (this.collection != null) {
            this.collection.setMessageCollectionHandler(null);
            this.collection.dispose();
        }
    }

    public void markAsRead() {
        Logger.d(">> ChatNotificationChannelViewModel::markAsRead()");
        if (channel != null) channel.markAsRead(null);
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     * since 3.5.0
     */
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.deleteMessage(message, e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ deleted message : %s", message);
        });
    }

    /**
     * Creates params for the message list when loading the message list.
     *
     * @return {@link MessageListParams} to be used when loading the message list
     * since 3.5.0
     */
    @NonNull
    public MessageListParams createMessageListParams() {
        final MessageListParams params = new MessageListParams();
        params.setReverse(true);
        return params;
    }

    /**
     * Called when a state transition event happens.
     *
     * @param source The source of the event
     * @param event  The event
     */
    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        Logger.i(">> ChatNotificationChannelViewModel::onStateChanged(%s)", event);
        switch (event) {
            case ON_RESUME:
                isVisible = true;
                markAsRead();
                break;
            case ON_PAUSE:
                isVisible = false;
                break;
        }
    }
}
