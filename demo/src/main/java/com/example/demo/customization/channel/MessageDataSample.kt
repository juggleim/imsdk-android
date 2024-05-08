package com.example.demo.customization.channel

import android.app.Activity
import com.sendbird.android.params.FileMessageCreateParams
import com.sendbird.android.params.UserMessageCreateParams
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.CustomParamsHandler
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.customization.GroupChannelRepository

/**
 * In this sample, a message is sent with the value “Custom data” in the data field of the message.
 *
 * You can customize the message data by using [CustomParamsHandler].
 * [SendbirdUIKit.setCustomParamsHandler] apply the custom params handler globally.
 * The 'onBeforeSendXXXMessage' method in [ChannelFragment] can be applied for each fragment.
 * The 'onBeforeSendXXXMessage' method in [ChannelFragment] is prioritized over [SendbirdUIKit.setCustomParamsHandler].
 *
 * step 1. Set custom [CustomParamsHandler] to [SendbirdUIKit.setCustomParamsHandler].
 * step 1-1. Create a [MessageDataSampleFragment] and set custom [CustomParamsHandler] to [ChannelFragment].
 * step 2. Set custom [ChannelFragmentProvider] to [FragmentProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showMessageDataSample(activity: Activity) {
    SendbirdUIKit.setCustomParamsHandler(object : CustomParamsHandler {
        override fun onBeforeSendFileMessage(params: FileMessageCreateParams) {
            params.data = "Custom data"
            // Uncomment this to customize message params.
            /*
            params.file
            params.fileName
            params.fileSize
            params.fileUrl
            params.mimeType
            params.thumbnailSizes
            params.metaArrays
            params.customType
            params.isPinnedMessage
            params.pushNotificationDeliveryOption
            */
        }

        // Uncomment this to customize message params.
        /*
        override fun onBeforeSendUserMessage(params: UserMessageCreateParams) {
            super.onBeforeSendUserMessage(params)
        }

        override fun onBeforeSendMultipleFilesMessage(params: MultipleFilesMessageCreateParams) {
            super.onBeforeSendMultipleFilesMessage(params)
        }

        override fun onBeforeUpdateUserMessage(params: UserMessageUpdateParams) {
            super.onBeforeUpdateUserMessage(params)
        }
        */
    })

    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl).withArguments(args)
            .setCustomFragment(MessageDataSampleFragment())
            .setUseHeader(true)
            .build()
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(
            ChannelActivity.newIntent(activity, channel.url)
        )
    }
}

/**
 * This class demonstrates how to customize the message data.
 *
 * step 1. Inherit [ChannelFragment] and override the 'onBeforeSendXXXMessage' method.
 * step 2. Return the custom params in the 'onBeforeSendXXXMessage' method.
 */
class MessageDataSampleFragment : ChannelFragment() {
    override fun onBeforeSendUserMessage(params: UserMessageCreateParams) {
        params.data = "Custom data"
        // Uncomment this to customize message params.
        /*
        params.message
        params.mentionedMessageTemplate
        params.pollId
        params.metaArrays
        params.customType
        params.translationTargetLanguages
        params.isPinnedMessage
        params.pushNotificationDeliveryOption
        */
    }

    // Uncomment this to customize message params.
    /*
    override fun onBeforeSendFileMessage(params: FileMessageCreateParams) {
        super.onBeforeSendFileMessage(params)
    }

    override fun onBeforeSendMultipleFilesMessage(params: MultipleFilesMessageCreateParams) {
        super.onBeforeSendMultipleFilesMessage(params)
    }

    override fun onBeforeUpdateUserMessage(params: UserMessageUpdateParams) {
        super.onBeforeUpdateUserMessage(params)
    }
    */
}
