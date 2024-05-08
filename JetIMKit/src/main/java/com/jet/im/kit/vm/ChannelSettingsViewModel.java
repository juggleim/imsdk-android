package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.GroupChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.GroupChannelUpdateParams;
import com.sendbird.android.user.MemberState;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.log.Logger;

/**
 * ViewModel preparing and managing data related with the settings of a channel
 *
 * since 3.0.0
 */
public class ChannelSettingsViewModel extends BaseViewModel {
    @NonNull
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_SETTINGS" + System.currentTimeMillis();
    @NonNull
    private final String channelUrl;
    @Nullable
    private GroupChannel channel;
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> shouldFinish = new MutableLiveData<>();

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.0.0
     */
    public ChannelSettingsViewModel(@NonNull String channelUrl) {
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
                GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                    ChannelSettingsViewModel.this.channel = channel;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterChannelHandler();
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
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    public LiveData<GroupChannel> getChannelUpdated() {
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
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_ID, new GroupChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {}

            @Override
            public void onUserJoined(@NonNull GroupChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserJoined()");
                    Logger.d("++ joind user : " + user);
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onUserLeft(@NonNull GroupChannel channel, @NonNull User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == MemberState.NONE) {
                        shouldFinish.postValue(true);
                        return;
                    }
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onChannelChanged(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onChannelChanged()");
                    notifyChannelUpdated((GroupChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ChannelSettingsViewModel::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    shouldFinish.postValue(true);
                }
            }

            @Override
            public void onOperatorUpdated(@NonNull BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onOperatorUpdated()");
                    notifyChannelUpdated((GroupChannel) channel);
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                }
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null &&
                    user.getUserId().equals(currentUser.getUserId())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserBanned()");
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

    private void notifyChannelUpdated(@NonNull GroupChannel channel) {
        this.channel = channel;
        channelUpdated.setValue(channel);
    }

    /**
     * Updates current channel.
     *
     * @param params Target GroupChannel
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void updateChannel(@NonNull GroupChannelUpdateParams params, @Nullable OnCompleteHandler handler) {
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
     * Leaves current channel.
     *
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void leaveChannel(@Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendbirdException("Couldn't retrieve the channel"));
            return;
        }
        channel.leave(e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }
}
