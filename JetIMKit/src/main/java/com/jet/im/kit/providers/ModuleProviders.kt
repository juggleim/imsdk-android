package com.jet.im.kit.providers

import com.jet.im.kit.interfaces.providers.*
import com.jet.im.kit.internal.ui.notifications.ChatNotificationChannelModule
import com.jet.im.kit.internal.ui.notifications.FeedNotificationChannelModule
import com.jet.im.kit.modules.*

/**
 * UIKit for Android, you need a module and components to create a view.
 * Components are the smallest unit of customizable views that can make up a whole screen and the module coordinates these components to be shown as the fragment's view.
 * Each module also has its own customizable style per screen.
 * A set of Providers that provide a Module that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ModuleProviders {

    /**
     * Returns the ChannelListModule provider.
     *
     * @return The [ChannelListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListModuleProvider

    /**
     * Returns the ChannelModule provider.
     *
     * @return The [ChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelModuleProvider

    /**
     * Returns the OpenChannelModule provider.
     *
     * @return The [OpenChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannel: OpenChannelModuleProvider

    /**
     * Returns the CreateChannelModule provider.
     *
     * @return The [CreateChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createChannel: CreateChannelModuleProvider

    /**
     * Returns the CreateOpenChannelModule provider.
     *
     * @return The [CreateOpenChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var createOpenChannel: CreateOpenChannelModuleProvider

    /**
     * Returns the ChannelSettingsModule provider.
     *
     * @return The [ChannelSettingsModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelSettings: ChannelSettingsModuleProvider

    /**
     * Returns the OpenChannelSettingsModule provider.
     *
     * @return The [OpenChannelSettingsModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelSettings: OpenChannelSettingsModuleProvider

    /**
     * Returns the InviteUserModule provider.
     *
     * @return The [InviteUserModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUser: InviteUserModuleProvider

    /**
     * Returns the RegisterOperatorModule provider.
     *
     * @return The [RegisterOperatorModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var registerOperator: RegisterOperatorModuleProvider

    /**
     * Returns the OpenChannelRegisterOperatorModule provider.
     *
     * @return The [OpenChannelRegisterOperatorModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelRegisterOperator: OpenChannelRegisterOperatorModuleProvider

    /**
     * Returns the ModerationModule provider.
     *
     * @return The [ModerationModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var moderation: ModerationModuleProvider

    /**
     * Returns the OpenChannelModerationModule provider.
     *
     * @return The [OpenChannelModerationModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelModeration: OpenChannelModerationModuleProvider

    /**
     * Returns the MemberListModule provider.
     *
     * @return The [MemberListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var memberList: MemberListModuleProvider

    /**
     * Returns the BannedUserListModule provider.
     *
     * @return The [BannedUserListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var bannedUserList: BannedUserListModuleProvider

    /**
     * Returns the OpenChannelBannedUserListModule provider.
     *
     * @return The [OpenChannelBannedUserListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelBannedUserList: OpenChannelBannedUserListModuleProvider

    /**
     * Returns the MutedMemberListModule provider.
     *
     * @return The [MutedMemberListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var mutedMemberList: MutedMemberListModuleProvider

    /**
     * Returns the OpenChannelMutedParticipantListModule provider.
     *
     * @return The [OpenChannelMutedParticipantListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelMutedParticipantList: OpenChannelMutedParticipantListModuleProvider

    /**
     * Returns the OperatorListModule provider.
     *
     * @return The [OperatorListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var operatorList: OperatorListModuleProvider

    /**
     * Returns the OpenChannelOperatorListModule provider.
     *
     * @return The [OpenChannelOperatorListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelOperatorList: OpenChannelOperatorListModuleProvider

    /**
     * Returns the MessageSearchModule provider.
     *
     * @return The [MessageSearchModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageSearch: MessageSearchModuleProvider

    /**
     * Returns the MessageThreadModule provider.
     *
     * @return The [MessageThreadModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageThread: MessageThreadModuleProvider

    /**
     * Returns the ParticipantListModule provider.
     *
     * @return The [ParticipantListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var participantList: ParticipantListModuleProvider

    /**
     * Returns the ChannelPushSettingModule provider.
     *
     * @return The [ChannelPushSettingModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelPushSetting: ChannelPushSettingModuleProvider

    /**
     * Returns the OpenChannelListModule provider.
     *
     * @return The [OpenChannelListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var openChannelList: OpenChannelListModuleProvider

    /**
     * Returns the OpenChannelParticipantListModule provider.
     *
     * @return The [FeedNotificationChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    internal lateinit var feedNotificationChannel: FeedNotificationChannelModuleProvider

    /**
     * Returns the ChatNotificationChannelModule provider.
     *
     * @return The [ChatNotificationChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    internal lateinit var chatNotificationChannel: ChatNotificationChannelModuleProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListModuleProvider { context, _ -> ChannelListModule(context) }

        this.channel = ChannelModuleProvider { context, _ -> ChannelModule(context) }

        this.openChannel = OpenChannelModuleProvider { context, _ -> OpenChannelModule(context) }

        this.createChannel = CreateChannelModuleProvider { context, _ -> CreateChannelModule(context) }

        this.createOpenChannel = CreateOpenChannelModuleProvider { context, _ -> CreateOpenChannelModule(context) }

        this.channelSettings = ChannelSettingsModuleProvider { context, _ -> ChannelSettingsModule(context) }

        this.openChannelSettings = OpenChannelSettingsModuleProvider { context, _ -> OpenChannelSettingsModule(context) }

        this.inviteUser = InviteUserModuleProvider { context, _ -> InviteUserModule(context) }

        this.registerOperator = RegisterOperatorModuleProvider { context, _ -> RegisterOperatorModule(context) }

        this.openChannelRegisterOperator =
            OpenChannelRegisterOperatorModuleProvider { context, _ -> OpenChannelRegisterOperatorModule(context) }

        this.moderation = ModerationModuleProvider { context, _ -> ModerationModule(context) }

        this.openChannelModeration =
            OpenChannelModerationModuleProvider { context, _ -> OpenChannelModerationModule(context) }

        this.memberList = MemberListModuleProvider { context, _ -> MemberListModule(context) }

        this.bannedUserList = BannedUserListModuleProvider { context, _ -> BannedUserListModule(context) }

        this.openChannelBannedUserList =
            OpenChannelBannedUserListModuleProvider { context, _ -> OpenChannelBannedUserListModule(context) }

        this.mutedMemberList = MutedMemberListModuleProvider { context, _ -> MutedMemberListModule(context) }

        this.openChannelMutedParticipantList =
            OpenChannelMutedParticipantListModuleProvider { context, _ -> OpenChannelMutedParticipantListModule(context) }

        this.operatorList = OperatorListModuleProvider { context, _ -> OperatorListModule(context) }

        this.openChannelOperatorList =
            OpenChannelOperatorListModuleProvider { context, _ -> OpenChannelOperatorListModule(context) }

        this.messageSearch = MessageSearchModuleProvider { context, _ -> MessageSearchModule(context) }

        this.messageThread = MessageThreadModuleProvider { context, _, message -> MessageThreadModule(context, message) }

        this.participantList = ParticipantListModuleProvider { context, _ -> ParticipantListModule(context) }

        this.channelPushSetting = ChannelPushSettingModuleProvider { context, _ -> ChannelPushSettingModule(context) }

        this.openChannelList = OpenChannelListModuleProvider { context, _ -> OpenChannelListModule(context) }

        this.feedNotificationChannel = FeedNotificationChannelModuleProvider { context, _, config ->
            FeedNotificationChannelModule(context, config)
        }

        this.chatNotificationChannel = ChatNotificationChannelModuleProvider { context, _, config ->
            ChatNotificationChannelModule(context, config)
        }
    }

    init {
        resetToDefault()
    }
}
