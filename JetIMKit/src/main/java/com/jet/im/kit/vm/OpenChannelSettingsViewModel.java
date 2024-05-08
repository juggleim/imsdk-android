package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.OpenChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.OpenChannelUpdateParams;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.log.Logger;

/**
 * ViewModel preparing and managing data related with the settings of an open channel
 *
 * since 3.0.0
 */
public class OpenChannelSettingsViewModel extends BaseViewModel {
    @NonNull
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHANNEL_SETTINGS" + System.currentTimeMillis();
    @NonNull
    private final String channelUrl;
    @Nullable
    private OpenChannel channel;
    @NonNull
    private final MutableLiveData<OpenChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> shouldFinish = new MutableLiveData<>();

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.0.0
     */
    public OpenChannelSettingsViewModel(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
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
                    OpenChannelSettingsViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        registerChannelHandler();
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     *
     * since 3.0.0
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterChannelHandler();
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
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code OpenChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<OpenChannel> getChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if the Activity or Fragment should be finished.
     *
     * @return LiveData holding the event for whether the Activity or Fragment should be finished
     * since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> shouldFinish() {
        return shouldFinish;
    }

    private void registerChannelHandler() {
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_ID, new OpenChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {}

            @Override
            public void onUserEntered(@NonNull OpenChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserEntered()");
                    Logger.d("++ joind user : " + user);
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onUserExited(@NonNull OpenChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onChannelChanged(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onChannelChanged()");
                    notifyChannelUpdated((OpenChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> OpenChannelSettingsFragment::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    shouldFinish.postValue(true);
                }
            }

            @Override
            public void onOperatorUpdated(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onOperatorUpdated()");
                    notifyChannelUpdated((OpenChannel) channel);
                    Logger.i("++ Am I an operator : " + ((OpenChannel) channel).isOperator(SendbirdChat.getCurrentUser()));
                    if (!((OpenChannel) channel).isOperator(SendbirdChat.getCurrentUser())) {
                        shouldFinish.postValue(true);
                    }
                }
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null &&
                    user.getUserId().equals(currentUser.getUserId())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserBanned()");
                    shouldFinish.postValue(true);
                }
            }
        });
    }

    private void unregisterChannelHandler() {
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        if (channel == null) return false;
        return channelUrl.equals(channel.getUrl());
    }

    private void notifyChannelUpdated(@NonNull OpenChannel channel) {
        this.channel = channel;
        channelUpdated.setValue(channel);
    }

    /**
     * Updates current channel.
     *
     * @param params Target OpenChannel.
     * @param handler Callback handler called when this method is completed.
     * since 3.0.0
     */
    public void updateChannel(@NonNull OpenChannelUpdateParams params, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("Couldn't retrieve the channel"));
            return;
        }
        channel.updateChannel(params, (updatedChannel, e) -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }

    /**
     * Updates current channel.
     *
     * @param handler Callback handler called when this method is completed.
     * since 3.0.0
     */
    public void deleteChannel(@Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("Couldn't retrieve the channel"));
            return;
        }
        channel.delete(e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }

}
