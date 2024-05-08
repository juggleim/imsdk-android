package com.jet.im.kit.providers

import com.jet.im.kit.fragments.*
import com.jet.im.kit.interfaces.providers.*

/**
 * Create a Fragment provider.
 * In situations where you need to create a fragment, create the fragment through the following providers.
 * If you need to use Custom Fragment, change the provider
 *
 * @since 3.9.0
 */
object FragmentProviders {
    /**
     * Returns the ChannelListFragment provider.
     *
     * @return The [ChannelListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListFragmentProvider

    /**
     * Returns the ChannelFragment provider.
     *
     * @return The [ChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelFragmentProvider

    /**
     * Returns the OpenChannelFragment provider.
     *
     * @return The [OpenChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannel: OpenChannelFragmentProvider

    /**
     * Returns the CreateChannelFragment provider.
     *
     * @return The [CreateChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannel: CreateChannelFragmentProvider

    /**
     * Returns the CreateOpenChannelFragment provider.
     *
     * @return The [CreateOpenChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createOpenChannel: CreateOpenChannelFragmentProvider

    /**
     * Returns the ChannelSettingsFragment provider.
     *
     * @return The [ChannelSettingsFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelSettings: ChannelSettingsFragmentProvider

    /**
     * Returns the OpenChannelSettingsFragment provider.
     *
     * @return The [OpenChannelSettingsFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelSettings: OpenChannelSettingsFragmentProvider

    /**
     * Returns the InviteUserFragment provider.
     *
     * @return The [InviteUserFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUser: InviteUserFragmentProvider

    /**
     * Returns the RegisterOperatorFragment provider.
     *
     * @return The [RegisterOperatorFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var registerOperator: RegisterOperatorFragmentProvider

    /**
     * Returns the OpenChannelRegisterOperatorFragment provider.
     *
     * @return The [OpenChannelRegisterOperatorFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelRegisterOperator: OpenChannelRegisterOperatorFragmentProvider

    /**
     * Returns the ModerationFragment provider.
     *
     * @return The [ModerationFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var moderation: ModerationFragmentProvider

    /**
     * Returns the OpenChannelModerationFragment provider.
     *
     * @return The [OpenChannelModerationFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelModeration: OpenChannelModerationFragmentProvider

    /**
     * Returns the MemberListFragment provider.
     *
     * @return The [MemberListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var memberList: MemberListFragmentProvider

    /**
     * Returns the BannedUserListFragment provider.
     *
     * @return The [BannedUserListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var bannedUserList: BannedUserListFragmentProvider

    /**
     * Returns the OpenChannelBannedUserListFragment provider.
     *
     * @return The [OpenChannelBannedUserListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelBannedUserList: OpenChannelBannedUserListFragmentProvider

    /**
     * Returns the MutedMemberListFragment provider.
     *
     * @return The [MutedMemberListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListFragmentProvider

    /**
     * Returns the OpenChannelMutedParticipantListFragment provider.
     *
     * @return The [OpenChannelMutedParticipantListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelMutedParticipantList: OpenChannelMutedParticipantListFragmentProvider

    /**
     * Returns the OperatorListFragment provider.
     *
     * @return The [OperatorListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListFragmentProvider

    /**
     * Returns the OpenChannelOperatorListFragment provider.
     *
     * @return The [OpenChannelOperatorListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelOperatorList: OpenChannelOperatorListFragmentProvider

    /**
     * Returns the MessageSearchFragment provider.
     *
     * @return The [MessageSearchFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageSearch: MessageSearchFragmentProvider

    /**
     * Returns the MessageThreadFragment provider.
     *
     * @return The [MessageThreadFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageThread: MessageThreadFragmentProvider

    /**
     * Returns the ParticipantListFragment provider.
     *
     * @return The [ParticipantListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var participantList: ParticipantListFragmentProvider

    /**
     * Returns the ChannelPushSettingFragment provider.
     *
     * @return The [ChannelPushSettingFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelPushSetting: ChannelPushSettingFragmentProvider

    /**
     * Returns the OpenChannelListFragment provider.
     *
     * @return The [OpenChannelListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelList: OpenChannelListFragmentProvider

    /**
     * Returns the FeedNotificationChannelFragment provider.
     *
     * @return The [FeedNotificationChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var feedNotificationChannel: FeedNotificationChannelFragmentProvider

    /**
     * Returns the ChatNotificationChannelFragment provider.
     *
     * @return The [ChatNotificationChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var chatNotificationChannel: ChatNotificationChannelFragmentProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListFragmentProvider { args ->
            ChannelListFragment.Builder().withArguments(args).setUseHeader(true).build()
        }

        this.channel = ChannelFragmentProvider { channelUrl, args ->
            ChannelFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.openChannel = OpenChannelFragmentProvider { channelUrl, args ->
            OpenChannelFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.createChannel = CreateChannelFragmentProvider { channelType, args ->
            CreateChannelFragment.Builder(channelType).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.createOpenChannel = CreateOpenChannelFragmentProvider { args ->
            CreateOpenChannelFragment.Builder().withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .build()
        }

        this.channelSettings = ChannelSettingsFragmentProvider { channelUrl, args ->
            ChannelSettingsFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.openChannelSettings = OpenChannelSettingsFragmentProvider { channelUrl, args ->
            OpenChannelSettingsFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.inviteUser = InviteUserFragmentProvider { channelUrl, args ->
            InviteUserFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.registerOperator = RegisterOperatorFragmentProvider { channelUrl, args ->
            RegisterOperatorFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build() as RegisterOperatorFragment // for backward compatibility
        }

        this.openChannelRegisterOperator = OpenChannelRegisterOperatorFragmentProvider { channelUrl, args ->
            OpenChannelRegisterOperatorFragment.Builder(channelUrl).withArguments(args)
                .build()
        }

        this.moderation = ModerationFragmentProvider { channelUrl, args ->
            ModerationFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.openChannelModeration = OpenChannelModerationFragmentProvider { channelUrl, args ->
            OpenChannelModerationFragment.Builder(channelUrl).withArguments(args)
                .build()
        }

        this.memberList = MemberListFragmentProvider { channelUrl, args ->
            MemberListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .build()
        }

        this.bannedUserList = BannedUserListFragmentProvider { channelUrl, args ->
            BannedUserListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(false)
                .build()
        }

        this.openChannelBannedUserList = OpenChannelBannedUserListFragmentProvider { channelUrl, args ->
            OpenChannelBannedUserListFragment.Builder(channelUrl).withArguments(args)
                .build()
        }

        this.mutedMemberList = MutedMemberListFragmentProvider { channelUrl, args ->
            MutedMemberListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(false)
                .build()
        }

        this.openChannelMutedParticipantList = OpenChannelMutedParticipantListFragmentProvider { channelUrl, args ->
            OpenChannelMutedParticipantListFragment.Builder(channelUrl).withArguments(args)
                .build()
        }

        this.operatorList = OperatorListFragmentProvider { channelUrl, args ->
            OperatorListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .build()
        }

        this.openChannelOperatorList = OpenChannelOperatorListFragmentProvider { channelUrl, args ->
            OpenChannelOperatorListFragment.Builder(channelUrl).withArguments(args)
                .build()
        }

        this.messageSearch = MessageSearchFragmentProvider { channelUrl, args ->
            MessageSearchFragment.Builder(channelUrl).withArguments(args)
                .setUseSearchBar(true)
                .build()
        }

        this.messageThread = MessageThreadFragmentProvider { channelUrl, message, args ->
            MessageThreadFragment.Builder(channelUrl, message).setStartingPoint(0L)
                .setUseHeader(true)
                .withArguments(args)
                .build()
        }

        this.participantList = ParticipantListFragmentProvider { channelUrl, args ->
            ParticipantListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.channelPushSetting = ChannelPushSettingFragmentProvider { channelUrl, args ->
            ChannelPushSettingFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.openChannelList = OpenChannelListFragmentProvider { args ->
            OpenChannelListFragment.Builder().withArguments(args)
                .setUseHeader(true)
                .build()
        }

        this.feedNotificationChannel = FeedNotificationChannelFragmentProvider { channelUrl, args ->
            FeedNotificationChannelFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeaderLeftButton(true)
                .build()
        }

        this.chatNotificationChannel = ChatNotificationChannelFragmentProvider { channelUrl, args ->
            ChatNotificationChannelFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeaderLeftButton(true)
                .build()
        }
    }

    init {
        resetToDefault()
    }
}
