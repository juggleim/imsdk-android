package com.jet.im.kit.internal.extensions

import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.model.configurations.ChannelConfig

internal fun GroupChannel.shouldDisableInput(channelConfig: ChannelConfig): Boolean {
    return channelConfig.enableSuggestedReplies && this.lastMessage?.extendedMessagePayload?.get(StringSet.disable_chat_input) == true.toString()
}
