package com.example.demo.customization.channel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sendbird.android.channel.MessageTypeFilter
import com.sendbird.android.params.MessageListParams
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.providers.ChannelViewModelProvider
import com.jet.im.kit.providers.ViewModelProviders
import com.example.demo.customization.GroupChannelRepository
import com.jet.im.kit.vm.ChannelViewModel
import com.jet.im.kit.vm.ViewModelFactory
import java.util.Objects

/**
 * In this sample, only text type messages are filtered and displayed in the message list.
 *
 * step 1. Create a [MessageFilteringSampleViewModel] to filter messages.
 * step 2. Create a [CustomViewModelFactory] to create a [MessageFilteringSampleViewModel].
 * step 3. Set custom [ChannelViewModelProvider] to [ViewModelProviders.channel].
 * step 4. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 *
 * To filter messages for each fragment, you can use [ChannelFragment.Builder.setMessageListParams].
 * If message list params is set from [ChannelFragment.Builder.setMessageListParams], [ChannelViewModel.createMessageListParams] is ignored.
 * '''
 * FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
 *         ChannelFragment.Builder(channelUrl).withArguments(args)
 *             .setUseHeader(true)
 *             .setMessageListParams(MessageListParams().apply {
 *                 val filter = MessagePayloadFilter().apply {
 *                     this.includeReactions = false
 *                     // Uncomment this to filter message list.
 *                     /*
 *                     this.includeMetaArray
 *                     this.includeParentMessageInfo
 *                     this.includeThreadInfo
 *                     */
 *                 }
 *                 messagePayloadFilter = filter
 *                 // Uncomment this to filter message list.
 *                 /*
 *                 messageTypeFilter =
 *                 customTypes =
 *                 */
 *             })
 *             .build()
 *     }
 * '''
 */
fun showMessageFilteringSample(activity: Activity) {
    ViewModelProviders.channel = ChannelViewModelProvider { owner, channelUrl, _, _ ->
        ViewModelProvider(
            owner,
            CustomViewModelFactory(channelUrl)
        )[channelUrl, MessageFilteringSampleViewModel::class.java]
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(
            ChannelActivity.newIntent(activity, channel.url)
        )
    }
}

/**
 * This class demonstrates how to filter messages.
 *
 * step 1. Create a custom ViewModel that inherits [ChannelViewModel].
 * step 2. Return a custom [MessageListParams] in [createMessageListParams] to filter messages.
 */
class MessageFilteringSampleViewModel(channelUrl: String) : ChannelViewModel(channelUrl, null) {
    override fun createMessageListParams(): MessageListParams {
        val params = super.createMessageListParams()
        params.messageTypeFilter = MessageTypeFilter.USER

        // Uncomment this to filter message list.
        /*
        params.messagePayloadFilter =
        params.customTypes =
        */
        return params
    }
}

/**
 * This class provides a custom ViewModel and default ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class CustomViewModelFactory(private vararg val params: Any?) : ViewModelFactory(*params) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == MessageFilteringSampleViewModel::class.java) {
            return MessageFilteringSampleViewModel(
                (Objects.requireNonNull(params)[0] as String)
            ) as T
        }
        return super.create(modelClass)
    }
}
