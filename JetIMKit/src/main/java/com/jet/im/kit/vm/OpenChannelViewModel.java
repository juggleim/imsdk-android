package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.exception.SendbirdError;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.android.handler.OpenChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.message.UserMessage;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.params.common.MessagePayloadFilter;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.jet.im.kit.consts.MessageLoadState;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnFilteringMessageHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.MessageList;
import com.jet.im.kit.widgets.StatusFrameView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of messages in an open channel
 *
 * since 3.0.0
 */
public class OpenChannelViewModel extends BaseViewModel implements OnPagedDataLoader<List<BaseMessage>> {
    @NonNull
    private final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT" + System.currentTimeMillis();
    @NonNull
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHANNEL_CHAT" + System.currentTimeMillis();
    @NonNull
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @NonNull
    private final MutableLiveData<List<BaseMessage>> messageList = new MutableLiveData<>();
    @NonNull
    private final MessageList messageCollection = new MessageList();
    @NonNull
    private final MutableLiveData<OpenChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Long> messageDeleted = new MutableLiveData<>();
    @Nullable
    private final MessageListParams messageListParams;
    @NonNull
    private final MutableLiveData<MessageLoadState> messageLoadState = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> myMutedInfo = new MutableLiveData<>();
    @Nullable
    private OpenChannel channel;
    @NonNull
    private final String channelUrl;

    private boolean hasPrevious = true;

    private final Observer<BaseMessage> pendingStatusObserver;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param messageListParams Parameters required to retrieve the message list from this view model
     * since 3.0.0
     */
    public OpenChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
        super();
        this.channel = null;
        this.channelUrl = channelUrl;
        this.messageListParams = messageListParams == null ? createMessageListParams() : messageListParams;
        this.messageListParams.setReverse(true);

        this.pendingStatusObserver = message -> {
            Logger.d("__ pending message events, message = %s", message.getMessage());
            if (channel != null && message.getChannelUrl().equals(channel.getUrl())) {
                final SendingStatus sendingStatus = message.getSendingStatus();
                Logger.i("__ pending status of message is changed, pending status = %s ", sendingStatus);
                if (sendingStatus == SendingStatus.SUCCEEDED) {
                    messageCollection.add(message);
                }
                notifyDataSetChanged();
            }

        };
        PendingMessageRepository.getInstance().addPendingMessageStatusChanged(pendingStatusObserver);

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
                // In preparation for the change of channel information, we have to call refresh of the channel.
                if (channel != null) {
                    channel.enter(e1 -> {
                        if (e1 != null) {
                            channelDeleted.postValue(true);
                            return;
                        }
                        refreshChannel(e -> {
                            if (e == null) requestChangeLogs(channel);
                        });
                    });
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });
    }

    private void registerChannelHandler() {
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_ID, new OpenChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {
                if (messageListParams == null || !messageListParams.belongsTo(baseMessage)) return;

                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageReceived(%s)", baseMessage.getMessageId());
                    messageCollection.add(baseMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onUserEntered(@NonNull OpenChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserEntered()");
                    Logger.d("++ joind user : " + user);
                    notifyDataSetChanged();
                    channelUpdated.postValue(channel);
                }
            }

            @Override
            public void onUserExited(@NonNull OpenChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    notifyDataSetChanged();
                    channelUpdated.postValue(channel);
                }
            }

            @Override
            public void onMessageDeleted(@NonNull BaseChannel baseChannel, long msgId) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageDeleted()");
                    Logger.d("++ deletedMessage : " + msgId);
                    messageCollection.deleteByMessageId(msgId);
                    notifyDataSetChanged();
                    messageDeleted.postValue(msgId);
                }
            }

            @Override
            public void onMessageUpdated(@NonNull BaseChannel baseChannel, @NonNull BaseMessage updatedMessage) {
                if (isCurrentChannel(baseChannel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onMessageUpdated()");
                    Logger.d("++ updatedMessage : " + updatedMessage.getMessageId());
                    if (messageListParams != null && !messageListParams.belongsTo(updatedMessage)) {
                        final long msgId = updatedMessage.getMessageId();
                        messageCollection.deleteByMessageId(msgId);
                        messageDeleted.postValue(msgId);
                    } else {
                        messageCollection.update(updatedMessage);
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChannelChanged(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelChanged()");
                    channelUpdated.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> OpenChannelViewModel::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onChannelFrozen(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelFrozen(%s)", channel.isFrozen());
                    notifyDataSetChanged();
                    channelUpdated.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelUnfrozen(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onChannelUnfrozen(%s)", channel.isFrozen());
                    notifyDataSetChanged();
                    channelUpdated.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onOperatorUpdated(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onOperatorUpdated()");
                    Logger.i("++ Am I an operator : " + ((OpenChannel) channel).isOperator(SendbirdChat.getCurrentUser()));
                    notifyDataSetChanged();
                    channelUpdated.postValue((OpenChannel) channel);
                }
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null &&
                    user.getUserId().equals(currentUser.getUserId())) {
                    Logger.i(">> OpenChannelViewModel::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserMuted(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserMuted()");
                    channelUpdated.postValue((OpenChannel) channel);
                    if (SendbirdChat.getCurrentUser() != null && user.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
                        myMutedInfo.postValue(true);
                    }
                }
            }

            @Override
            public void onUserUnmuted(@NonNull BaseChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelViewModel::onUserUnmuted()");
                    channelUpdated.postValue((OpenChannel) channel);
                    if (SendbirdChat.getCurrentUser() != null && user.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
                        myMutedInfo.postValue(false);
                    }
                }
            }

            @Override
            public void onChannelParticipantCountChanged(@NonNull List<OpenChannel> channels) {
                Logger.i(">> OpenChannelViewModel::onChannelParticipantCountChanged()");
                if (!channels.isEmpty()) {
                    for (OpenChannel channel : channels) {
                        if (isCurrentChannel(channel.getUrl())) {
                            channelUpdated.postValue(channel);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onMentionReceived(@NonNull BaseChannel channel, @NonNull BaseMessage message) {
                Logger.i(">> MessageCollection::onMentionReceived()");
                if (isCurrentChannel(channel.getUrl())) {
                    channelUpdated.postValue((OpenChannel) channel);
                }
            }
        });
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channel != null && channelUrl.equals(channel.getUrl());
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code OpenChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<OpenChannel> onChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding whether {@code OpenChannel} has been deleted
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> onChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns LiveData that can be observed if the messages has been deleted.
     *
     * @return LiveData holding the list of IDs of deleted messages
     * since 3.0.0
     */
    @NonNull
    public LiveData<Long> onMessageDeleted() {
        return messageDeleted;
    }

    /**
     * Returns LiveData that can be observed if the current user is muted or not.
     *
     * @return LiveData holding the current user muted information
     * since 3.1.0
     */
    @NonNull
    public LiveData<Boolean> getMyMutedInfo() {
        return myMutedInfo;
    }

    /**
     * Returns {@code OpenChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code OpenChannel} this view model is currently associated with
     * since 3.0.0
     */
    @Nullable
    public OpenChannel getChannel() {
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
     * Returns LiveData that can be observed for the list of messages.
     *
     * @return LiveData holding the latest list of messages
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<BaseMessage>> getMessageList() {
        return messageList;
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
     * Requests the list of <code>BaseMessage</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * since 3.0.0
     */
    public void loadInitial() {
        worker.execute(() -> {
            try {
                loadPrevious();
            } catch (Exception e) {
                Logger.w(e);
            }
        });
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @param ts Timestamp to be the starting point for retrieving previous messages.
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.0.0
     */
    @NonNull
    private List<BaseMessage> loadPrevious(long ts) throws Exception {
        Logger.dev(">> ChannelViewModel::loadPrevious()");
        if (messageListParams == null) return Collections.emptyList();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        if (channel == null) return Collections.emptyList();
        channel.getMessagesByTimestamp(ts, messageListParams, (messages, e) -> {
            try {
                if (e != null) {
                    error.set(e);
                    return;
                }
                result.set(messages);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        if (error.get() != null) throw error.get();
        final List<BaseMessage> newMessageList = result.get();
        Logger.i("++ load previous result size : " + newMessageList.size());
        return newMessageList;
    }

    /**
     * Requests the list of <code>BaseMessage</code>s when the page goes to the previous.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getMessageList()}.
     *
     * @return Returns the list of <code>BaseMessage</code>s if no error occurs
     * @throws Exception Throws exception if getting the message list are failed
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<BaseMessage> loadPrevious() throws Exception {
        if (!hasPrevious() || messageListParams == null) return Collections.emptyList();

        List<BaseMessage> newMessageList;
        try {
            messageLoadState.postValue(MessageLoadState.LOAD_STARTED);
            int cacheMessageSize = messageCollection.size();
            BaseMessage oldMessage = messageCollection.getOldestMessage();
            long ts = cacheMessageSize > 0 && oldMessage != null ? oldMessage.getCreatedAt() : Long.MAX_VALUE;
            newMessageList = loadPrevious(ts);
            Logger.i("++ load previous message list : " + newMessageList);
            messageCollection.addAll(newMessageList);
            hasPrevious = newMessageList.size() >= messageListParams.getPreviousResultSize();
            return newMessageList;
        } catch (Exception e) {
            Logger.w(e);
            throw e;
        } finally {
            notifyDataSetChanged();
            messageLoadState.postValue(MessageLoadState.LOAD_ENDED);
        }
    }

    /**
     * Returns the empty list as the message list for {@code OpenChannel} do not support to load for the next by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<BaseMessage> loadNext() {
        return Collections.emptyList();
    }

    /**
     * Returns {@code false} as the message list for {@code OpenChannel} do not support to load for the next by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return hasPrevious;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (channel != null) {
            channel.exit(e -> Logger.i("__ exit"));
        }

        Logger.dev("-- onCleared ChannelViewModel");
        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_ID);
        PendingMessageRepository.getInstance().removePendingMessageStatusObserver(pendingStatusObserver);
        worker.shutdownNow();
    }

    private void notifyDataSetChanged() {
        final List<BaseMessage> currentList = messageCollection.toList();
        if (channel != null) {
            currentList.addAll(0, PendingMessageRepository.getInstance().getPendingMessageList(channel.getUrl()));
        }
        if (currentList.size() == 0) {
            statusFrame.postValue(StatusFrameView.Status.EMPTY);
        } else {
            statusFrame.postValue(StatusFrameView.Status.NONE);
        }
        messageList.postValue(currentList);
    }

    private void requestChangeLogs(@NonNull BaseChannel channel) {
        if (messageListParams == null) return;
        final String channelUrl = channel.getUrl();
        int cacheMessageSize = messageCollection.size();
        BaseMessage lastMessage = messageCollection.getLatestMessage();
        long lastSyncTs = cacheMessageSize > 0 && lastMessage != null ? lastMessage.getCreatedAt() : 0;
        Logger.dev("++ change logs channel url = %s, lastSyncTs = %s", channelUrl, lastSyncTs);

        if (lastSyncTs > 0) {
            MessageChangeLogsPager pager = new MessageChangeLogsPager(channel, lastSyncTs, messageListParams);
            pager.load(new MessageChangeLogsPager.MessageChangeLogsResultHandler() {
                @Override
                public void onError(@NonNull SendbirdException e) {
                    Logger.e(e);
                }

                @Override
                public void onResult(@NonNull List<BaseMessage> added, @NonNull List<BaseMessage> updated, @NonNull List<Long> deletedIds) {
                    for (long deletedId : deletedIds) {
                        BaseMessage deletedMessage = messageCollection.getById(deletedId);
                        if (deletedMessage != null) {
                            messageCollection.delete(deletedMessage);
                        }
                    }
                    List<BaseMessage> filteredAdded = new ArrayList<>();
                    for (BaseMessage addedMessage : added) {
                        if (messageListParams.belongsTo(addedMessage)) {
                            filteredAdded.add(addedMessage);
                        }
                    }
                    List<BaseMessage> filteredUpdated = new ArrayList<>();
                    for (BaseMessage updatedMessage : updated) {
                        if (messageListParams.belongsTo(updatedMessage)) {
                            filteredUpdated.add(updatedMessage);
                        }
                    }
                    Logger.i("++ channel message change logs result >> deleted message size : %s, current message size : %s, added message size : %s", deletedIds.size(), messageCollection.size(), filteredAdded.size());
                    Logger.i("++ updated Message size : %s", filteredUpdated.size());
                    messageCollection.updateAll(filteredUpdated);
                    if (filteredAdded.size() > 0) {
                        messageCollection.addAll(filteredAdded);
                    }
                    Logger.i("++ merged message size : %s", messageCollection.size());
                    boolean changed = filteredAdded.size() > 0 || filteredUpdated.size() > 0 || deletedIds.size() > 0;
                    Logger.dev("++ changeLogs updated : %s", changed);

                    if (changed) {
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    /**
     * Sends a text message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @param handler Callback notifying that the message which the current user tried to send is filtered
     * since 3.0.0
     */
    public void sendUserMessage(@NonNull UserMessageCreateParams params, @Nullable OnFilteringMessageHandler handler) {
        Logger.i("++ request send message : %s", params);
        if (channel == null || messageListParams == null) return;
        final String channelUrl = channel.getUrl();
        UserMessage pendingUserMessage = channel.sendUserMessage(params, (message, e) -> {
            if (e != null) {
                Logger.e(e);
                PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message);
                notifyDataSetChanged();
                return;
            }

            if (message != null && messageListParams.belongsTo(message)) {
                Logger.i("++ sent message : %s", message);
                messageCollection.add(message);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
                notifyDataSetChanged();
            }
        });
        if (messageListParams.belongsTo(pendingUserMessage)) {
            PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingUserMessage);
            notifyDataSetChanged();
        } else {
            if (handler != null) handler.onFiltered(pendingUserMessage);
        }
    }

    /**
     * Sends a file message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @param fileInfo File information to send to the channel
     * @param handler Callback notifying that the message which the current user tried to send is filtered
     * since 3.0.0
     */
    public void sendFileMessage(@NonNull FileMessageCreateParams params, @NonNull FileInfo fileInfo, @Nullable OnFilteringMessageHandler handler) {
        Logger.i("++ request send file message : %s", params);
        if (channel == null || messageListParams == null) return;
        final String channelUrl = channel.getUrl();
        FileMessage pendingFileMessage = channel.sendFileMessage(params, (message, ee) -> {
            if (ee != null) {
                Logger.e(ee);
                if (message != null) {
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, message);
                    notifyDataSetChanged();
                }
                return;
            }

            if (message != null && messageListParams.belongsTo(message)) {
                Logger.i("++ sent message : %s", message);
                //if (file.exists()) file.deleteOnExit();
                messageCollection.add(message);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, message);
                notifyDataSetChanged();
            }
        });

        if (pendingFileMessage != null) {
            if (messageListParams.belongsTo(pendingFileMessage)) {
                PendingMessageRepository.getInstance().addPendingMessage(channelUrl, pendingFileMessage);
                PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
                notifyDataSetChanged();
            } else {
                if (handler != null) handler.onFiltered(pendingFileMessage);
            }
        }
    }

    /**
     * Resends a message to the channel.
     *
     * @param message Message to resend
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void resendMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        final String channelUrl = channel.getUrl();
        if (message instanceof UserMessage) {
            UserMessage pendingMessage = channel.resendMessage((UserMessage) message, (resentMessage, e) -> {
                if (e != null) {
                    Logger.e(e);
                    if (handler != null) handler.onComplete(e);
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, resentMessage);
                    notifyDataSetChanged();
                    return;
                }

                Logger.i("__ resent message : %s", resentMessage);
                if (resentMessage == null) return;
                messageCollection.add(resentMessage);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, resentMessage);
                notifyDataSetChanged();
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
            notifyDataSetChanged();
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            Logger.d("++ file info=%s", info);
            final File file = info == null ? null : info.getFile();
            FileMessage pendingMessage = channel.resendMessage((FileMessage) message, file, (resentMessage, e) -> {
                if (e != null) {
                    Logger.e(e);
                    if (handler != null) handler.onComplete(e);
                    PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, resentMessage);
                    notifyDataSetChanged();
                    return;
                }

                Logger.i("__ resent file message : %s", resentMessage);
                //if (file.exists()) file.deleteOnExit();
                if (resentMessage == null) return;
                messageCollection.add(resentMessage);
                PendingMessageRepository.getInstance().removePendingMessage(channelUrl, resentMessage);
                notifyDataSetChanged();
            });
            PendingMessageRepository.getInstance().updatePendingMessage(channelUrl, pendingMessage);
            notifyDataSetChanged();
        }
    }

    /**
     * Updates a text message with {@code messageId}.
     *
     * @param messageId ID of message to be updated
     * @param params Parameters to be applied to the message
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (e != null) {
                if (handler != null) handler.onComplete(e);
                return;
            }

            Logger.i("++ updated message : %s", message);
            if (message == null) return;
            messageCollection.update(message);
            notifyDataSetChanged();
        });
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (message.getSendingStatus() == SendingStatus.SUCCEEDED) {
            if (channel == null) return;
            channel.deleteMessage(message, e -> {
                if (e != null) {
                    if (handler != null) handler.onComplete(e);
                    return;
                }
            });
        } else {
            PendingMessageRepository.getInstance().removePendingMessage(message.getChannelUrl(), message);
            notifyDataSetChanged();
        }
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
        messageListParams.setNextResultSize(1);
        messageListParams.setMessagePayloadFilter(new MessagePayloadFilter(false, false, false, false));
        if (messageListParams.getPreviousResultSize() <= 0) {
            messageListParams.setPreviousResultSize(40);
        }
        return messageListParams;
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
                OpenChannel.getChannel(channelUrl, (channel, e1) -> {
                    OpenChannelViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        handler.onAuthenticated();
                        // TODO to be deleted after Chat SDK support
                        if (channel != null) getMyMutedInfo(channel);
                    }
                    refreshChannel(null);
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Try enter this channel.
     * Entering the channel is a prerequisite for chatting.
     *
     * @param channel the open channel.
     * @param handler the result handler.
     * since 3.0.0
     */
    public void enterChannel(@NonNull OpenChannel channel, @NonNull OnCompleteHandler handler) {
        channel.enter(e -> {
            if (e == null) {
                registerChannelHandler();
            }
            handler.onComplete(e);
        });
    }

    // TODO to be deleted after Chat SDK support
    private void getMyMutedInfo(@NonNull OpenChannel channel) {
        channel.getMyMutedInfo((isMuted, description, startAt, endAt, remainingDuration, e) -> {
            if (e == null) {
                myMutedInfo.postValue(isMuted);
            }
        });
    }

    private void refreshChannel(@Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.refresh(e -> {
            if (e != null) {
                Logger.dev(e);
                // already left this channel at the other device.
                if (e.getCode() == SendbirdError.ERR_NON_AUTHORIZED) {
                    channelDeleted.postValue(true);
                }
            } else {
                channelUpdated.postValue(channel);
            }
            if (handler != null) handler.onComplete(e);
        });
    }
}
