package com.jet.im.kit.utils;

import androidx.annotation.Nullable;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.jet.im.kit.model.configurations.ChannelConfig;

/**
 * @deprecated 3.6.0
 */
@Deprecated
public class ReactionUtils {
    /**
     * @deprecated 3.6.0
     * Use {@link ChannelConfig#getEnableReactions(ChannelConfig, BaseChannel)}
     */
    @Deprecated
    public static boolean useReaction(@Nullable BaseChannel channel) {
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            if (groupChannel.isSuper() || groupChannel.isBroadcast() || groupChannel.isChatNotification()) {
                return false;
            } else {
                return Available.isSupportReaction();
            }
        } else {
            return false;
        }
    }

    /**
     * @deprecated 3.6.0
     * Use {@link ChannelConfig#canSendReactions(ChannelConfig, BaseChannel)}
     */
    @Deprecated
    public static boolean canSendReaction(@Nullable BaseChannel channel) {
        boolean useReaction = useReaction(channel);
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            boolean isOperator = groupChannel.getMyRole() == Role.OPERATOR;
            boolean isFrozen = groupChannel.isFrozen();
            return useReaction && (isOperator || !isFrozen);
        }
        return false;
    }
}
