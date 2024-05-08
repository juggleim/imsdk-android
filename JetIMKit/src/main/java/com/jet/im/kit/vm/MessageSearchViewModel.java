package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.query.MessageSearchQuery;
import com.sendbird.android.params.MessageSearchQueryParams;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnListResultHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data to search a message in a channel
 *
 * since 3.0.0
 */
public class MessageSearchViewModel extends BaseViewModel implements LifecycleObserver, OnPagedDataLoader<List<BaseMessage>> {
    @NonNull
    private final MutableLiveData<List<BaseMessage>> searchResultList = new MutableLiveData<>();
    @NonNull
    private final String channelUrl;
    @Nullable
    private GroupChannel channel;
    @Nullable
    private MessageSearchQuery query;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param query The {@link MessageSearchQuery} instance that you want to use.
     * since 3.0.0
     */
    public MessageSearchViewModel(@NonNull String channelUrl, @Nullable MessageSearchQuery query) {
        super();
        this.channelUrl = channelUrl;
        this.query = query;
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * since 3.0.0
     */
    @Nullable
    public GroupChannel getChannel() {
        return channel;
    }

    /**
     * Returns URL of GroupChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * since 3.0.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    /**
     * Creates message search query.
     *
     * @param keyword Keyword to search for messages
     * @return {@code MessageSearchQuery} to retrieve the list of messages that were searched
     * since 3.0.0
     */
    @NonNull
    protected MessageSearchQuery createMessageSearchQuery(@NonNull String keyword) {
        final MessageSearchQueryParams params = new MessageSearchQueryParams(keyword);
        if (query != null) {
            params.setAdvancedQuery(query.isAdvancedQuery());
            params.setChannelCustomType(query.getChannelCustomType());
            params.setExactMatch(query.getExactMatch());
            params.setLimit(query.getLimit());
            params.setMessageTimestampTo(query.getMessageTimestampTo());
            params.setTargetFields(query.getTargetFields());
            params.setOrder(query.getOrder());
            params.setMessageTimestampFrom(query.getMessageTimestampFrom());
        } else {
            final long timestampFrom = channel == null ? 0 : channel.getMessageOffsetTimestamp();
            params.setMessageTimestampFrom(timestampFrom);
            params.setOrder(MessageSearchQuery.Order.TIMESTAMP);
        }
        params.setChannelUrl(channelUrl);
        params.setReverse(false);
        return SendbirdChat.createMessageSearchQuery(params);
    }

    /**
     * Returns LiveData that can be observed for the list of messages that were searched.
     *
     * @return LiveData holding the latest searched list of messages
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<BaseMessage>> getSearchResultList() {
        return searchResultList;
    }

    /**
     * Searches for messages using {@code keyword}.
     *
     * @param keyword Keyword to search for messages
     * @param handler Callback handler notifying the message search results.
     * since 3.0.0
     */
    public void search(@NonNull String keyword, @Nullable OnListResultHandler<BaseMessage> handler) {
        if (TextUtils.isEmpty(keyword)) return;

        this.query = createMessageSearchQuery(keyword.trim());
        List<BaseMessage> value = this.searchResultList.getValue();
        if (value != null) {
            value.clear();
        }
        this.query.next((queryResult, e) -> {
            if (handler != null) {
                handler.onResult(queryResult != null ? new ArrayList<>(queryResult) : null, e);
            }
            MessageSearchViewModel.this.onResult(queryResult, e);
        });
    }

    /**
     * Returns the keyword used in the most recent query.
     *
     * @return String used as the keyword
     * since 3.0.0
     */
    @Nullable
    public String getKeyword() {
        return this.query != null ? this.query.getKeyword() : null;
    }

    private void onResult(@Nullable List<BaseMessage> results, @Nullable Exception e) {
        if (e != null) {
            Logger.w(e);
            return;
        }

        List<BaseMessage> newDataList = new ArrayList<>();
        if (results != null && results.size() > 0) {
            newDataList.addAll(results);
            List<BaseMessage> origin = this.searchResultList.getValue();
            if (origin != null) {
                newDataList.addAll(0, origin);
                Logger.d("____________ onResult origin=%s", origin.size());
            }
        }
        Logger.d("____________ onResult newDataList=%s", newDataList.size());

        searchResultList.postValue(newDataList);
    }

    @Override
    public boolean hasNext() {
        return query != null && query.getHasNext();
    }

    /**
     * Returns {@code false} as the message search do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Returns the empty list as the message search do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() {
        return Collections.emptyList();
    }

    /**
     * Requests the list of <code>BaseMessage</code>s to be searched.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getSearchResultList()}.
     *
     * @return Returns the queried list of <code>BaseMessage</code>s if no error occurs
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<BaseMessage> loadNext() {
        Logger.d("____________ loadNext hasNext=%s", hasNext());
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();
            try {
                if (query == null) return result.get();
                query.next((queryResult, e) -> {
                    try {
                        if (e != null) {
                            error.set(e);
                            return;
                        }
                        result.set(queryResult);
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
            } catch (Exception e) {
                error.set(e);
            } finally {
                onResult(result.get(), error.get());
            }
            return result.get();
        }
        return Collections.emptyList();
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                    MessageSearchViewModel.this.channel = channel;
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
}
