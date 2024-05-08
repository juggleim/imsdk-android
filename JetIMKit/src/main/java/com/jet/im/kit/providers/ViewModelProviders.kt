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
     * Returns the OpenChannelViewModel provider.
     *
     * @return The [OpenChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannel: OpenChannelViewModelProvider

    /**
     * Returns the CreateChannelViewModel provider.
     *
     * @return The [CreateChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannel: CreateChannelViewModelProvider

    /**
     * Returns the CreateOpenChannelViewModel provider.
     *
     * @return The [CreateOpenChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createOpenChannel: CreateOpenChannelViewModelProvider

    /**
     * Returns the ChannelSettingsViewModel provider.
     *
     * @return The [ChannelSettingsViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelSettings: ChannelSettingsViewModelProvider

    /**
     * Returns the OpenChannelSettingsViewModel provider.
     *
     * @return The [OpenChannelSettingsViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelSettings: OpenChannelSettingsViewModelProvider

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
     * Returns the OpenChannelRegisterOperatorViewModel provider.
     *
     * @return The [OpenChannelRegisterOperatorViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelRegisterOperator: OpenChannelRegisterOperatorViewModelProvider

    /**
     * Returns the ModerationViewModel provider.
     *
     * @return The [ModerationViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var moderation: ModerationViewModelProvider

    /**
     * Returns the OpenChannelModerationViewModel provider.
     *
     * @return The [OpenChannelModerationViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelModeration: OpenChannelModerationViewModelProvider

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
     * Returns the OpenChannelBannedUserListViewModel provider.
     *
     * @return The [OpenChannelBannedUserListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelBannedUserList: OpenChannelBannedUserListViewModelProvider

    /**
     * Returns the MutedMemberListViewModel provider.
     *
     * @return The [MutedMemberListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListViewModelProvider

    /**
     * Returns the OpenChannelMutedParticipantListViewModel provider.
     *
     * @return The [OpenChannelMutedParticipantListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelMutedParticipantList: OpenChannelMutedParticipantListViewModelProvider

    /**
     * Returns the OperatorListViewModel provider.
     *
     * @return The [OperatorListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListViewModelProvider

    /**
     * Returns the OpenChannelOperatorListViewModel provider.
     *
     * @return The [OpenChannelOperatorListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelOperatorList: OpenChannelOperatorListViewModelProvider

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
     * Returns the ParticipantViewModel provider.
     *
     * @return The [ParticipantViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var participantList: ParticipantViewModelProvider

    /**
     * Returns the ChannelPushSettingViewModel provider.
     *
     * @return The [ChannelPushSettingViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelPushSetting: ChannelPushSettingViewModelProvider

    /**
     * Returns the OpenChannelListViewModel provider.
     *
     * @return The [OpenChannelListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelList: OpenChannelListViewModelProvider

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

        this.openChannel = OpenChannelViewModelProvider { owner, channelUrl, params ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, params)
            )[channelUrl, OpenChannelViewModel::class.java]
        }

        this.createChannel = CreateChannelViewModelProvider { owner, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(pagedQueryHandler)
            )[CreateChannelViewModel::class.java]
        }

        this.createOpenChannel = CreateOpenChannelViewModelProvider { owner ->
            ViewModelProvider(
                owner,
                ViewModelFactory()
            )[CreateOpenChannelViewModel::class.java]
        }

        this.channelSettings = ChannelSettingsViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ChannelSettingsViewModel::class.java]
        }

        this.openChannelSettings = OpenChannelSettingsViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, OpenChannelSettingsViewModel::class.java]
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

        this.openChannelRegisterOperator =
            OpenChannelRegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
                ViewModelProvider(
                    owner,
                    ViewModelFactory(channelUrl, pagedQueryHandler)
                )[OpenChannelRegisterOperatorViewModel::class.java]
            }

        this.moderation = ModerationViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ModerationViewModel::class.java]
        }

        this.openChannelModeration = OpenChannelModerationViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, OpenChannelModerationViewModel::class.java]
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

        this.openChannelBannedUserList = OpenChannelBannedUserListViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, OpenChannelBannedUserListViewModel::class.java]
        }

        this.mutedMemberList = MutedMemberListViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, MutedMemberListViewModel::class.java]
        }

        this.openChannelMutedParticipantList =
            OpenChannelMutedParticipantListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
                ViewModelProvider(
                    owner,
                    ViewModelFactory(channelUrl, pagedQueryHandler)
                )[channelUrl, OpenChannelMutedParticipantListViewModel::class.java]
            }

        this.operatorList = OperatorListViewModelProvider { owner, channelUrl, channelType, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, channelType, pagedQueryHandler)
            )[channelUrl, OperatorListViewModel::class.java]
        }

        this.openChannelOperatorList = OpenChannelOperatorListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[channelUrl, OpenChannelOperatorListViewModel::class.java]
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

        this.participantList = ParticipantViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[channelUrl, ParticipantViewModel::class.java]
        }

        this.channelPushSetting = ChannelPushSettingViewModelProvider { owner, channelUrl ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl)
            )[channelUrl, ChannelPushSettingViewModel::class.java]
        }

        this.openChannelList = OpenChannelListViewModelProvider { owner, params ->
            ViewModelProvider(
                owner,
                ViewModelFactory(params)
            )[OpenChannelListViewModel::class.java]
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
