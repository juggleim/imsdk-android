package com.jet.im.kit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
    public Fragment newChannelFragment(@NonNull int conversationType,@NonNull String conversationId, @NonNull Bundle args) {
        return FragmentProviders.getChannel().provide(conversationType,conversationId, args);
    }
}
