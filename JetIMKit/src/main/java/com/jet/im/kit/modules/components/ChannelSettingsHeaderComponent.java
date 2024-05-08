package com.jet.im.kit.modules.components;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.jet.im.kit.internal.ui.components.StateHeaderView;

/**
 * This class creates and performs a view corresponding the channel settings header area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ChannelSettingsHeaderComponent extends StateHeaderComponent {
    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        final View rootView = getRootView();
        if (!(rootView instanceof StateHeaderView)) return;

        final StateHeaderView headerView = (StateHeaderView) rootView;
        if (channel.isBroadcast() && channel.getMyRole() != Role.OPERATOR) {
            headerView.setUseRightButton(false);
        }
    }
}
