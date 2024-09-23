package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.juggle.im.JIM;
import com.juggle.im.interfaces.IConversationManager;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.contracts.GroupChannelCollectionContract;
import com.jet.im.kit.internal.contracts.GroupChannelCollectionImpl;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.internal.contracts.TaskQueueContract;
import com.jet.im.kit.internal.contracts.TaskQueueImpl;
import com.jet.im.kit.internal.tasks.JobTask;
import com.jet.im.kit.internal.testmodel.ChannelListViewModelDataContract;
import com.jet.im.kit.log.Logger;
import com.juggle.im.model.ConversationInfo;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.exception.SendbirdException;

import org.jetbrains.annotations.TestOnly;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of channels
 * <p>
 * since 3.0.0
 */
public class ChannelListViewModel extends BaseViewModel implements OnPagedDataLoader<List<ConversationInfo>> {

    @Nullable
    private GroupChannelCollectionContract collection;

    @NonNull
    private final IConversationManager query;
    @NonNull
    private final MutableLiveData<List<ConversationInfo>> channelList;

    @NonNull
    private final IConversationManager.IConversationListener collectionHandler;
    @NonNull
    private final TaskQueueContract taskQueue;

    @Nullable
    @VisibleForTesting
    ChannelListViewModelDataContract contract;

    /**
     * Constructor
     *
     * @param query A query to retrieve {@code GroupChannel} list for the current user
     */
    public ChannelListViewModel(@Nullable IConversationManager query) {
        this(query, new SendbirdUIKitImpl(), new TaskQueueImpl());
    }

    @VisibleForTesting
    ChannelListViewModel(@Nullable IConversationManager query, @NonNull SendbirdUIKitContract sendbirdUIKit, @NonNull TaskQueueContract taskQueue) {
        super(sendbirdUIKit);
        this.query = query == null ? createGroupChannelListQuery() : query;
        this.channelList = new MutableLiveData<>();
        this.taskQueue = taskQueue;
        this.collectionHandler = new IConversationManager.IConversationListener() {
            @Override
            public void onConversationInfoAdd(List<ConversationInfo> conversationInfoList) {
                notifyChannelChanged();
            }

            @Override
            public void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList) {
                notifyChannelChanged();
            }

            @Override
            public void onConversationInfoDelete(List<ConversationInfo> conversationInfoList) {
                notifyChannelChanged();
            }

            @Override
            public void onTotalUnreadMessageCountUpdate(int count) {
                notifyChannelChanged();
            }
        };
    }

    @TestOnly
    ChannelListViewModel(@NonNull ChannelListViewModelDataContract contract) {
        super(contract.getSendbirdUIKit());
        this.collection = contract.getCollection();
        this.query = contract.getQuery();
        this.channelList = contract.getChannelList();
        this.collectionHandler = contract.getCollectionHandler();
        this.taskQueue = contract.getTaskQueue();
        this.contract = contract;
    }

    @TestOnly
    boolean isSameProperties(@NonNull ChannelListViewModelDataContract contract) {
        // It's enough to check the instance's reference.
        return contract.getSendbirdUIKit() == this.sendbirdUIKit
                && contract.getCollection() == this.collection
                && contract.getQuery() == query
                && contract.getChannelList() == channelList
                && contract.getCollectionHandler() == collectionHandler
                && contract.getTaskQueue() == taskQueue;
    }

    /**
     * Live data that can be observed for a list of channels.
     *
     * @return LiveData holding the list of {@code GroupChannel} for the current user
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<ConversationInfo>> getChannelList() {
        return channelList;
    }

    @VisibleForTesting
    synchronized void initChannelCollection() {
        Logger.d(">> ChannelListViewModel::initChannelCollection()");
        if (this.collection != null) {
            disposeChannelCollection();
        }
        this.collection = createGroupChannelCollection();
        this.collection.setConversationCollectionHandler(collectionHandler);
    }

    private synchronized void disposeChannelCollection() {
        Logger.d(">> ChannelListViewModel::disposeChannelCollection()");
        if (this.collection != null) {
            this.collection.dispose();
        }
    }

    private void notifyChannelChanged() {
        if (collection == null) return;
        List<ConversationInfo> newList = collection.getChannelList();
        Logger.d(">> ChannelListViewModel::notifyDataSetChanged(), size = %s", newList.size());
        channelList.postValue(newList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposeChannelCollection();
    }

    /**
     * Returns {@code false} as the channel list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Returns the empty list as the channel list do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<ConversationInfo> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNext() {
        return collection != null && collection.getHasMore();
    }

    /**
     * Requests the list of <code>GroupChannel</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     * <p>
     * since 3.0.0
     */
    public void loadInitial() {
        initChannelCollection();
        taskQueue.addTask(new JobTask<List<ConversationInfo>>() {
            @Override
            protected List<ConversationInfo> call() throws Exception {
                return loadNext();
            }
        });
    }

    /**
     * Requests the list of <code>GroupChannel</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * @return Returns the queried list of <code>GroupChannel</code>s if no error occurs
     * @throws Exception Throws exception if getting the channel list are failed
     *                   since 3.0.0
     */
    @NonNull
    @Override
    public List<ConversationInfo> loadNext() throws Exception {
        if (!hasNext()) return Collections.emptyList();

        try {
            return loadMoreBlocking();
        } finally {
            notifyChannelChanged();
        }
    }

    @NonNull
    private List<ConversationInfo> loadMoreBlocking() throws Exception {
        if (collection == null) return Collections.emptyList();

        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<SendbirdException> error = new AtomicReference<>();
        final AtomicReference<List<ConversationInfo>> channelListRef = new AtomicReference<>();
        collection.loadMore((channelList) -> {
            channelListRef.set(channelList);
            lock.countDown();
        });
        lock.await();

        if (error.get() != null) throw error.get();
        return channelListRef.get();
    }

    /**
     * Sets push notification settings of this channel.
     *
     * @param channel Target GroupChannel
     * @param enable  Whether the push notification turns on
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void setPushNotification(@NonNull GroupChannel channel, boolean enable, @Nullable OnCompleteHandler handler) {
        channel.setMyPushTriggerOption(enable ? GroupChannel.PushTriggerOption.ALL :
                        GroupChannel.PushTriggerOption.OFF,
                e -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ setPushNotification enable : %s result : %s", enable, e == null ? "success" : "error");
                });
    }

    /**
     * Leaves the targeted channel.
     *
     * @param channel Target GroupChannel
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void leaveChannel(@NonNull final GroupChannel channel, @Nullable OnCompleteHandler handler) {
        channel.leave(false, e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }

    /**
     * Tries to connect Sendbird Server.
     *
     * @param handler Callback notifying the result of authentication
     *                since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((e) -> {
            if (e == null) {
                handler.onAuthenticated();
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates group channel list query.
     *
     * @return {@code GroupChannelListQuery} to retrieve the list of channels
     * since 3.0.0
     */
    @NonNull
    protected IConversationManager createGroupChannelListQuery() {
        return JIM.getInstance().getConversationManager();
    }

    @NonNull
    private GroupChannelCollectionContract createGroupChannelCollection() {
        return new GroupChannelCollectionImpl(query);
    }
}
