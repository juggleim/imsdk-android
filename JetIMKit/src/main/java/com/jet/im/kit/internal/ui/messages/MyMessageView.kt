package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.jet.im.kit.R
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewMyMessageComponentBinding
import com.jet.im.kit.internal.extensions.toContextThemeWrapper
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ViewUtils

internal class MyMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int

    init {
        binding = SbViewMyMessageComponentBinding.inflate(
            LayoutInflater.from(context.toContextThemeWrapper(defStyle)), this, true
        )
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val messageBackground =
                a.getResourceId(
                    R.styleable.MessageView_sb_message_me_background,
                    R.drawable.sb_shape_chat_bubble
                )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_sb_message_me_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val enableReactions = message.reactions.isNotEmpty() &&
            ChannelConfig.getEnableReactions(params.channelConfig, channel)
        val messageGroupType = params.messageGroupType
        binding.emojiReactionListBackground.visibility = if (enableReactions) VISIBLE else GONE
        binding.rvEmojiReactionList.visibility = if (enableReactions) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        messageUIConfig?.let {
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            val background = it.myMessageBackground
            val reactionBackground = it.myReactionListBackground
            background?.let { binding.contentPanel.background = background }
            reactionBackground?.let { binding.emojiReactionListBackground.background = reactionBackground }
        }

        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)

        if (params.shouldUseQuotedView() && channel is GroupChannel) {
            ViewUtils.drawQuotedMessage(
                binding.quoteReplyPanel,
                channel,
                message,
                messageUIConfig?.repliedMessageTextUIConfig,
                params
            )
        } else {
            binding.quoteReplyPanel.visibility = GONE
        }
        ViewUtils.drawThreadInfo(binding.threadInfo, message, params)
    }

    fun attachContentView(view: View) {
        binding.customContentPanel.addView(view)
    }
}
