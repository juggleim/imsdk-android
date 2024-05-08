package com.example.demo.customization.global

import android.app.Activity
import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.activities.adapter.MessageListAdapter
import com.jet.im.kit.interfaces.providers.MessageListAdapterProvider
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.providers.AdapterProviders
import com.example.demo.customization.GroupChannelRepository

/**
 * In this sample, the UI that groups messages is disabled when the same user sends consecutive messages.
 *
 * @see [setCustomAdapters]
 */
fun showAdapterProvidersSample(activity: Activity) {
    setCustomAdapters()
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * Customized Adapters can be applied to UIKit globally via the [AdapterProviders],
 * so it is recommended that you use them in your Application's onCreate.
 *
 * If you don't want to apply the adapter globally, you can set the adapter through the fragment's builder.
 * ```
 * val fragment = ChannelFragment.Builder(channelUrl)
 *    .setMessageListAdapter(CustomMessageListAdapter(channel))
 *    .build()
 * ```
 * Refer to the documentation below to see the different methods provided by UIKit.
 * **See Also:** [API reference](https://sendbird.com/docs/chat/uikit/v3/android/ref/-sendbird%20-u-i-kit/com.jet.im.kit.providers/-adapter-providers/index.html)
 */
fun setCustomAdapters() {
    class CustomMessageListAdapter(
        channel: GroupChannel
    ) : MessageListAdapter(
        channel,
        MessageListUIParams.Builder()
            .setUseMessageGroupUI(false)
            .build()
    )

    AdapterProviders.messageList = MessageListAdapterProvider { channel, _ ->
        CustomMessageListAdapter(channel)
    }

    // Below is a list of adapters provided by uikit. Customize the list you want to change.
    /*
    AdapterProviders.bannedUserList = BannedUserListAdapterProvider { BannedUserListAdapter() }

    AdapterProviders.channelList = ChannelListAdapterProvider { uiParams ->
        ChannelListAdapter(null, uiParams)
    }

    AdapterProviders.createChannelUserList = CreateChannelUserListAdapterProvider { CreateChannelUserListAdapter() }

    AdapterProviders.inviteUserList = InviteUserListAdapterProvider { InviteUserListAdapter() }

    AdapterProviders.memberList = MemberListAdapterProvider { MemberListAdapter() }

    AdapterProviders.messageSearch = MessageSearchAdapterProvider { MessageSearchAdapter() }

    AdapterProviders.mutedMemberList = MutedMemberListAdapterProvider { MutedMemberListAdapter() }

    AdapterProviders.openChannelBannedUserList = OpenChannelBannedUserListAdapterProvider {
        OpenChannelBannedUserListAdapter()
    }

    AdapterProviders.openChannelList = OpenChannelListAdapterProvider { OpenChannelListAdapter() }

    AdapterProviders.openChannelMessageList = OpenChannelMessageListAdapterProvider { messageListUIParams ->
        OpenChannelMessageListAdapter(null, messageListUIParams)
    }

    AdapterProviders.openChannelMutedParticipantList = OpenChannelMutedParticipantListAdapterProvider { OpenChannelMutedParticipantListAdapter() }

    AdapterProviders.openChannelRegisterOperatorList = OpenChannelRegisterOperatorListAdapterProvider { channel ->
        OpenChannelRegisterOperatorListAdapter(channel)
    }

    AdapterProviders.operatorList = OperatorListAdapterProvider { OperatorListAdapter() }

    AdapterProviders.participantList = ParticipantListAdapterProvider { ParticipantListAdapter() }

    AdapterProviders.registerOperatorList = RegisterOperatorListAdapterProvider { RegisterOperatorListAdapter() }

    AdapterProviders.threadList = ThreadListAdapterProvider { messageListUIParams ->
        ThreadListAdapter(null, messageListUIParams)
    }
    */
}
