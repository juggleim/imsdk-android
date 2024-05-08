package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.handler.OpenChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.log.Logger;

/**
 * ViewModel preparing and managing data related with the push setting of a channel
 *
 * since 3.1.0
 */
public class OpenChannelModerationViewModel extends BaseViewModel {
    @NonNull
    private final String CHANNEL_HANDLER_OPEN_CHANNEL_MODERATION = "CHANNEL_HANDLER_OPEN_CHANNEL_MODERATION" + System.currentTimeMillis();
    @NonNull
    private final String channelUrl;
    @Nullable
    private OpenChannel channel;
    @NonNull
    private final MutableLiveData<Boolean> currentUserRegisteredOperator = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<String> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> currentUserBanned = new MutableLiveData<>();

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.1.0
     */
    public OpenChannelModerationViewModel(@NonNull String channelUrl) {
        super();
        this.channelUrl = channelUrl;
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_OPEN_CHANNEL_MODERATION, new OpenChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {}

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> OpenChannelModerationViewModel::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    OpenChannelModerationViewModel.this.channelDeleted.setValue(channelUrl);
                }
            }

            @Override
            public void onOperatorUpdated(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl()) && channel instanceof OpenChannel) {
                    Logger.i(">> OpenChannelModerationViewModel::onOperatorUpdated()");
                    OpenChannelModerationViewModel.this.currentUserRegisteredOperator.setValue(((OpenChannel) channel).isOperator(SendbirdChat.getCurrentUser()));
                }
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser restrictedUser) {
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null) {
                    Logger.i(">> OpenChannelModerationViewModel::onUserBanned()");
                    OpenChannelModerationViewModel.this.currentUserBanned.setValue(restrictedUser.getUserId().equals(currentUser.getUserId()));
                }
            }
        });
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.1.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                OpenChannel.getChannel(channelUrl, (channel, e1) -> {
                    OpenChannelModerationViewModel.this.channel = channel;
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
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_OPEN_CHANNEL_MODERATION);
    }

    /**
     * Returns LiveData that can be observed whether the current user is banned.
     *
     * @return LiveData holding whether the current user is banned or not
     * since 3.1.0
     */
    @NonNull
    public LiveData<Boolean> getCurrentUserBanned() {
        return currentUserBanned;
    }

    /**
     * Returns LiveData that can be observed if the current user is operator or not.
     *
     * @return LiveData holding whether the current user is registered as operator or not
     * since 3.1.0
     */
    @NonNull
    public LiveData<Boolean> getCurrentUserRegisteredOperator() {
        return currentUserRegisteredOperator;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted channel
     * since 3.1.0
     */
    @NonNull
    public LiveData<String> getChannelDeleted() {
        return channelDeleted;
    }

    /**
     * Returns {@code OpenChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code OpenChannel} this view model is currently associated with
     * since 3.1.0
     */
    @Nullable
    public OpenChannel getChannel() {
        return channel;
    }

    /**
     * Returns URL of OpenChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * since 3.1.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        if (channel == null) return false;
        return channelUrl.equals(channel.getUrl());
    }
}
