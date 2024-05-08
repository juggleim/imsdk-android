package com.example.demo.customization.channel

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.customization.GroupChannelRepository

/**
 * In this sample, a toast appears when a message item is clicked or long-clicked.
 * To customize the message click event, you can use the [ChannelFragment.Builder] or a custom fragment.
 *
 * step 1. Create [MessageClickSampleFragment] and set it to [ChannelFragment.Builder.setCustomFragment].
 * step 1-1. Create a custom listener and set it to [ChannelFragment.Builder.setOnMessageClickListener].
 * step 2. Set custom [ChannelFragmentProvider] to [FragmentProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showMessageClickSample(activity: Activity) {
    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setCustomFragment(MessageClickSampleFragment())
            .setOnMessageLongClickListener { view, _, _ ->
                Toast.makeText(view.context, "Message Long Click Sample", Toast.LENGTH_SHORT).show()
            }
            /*
            .setOnMessageClickListener()
            .setOnMessageMentionClickListener()
            .setOnMessageProfileClickListener()
            .setOnMessageProfileLongClickListener()
            .setOnQuoteReplyMessageClickListener()
            .setOnQuoteReplyMessageLongClickListener()
            .setOnThreadInfoClickListener()
             */
            .build()
    }
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * This class is used to customize the message click event.
 *
 * step 1. Inherit [ChannelFragment] and override the method you want to customize.
 * step 2. Implement what you want to do in the overridden method.
 */
class MessageClickSampleFragment : ChannelFragment() {
    override fun onMessageClicked(view: View, position: Int, message: BaseMessage) {
        Toast.makeText(requireContext(), "Message Click Sample", Toast.LENGTH_SHORT).show()
        super.onMessageClicked(view, position, message)
    }

    /*
    override fun onMessageLongClicked(view: View, position: Int, message: BaseMessage) {
        super.onMessageLongClicked(view, position, message)
    }

    override fun onMessageProfileClicked(view: View, position: Int, message: BaseMessage) {
        super.onMessageProfileClicked(view, position, message)
    }

    override fun onMessageProfileLongClicked(view: View, position: Int, message: BaseMessage) {
        super.onMessageProfileLongClicked(view, position, message)
    }

    override fun onMessageMentionClicked(view: View, position: Int, user: User) {
        super.onMessageMentionClicked(view, position, user)
    }

    override fun onQuoteReplyMessageClicked(view: View, position: Int, message: BaseMessage) {
        super.onQuoteReplyMessageClicked(view, position, message)
    }

    override fun onQuoteReplyMessageLongClicked(view: View, position: Int, message: BaseMessage) {
        super.onQuoteReplyMessageLongClicked(view, position, message)
    }

    override fun onThreadInfoClicked(view: View, position: Int, message: BaseMessage) {
        super.onThreadInfoClicked(view, position, message)
    }
    */
}
