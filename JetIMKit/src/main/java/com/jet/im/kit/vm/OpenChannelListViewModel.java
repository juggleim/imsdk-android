package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.channel.query.OpenChannelListQuery;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.params.OpenChannelListQueryParams;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.tasks.JobTask;
import com.jet.im.kit.internal.tasks.TaskQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of open channels
 *
 * since 3.2.0
 */
public class OpenChannelListViewModel extends BaseViewModel implements OnPagedDataLoader<List<OpenChannel>> {
    @NonNull
    private final OpenChannelListQueryParams params;
    @NonNull
    private final MutableLiveData<List<OpenChannel>> channelList = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> initialLoaded = new MutableLiveData<>();
    @Nullable
    private OpenChannelListRepository repository;

    /**
     * Constructor
     *
     * @param params A params to retrieve {@code OpenChannel} list for the current user
     * since 3.2.0
     */
    public OpenChannelListViewModel(@Nullable OpenChannelListQueryParams params) {
        this.params = params == null ? createOpenChannelListQueryParams() : params;
    }

    /**
     * Live data that can be observed for a list of channels.
     *
     * @return LiveData holding the list of {@code OpenChannel} for the current user
     * since 3.2.0
     */
    @NonNull
    public LiveData<List<OpenChannel>> getChannelList() {
        return channelList;
    }

    /**
     * Returns LiveData that can be observed if the Initial load has ended.
     *
     * @return LiveData holding whether initial loading is finished
     * since 3.2.0
     */
    @NonNull
    public LiveData<Boolean> getInitialLoaded() {
        return initialLoaded;
    }

    private void notifyChannelChanged() {
        if (repository == null) return;
        channelList.postValue(repository.getChannelList());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * Returns {@code false} as the channel list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.2.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Returns the empty list as the channel list do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.2.0
     */
    @NonNull
    @Override
    public List<OpenChannel> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNext() {
        return repository != null && repository.hasNext();
    }

    /**
     * Requests the list of <code>OpenChannel</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * since 3.2.0
     */
    public synchronized void loadInitial() {
        this.repository = new OpenChannelListRepository(params);
        TaskQueue.addTask(new JobTask<List<OpenChannel>>() {
            @Override
            protected List<OpenChannel> call() throws Exception {
                try {
                    return loadNext();
                } finally {
                    initialLoaded.postValue(true);
                }
            }
        });
    }

    /**
     * Requests the list of <code>OpenChannel</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * @return Returns the queried list of <code>OpenChannel</code>s if no error occurs
     * @throws Exception Throws exception if getting the channel list are failed
     * since 3.2.0
     */
    @NonNull
    @Override
    public List<OpenChannel> loadNext() throws Exception {
        if (repository == null) return Collections.emptyList();
        try {
            return repository.loadNext();
        } finally {
            notifyChannelChanged();
        }
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.2.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                handler.onAuthenticated();
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates group channel list query.
     *
     * @return {@code OpenChannelListQuery} to retrieve the list of channels
     * since 3.2.0
     */
    @NonNull
    protected OpenChannelListQueryParams createOpenChannelListQueryParams() {
        return new OpenChannelListQueryParams();
    }

    private static class OpenChannelListRepository implements OnPagedDataLoader<List<OpenChannel>> {
        @NonNull
        private final OpenChannelListQuery query;
        @NonNull
        private final List<OpenChannel> openChannels = new ArrayList<>();

        public OpenChannelListRepository(@NonNull OpenChannelListQueryParams params) {
            this.query = OpenChannel.createOpenChannelListQuery(params);
        }

        @WorkerThread
        @NonNull
        private List<OpenChannel> loadNextBlocking() throws Exception {
            final CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<SendbirdException> error = new AtomicReference<>();
            final AtomicReference<List<OpenChannel>> channelListRef = new AtomicReference<>();
            this.query.next((list, e) -> {
                channelListRef.set(list);
                error.set(e);
                lock.countDown();
            });
            lock.await();

            if (error.get() != null) throw error.get();
            return channelListRef.get();
        }

        @NonNull
        @Override
        public List<OpenChannel> loadPrevious() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<OpenChannel> loadNext() throws Exception {
            if (!hasNext()) return Collections.emptyList();

            final List<OpenChannel> list = loadNextBlocking();
            openChannels.addAll(list);
            return list;
        }

        @Override
        public boolean hasNext() {
            return query.getHasNext();
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @NonNull
        public List<OpenChannel> getChannelList() {
            return openChannels;
        }
    }
}
