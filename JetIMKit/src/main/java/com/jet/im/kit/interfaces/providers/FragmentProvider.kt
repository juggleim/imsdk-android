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
 * Interface definition to be invoked when RegisterOperatorFragment is created.
 * @see [FragmentProviders.registerOperator]
 * @since 3.9.0
 */
fun interface RegisterOperatorFragmentProvider {
    /**
     * Returns the RegisterOperatorFragment.
     *
     * @return The [RegisterOperatorFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): RegisterOperatorFragment
}

/**
 * Interface definition to be invoked when OpenChannelRegisterOperatorFragment is created.
 * @see [FragmentProviders.openChannelRegisterOperator]
 * @since 3.9.0
 */

/**
 * Interface definition to be invoked when ModerationFragment is created.
 * @see [FragmentProviders.moderation]
 * @since 3.9.0
 */
fun interface ModerationFragmentProvider {
    /**
     * Returns the ModerationFragment.
     *
     * @return The [ModerationFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): ModerationFragment
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
 * Interface definition to be invoked when BannedUserListFragment is created.
 * @see [FragmentProviders.bannedUserList]
 * @since 3.9.0
 */
fun interface BannedUserListFragmentProvider {
    /**
     * Returns the BannedUserListFragment.
     *
     * @return The [BannedUserListFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): BannedUserListFragment
}


/**
 * Interface definition to be invoked when MutedMemberListFragment is created.
 * @see [FragmentProviders.mutedMemberList]
 * @since 3.9.0
 */
fun interface MutedMemberListFragmentProvider {
    /**
     * Returns the MutedMemberListFragment.
     *
     * @return The [MutedMemberListFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): MutedMemberListFragment
}


/**
 * Interface definition to be invoked when OperatorListFragment is created.
 * @see [FragmentProviders.operatorList]
 * @since 3.9.0
 */
fun interface OperatorListFragmentProvider {
    /**
     * Returns the OperatorListFragment.
     *
     * @return The [OperatorListFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): OperatorListFragment
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
 * Interface definition to be invoked when ChannelPushSettingFragment is created.
 * @see [FragmentProviders.channelPushSetting]
 * @since 3.9.0
 */
fun interface ChannelPushSettingFragmentProvider {
    /**
     * Returns the ChannelPushSettingFragment.
     *
     * @return The [ChannelPushSettingFragment].
     * @since 3.9.0
     */
    fun provide(channelUrl: String, args: Bundle): ChannelPushSettingFragment
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
