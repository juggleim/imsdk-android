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

import com.sendbird.android.channel.FeedChannel;
import com.sendbird.android.collection.FeedChannelContext;
import com.sendbird.android.collection.MessageCollectionInitPolicy;
import com.sendbird.android.collection.NotificationCollection;
import com.sendbird.android.collection.NotificationContext;
import com.sendbird.android.collection.Traceable;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.MessageCollectionInitHandler;
import com.sendbird.android.handler.NotificationCollectionHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.MessageListParams;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.LiveDataEx;
import com.jet.im.kit.model.MessageData;
import com.jet.im.kit.model.MutableLiveDataEx;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the notification channel.
 * since 3.5.0
 */
public class FeedNotificationChannelViewModel extends BaseViewModel implements OnPagedDataLoader<List<BaseMessage>>, LifecycleEventObserver {
    @NonNull
    private final MutableLiveData<FeedChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<BaseMessage>> messagesDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveDataEx<MessageData> messageList = new MutableLiveDataEx<>();
    @Nullable
    private MessageListParams messageListParams;
    @Nullable
    private NotificationCollection collection;
    @NonNull
    private final String channelUrl;
    @Nullable
    private FeedChannel channel;
    private boolean isVisible = false;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param messageListParams Parameters required to retrieve the message list from this view model
     * since 3.5.0
     */
    public FeedNotificationChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
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
        SendbirdUIKit.authenticateFeed((user, e) -> {
            if (user != null) {
                FeedChannel.getChannel(channelUrl, (channel, e1) -> {
                    FeedNotificationChannelViewModel.this.channel = channel;
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
        Logger.d("-- onCleared FeedNotificationChannelViewModel");
        disposeNotificationCollection();
    }

    /**
     * Returns {@code FeedChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code FeedChannel} this view model is currently associated with
     * since 3.5.0
     */
    @Nullable
    public FeedChannel getChannel() {
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
     * Returns URL of FeedChannel.
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
    public LiveDataEx<MessageData> getMessageList() {
        return messageList;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code FeedChannel}
     * since 3.5.0
     */
    @NonNull
    public LiveData<FeedChannel> onChannelUpdated() {
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
     * @return LiveData holding the URL of the deleted {@code FeedChannel}
     * since 3.5.0
     */
    @NonNull
    public LiveData<String> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @param startingPoint Timestamp that is the starting point when the message list is fetched
     * since 3.5.0
     */
    @UiThread
    public synchronized boolean loadInitial(final long startingPoint) {
        return loadInitial(startingPoint, Collections.emptyList());
    }

    public synchronized boolean loadInitial(final long startingPoint, @Nullable List<String> customTypes) {
        Logger.d(">> FeedNotificationChannelViewModel::loadInitial() startingPoint=%s", startingPoint);
        initNotificationCollection(startingPoint, customTypes);
        if (collection == null) {
            Logger.d("-- channel instance is null. an authenticate process must be proceed first");
            return false;
        }

        collection.initialize(MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API, new MessageCollectionInitHandler() {
            @Override
            public void onCacheResult(@Nullable List<BaseMessage> cachedList, @Nullable SendbirdException e) {
                if (e == null && cachedList != null && cachedList.size() > 0) {
                    Logger.d("++ loadInitial from cache cachedList.size=%s", cachedList.size());
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_CACHE);
                }
            }

            @Override
            public void onApiResult(@Nullable List<BaseMessage> apiResultList, @Nullable SendbirdException e) {
                if (e == null && apiResultList != null) {
                    Logger.d("++ loadInitial from remote apiResultList.size=%s", apiResultList.size());
                    notifyDataSetChanged(StringSet.ACTION_INIT_FROM_REMOTE);
                    if (apiResultList.size() > 0) {
                        if (isVisible) markAsRead();
                    }
                }
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
     * since 3.5.0
     */
    @WorkerThread
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious() || collection == null) return Collections.emptyList();
        Logger.i(">> FeedNotificationChannelViewModel::loadPrevious()");

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
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
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
        Logger.d(">> FeedNotificationChannelViewModel::notifyChannelDataChanged()");
        channelUpdated.setValue(channel);
    }

    @UiThread
    private synchronized void notifyDataSetChanged(@NonNull String traceName) {
        Logger.d(">> FeedNotificationChannelViewModel::notifyDataSetChanged()");
        if (collection == null) return;
        final List<BaseMessage> copiedList = collection.getSucceededMessages();
        if (copiedList.size() == 0) {
            statusFrame.setValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.setValue(StatusFrameView.Status.NONE);
            messageList.setValue(new MessageData(traceName, copiedList));
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

    private synchronized void initNotificationCollection(final long startingPoint, @Nullable List<String> customTypes) {
        Logger.i(">> FeedNotificationChannelViewModel::initMessageCollection()");
        final FeedChannel channel = getChannel();
        if (channel == null) return;
        if (this.collection != null) {
            disposeNotificationCollection();
        }
        if (this.messageListParams == null) {
            this.messageListParams = createMessageListParams();
        }

        final MessageListParams params = this.messageListParams.clone();
        params.setReverse(true);
        if (customTypes != null) {
            params.setCustomTypes(customTypes);
        }
        this.collection = channel.createNotificationCollection(params, startingPoint, new NotificationCollectionHandler() {
            @UiThread
            @Override
            public void onMessagesAdded(@NonNull NotificationContext context, @NonNull FeedChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> FeedNotificationChannelViewModel::onMessagesAdded() from=%s", context.getCollectionEventSource());
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
            public void onMessagesUpdated(@NonNull NotificationContext context, @NonNull FeedChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> FeedNotificationChannelViewModel::onMessagesUpdated() from=%s", context.getCollectionEventSource());
                notifyDataSetChanged(context);
            }

            @UiThread
            @Override
            public void onMessagesDeleted(@NonNull NotificationContext context, @NonNull FeedChannel channel, @NonNull List<BaseMessage> messages) {
                Logger.d(">> FeedNotificationChannelViewModel::onMessagesDeleted() from=%s", context.getCollectionEventSource());
                // Remove the succeeded message from the succeeded message datasource.
                notifyMessagesDeleted(messages);
                notifyDataSetChanged(context);
            }

            @UiThread
            @Override
            public void onChannelDeleted(@NonNull FeedChannelContext context, @NonNull String channelUrl) {
                Logger.d(">> FeedNotificationChannelViewModel::onChannelDeleted() from=%s", context.getCollectionEventSource());
                notifyChannelDeleted(channelUrl);
            }

            @UiThread
            @Override
            public void onHugeGapDetected() {
                Logger.d(">> FeedNotificationChannelViewModel::onHugeGapDetected()");
            }

            @Override
            public void onChannelUpdated(@NonNull FeedChannelContext context, @NonNull FeedChannel channel) {
                Logger.d(">> FeedNotificationChannelViewModel::onChannelUpdated() from=%s, url=%s", context.getCollectionEventSource(), channel.getUrl());
                notifyChannelDataChanged();
            }
        });
    }

    private synchronized void disposeNotificationCollection() {
        Logger.i(">> FeedNotificationChannelViewModel::disposeNotificationCollection()");
        if (this.collection != null) {
            this.collection.setNotificationCollectionHandler(null);
            this.collection.dispose();
        }
    }

    public void markAsRead() {
        Logger.d(">> FeedNotificationChannelViewModel::markAsRead()");
        if (channel != null) channel.markAsRead(null);
    }

    /**
     * Creates params for the message list when loading the message list.
     *
     * @return {@link MessageListParams} to be used when loading the message list
     * since 3.5.0
     */
    @NonNull
    public MessageListParams createMessageListParams() {
        return new MessageListParams();
    }

    /**
     * Called when a state transition event happens.
     *
     * @param source The source of the event
     * @param event  The event
     */
    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        Logger.i(">> FeedNotificationChannelViewModel::onStateChanged(%s)", event);
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

    /**
     * Sends the log impression to Sendbird server.
     * It is working only when the channel is visible.
     *
     * @param messages The list of messages to be sent
     * since 3.12.0
     *
     * @deprecated 3.15.0
     * <p> Use {@link #sendLogViewed(List)} instead.</p>
     */
    @Deprecated
    public void sendLogImpression(@NonNull List<BaseMessage> messages) {
        sendLogViewed(messages);
    }

    /**
     * Sends the log viewed to Sendbird server.
     * It is working only when the channel is visible.
     *
     * @param messages The list of messages to be sent
     * since 3.15.0
     */
    public void sendLogViewed(@NonNull List<BaseMessage> messages) {
        Logger.d(">> FeedNotificationChannelViewModel::sendLogView(), size=%s, isVisible", messages.size(), isVisible);
        if (channel == null || !isVisible) return;
        final boolean result = channel.logViewed(messages);
        Logger.d("++ sendlogView result=%s", result);
    }
}
