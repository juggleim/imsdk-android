package com.example.demo.customization.channel

import android.app.Activity
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.params.UserMessageCreateParams
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.activities.adapter.MessageListAdapter
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.interfaces.CustomParamsHandler
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.interfaces.providers.MessageListAdapterProvider
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.providers.AdapterProviders
import com.example.demo.customization.GroupChannelRepository
import com.example.demo.databinding.ViewCustomMessageMeBinding

// 0~1000 are reserved for the UIKit's message types.
const val NEW_MESSAGE_TYPE = 1001

/**
 * In this sample, a new type is applied to newly sent text messages, showing a new message view with an orange background.
 *
 * step 1. Create a [CustomParamsHandler] to set [UserMessageCreateParams.customType] when sending a new message.
 * step 2. Create a [NewMessageTypeSampleAdapter].
 * step 3. Create a [MessageListAdapterProvider] to set [NewMessageTypeSampleAdapter] for the new message type.
 * step 4. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 * @see [NewMessageTypeSampleAdapter]
 */
fun showNewMessageTypeSample(activity: Activity) {
    SendbirdUIKit.setCustomParamsHandler(object : CustomParamsHandler {
        override fun onBeforeSendUserMessage(params: UserMessageCreateParams) {
            params.customType = NEW_MESSAGE_TYPE.toString()
        }
    })

    AdapterProviders.messageList = MessageListAdapterProvider { channel, uiParams ->
        NewMessageTypeSampleAdapter(channel, uiParams)
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * This class is an example of how to create a new type of message.
 *
 * step 1. Define new message type in [getItemViewType]
 * step 2. Implement the ViewHolder for new message type
 * step 2. Return the ViewHolder for the new message type in [onCreateViewHolder]
 */
class NewMessageTypeSampleAdapter(
    channel: GroupChannel?,
    uiParams: MessageListUIParams
) : MessageListAdapter(channel, uiParams) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (NEW_MESSAGE_TYPE == viewType) {
            return NewMessageTypeSampleViewHolder(
                ViewCustomMessageMeBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        if (message.customType == NEW_MESSAGE_TYPE.toString()) {
            return NEW_MESSAGE_TYPE
        }
        return super.getItemViewType(position)
    }

    class NewMessageTypeSampleViewHolder(
        val binding: ViewCustomMessageMeBinding
    ) : GroupChannelMessageViewHolder(binding.root) {
        override fun setEmojiReaction(
            reactionList: List<Reaction>,
            emojiReactionClickListener: OnItemClickListener<String>?,
            emojiReactionLongClickListener: OnItemLongClickListener<String>?,
            moreButtonClickListener: View.OnClickListener?
        ) {
        }

        override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
            val context = binding.getRoot().context
            val sendingState = message.sendingStatus == SendingStatus.SUCCEEDED

            binding.tvSentAt.visibility = if (sendingState) View.VISIBLE else View.GONE
            val sentAt = DateUtils.formatDateTime(context, message.createdAt, DateUtils.FORMAT_SHOW_TIME)
            binding.tvSentAt.text = sentAt
            binding.tvMessage.text = message.message
        }

        override fun getClickableViewMap(): Map<String, View> =
            mapOf(ClickableViewIdentifier.Chat.name to binding.tvMessage)
    }
}
