package com.example.demo.customization.channel

import android.app.Activity
import android.view.View
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.message.UserMessage
import com.sendbird.android.params.UserMessageCreateParams
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.model.DialogListItem
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.R
import com.example.demo.customization.GroupChannelRepository
import com.jet.im.kit.utils.MessageUtils

/**
 * In this sample, the edit menu is disabled, and a resend menu is added to UserMessage
 * in the menu that appears when you long-click a message.
 *
 * step 1. Create a [MessageMenuSampleFragment].
 * step 2. Set custom [ChannelFragmentProvider] to [FragmentProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showMessageMenuSample(activity: Activity) {
    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl).withArguments(args)
            .setCustomFragment(MessageMenuSampleFragment())
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
 * This class is used to customize the message menu.
 *
 * step 1. Override [ChannelFragment.makeMessageContextMenu] to add or remove menu items.
 * step 2. Override [ChannelFragment.onMessageContextMenuItemClicked] to handle the menu item click event.
 */
class MessageMenuSampleFragment : ChannelFragment() {
    override fun makeMessageContextMenu(message: BaseMessage): MutableList<DialogListItem> {
        val menuList = super.makeMessageContextMenu(message)
        if (message.sendingStatus == SendingStatus.SUCCEEDED) {
            if (MessageUtils.isMine(message)) {
                menuList.find { it.key == com.jet.im.kit.R.string.sb_text_channel_anchor_edit }?.let {
                    menuList.remove(it)
                    menuList.add(DialogListItem(com.jet.im.kit.R.string.sb_text_channel_anchor_edit, com.jet.im.kit.R.drawable.icon_edit, isAlert = false, isDisabled = true))
                }
            }
            if (message is UserMessage) {
                menuList.add(DialogListItem(R.string.text_resend, com.jet.im.kit.R.drawable.icon_reply))
            }
        }
        return menuList
    }

    override fun onMessageContextMenuItemClicked(message: BaseMessage, view: View, position: Int, item: DialogListItem): Boolean {
        super.onMessageContextMenuItemClicked(message, view, position, item)
        when (item.key) {
            R.string.text_resend -> {
                sendUserMessage(UserMessageCreateParams(message.message))
                return true
            }
        }
        return false
    }
}
