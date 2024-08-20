package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewOtherVoiceMessageComponentBinding
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.messages.VoiceMessage

internal class OtherVoiceMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_voice_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherVoiceMessageComponentBinding
    override val layout: OtherVoiceMessageView
        get() = this
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        val messageGroupType = params.messageGroupType
        val fileMessage = message as FileMessage
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                (!MessageUtils.hasParentMessage(message))

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else INVISIBLE
        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            val background = it.otherMessageBackground
            if (background != null) binding.contentPanel.background = background
        }
        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
        ViewUtils.drawVoiceMessage(binding.voiceMessage, fileMessage)
    }

    override fun drawMessage(
        channel: ConversationInfo,
        message: Message,
        params: MessageListUIParams
    ) {
        val messageGroupType = params.messageGroupType
        val fileMessage = message.content as VoiceMessage
        val isSent = message.state == Message.MessageState.SENT
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                    (!MessageUtils.hasParentMessage(message))

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else INVISIBLE
        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            val background = it.otherMessageBackground
            if (background != null) binding.contentPanel.background = background
        }
        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
        ViewUtils.drawVoiceMessage(binding.voiceMessage, fileMessage,message)
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewOtherVoiceMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_File_sb_message_other_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            val progressColor =
                a.getResourceId(R.styleable.MessageView_File_sb_voice_message_progress_color, R.color.ondark_03)
            val progressTrackColor =
                a.getResourceId(R.styleable.MessageView_File_sb_voice_message_progress_track_color, R.color.background_100)
            val timelineTextAppearance =
                a.getResourceId(
                    R.styleable.MessageView_File_sb_voice_message_timeline_text_appearance,
                    R.style.SendbirdBody3OnLight01
                )

            binding.contentPanelWithReactions.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.voiceMessage.setProgressCornerRadius(context.resources.getDimension(R.dimen.sb_size_16))
            binding.voiceMessage.setProgressTrackColor(
                AppCompatResources.getColorStateList(
                    context,
                    progressTrackColor
                )
            )
            binding.voiceMessage.setProgressProgressColor(AppCompatResources.getColorStateList(context, progressColor))
            binding.voiceMessage.setTimelineTextAppearance(timelineTextAppearance)
            val buttonBackgroundTint = if (SendbirdUIKit.isDarkMode()) R.color.background_600 else R.color.background_50
            val buttonTint = if (SendbirdUIKit.isDarkMode()) R.color.primary_200 else R.color.primary_300
            val inset = context.resources.getDimension(R.dimen.sb_size_12).toInt()
            val playIcon =
                DrawableUtils.createOvalIcon(
                    context,
                    buttonBackgroundTint,
                    224,
                    R.drawable.icon_play,
                    buttonTint,
                    inset
                )
            binding.voiceMessage.setPlayButtonImageDrawable(playIcon)
            val pauseIcon =
                DrawableUtils.createOvalIcon(
                    context,
                    buttonBackgroundTint,
                    224,
                    R.drawable.icon_pause,
                    buttonTint,
                    inset
                )
            binding.voiceMessage.setPauseButtonImageDrawable(pauseIcon)
        } finally {
            a.recycle()
        }
    }
}
