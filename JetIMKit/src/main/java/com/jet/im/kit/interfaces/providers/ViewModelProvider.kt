package com.jet.im.kit.interfaces.providers

import androidx.lifecycle.ViewModelStoreOwner
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.query.MessageSearchQuery
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.OpenChannelListQueryParams
import com.sendbird.android.params.ThreadMessageListParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.User
import com.jet.im.kit.interfaces.PagedQueryHandler
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.vm.*

/**
 * Interface definition to be invoked when ChannelListViewModel is created.
 * @see [ModuleProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListViewModelProvider {
    /**
     * Returns the ChannelListViewModel.
     *
     * @return The [ChannelListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, query: GroupChannelListQuery?): ChannelListViewModel
}

/**
 * Interface definition to be invoked when ChannelViewModel is created.
 * @see [ModuleProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelViewModelProvider {
    /**
     * Returns the ChannelViewModel.
     *
     * @return The [ChannelViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        params: MessageListParams?,
        config: ChannelConfig
    ): ChannelViewModel
}

/**
 * Interface definition to be invoked when OpenChannelViewModel is created.
 * @see [ModuleProviders.openChannel]
 * @since 3.9.0
 */
fun interface OpenChannelViewModelProvider {
    /**
     * Returns the OpenChannelViewModel.
     *
     * @return The [OpenChannelViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String, params: MessageListParams?): OpenChannelViewModel
}

/**
 * Interface definition to be invoked when CreateChannelViewModel is created.
 * @see [ModuleProviders.createChannel]
 * @since 3.9.0
 */
fun interface CreateChannelViewModelProvider {
    /**
     * Returns the CreateChannelViewModel.
     *
     * @return The [CreateChannelViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, queryHandler: PagedQueryHandler<UserInfo>?): CreateChannelViewModel
}

/**
 * Interface definition to be invoked when CreateOpenChannelViewModel is created.
 * @see [ModuleProviders.createOpenChannel]
 * @since 3.9.0
 */
fun interface CreateOpenChannelViewModelProvider {
    /**
     * Returns the CreateOpenChannelViewModel.
     *
     * @return The [CreateOpenChannelViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner): CreateOpenChannelViewModel
}

/**
 * Interface definition to be invoked when ChannelSettingsViewModel is created.
 * @see [ModuleProviders.channelSettings]
 * @since 3.9.0
 */
fun interface ChannelSettingsViewModelProvider {
    /**
     * Returns the ChannelSettingsViewModel.
     *
     * @return The [ChannelSettingsViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): ChannelSettingsViewModel
}

/**
 * Interface definition to be invoked when OpenChannelSettingsViewModel is created.
 * @see [ModuleProviders.openChannelSettings]
 * @since 3.9.0
 */
fun interface OpenChannelSettingsViewModelProvider {
    /**
     * Returns the OpenChannelSettingsViewModel.
     *
     * @return The [OpenChannelSettingsViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): OpenChannelSettingsViewModel
}

/**
 * Interface definition to be invoked when InviteUserViewModel is created.
 * @see [ModuleProviders.inviteUser]
 * @since 3.9.0
 */
fun interface InviteUserViewModelProvider {
    /**
     * Returns the InviteUserViewModel.
     *
     * @return The [InviteUserViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<UserInfo>?
    ): InviteUserViewModel
}

/**
 * Interface definition to be invoked when RegisterOperatorViewModel is created.
 * @see [ModuleProviders.registerOperator]
 * @since 3.9.0
 */
fun interface RegisterOperatorViewModelProvider {
    /**
     * Returns the RegisterOperatorViewModel.
     *
     * @return The [RegisterOperatorViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<Member>?
    ): RegisterOperatorViewModel
}

/**
 * Interface definition to be invoked when OpenChannelRegisterOperatorViewModel is created.
 * @see [ModuleProviders.openChannelRegisterOperator]
 * @since 3.9.0
 */
fun interface OpenChannelRegisterOperatorViewModelProvider {
    /**
     * Returns the OpenChannelRegisterOperatorViewModel.
     *
     * @return The [OpenChannelRegisterOperatorViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<User>?
    ): OpenChannelRegisterOperatorViewModel
}

/**
 * Interface definition to be invoked when ModerationViewModel is created.
 * @see [ModuleProviders.moderation]
 * @since 3.9.0
 */
fun interface ModerationViewModelProvider {
    /**
     * Returns the ModerationViewModel.
     *
     * @return The [ModerationViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): ModerationViewModel
}

/**
 * Interface definition to be invoked when OpenChannelModerationViewModel is created.
 * @see [ModuleProviders.openChannelModeration]
 * @since 3.9.0
 */
fun interface OpenChannelModerationViewModelProvider {
    /**
     * Returns the OpenChannelModerationViewModel.
     *
     * @return The [OpenChannelModerationViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): OpenChannelModerationViewModel
}

/**
 * Interface definition to be invoked when MemberListViewModel is created.
 * @see [ModuleProviders.memberList]
 * @since 3.9.0
 */
fun interface MemberListViewModelProvider {
    /**
     * Returns the MemberListViewModel.
     *
     * @return The [MemberListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): MemberListViewModel
}

/**
 * Interface definition to be invoked when BannedUserListViewModel is created.
 * @see [ModuleProviders.bannedUserList]
 * @since 3.9.0
 */
fun interface BannedUserListViewModelProvider {
    /**
     * Returns the BannedUserListViewModel.
     *
     * @return The [BannedUserListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String, channelType: ChannelType?): BannedUserListViewModel
}

/**
 * Interface definition to be invoked when OpenChannelBannedUserListViewModel is created.
 * @see [ModuleProviders.openChannelBannedUserList]
 * @since 3.9.0
 */
fun interface OpenChannelBannedUserListViewModelProvider {
    /**
     * Returns the OpenChannelBannedUserListViewModel.
     *
     * @return The [OpenChannelBannedUserListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): OpenChannelBannedUserListViewModel
}

/**
 * Interface definition to be invoked when MutedMemberListViewModel is created.
 * @see [ModuleProviders.mutedMemberList]
 * @since 3.9.0
 */
fun interface MutedMemberListViewModelProvider {
    /**
     * Returns the MutedMemberListViewModel.
     *
     * @return The [MutedMemberListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): MutedMemberListViewModel
}

/**
 * Interface definition to be invoked when OpenChannelMutedParticipantListViewModel is created.
 * @see [ModuleProviders.openChannelMutedParticipantList]
 * @since 3.9.0
 */
fun interface OpenChannelMutedParticipantListViewModelProvider {
    /**
     * Returns the OpenChannelMutedParticipantListViewModel.
     *
     * @return The [OpenChannelMutedParticipantListViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<UserInfo>?
    ): OpenChannelMutedParticipantListViewModel
}

/**
 * Interface definition to be invoked when OperatorListViewModel is created.
 * @see [ModuleProviders.operatorList]
 * @since 3.9.0
 */
fun interface OperatorListViewModelProvider {
    /**
     * Returns the OperatorListViewModel.
     *
     * @return The [OperatorListViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        channelType: ChannelType?,
        queryHandler: PagedQueryHandler<User>?
    ): OperatorListViewModel
}

/**
 * Interface definition to be invoked when OpenChannelOperatorListViewModel is created.
 * @see [ModuleProviders.openChannelOperatorList]
 * @since 3.9.0
 */
fun interface OpenChannelOperatorListViewModelProvider {
    /**
     * Returns the OpenChannelOperatorListViewModel.
     *
     * @return The [OpenChannelOperatorListViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<User>?
    ): OpenChannelOperatorListViewModel
}

/**
 * Interface definition to be invoked when MessageSearchViewModel is created.
 * @see [ModuleProviders.messageSearch]
 * @since 3.9.0
 */
fun interface MessageSearchViewModelProvider {
    /**
     * Returns the MessageSearchViewModel.
     *
     * @return The [MessageSearchViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String, query: MessageSearchQuery?): MessageSearchViewModel
}

/**
 * Interface definition to be invoked when MessageThreadViewModel is created.
 * @see [ModuleProviders.messageThread]
 * @since 3.9.0
 */
fun interface MessageThreadViewModelProvider {
    /**
     * Returns the MessageThreadViewModel.
     *
     * @return The [MessageThreadViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        message: BaseMessage,
        params: ThreadMessageListParams?
    ): MessageThreadViewModel
}

/**
 * Interface definition to be invoked when ParticipantViewModel is created.
 * @see [ModuleProviders.participantList]
 * @since 3.9.0
 */
fun interface ParticipantViewModelProvider {
    /**
     * Returns the ParticipantViewModel.
     *
     * @return The [ParticipantViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        queryHandler: PagedQueryHandler<User>?
    ): ParticipantViewModel
}

/**
 * Interface definition to be invoked when ChannelPushSettingViewModel is created.
 * @see [ModuleProviders.channelPushSetting]
 * @since 3.9.0
 */
fun interface ChannelPushSettingViewModelProvider {
    /**
     * Returns the ChannelPushSettingViewModel.
     *
     * @return The [ChannelPushSettingViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, channelUrl: String): ChannelPushSettingViewModel
}

/**
 * Interface definition to be invoked when OpenChannelListViewModel is created.
 * @see [ModuleProviders.openChannelList]
 * @since 3.9.0
 */
fun interface OpenChannelListViewModelProvider {
    /**
     * Returns the OpenChannelListViewModel.
     *
     * @return The [OpenChannelListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, params: OpenChannelListQueryParams?): OpenChannelListViewModel
}

/**
 * Interface definition to be invoked when FeedNotificationChannelViewModel is created.
 * @see [ModuleProviders.feedNotificationChannel]
 * @since 3.9.0
 */
internal fun interface FeedNotificationChannelViewModelProvider {
    /**
     * Returns the FeedNotificationChannelViewModel.
     *
     * @return The [FeedNotificationChannelViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        params: MessageListParams?
    ): FeedNotificationChannelViewModel
}

/**
 * Interface definition to be invoked when ChatNotificationChannelViewModel is created.
 * @see [ModuleProviders.chatNotificationChannel]
 * @since 3.9.0
 */
internal fun interface ChatNotificationChannelViewModelProvider {
    /**
     * Returns the ChatNotificationChannelViewModel.
     *
     * @return The [ChatNotificationChannelViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        channelUrl: String,
        params: MessageListParams?
    ): ChatNotificationChannelViewModel
}
