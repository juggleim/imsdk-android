package com.example.demo.customization.global

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.interfaces.providers.ChannelModuleProvider
import com.jet.im.kit.modules.ChannelModule
import com.jet.im.kit.modules.components.ChannelHeaderComponent
import com.jet.im.kit.providers.ModuleProviders
import com.example.demo.customization.GroupChannelRepository
import com.example.demo.databinding.ViewCustomHeaderBinding

/**
 * In this sample, the UI of the channel header is changed.
 *
 * @see [setCustomModuleProviders]
 */
fun showModuleProvidersSample(activity: Activity) {
    setCustomModuleProviders()
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * Customized Modules and Components can be applied to UIKit globally via the [ModuleProviders],
 * so it is recommended that you use them in your Application's onCreate.
 *
 * If you don't want to apply the adapter globally, you can set them through the fragment's inheritance.
 * ```
 * class CustomChannelFragment : ChannelFragment() {
 *     override fun onCreateModule(args: Bundle): ChannelModule {
 *         // You can apply the customized module.
 *         val module = CustomChannelModule(requireContext())
 *         // You can apply the customized component.
 *         module.setHeaderComponent(CustomHeaderComponent())
 *         return module
 *     }
 * }
 * ```
 * Refer to the documentation below to see the different methods provided by UIKit.
 * **See Also:** [API reference](https://sendbird.com/docs/chat/uikit/v3/android/ref/-sendbird%20-u-i-kit/com.jet.im.kit.providers/-module-providers/index.html)
 */
fun setCustomModuleProviders() {
    class CustomChannelModule(context: Context) : ChannelModule(context)
    class CustomHeaderComponent : ChannelHeaderComponent() {
        override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
            return ViewCustomHeaderBinding.inflate(inflater, parent, false).root
        }
    }

    ModuleProviders.channel = ChannelModuleProvider { context, _ ->
        // You can apply the customized module.
        val module = CustomChannelModule(context)
        // You can apply the customized component.
        module.setHeaderComponent(CustomHeaderComponent())
        module
    }

    // Below is a list of modules provided by uikit. Customize the screens you want to change.
    /*
    ModuleProviders.channelList = ChannelListModuleProvider { context, _ -> ChannelListModule(context) }

    ModuleProviders.openChannel = OpenChannelModuleProvider { context, _ -> OpenChannelModule(context) }

    ModuleProviders.createChannel = CreateChannelModuleProvider { context, _ -> CreateChannelModule(context) }

    ModuleProviders.createOpenChannel = CreateOpenChannelModuleProvider { context, _ -> CreateOpenChannelModule(context) }

    ModuleProviders.channelSettings = ChannelSettingsModuleProvider { context, _ -> ChannelSettingsModule(context) }

    ModuleProviders.openChannelSettings = OpenChannelSettingsModuleProvider { context, _ -> OpenChannelSettingsModule(context) }

    ModuleProviders.inviteUser = InviteUserModuleProvider { context, _ -> InviteUserModule(context) }

    ModuleProviders.registerOperator = RegisterOperatorModuleProvider { context, _ -> RegisterOperatorModule(context) }

    ModuleProviders.openChannelRegisterOperator =
        OpenChannelRegisterOperatorModuleProvider { context, _ -> OpenChannelRegisterOperatorModule(context) }

    ModuleProviders.moderation = ModerationModuleProvider { context, _ -> ModerationModule(context) }

    ModuleProviders.openChannelModeration =
        OpenChannelModerationModuleProvider { context, _ -> OpenChannelModerationModule(context) }

    ModuleProviders.memberList = MemberListModuleProvider { context, _ -> MemberListModule(context) }

    ModuleProviders.bannedUserList = BannedUserListModuleProvider { context, _ -> BannedUserListModule(context) }

    ModuleProviders.openChannelBannedUserList =
        OpenChannelBannedUserListModuleProvider { context, _ -> OpenChannelBannedUserListModule(context) }

    ModuleProviders.mutedMemberList = MutedMemberListModuleProvider { context, _ -> MutedMemberListModule(context) }

    ModuleProviders.openChannelMutedParticipantList =
        OpenChannelMutedParticipantListModuleProvider { context, _ -> OpenChannelMutedParticipantListModule(context) }

    ModuleProviders.operatorList = OperatorListModuleProvider { context, _ -> OperatorListModule(context) }

    ModuleProviders.openChannelOperatorList =
        OpenChannelOperatorListModuleProvider { context, _ -> OpenChannelOperatorListModule(context) }

    ModuleProviders.messageSearch = MessageSearchModuleProvider { context, _ -> MessageSearchModule(context) }

    ModuleProviders.messageThread = MessageThreadModuleProvider { context, _, message -> MessageThreadModule(context, message) }

    ModuleProviders.participantList = ParticipantListModuleProvider { context, _ -> ParticipantListModule(context) }

    ModuleProviders.channelPushSetting = ChannelPushSettingModuleProvider { context, _ -> ChannelPushSettingModule(context) }

    ModuleProviders.openChannelList = OpenChannelListModuleProvider { context, _ -> OpenChannelListModule(context) }
    */
}
