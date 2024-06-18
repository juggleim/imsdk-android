package com.jet.im.kit.interfaces.providers

import androidx.lifecycle.ViewModelStoreOwner
import com.jet.im.interfaces.IConversationManager
import com.jet.im.kit.interfaces.PagedQueryHandler
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.vm.ChannelListViewModel
import com.jet.im.kit.vm.ChannelViewModel
import com.jet.im.kit.vm.ChatNotificationChannelViewModel
import com.jet.im.kit.vm.CreateChannelViewModel
import com.jet.im.kit.vm.FeedNotificationChannelViewModel
import com.jet.im.kit.vm.InviteUserViewModel
import com.jet.im.kit.vm.MessageThreadViewModel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.ThreadMessageListParams

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
