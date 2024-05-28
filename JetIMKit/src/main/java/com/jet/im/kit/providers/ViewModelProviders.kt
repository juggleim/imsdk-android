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
     * Returns the CreateChannelViewModel provider.
     *
     * @return The [CreateChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannel: CreateChannelViewModelProvider

    /**
     * Returns the ChannelSettingsViewModel provider.
     *
     * @return The [ChannelSettingsViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelSettings: ChannelSettingsViewModelProvider

    /**
     * Returns the InviteUserViewModel provider.
     *
     * @return The [InviteUserViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUser: InviteUserViewModelProvider

    /**
     * Returns the RegisterOperatorViewModel provider.
     *
     * @return The [RegisterOperatorViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var registerOperator: RegisterOperatorViewModelProvider

    /**
     * Returns the ModerationViewModel provider.
     *
     * @return The [ModerationViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var moderation: ModerationViewModelProvider

    /**
     * Returns the MemberListViewModel provider.
     *
     * @return The [MemberListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var memberList: MemberListViewModelProvider

    /**
     * Returns the BannedUserListViewModel provider.
     *
     * @return The [BannedUserListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var bannedUserList: BannedUserListViewModelProvider

    /**
     * Returns the MutedMemberListViewModel provider.
     *
     * @return The [MutedMemberListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListViewModelProvider

    /**
     * Returns the OperatorListViewModel provider.
     *
     * @return The [OperatorListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListViewModelProvider

    /**
     * Returns the MessageSearchViewModel provider.
     *
     * @return The [MessageSearchViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageSearch: MessageSearchViewModelProvider

    /**
     * Returns the MessageThreadViewModel provider.
     *
     * @return The [MessageThreadViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageThread: MessageThreadViewModelProvider

    /**
     * Returns the ChannelPushSettingViewModel provider.
     *
     * @return The [ChannelPushSettingViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelPushSetting: ChannelPushSettingViewModelProvider

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

        this.createChannel = CreateChannelViewModelProvider { owner, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(pagedQueryHandler)
            )[CreateChannelViewModel::class.java]
        }

        this.channelSettings = ChannelSettingsViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ChannelSettingsViewModel::class.java]
        }

        this.inviteUser = InviteUserViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[InviteUserViewModel::class.java]
        }

        this.registerOperator = RegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[RegisterOperatorViewModel::class.java]
        }

        this.moderation = ModerationViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ModerationViewModel::class.java]
        }

        this.memberList = MemberListViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, MemberListViewModel::class.java]
        }

        this.bannedUserList = BannedUserListViewModelProvider { owner, channelUrl, channelType ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, channelType)
            )[channelUrl, BannedUserListViewModel::class.java]
        }

        this.mutedMemberList = MutedMemberListViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, MutedMemberListViewModel::class.java]
        }

        this.operatorList = OperatorListViewModelProvider { owner, channelUrl, channelType, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, channelType, pagedQueryHandler)
            )[channelUrl, OperatorListViewModel::class.java]
        }

        this.messageSearch = MessageSearchViewModelProvider { owner, channelUrl, query ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, query)
            )[channelUrl, MessageSearchViewModel::class.java]
        }

        this.messageThread = MessageThreadViewModelProvider { owner, channelUrl, parentMessage, params ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, parentMessage, params)
            )[channelUrl, MessageThreadViewModel::class.java]
        }

        this.channelPushSetting = ChannelPushSettingViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ChannelPushSettingViewModel::class.java]
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
