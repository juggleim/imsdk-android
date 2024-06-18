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
     * Returns the InviteUserModule provider.
     *
     * @return The [InviteUserModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var inviteUser: InviteUserModuleProvider

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

        this.inviteUser = InviteUserModuleProvider { context, _ -> InviteUserModule(context) }

        this.messageThread = MessageThreadModuleProvider { context, _, message -> MessageThreadModule(context, message) }

        this.participantList = ParticipantListModuleProvider { context, _ -> ParticipantListModule(context) }

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
