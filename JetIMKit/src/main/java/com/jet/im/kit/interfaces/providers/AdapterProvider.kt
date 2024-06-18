package com.jet.im.kit.interfaces.providers

import com.jet.im.kit.activities.adapter.ChannelListAdapter
import com.jet.im.kit.activities.adapter.InviteUserListAdapter
import com.jet.im.kit.activities.adapter.MessageListAdapter
import com.jet.im.kit.activities.adapter.ParticipantListAdapter
import com.jet.im.kit.activities.adapter.ThreadListAdapter
import com.jet.im.kit.model.ChannelListUIParams
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.providers.AdapterProviders
import com.sendbird.android.channel.GroupChannel

/**
 * Interface definition to be invoked when message list adapter is created.
 * @see [AdapterProviders.messageList]
 * @since 3.9.0
 */
fun interface MessageListAdapterProvider {
    /**
     * Returns the MessageListAdapter.
     *
     * @return The [MessageListAdapter].
     * @since 3.9.0
     */
    fun provide(channel: GroupChannel, uiParams: MessageListUIParams): MessageListAdapter
}


/**
 * Interface definition to be invoked when channel list adapter is created.
 * @see [AdapterProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListAdapterProvider {
    /**
     * Returns the ChannelListAdapter.
     *
     * @return The [ChannelListAdapter].
     * @since 3.9.0
     */
    fun provide(uiParams: ChannelListUIParams): ChannelListAdapter
}

/**
 * Interface definition to be invoked when invite user list adapter is created.
 * @see [AdapterProviders.inviteUserList]
 * @since 3.9.0
 */
fun interface InviteUserListAdapterProvider {
    /**
     * Returns the InviteUserListAdapter.
     *
     * @return The [InviteUserListAdapter].
     * @since 3.9.0
     */
    fun provide(): InviteUserListAdapter
}


/**
 * Interface definition to be invoked when participant list adapter is created.
 * @see [AdapterProviders.participantList]
 * @since 3.9.0
 */
fun interface ParticipantListAdapterProvider {
    /**
     * Returns the ParticipantListAdapter.
     *
     * @return The [ParticipantListAdapter].
     * @since 3.9.0
     */
    fun provide(): ParticipantListAdapter
}

/**
 * Interface definition to be invoked when thread list adapter is created.
 * @see [AdapterProviders.threadList]
 * @since 3.9.0
 */
fun interface ThreadListAdapterProvider {
    /**
     * Returns the ThreadListAdapter.
     *
     * @return The [ThreadListAdapter].
     * @since 3.9.0
     */
    fun provide(params: MessageListUIParams): ThreadListAdapter
}
