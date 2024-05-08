package com.example.demo.customization.global

import android.app.Activity
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.customization.GroupChannelRepository

/**
 * In this sample, the header of the channel is not displayed.
 *
 * @see [setCustomFragmentByBuilder]
 */
fun showFragmentProvidersSample(activity: Activity) {
    setCustomFragmentByBuilder()
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * Fragments provide a variety of customizable setters through the builder.
 * You can use these setters to easily create customized fragments.
 *
 * Customized Fragments can be applied to UIKit globally via the [FragmentProviders],
 * so it is recommended that you use them in your Application's onCreate.
 *
 * If you don't want to apply the fragment globally, you can use the code below in your activity.
 * ```
 * val fragment = ChannelFragment.Builder(channelUrl)
 *    .setUseHeader(false)
 *    .build()
 *    .also { supportFragmentManager.beginTransaction().replace(R.id.fragment_container, it).commit() }
 * ```
 * Refer to the documentation below to see the different methods provided by UIKit.
 * **See Also:** [API reference](https://sendbird.com/docs/chat/uikit/v3/android/ref/-sendbird%20-u-i-kit/com.jet.im.kit.providers/-fragment-providers/index.html)
 */
fun setCustomFragmentByBuilder() {
    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        // Use the fragment's builder to customize your screen.
        ChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeader(false)
            /*
            .setCustomFragment()
            .setUseHeaderRightButton()
            .setUseHeaderLeftButton()
            .setHeaderTitle()
            .setHeaderLeftButtonIconResId()
            .setHeaderLeftButtonIcon()
            .setHeaderRightButtonIconResId()
            .setHeaderRightButtonIcon()
            .setUseInputLeftButton()
            .setInputLeftButtonIconResId()
            .setInputLeftButtonIcon()
            .setInputRightButtonIconResId()
            .setInputRightButtonIcon()
            .showInputRightButtonAlways()
            .setInputHint()
            .setOnHeaderLeftButtonClickListener()
            .setOnHeaderRightButtonClickListener()
            .setMessageListAdapter()
            .setMessageListAdapter()
            .setOnMessageClickListener()
            .setOnMessageLongClickListener()
            .setOnQuoteReplyMessageClickListener()
            .setOnQuoteReplyMessageLongClickListener()
            .setOnThreadInfoClickListener()
            .setOnInputLeftButtonClickListener()
            .setMessageListParams()
            .setEmojiReactionClickListener()
            .setEmojiReactionLongClickListener()
            .setEmojiReactionMoreButtonClickListener()
            .setUseMessageGroupUI()
            .setOnMessageProfileClickListener()
            .setOnMessageProfileLongClickListener()
            .setUseUserProfile()
            .setKeyboardDisplayType()
            .setLoadingDialogHandler()
            .setEmptyIcon()
            .setEmptyIcon()
            .setEmptyText()
            .setErrorText()
            .setOnEditModeTextChangedListener()
            .setInputText()
            .setOnInputTextChangedListener()
            .setStartingPoint()
            .setUseHeaderProfileImage()
            .setSuggestedMentionListAdapter()
            .setMentionUIConfig()
            .setEditedTextMarkUIConfig()
            .setMessageTextUIConfig()
            .setSentAtTextUIConfig()
            .setNicknameTextUIConfig()
            .setRepliedMessageTextUIConfig()
            .setMessageInputTextUIConfig()
            .setMessageBackground()
            .setReactionListBackground()
            .setOgtagBackground()
            .setLinkedTextColor()
            .setOnInputRightButtonClickListener()
            .setOnEditModeCancelButtonClickListener()
            .setOnEditModeSaveButtonClickListener()
            .setOnQuoteReplyModeCloseButtonClickListener()
            .setOnInputModeChangedListener()
            .setUseSuggestedMentionListDivider()
            .setOnTooltipClickListener()
            .setOnScrollFirstButtonClickListener()
            .setUseMessageListBanner()
            .setOnVoiceRecorderButtonClickListener()
            .setOnMessageMentionClickListener()
            .setChannelConfig()
            .setUseMessageGroupUI()
             */
            .build()
    }

    // Below is a list of fragments provided by uikit. Customize the screens you want to change.
    /*
    FragmentProviders.channelList = ChannelListFragmentProvider { args ->
        ChannelListFragment.Builder().withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.openChannel = OpenChannelFragmentProvider { channelUrl, args ->
        OpenChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.createChannel = CreateChannelFragmentProvider { channelType, args ->
        CreateChannelFragment.Builder(channelType).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.createOpenChannel = CreateOpenChannelFragmentProvider { args ->
        CreateOpenChannelFragment.Builder().withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    FragmentProviders.channelSettings = ChannelSettingsFragmentProvider { channelUrl, args ->
        ChannelSettingsFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.openChannelSettings = OpenChannelSettingsFragmentProvider { channelUrl, args ->
        OpenChannelSettingsFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.inviteUser = InviteUserFragmentProvider { channelUrl, args ->
        InviteUserFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.registerOperator = RegisterOperatorFragmentProvider { channelUrl, args ->
        RegisterOperatorFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build() as RegisterOperatorFragment // for backward compatibility
    }

    FragmentProviders.openChannelRegisterOperator = OpenChannelRegisterOperatorFragmentProvider { channelUrl, args ->
        OpenChannelRegisterOperatorFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    FragmentProviders.moderation = ModerationFragmentProvider { channelUrl, args ->
        ModerationFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.openChannelModeration = OpenChannelModerationFragmentProvider { channelUrl, args ->
        OpenChannelModerationFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    FragmentProviders.memberList = MemberListFragmentProvider { channelUrl, args ->
        MemberListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    FragmentProviders.bannedUserList = BannedUserListFragmentProvider { channelUrl, args ->
        BannedUserListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(false)
            .build()
    }

    FragmentProviders.openChannelBannedUserList = OpenChannelBannedUserListFragmentProvider { channelUrl, args ->
        OpenChannelBannedUserListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    FragmentProviders.mutedMemberList = MutedMemberListFragmentProvider { channelUrl, args ->
        MutedMemberListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(false)
            .build()
    }

    FragmentProviders.openChannelMutedParticipantList = OpenChannelMutedParticipantListFragmentProvider { channelUrl, args ->
        OpenChannelMutedParticipantListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    FragmentProviders.operatorList = OperatorListFragmentProvider { channelUrl, args ->
        OperatorListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    FragmentProviders.openChannelOperatorList = OpenChannelOperatorListFragmentProvider { channelUrl, args ->
        OpenChannelOperatorListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    FragmentProviders.messageSearch = MessageSearchFragmentProvider { channelUrl, args ->
        MessageSearchFragment.Builder(channelUrl).withArguments(args)
            .setUseSearchBar(true)
            .build()
    }

    FragmentProviders.messageThread = MessageThreadFragmentProvider { channelUrl, message, args ->
        MessageThreadFragment.Builder(channelUrl, message).setStartingPoint(0L)
            .setUseHeader(true)
            .withArguments(args)
            .build()
    }

    FragmentProviders.participantList = ParticipantListFragmentProvider { channelUrl, args ->
        ParticipantListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.channelPushSetting = ChannelPushSettingFragmentProvider { channelUrl, args ->
        ChannelPushSettingFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.openChannelList = OpenChannelListFragmentProvider { args ->
        OpenChannelListFragment.Builder().withArguments(args)
            .setUseHeader(true)
            .build()
    }

    FragmentProviders.feedNotificationChannel = FeedNotificationChannelFragmentProvider { channelUrl, args ->
        FeedNotificationChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeaderLeftButton(true)
            .build()
    }

    FragmentProviders.chatNotificationChannel = ChatNotificationChannelFragmentProvider { channelUrl, args ->
        ChatNotificationChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeaderLeftButton(true)
            .build()
    }
    */
}
