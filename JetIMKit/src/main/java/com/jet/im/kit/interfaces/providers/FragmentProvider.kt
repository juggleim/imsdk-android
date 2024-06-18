package com.jet.im.kit.interfaces.providers

import android.os.Bundle
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.consts.CreatableChannelType
import com.jet.im.kit.fragments.*
import com.jet.im.kit.providers.*

/**
 * Interface definition to be invoked when ChannelListFragment is created.
 * @see [FragmentProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListFragmentProvider {
    /**
     * Returns the ChannelListFragment.
     *
     * @return The [ChannelListFragment].
     * @since 3.9.0
     */
    fun provide(args: Bundle): ChannelListFragment
}

/**
 * Interface definition to be invoked when ChannelFragment is created.
 * @see [FragmentProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelFragmentProvider {
    /**
     * Returns the ChannelFragment.
     *
     * @return The [ChannelFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): ChannelFragment
}

/**
 * Interface definition to be invoked when OpenChannelFragment is created.
 * @see [FragmentProviders.openChannel]
 * @since 3.9.0
 */
/**
 * Interface definition to be invoked when CreateChannelFragment is created.
 * @see [FragmentProviders.createChannel]
 * @since 3.9.0
 */
fun interface CreateChannelFragmentProvider {
    /**
     * Returns the CreateChannelFragment.
     *
     * @return The [CreateChannelFragment].
     * @since 3.9.0
     */
    fun provide(channelType: CreatableChannelType, args: Bundle): CreateChannelFragment
}

/**
 * Interface definition to be invoked when ChannelSettingsFragment is created.
 * @see [FragmentProviders.channelSettings]
 * @since 3.9.0
 */
fun interface ChannelSettingsFragmentProvider {
    /**
     * Returns the ChannelSettingsFragment.
     *
     * @return The [ChannelSettingsFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): ChannelSettingsFragment
}

/**
 * Interface definition to be invoked when OpenChannelSettingsFragment is created.
 * @see [FragmentProviders.openChannelSettings]
 * @since 3.9.0
 */
/**
 * Interface definition to be invoked when InviteUserFragment is created.
 * @see [FragmentProviders.inviteUser]
 * @since 3.9.0
 */
fun interface InviteUserFragmentProvider {
    /**
     * Returns the InviteUserFragment.
     *
     * @return The [InviteUserFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): InviteUserFragment
}




/**
 * Interface definition to be invoked when MemberListFragment is created.
 * @see [FragmentProviders.memberList]
 * @since 3.9.0
 */
fun interface MemberListFragmentProvider {
    /**
     * Returns the MemberListFragment.
     *
     * @return The [MemberListFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): MemberListFragment
}

/**
 * Interface definition to be invoked when MessageSearchFragment is created.
 * @see [FragmentProviders.messageSearch]
 * @since 3.9.0
 */
fun interface MessageSearchFragmentProvider {
    /**
     * Returns the MessageSearchFragment.
     *
     * @return The [MessageSearchFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): MessageSearchFragment
}

/**
 * Interface definition to be invoked when MessageThreadFragment is created.
 * @see [FragmentProviders.messageThread]
 * @since 3.9.0
 */
fun interface MessageThreadFragmentProvider {
    /**
     * Returns the MessageThreadFragment.
     *
     * @return The [MessageThreadFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, message: BaseMessage, args: Bundle): MessageThreadFragment
}

/**
 * Interface definition to be invoked when FeedNotificationChannelFragment is created.
 * @see [FragmentProviders.feedNotificationChannel]
 * @since 3.9.0
 */
fun interface FeedNotificationChannelFragmentProvider {
    /**
     * Returns the FeedNotificationChannelFragment.
     *
     * @return The [FeedNotificationChannelFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): FeedNotificationChannelFragment
}

/**
 * Interface definition to be invoked when ChatNotificationChannelFragment is created.
 * @see [FragmentProviders.chatNotificationChannel]
 * @since 3.9.0
 */
fun interface ChatNotificationChannelFragmentProvider {
    /**
     * Returns the ChatNotificationChannelFragment.
     *
     * @return The [ChatNotificationChannelFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): ChatNotificationChannelFragment
}
