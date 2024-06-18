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
 * Interface definition to be invoked when ParticipantListModule is created.
 * @see [ModuleProviders.participantList]
 * @since 3.9.0
 */
fun interface ParticipantListModuleProvider {
    fun provide(context: Context, args: Bundle): ParticipantListModule
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
