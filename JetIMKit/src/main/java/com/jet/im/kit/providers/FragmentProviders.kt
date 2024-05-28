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
     * Returns the CreateChannelFragment provider.
     *
     * @return The [CreateChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannel: CreateChannelFragmentProvider

    /**
     * Returns the ChannelSettingsFragment provider.
     *
     * @return The [ChannelSettingsFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelSettings: ChannelSettingsFragmentProvider


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
     * Returns the ModerationFragment provider.
     *
     * @return The [ModerationFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var moderation: ModerationFragmentProvider

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
     * Returns the MutedMemberListFragment provider.
     *
     * @return The [MutedMemberListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListFragmentProvider

    /**
     * Returns the OperatorListFragment provider.
     *
     * @return The [OperatorListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListFragmentProvider


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
     * Returns the ChannelPushSettingFragment provider.
     *
     * @return The [ChannelPushSettingFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelPushSetting: ChannelPushSettingFragmentProvider


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



        this.createChannel = CreateChannelFragmentProvider { channelType, args ->
            CreateChannelFragment.Builder(channelType).withArguments(args)
                .setUseHeader(true)
                .build()
        }


        this.channelSettings = ChannelSettingsFragmentProvider { channelUrl, args ->
            ChannelSettingsFragment.Builder(channelUrl).withArguments(args)
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



        this.moderation = ModerationFragmentProvider { channelUrl, args ->
            ModerationFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
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

        this.mutedMemberList = MutedMemberListFragmentProvider { channelUrl, args ->
            MutedMemberListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(false)
                .build()
        }



        this.operatorList = OperatorListFragmentProvider { channelUrl, args ->
            OperatorListFragment.Builder(channelUrl).withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
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

        this.channelPushSetting = ChannelPushSettingFragmentProvider { channelUrl, args ->
            ChannelPushSettingFragment.Builder(channelUrl).withArguments(args)
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
