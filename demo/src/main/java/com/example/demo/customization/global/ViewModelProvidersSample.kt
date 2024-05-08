package com.example.demo.customization.global

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sendbird.android.params.UserMessageCreateParams
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.interfaces.providers.ChannelViewModelProvider
import com.jet.im.kit.providers.ViewModelProviders
import com.example.demo.customization.GroupChannelRepository
import com.jet.im.kit.vm.ChannelViewModel
import com.jet.im.kit.vm.ViewModelFactory
import java.util.Objects

/**
 * In this sample, when sending a message, the message is sent with the prefix "[Custom ViewModel]".
 *
 * @see [setCustomViewModelProviders]
 */
fun showViewModelProvidersSample(activity: Activity) {
    setCustomViewModelProviders()
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * Customized ViewModels can be applied to UIKit globally via the [ViewModelProviders],
 * so it is recommended that you use them in your Application's onCreate.
 *
 * If you don't want to apply the ViewModel globally, you can set them through the fragment's inheritance.
 * ```
 * class CustomChannelFragment : ChannelFragment() {
 *     override fun onCreateViewModel(): ChannelViewModel {
 *         return ViewModelProvider(
 *             viewModelStore,
 *             CustomViewModelFactory(channelUrl)
 *         )[channelUrl, CustomChannelViewModel::class.java]
 *     }
 * }
 * ```
 * Refer to the documentation below to see the different methods provided by UIKit.
 * **See Also:** [API reference](https://sendbird.com/docs/chat/uikit/v3/android/ref/-sendbird%20-u-i-kit/com.jet.im.kit.providers/-view-model-providers/index.html)
 */
fun setCustomViewModelProviders() {
    // Create custom ViewModel
    class CustomChannelViewModel(channelUrl: String) : ChannelViewModel(channelUrl, null) {
        override fun sendUserMessage(params: UserMessageCreateParams) {
            params.message = "[Custom ViewModel] ${params.message}"
            super.sendUserMessage(params)
        }
    }

    // Create custom ViewModelFactory
    @Suppress("UNCHECKED_CAST")
    class CustomViewModelFactory(private vararg val params: Any?) : ViewModelFactory(*params) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == CustomChannelViewModel::class.java) {

                return CustomChannelViewModel(
                    (Objects.requireNonNull(params)[0] as String)
                ) as T
            }
            return super.create(modelClass)
        }
    }

    // Apply custom ViewModel to UIKit
    ViewModelProviders.channel = ChannelViewModelProvider { owner, channelUrl, _, _ ->
        ViewModelProvider(
            owner,
            CustomViewModelFactory(channelUrl)
        )[channelUrl, CustomChannelViewModel::class.java]
    }

    // Below is a list of ViewModels provided by uikit. Customize the ViewModels you want to change.
    /*
    ViewModelProviders.channelList = ChannelListViewModelProvider { owner, query ->
        ViewModelProvider(owner, ViewModelFactory(query))[ChannelListViewModel::class.java]
    }

    ViewModelProviders.openChannel = OpenChannelViewModelProvider { owner, channelUrl, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, params)
        )[channelUrl, OpenChannelViewModel::class.java]
    }

    ViewModelProviders.createChannel = CreateChannelViewModelProvider { owner, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(pagedQueryHandler)
        )[CreateChannelViewModel::class.java]
    }

    ViewModelProviders.createOpenChannel = CreateOpenChannelViewModelProvider { owner ->
        ViewModelProvider(
            owner,
            ViewModelFactory()
        )[CreateOpenChannelViewModel::class.java]
    }

    ViewModelProviders.channelSettings = ChannelSettingsViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ChannelSettingsViewModel::class.java]
    }

    ViewModelProviders.openChannelSettings = OpenChannelSettingsViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelSettingsViewModel::class.java]
    }

    ViewModelProviders.inviteUser = InviteUserViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[InviteUserViewModel::class.java]
    }

    ViewModelProviders.registerOperator = RegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[RegisterOperatorViewModel::class.java]
    }

    ViewModelProviders.openChannelRegisterOperator =
        OpenChannelRegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[OpenChannelRegisterOperatorViewModel::class.java]
        }

    ViewModelProviders.moderation = ModerationViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ModerationViewModel::class.java]
    }

    ViewModelProviders.openChannelModeration = OpenChannelModerationViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelModerationViewModel::class.java]
    }

    ViewModelProviders.memberList = MemberListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, MemberListViewModel::class.java]
    }

    ViewModelProviders.bannedUserList = BannedUserListViewModelProvider { owner, channelUrl, channelType ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, channelType)
        )[channelUrl, BannedUserListViewModel::class.java]
    }

    ViewModelProviders.openChannelBannedUserList = OpenChannelBannedUserListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelBannedUserListViewModel::class.java]
    }

    ViewModelProviders.mutedMemberList = MutedMemberListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, MutedMemberListViewModel::class.java]
    }

    ViewModelProviders.openChannelMutedParticipantList =
        OpenChannelMutedParticipantListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[channelUrl, OpenChannelMutedParticipantListViewModel::class.java]
        }

    ViewModelProviders.operatorList = OperatorListViewModelProvider { owner, channelUrl, channelType, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, channelType, pagedQueryHandler)
        )[channelUrl, OperatorListViewModel::class.java]
    }

    ViewModelProviders.openChannelOperatorList = OpenChannelOperatorListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[channelUrl, OpenChannelOperatorListViewModel::class.java]
    }

    ViewModelProviders.messageSearch = MessageSearchViewModelProvider { owner, channelUrl, query ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, query)
        )[channelUrl, MessageSearchViewModel::class.java]
    }

    ViewModelProviders.messageThread = MessageThreadViewModelProvider { owner, channelUrl, parentMessage, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, parentMessage, params)
        )[channelUrl, MessageThreadViewModel::class.java]
    }

    ViewModelProviders.participantList = ParticipantViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[channelUrl, ParticipantViewModel::class.java]
    }

    ViewModelProviders.channelPushSetting = ChannelPushSettingViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ChannelPushSettingViewModel::class.java]
    }

    ViewModelProviders.openChannelList = OpenChannelListViewModelProvider { owner, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(params)
        )[OpenChannelListViewModel::class.java]
    }
    */
}
