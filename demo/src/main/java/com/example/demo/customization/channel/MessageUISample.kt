package com.example.demo.customization.channel

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.message.SendingStatus
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.activities.adapter.MessageListAdapter
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.activities.viewholder.MessageType
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.MessageUIConfig
import com.jet.im.kit.model.TextUIConfig
import com.jet.im.kit.modules.ChannelModule
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.R
import com.example.demo.customization.GroupChannelRepository
import com.example.demo.databinding.ViewCustomMessageMeBinding

/**
 * In this sample, text-type messages sent by are applied for a full custom view with an orange background,
 * text-type messages sent by others are applied for the font size and color change, and displayed the nickname text color in red.
 * Additionally, consecutive messages sent by the same user are not grouped.
 *
 * The range of customization varies depending on below ways.
 *
 * 1. CustomMessageListAdapter : The entire message view is replaced according to the message type.
 * 2. MessageListUIParams : Customize UI settings that apply to the entire message list.
 * 3. MessageUIConfig : Customize the text properties or background of the message item.
 * 4. Resources : Customize through theme resource.
 *
 * step 1. Create a [MessageUISampleAdapter].
 * step 2. Create a [MessageListUIParams] and apply it to the [MessageUISampleAdapter].
 * step 3. Create a [MessageUIConfig] and apply it to the [MessageUISampleAdapter].
 * step 4. Create a [MessageUISampleFragment] and set [MessageUISampleAdapter] to [ChannelFragment].
 * step 5. Set custom [ChannelFragmentProvider] to [FragmentProviders.channel].
 * step 6. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showMessageUISample(activity: Activity) {
    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, _ ->
        val adapter = MessageUISampleAdapter(
            // apply the custom messageListUIParams
            null, customMessageListUIParams
        ).apply {
            // apply the custom messageUIConfig
            this.messageUIConfig = customMessageUIConfig
        }

        ChannelFragment.Builder(channelUrl)
            // apply the fragment with custom theme
            .setCustomFragment(MessageUISampleFragment())
            // apply the custom adapter
            .setMessageListAdapter(adapter)
            .build()
    }
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * This class demonstrates how to change the entire item view for the message type provided by UIKit.
 *
 * step 1. Create a custom view holder that inherits [GroupChannelMessageViewHolder]
 * step 2. Create a custom adapter that inherits [MessageListAdapter]
 * step 3. Return the custom view holder in [onCreateViewHolder] depending on the viewType provided by UIKit
 *
 * @see [MessageType]
 */
class MessageUISampleAdapter(
    channel: GroupChannel?,
    uiParams: MessageListUIParams
) : MessageListAdapter(channel, uiParams) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (MessageType.from(viewType)) {
            MessageType.VIEW_TYPE_USER_MESSAGE_ME -> CustomMessageMeViewHolder(
                ViewCustomMessageMeBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            // You can also apply the configuration to all items.
            /*
            MessageType.VIEW_TYPE_USER_MESSAGE_OTHER ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_ME ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_ME ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_ME ->
            MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER ->
            MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME ->
            MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER ->
            MessageType.VIEW_TYPE_VOICE_MESSAGE_ME ->
            MessageType.VIEW_TYPE_VOICE_MESSAGE_OTHER ->
            MessageType.VIEW_TYPE_ADMIN_MESSAGE ->
            MessageType.VIEW_TYPE_TIME_LINE ->
            MessageType.VIEW_TYPE_PARENT_MESSAGE_INFO ->
            MessageType.VIEW_TYPE_CHAT_NOTIFICATION ->
            MessageType.VIEW_TYPE_PARENT_MESSAGE_INFO ->
            MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME ->
            MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER ->
            */
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    class CustomMessageMeViewHolder(
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

/**
 * You can customize a message list using the [MessageListUIParams.Builder].
 * [MessageListUIParams] can be applied as an argument to the constructor of [MessageListAdapter].
 */
val customMessageListUIParams = MessageListUIParams.Builder()
    .setUseMessageGroupUI(false)
    // You can also apply the configuration to all items.
    /*
    .setMessageGroupType()
    .setChannelConfig()
    .setUseMessageReceipt()
    .setUseQuotedView()
    .setUseReverseLayout()
     */
    .build()

/**
 * Through [MessageUIConfig], you can customize text properties
 * such as text color, font, and style and the background used in UIKit messages.
 */
val customMessageUIConfig = MessageUIConfig().apply {
    val textUIConfig = TextUIConfig.Builder()
        .setTextColor(Color.RED)
        // You can also apply the configuration to all items.
        /*
        .setTextBackgroundColor()
        .setTextSize()
        .setTextStyle()
        .setFamilyName()
        .setCustomFontRes()
         */
        .build()
    this.otherNicknameTextUIConfig.apply(textUIConfig)

    // You can also apply the configuration to each item.
    /*
    this.myMessageTextUIConfig.apply()
    this.otherMessageTextUIConfig.apply()
    this.myMessageBackground =
    this.myEditedTextMarkUIConfig.apply()
    this.myMentionUIConfig.apply()
    this.myNicknameTextUIConfig.apply()
    this.linkedTextColor =
    this.myOgtagBackground =
    this.myReactionListBackground =
    this.mySentAtTextUIConfig.apply()
    this.operatorNicknameTextUIConfig.apply()
    this.otherEditedTextMarkUIConfig.apply()
    this.otherMentionUIConfig.apply()
    this.otherMessageBackground =
    this.otherOgtagBackground =
    this.otherReactionListBackground =
    this.otherSentAtTextUIConfig.apply()
    this.repliedMessageTextUIConfig.apply()
    */
}

/**
 * UiKit provides various resource attributes for each modules, components, and views.
 * You can customize the message item by overwriting or inheriting attributes of styles.
 *
 * step 1. Create a custom theme that inherits or overwrites [R.style.AppTheme_Sendbird]
 * step 2. Create a channel module with params that apply the custom theme
 * step 3. Return the channel module in [onCreateModule]
 *
 * You shouldn't use [ChannelFragment.Builder.withArguments] to apply the custom theme.
 */
class MessageUISampleFragment : ChannelFragment() {
    override fun onCreateModule(args: Bundle): ChannelModule {
        val params = ChannelModule.Params(requireContext(), R.style.AppTheme_Sendbird_Custom)
        return ChannelModule(requireContext(), params)
    }
}
