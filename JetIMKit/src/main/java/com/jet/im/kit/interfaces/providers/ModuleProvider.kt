package com.jet.im.kit.interfaces.providers

import android.content.Context
import android.os.Bundle
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.notifications.ChatNotificationChannelModule
import com.jet.im.kit.internal.ui.notifications.FeedNotificationChannelModule
import com.jet.im.kit.modules.*
import com.jet.im.kit.providers.ModuleProviders

/**
 * Interface definition to be invoked when ChannelListModule is created.
 * @see [ModuleProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListModuleProvider {
    /**
     * Returns the ChannelListModule.
     *
     * @return The [ChannelListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelListModule
}

/**
 * Interface definition to be invoked when ChannelModule is created.
 * @see [ModuleProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelModuleProvider {
    /**
     * Returns the ChannelModule.
     *
     * @return The [ChannelModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelModule
}

/**
 * Interface definition to be invoked when OpenChannelModule is created.
 * @see [ModuleProviders.openChannel]
 * @since 3.9.0
 */
fun interface OpenChannelModuleProvider {
    /**
     * Returns the OpenChannelModule.
     *
     * @return The [OpenChannelModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelModule
}

/**
 * Interface definition to be invoked when CreateChannelModule is created.
 * @see [ModuleProviders.createChannel]
 * @since 3.9.0
 */
fun interface CreateChannelModuleProvider {
    /**
     * Returns the CreateChannelModule.
     *
     * @return The [CreateChannelModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): CreateChannelModule
}

/**
 * Interface definition to be invoked when CreateOpenChannelModule is created.
 * @see [ModuleProviders.createOpenChannel]
 * @since 3.9.0
 */
fun interface CreateOpenChannelModuleProvider {
    /**
     * Returns the CreateOpenChannelModule.
     *
     * @return The [CreateOpenChannelModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): CreateOpenChannelModule
}

/**
 * Interface definition to be invoked when ChannelSettingsModule is created.
 * @see [ModuleProviders.channelSettings]
 * @since 3.9.0
 */
fun interface ChannelSettingsModuleProvider {
    /**
     * Returns the ChannelSettingsModule.
     *
     * @return The [ChannelSettingsModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelSettingsModule
}

/**
 * Interface definition to be invoked when OpenChannelSettingsModule is created.
 * @see [ModuleProviders.openChannelSettings]
 * @since 3.9.0
 */
fun interface OpenChannelSettingsModuleProvider {
    /**
     * Returns the OpenChannelSettingsModule.
     *
     * @return The [OpenChannelSettingsModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelSettingsModule
}

/**
 * Interface definition to be invoked when InviteUserModule is created.
 * @see [ModuleProviders.inviteUser]
 * @since 3.9.0
 */
fun interface InviteUserModuleProvider {
    /**
     * Returns the InviteUserModule.
     *
     * @return The [InviteUserModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): InviteUserModule
}

/**
 * Interface definition to be invoked when RegisterOperatorModule is created.
 * @see [ModuleProviders.registerOperator]
 * @since 3.9.0
 */
fun interface RegisterOperatorModuleProvider {
    /**
     * Returns the RegisterOperatorModule.
     *
     * @return The [RegisterOperatorModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): RegisterOperatorModule
}

/**
 * Interface definition to be invoked when OpenChannelRegisterOperatorModule is created.
 * @see [ModuleProviders.openChannelRegisterOperator]
 * @since 3.9.0
 */
fun interface OpenChannelRegisterOperatorModuleProvider {
    /**
     * Returns the OpenChannelRegisterOperatorModule.
     *
     * @return The [OpenChannelRegisterOperatorModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelRegisterOperatorModule
}

/**
 * Interface definition to be invoked when ModerationModule is created.
 * @see [ModuleProviders.moderation]
 * @since 3.9.0
 */
fun interface ModerationModuleProvider {
    /**
     * Returns the ModerationModule.
     *
     * @return The [ModerationModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ModerationModule
}

/**
 * Interface definition to be invoked when OpenChannelModerationModule is created.
 * @see [ModuleProviders.openChannelModeration]
 * @since 3.9.0
 */
fun interface OpenChannelModerationModuleProvider {
    /**
     * Returns the OpenChannelModerationModule.
     *
     * @return The [OpenChannelModerationModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelModerationModule
}

/**
 * Interface definition to be invoked when MemberListModule is created.
 * @see [ModuleProviders.memberList]
 * @since 3.9.0
 */
fun interface MemberListModuleProvider {
    /**
     * Returns the MemberListModule.
     *
     * @return The [MemberListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): MemberListModule
}

/**
 * Interface definition to be invoked when BannedUserListModule is created.
 * @see [ModuleProviders.bannedUserList]
 * @since 3.9.0
 */
fun interface BannedUserListModuleProvider {
    /**
     * Returns the BannedUserListModule.
     *
     * @return The [BannedUserListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): BannedUserListModule
}

/**
 * Interface definition to be invoked when OpenChannelBannedUserListModule is created.
 * @see [ModuleProviders.openChannelBannedUserList]
 * @since 3.9.0
 */
fun interface OpenChannelBannedUserListModuleProvider {
    /**
     * Returns the OpenChannelBannedUserListModule.
     *
     * @return The [OpenChannelBannedUserListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelBannedUserListModule
}

/**
 * Interface definition to be invoked when MutedMemberListModule is created.
 * @see [ModuleProviders.mutedMemberList]
 * @since 3.9.0
 */
fun interface MutedMemberListModuleProvider {
    /**
     * Returns the MutedMemberListModule.
     *
     * @return The [MutedMemberListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): MutedMemberListModule
}

/**
 * Interface definition to be invoked when OpenChannelMutedParticipantListModule is created.
 * @see [ModuleProviders.openChannelMutedParticipantList]
 * @since 3.9.0
 */
fun interface OpenChannelMutedParticipantListModuleProvider {
    /**
     * Returns the OpenChannelMutedParticipantListModule.
     *
     * @return The [OpenChannelMutedParticipantListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelMutedParticipantListModule
}

/**
 * Interface definition to be invoked when OperatorListModule is created.
 * @see [ModuleProviders.operatorList]
 * @since 3.9.0
 */
fun interface OperatorListModuleProvider {
    /**
     * Returns the OperatorListModule.
     *
     * @return The [OperatorListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OperatorListModule
}

/**
 * Interface definition to be invoked when OpenChannelOperatorListModule is created.
 * @see [ModuleProviders.openChannelOperatorList]
 * @since 3.9.0
 */
fun interface OpenChannelOperatorListModuleProvider {
    /**
     * Returns the OpenChannelOperatorListModule.
     *
     * @return The [OpenChannelOperatorListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelOperatorListModule
}

/**
 * Interface definition to be invoked when MessageSearchModule is created.
 * @see [ModuleProviders.messageSearch]
 * @since 3.9.0
 */
fun interface MessageSearchModuleProvider {
    fun provide(context: Context, args: Bundle): MessageSearchModule
}

/**
 * Interface definition to be invoked when ParticipantListModule is created.
 * @see [ModuleProviders.participantList]
 * @since 3.9.0
 */
fun interface ParticipantListModuleProvider {
    fun provide(context: Context, args: Bundle): ParticipantListModule
}

/**
 * Interface definition to be invoked when ChannelPushSettingModule is created.
 * @see [ModuleProviders.channelPushSetting]
 * @since 3.9.0
 */
fun interface ChannelPushSettingModuleProvider {
    /**
     * Returns the ChannelPushSettingModule.
     *
     * @return The [ChannelPushSettingModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelPushSettingModule
}

/**
 * Interface definition to be invoked when OpenChannelListModule is created.
 * @see [ModuleProviders.openChannelList]
 * @since 3.9.0
 */
fun interface OpenChannelListModuleProvider {
    /**
     * Returns the OpenChannelListModule.
     *
     * @return The [OpenChannelListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): OpenChannelListModule
}

/**
 * Interface definition to be invoked when MessageThreadModule is created.
 * @see [ModuleProviders.messageThread]
 * @since 3.9.0
 */
fun interface MessageThreadModuleProvider {
    /**
     * Returns the MessageThreadModule.
     *
     * @return The [MessageThreadModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle, message: BaseMessage): MessageThreadModule
}

internal fun interface FeedNotificationChannelModuleProvider {
    fun provide(context: Context, args: Bundle, config: NotificationConfig?): FeedNotificationChannelModule
}

internal fun interface ChatNotificationChannelModuleProvider {
    fun provide(context: Context, args: Bundle, config: NotificationConfig?): ChatNotificationChannelModule
}
