package com.jet.im.kit.interfaces.providers

import androidx.lifecycle.ViewModelStoreOwner
import com.jet.im.interfaces.IConversationManager
import com.jet.im.kit.interfaces.PagedQueryHandler
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.vm.BannedUserListViewModel
import com.jet.im.kit.vm.ChannelListViewModel
import com.jet.im.kit.vm.ChannelPushSettingViewModel
import com.jet.im.kit.vm.ChannelSettingsViewModel
import com.jet.im.kit.vm.ChannelViewModel
import com.jet.im.kit.vm.ChatNotificationChannelViewModel
import com.jet.im.kit.vm.CreateChannelViewModel
import com.jet.im.kit.vm.FeedNotificationChannelViewModel
import com.jet.im.kit.vm.InviteUserViewModel
import com.jet.im.kit.vm.MemberListViewModel
import com.jet.im.kit.vm.MessageSearchViewModel
import com.jet.im.kit.vm.MessageThreadViewModel
import com.jet.im.kit.vm.ModerationViewModel
import com.jet.im.kit.vm.MutedMemberListViewModel
import com.jet.im.kit.vm.OperatorListViewModel
import com.jet.im.kit.vm.RegisterOperatorViewModel
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.query.MessageSearchQuery
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.ThreadMessageListParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.User

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
    fun provide(owner: ViewModelStoreOwner, query: IConversationManager?): ChannelListViewModel
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
