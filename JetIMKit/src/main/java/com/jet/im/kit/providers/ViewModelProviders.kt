package com.jet.im.kit.providers

import androidx.lifecycle.ViewModelProvider
import com.jet.im.kit.interfaces.providers.*
import com.jet.im.kit.vm.*

/**
 * A set of Providers that provide a [BaseViewModel] that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ViewModelProviders {
    /**
     * Returns the ChannelListViewModel provider.
     *
     * @return The [ChannelListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListViewModelProvider

    /**
     * Returns the ChannelViewModel provider.
     *
     * @return The [ChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelViewModelProvider

    /**
     * Returns the InviteUserViewModel provider.
     *
     * @return The [InviteUserViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUser: InviteUserViewModelProvider

    /**
     * Returns the MessageThreadViewModel provider.
     *
     * @return The [MessageThreadViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageThread: MessageThreadViewModelProvider

    /**
     * Returns the FeedNotificationChannelViewModel provider.
     *
     * @return The [FeedNotificationChannelViewModel].
     * @since 3.9.0
     */
    @JvmStatic
    internal lateinit var feedNotificationChannel: FeedNotificationChannelViewModelProvider

    /**
     * Returns the ChatNotificationChannelViewModel provider.
     *
     * @return The [ChatNotificationChannelViewModel].
     * @since 3.9.0
     */
    @JvmStatic
    internal lateinit var chatNotificationChannel: ChatNotificationChannelViewModelProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListViewModelProvider { owner, query ->
            ViewModelProvider(owner, ViewModelFactory(query))[ChannelListViewModel::class.java]
        }

        this.channel = ChannelViewModelProvider { owner, channelUrl, params, channelConfig ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, params, channelConfig)
            )[channelUrl, ChannelViewModel::class.java]
        }

        this.inviteUser = InviteUserViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[InviteUserViewModel::class.java]
        }

        this.messageThread = MessageThreadViewModelProvider { owner, channelUrl, parentMessage, params ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, parentMessage, params)
            )[channelUrl, MessageThreadViewModel::class.java]
        }

        this.feedNotificationChannel = FeedNotificationChannelViewModelProvider { owner, channelUrl, params ->
            ViewModelProvider(
                owner,
                NotificationViewModelFactory(channelUrl, params)
            )[channelUrl, FeedNotificationChannelViewModel::class.java]
        }

        this.chatNotificationChannel = ChatNotificationChannelViewModelProvider { owner, channelUrl, params ->
            ViewModelProvider(
                owner,
                NotificationViewModelFactory(channelUrl, params)
            )[channelUrl, ChatNotificationChannelViewModel::class.java]
        }
    }

    init {
        resetToDefault()
    }
}
