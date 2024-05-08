package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.android.handler.GroupChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.MemberState;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of users
 *
 * since 3.0.0
 */
public abstract class UserViewModel<T> extends BaseViewModel implements OnPagedDataLoader<List<T>> {
    @NonNull
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();
    @NonNull
    private final String CHANNEL_HANDLER_MEMBER_LIST = "CHANNEL_HANDLER_MEMBER_LIST" + System.currentTimeMillis();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<T>> userList = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> operatorUnregistered = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final String channelUrl;
    @Nullable
    private PagedQueryHandler<T> query;
    @Nullable
    private GroupChannel channel;
    private volatile boolean isInitialRequest = false;
    @NonNull
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    private Future<Boolean> currentFuture;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.0.0
     */
    public UserViewModel(@NonNull String channelUrl) {
        this(channelUrl, null);
    }

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * since 3.0.0
     */
    public UserViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<T> queryHandler) {
        super();
        this.channelUrl = channelUrl;
        this.query = queryHandler;
        registerChannelHandler();
    }

    private void registerChannelHandler() {
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_MEMBER_LIST, new GroupChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel channel, @NonNull BaseMessage message) {
            }

            @Override
            public void onUserLeft(@NonNull GroupChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> UserViewModel::onUserLeft()");
                    if (channel.getMyMemberState() == MemberState.NONE) {
                        channelDeleted.postValue(true);
                    }
                }
            }

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> UserViewModel::onChannelDeleted()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onOperatorUpdated(@NonNull BaseChannel channel) {
                updateChannel(channel);
                if (isCurrentChannel(channel.getUrl()) &&
                    ((GroupChannel) channel).getMyRole() != Role.OPERATOR) {
                    Logger.i(">> UserViewModel::onOperatorUpdated()");
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                    operatorUnregistered.postValue(true);
                }
            }

            @Override
            public void onUserMuted(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                updateChannel(channel);
            }

            @Override
            public void onUserUnmuted(@NonNull BaseChannel channel, @NonNull User user) {
                updateChannel(channel);
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                updateChannel(channel);
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null &&
                    user.getUserId().equals(currentUser.getUserId())) {
                    Logger.i(">> UserViewModel::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserUnbanned(@NonNull BaseChannel channel, @NonNull User user) {
                updateChannel(channel);
            }

            @Override
            public void onChannelChanged(@NonNull BaseChannel channel) {
                updateChannel(channel);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_MEMBER_LIST);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channel != null && channelUrl.equals(channel.getUrl());
    }

    private void onResult(@Nullable List<T> memberList, @Nullable Exception e) {
        if (e != null) {
            Logger.e(e);
            if (isInitialRequest) {
                SendbirdChat.addConnectionHandler(CONNECTION_HANDLER_ID, new ConnectionHandler() {
                    @Override
                    public void onDisconnected(@NonNull String s) {
                    }

                    @Override
                    public void onConnected(@NonNull String s) {
                    }

                    @Override
                    public void onReconnectStarted() {
                    }

                    @Override
                    public void onReconnectSucceeded() {
                        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
                        loadInitial();
                    }

                    @Override
                    public void onReconnectFailed() {
                    }
                });
                return;
            }
            changeAlertStatus(StatusFrameView.Status.ERROR);
            notifyDataSetChanged(this.userList.getValue());
        } else {
            Logger.d("__ added");
            final List<T> newUsers = new ArrayList<>();
            if (memberList != null) {
                newUsers.addAll(memberList);
            }
            final List<T> origin = this.userList.getValue();
            if (origin != null) {
                newUsers.addAll(0, origin);
            }
            applyUserList(newUsers);
        }
        isInitialRequest = false;
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
     * Returns LiveData that can be observed if the current user has been unregistered from the operator.
     *
     * @return LiveData holding whether the current user has been unregistered from the operator
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getOperatorUnregistered() {
        return operatorUnregistered;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding whether the channel has been deleted
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getChannelDeleted() {
        return channelDeleted;
    }

    private void updateChannel(@NonNull BaseChannel channel) {
        if (isCurrentChannel(channel.getUrl())) {
            Logger.i(">> UserViewModel::updateChannel()");
            loadInitial();
        }
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the user list.
     * When the user list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the user list
     * since 3.0.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    private void changeAlertStatus(@NonNull StatusFrameView.Status status) {
        if (!hasData() || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    private boolean hasData() {
        List<T> origin = userList.getValue();
        return origin != null && origin.size() > 0;
    }

    /**
     * Returns LiveData that can be observed for the list of users.
     *
     * @return LiveData holding the latest list of users
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<T>> getUserList() {
        return userList;
    }

    private void applyUserList(@NonNull List<T> newUserList) {
        changeAlertStatus(newUserList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newUserList);
    }

    private void notifyDataSetChanged(@Nullable List<T> list) {
        userList.postValue(list == null ? new ArrayList<>() : list);
    }

    /**
     * Returns {@code false} as the user list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return query != null && query.hasMore();
    }

    /**
     * Requests the list of <code>User</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getUserList()}.
     *
     * since 3.0.0
     */
    public synchronized boolean loadInitial() {
        Logger.d(">> UserViewModel::loadInitial()");
        if (this.query == null) {
            this.query = createQueryHandler(channelUrl);
        }
        if (currentFuture != null) currentFuture.cancel(true);
        this.currentFuture = executorService.schedule(() -> {
            List<T> origin = userList.getValue();
            if (origin != null) {
                origin.clear();
            }
            isInitialRequest = true;
            query.loadInitial(UserViewModel.this::onResult);
            return true;
        }, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * Requests the list of <code>User</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getUserList()}.
     *
     * @return Returns the queried list of <code>User</code>s if no error occurs
     * @throws Exception Throws exception if getting the user list are failed
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<T> loadNext() throws Exception {
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<T>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();
            try {
                if (query == null) return Collections.emptyList();
                query.loadMore((userList, e) -> {
                    try {
                        if (e != null) {
                            error.set(e);
                            return;
                        }
                        result.set(userList);
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
            } catch (Exception e) {
                error.set(e);
                throw e;
            } finally {
                onResult(result.get(), error.get());
            }
            return result.get();
        }
        return Collections.emptyList();
    }

    /**
     * Returns the empty list as the user list do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<T> loadPrevious() {
        return Collections.emptyList();
    }

    /**
     * Adds users with id in {@code userIds} as operators.
     *
     * @param userIds User IDs to be added as operators
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void addOperators(@NonNull List<String> userIds, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.addOperators(userIds, e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Adds user with {@code userId} as operators.
     *
     * @param userId User ID to be added as operator
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void addOperator(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.addOperators(Collections.singletonList(userId), e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Dismisses operator with {@code userId}.
     *
     * @param userId User ID to be dismissed from operator
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void removeOperator(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.removeOperators(Collections.singletonList(userId), e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Mutes the user with {@code userId}.
     *
     * @param userId ID of the user to be muted
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void muteUser(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.muteUser(userId, e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Unmutes the user with {@code userId}.
     *
     * @param userId ID of the user to be unmuted
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void unmuteUser(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.unmuteUser(userId, e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Bans the user with {@code userId}.
     *
     * @param userId ID of the user to be banned
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void banUser(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.banUser(userId, null, -1, e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Unbans the user with {@code userId}.
     *
     * @param userId ID of the user to be unbanned
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void unbanUser(@NonNull String userId, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.unbanUser(userId, e -> {
            if (handler != null) handler.onComplete(e);
        });
    }

    /**
     * Invites users with id in {@code userIds} to the channel.
     *
     * @param userIds User IDs to be invited to the channel
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void inviteUser(@NonNull List<String> userIds, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("channel instance not exists"));
            return;
        }
        channel.invite(userIds, e -> {
            if (handler != null) handler.onComplete(e);
        });
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
                if (TextUtils.isNotEmpty(channelUrl)) {
                    GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                        UserViewModel.this.channel = channel;
                        if (e1 != null) {
                            handler.onAuthenticationFailed();
                        } else {
                            handler.onAuthenticated();
                        }
                    });
                } else {
                    handler.onAuthenticated();
                }
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Create a query handler that is loading paged data.
     *
     * @param channelUrl channel's url
     * @return A paged query handler.
     * since 3.0.0
     */
    @NonNull
    protected abstract PagedQueryHandler<T> createQueryHandler(@NonNull String channelUrl);
}
