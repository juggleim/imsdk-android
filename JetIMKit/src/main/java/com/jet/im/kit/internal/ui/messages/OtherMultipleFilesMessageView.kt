package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.message.SendingStatus
import com.jet.im.kit.R
import com.jet.im.kit.activities.PhotoViewActivity
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewOtherMultipleFilesMessageComponentBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.toContextThemeWrapper
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils

internal class OtherMultipleFilesMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_multiple_files_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherMultipleFilesMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_MultipleFiles, defStyle, 0)
        try {
            binding =
                SbViewOtherMultipleFilesMessageComponentBinding.inflate(
                    LayoutInflater.from(
                        context.toContextThemeWrapper(
                            defStyle
                        )
                    ),
                    this, true
                )
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_MultipleFiles_sb_message_other_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_MultipleFiles_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
            binding.rvMultipleFilesMessage.setOnLongClickListener { binding.contentPanel.performLongClick() }
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        if (message !is MultipleFilesMessage) return
        val messageGroupType = params.messageGroupType
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val enableReactions =
            message.reactions.isNotEmpty() && ChannelConfig.getEnableReactions(params.channelConfig, channel)
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                (!params.shouldUseQuotedView() || !MessageUtils.hasParentMessage(message))

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.emojiReactionListBackground.visibility =
            if (enableReactions) VISIBLE else GONE
        binding.rvEmojiReactionList.visibility = if (enableReactions) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else INVISIBLE
        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            val background = it.otherMessageBackground
            val reactionBackground = it.otherReactionListBackground
            if (background != null) binding.contentPanel.background = background
            if (reactionBackground != null) binding.emojiReactionListBackground.background = reactionBackground
        }
        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
        if (params.shouldUseQuotedView()) {
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
        binding.rvMultipleFilesMessage.bind(message)
        binding.rvMultipleFilesMessage.onItemClickListener = OnItemClickListener { _, index, _ ->
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                val intent =
                    PhotoViewActivity.newIntent(context, ChannelType.GROUP, message, index)
                context.startActivity(intent)
            } else {
                binding.rvMultipleFilesMessage.performClick()
            }
        }
    }
}
