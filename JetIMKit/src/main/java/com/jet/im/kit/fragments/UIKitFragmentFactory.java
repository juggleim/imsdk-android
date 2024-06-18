package com.jet.im.kit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.consts.CreatableChannelType;
import com.jet.im.kit.providers.FragmentProviders;

/**
 * Create a new Fragment.
 * Each screen provided at UIKit creates a fragment via this Factory.
 * To use custom fragment, not a default fragment, you must inherit this Factory.
 * Extended Factory must be registered in SDK through {@link com.jet.im.kit.SendbirdUIKit#setUIKitFragmentFactory(UIKitFragmentFactory)} method.
 *
 * @deprecated 3.9.0
 * <p> Use {@link FragmentProviders} instead.</p>
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class UIKitFragmentFactory {

    /**
     * Returns the ChannelListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelListFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getChannelList()} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public Fragment newChannelListFragment(@NonNull Bundle args) {
        return FragmentProviders.getChannelList().provide(args);
    }

    /**
     * Returns the ChannelFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args       the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getChannel()} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public Fragment newChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return FragmentProviders.getChannel().provide(channelUrl, args);
    }

    /**
     * Returns the ChannelSettingsFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelSettingsFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getChannelSettings()} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public Fragment newChannelSettingsFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return FragmentProviders.getChannelSettings().provide(channelUrl, args);
    }

    /**
     * Returns the CreateChannelFragment.
     *
     * @param channelType the channel type to be created.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link CreateChannelFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getCreateChannel()} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public Fragment newCreateChannelFragment(@NonNull CreatableChannelType channelType, @NonNull Bundle args) {
        return FragmentProviders.getCreateChannel().provide(channelType, args);
    }

    /**
     * Returns the InviteUserFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link InviteUserFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getInviteUser()} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public Fragment newInviteUserFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return FragmentProviders.getInviteUser().provide(channelUrl, args);
    }

    /**
     * Returns the MessageThreadFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param parentMessage the parent message of the message thread fragment.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link MessageThreadFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getMessageThread()} instead.</p>
     * since 3.3.0
     */
    @Deprecated
    @NonNull
    public Fragment newMessageThreadFragment(@NonNull String channelUrl, @NonNull BaseMessage parentMessage, @NonNull Bundle args) {
        return FragmentProviders.getMessageThread().provide(channelUrl, parentMessage, args);
    }

    /**
     * Returns the FeedNotificationChannelFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link FeedNotificationChannelFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getFeedNotificationChannel()} instead.</p>
     * since 3.5.0
     */
    @Deprecated
    @NonNull
    public Fragment newFeedNotificationChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return FragmentProviders.getFeedNotificationChannel().provide(channelUrl, args);
    }

    /**
     * Returns the ChatNotificationChannelFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link FeedNotificationChannelFragment}
     * @deprecated 3.9.0
     * <p> Use {@link FragmentProviders#getChatNotificationChannel()} instead.</p>
     * since 3.5.0
     */
    @Deprecated
    @NonNull
    public Fragment newChatNotificationChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return FragmentProviders.getChatNotificationChannel().provide(channelUrl, args);
    }
}
