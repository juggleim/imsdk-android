package com.jet.im.kit.interfaces.providers

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.OpenChannel
import com.jet.im.kit.activities.adapter.*
import com.jet.im.kit.model.ChannelListUIParams
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.providers.*

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
 * Interface definition to be invoked when banned user list adapter is created.
 * @see [AdapterProviders.bannedUserList]
 * @since 3.9.0
 */
fun interface BannedUserListAdapterProvider {
    /**
     * Returns the BannedUserListAdapter.
     *
     * @return The [BannedUserListAdapter].
     * @since 3.9.0
     */
    fun provide(): BannedUserListAdapter
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
 * Interface definition to be invoked when create channel user list adapter is created.
 * @see [AdapterProviders.createChannelUserList]
 * @since 3.9.0
 */
fun interface CreateChannelUserListAdapterProvider {
    /**
     * Returns the CreateChannelUserListAdapter.
     *
     * @return The [CreateChannelUserListAdapter].
     * @since 3.9.0
     */
    fun provide(): CreateChannelUserListAdapter
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
 * Interface definition to be invoked when member list adapter is created.
 * @see [AdapterProviders.memberList]
 * @since 3.9.0
 */
fun interface MemberListAdapterProvider {
    /**
     * Returns the MemberListAdapter.
     *
     * @return The [MemberListAdapter].
     * @since 3.9.0
     */
    fun provide(): MemberListAdapter
}

/**
 * Interface definition to be invoked when message search adapter is created.
 * @see [AdapterProviders.messageSearch]
 * @since 3.9.0
 */
fun interface MessageSearchAdapterProvider {
    /**
     * Returns the MessageSearchAdapter.
     *
     * @return The [MessageSearchAdapter].
     * @since 3.9.0
     */
    fun provide(): MessageSearchAdapter
}

/**
 * Interface definition to be invoked when muted member list adapter is created.
 * @see [AdapterProviders.mutedMemberList]
 * @since 3.9.0
 */
fun interface MutedMemberListAdapterProvider {
    /**
     * Returns the MutedMemberListAdapter.
     *
     * @return The [MutedMemberListAdapter].
     * @since 3.9.0
     */
    fun provide(): MutedMemberListAdapter
}

/**
 * Interface definition to be invoked when open channel banned user list adapter is created.
 * @see [AdapterProviders.openChannelBannedUserList]
 * @since 3.9.0
 */
fun interface OpenChannelBannedUserListAdapterProvider {
    /**
     * Returns the OpenChannelBannedUserListAdapter.
     *
     * @return The [OpenChannelBannedUserListAdapter].
     * @since 3.9.0
     */
    fun provide(): OpenChannelBannedUserListAdapter
}

/**
 * Interface definition to be invoked when open channel list adapter is created.
 * @see [AdapterProviders.openChannelList]
 * @since 3.9.0
 */
fun interface OpenChannelListAdapterProvider {
    /**
     * Returns the OpenChannelListAdapter.
     *
     * @return The [OpenChannelListAdapter].
     * @since 3.9.0
     */
    fun provide(): OpenChannelListAdapter
}

/**
 * Interface definition to be invoked when open channel message list adapter is created.
 * @see [AdapterProviders.openChannelMessageList]
 * @since 3.9.0
 */
fun interface OpenChannelMessageListAdapterProvider {
    /**
     * Returns the OpenChannelMessageListAdapter.
     *
     * @return The [OpenChannelMessageListAdapter].
     * @since 3.9.0
     */
    fun provide(params: MessageListUIParams): OpenChannelMessageListAdapter
}

/**
 * Interface definition to be invoked when open channel muted participant list adapter is created.
 * @see [AdapterProviders.openChannelMutedParticipantList]
 * @since 3.9.0
 */
fun interface OpenChannelMutedParticipantListAdapterProvider {
    /**
     * Returns the OpenChannelMutedParticipantListAdapter.
     *
     * @return The [OpenChannelMutedParticipantListAdapter].
     * @since 3.9.0
     */
    fun provide(): OpenChannelMutedParticipantListAdapter
}

/**
 * Interface definition to be invoked when open channel register operator list adapter is created.
 * @see [AdapterProviders.openChannelRegisterOperatorList]
 * @since 3.9.0
 */
fun interface OpenChannelRegisterOperatorListAdapterProvider {
    /**
     * Returns the OpenChannelRegisterOperatorListAdapter.
     *
     * @return The [OpenChannelRegisterOperatorListAdapter].
     * @since 3.9.0
     */
    fun provide(channel: OpenChannel?): OpenChannelRegisterOperatorListAdapter
}

/**
 * Interface definition to be invoked when operator list adapter is created.
 * @see [AdapterProviders.operatorList]
 * @since 3.9.0
 */
fun interface OperatorListAdapterProvider {
    /**
     * Returns the OperatorListAdapter.
     *
     * @return The [OperatorListAdapter].
     * @since 3.9.0
     */
    fun provide(): OperatorListAdapter
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
 * Interface definition to be invoked when register operator list adapter is created.
 * @see [AdapterProviders.registerOperatorList]
 * @since 3.9.0
 */
fun interface RegisterOperatorListAdapterProvider {
    /**
     * Returns the RegisterOperatorListAdapter.
     *
     * @return The [RegisterOperatorListAdapter].
     * @since 3.9.0
     */
    fun provide(): RegisterOperatorListAdapter
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
