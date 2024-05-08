package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.message.UserMessage
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.PhotoViewActivity
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewParentMessageInfoBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.toContextThemeWrapper
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.MessageUIConfig
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils
import java.util.Locale

internal class ParentMessageInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_parent_message_info
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewParentMessageInfoBinding
    override val layout: View
        get() = binding.root
    private val parentMessageInfoUIConfig: MessageUIConfig = MessageUIConfig()
    var mentionClickListener: OnItemClickListener<User>? = null

    override fun drawMessage(channel: GroupChannel, message: BaseMessage, params: MessageListUIParams) {
        // sender
        ViewUtils.drawNickname(binding.tvNickname, message, null, false)
        ViewUtils.drawProfile(binding.ivProfile, message)
        ViewUtils.drawParentMessageSentAt(binding.tvSentAt, message, null)

        // reaction
        binding.rvEmojiReactionList.visibility =
            if (ChannelConfig.getEnableReactions(params.channelConfig, channel)) VISIBLE else INVISIBLE
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList, channel, params.channelConfig)

        // thread info
        val hasThreadInfo = message.threadInfo.replyCount > 0
        binding.threadInfoGroup.visibility = if (hasThreadInfo) VISIBLE else GONE
        if (hasThreadInfo) {
            binding.tvReplyCount.text = String.format(
                if (message.threadInfo.replyCount == 1)
                    context.getString(R.string.sb_text_number_of_reply)
                else
                    context.getString(R.string.sb_text_number_of_replies),
                message.threadInfo.replyCount
            )
        }

        // message content
        when (message) {
            is UserMessage -> drawUserMessage(message, params)

            is FileMessage -> {
                val mimeType: String = message.type.lowercase(Locale.getDefault())
                if (MessageUtils.isVoiceMessage(message)) {
                    drawVoiceMessage(channel, message)
                } else if (mimeType.startsWith(StringSet.image)) {
                    if (mimeType.contains(StringSet.svg)) {
                        drawFileMessage(message)
                    } else {
                        drawImageMessage(message)
                    }
                } else if (mimeType.startsWith(StringSet.video)) {
                    drawImageMessage(message)
                } else {
                    drawFileMessage(message)
                }
            }

            is MultipleFilesMessage -> drawMultipleFilesMessage(message)

            else -> drawUnknownMessage()
        }
    }

    fun setOnMoreButtonClickListener(onMoreButtonClickListener: OnClickListener) {
        binding.ivMoreIcon.setOnClickListener(onMoreButtonClickListener)
    }

    private fun drawUnknownMessage() {
        binding.tvTextMessage.visibility = VISIBLE
        binding.fileGroup.visibility = GONE
        binding.imageGroup.visibility = GONE
        binding.voiceMessage.visibility = GONE
        binding.multipleFilesMessage.visibility = GONE
        ViewUtils.drawUnknownMessage(binding.tvTextMessage, false)
    }

    private fun drawUserMessage(message: UserMessage, messageListUIConfig: MessageListUIParams) {
        val enableMention = messageListUIConfig.channelConfig.enableMention
        binding.tvTextMessage.visibility = VISIBLE
        binding.fileGroup.visibility = GONE
        binding.imageGroup.visibility = GONE
        binding.voiceMessage.visibility = GONE
        binding.multipleFilesMessage.visibility = GONE
        ViewUtils.drawTextMessage(
            binding.tvTextMessage, message, parentMessageInfoUIConfig, enableMention, null
        ) { view, position, data -> mentionClickListener?.onItemClick(view, position, data) }
    }

    private fun drawVoiceMessage(channel: GroupChannel, message: FileMessage) {
        binding.tvTextMessage.visibility = GONE
        binding.fileGroup.visibility = GONE
        binding.imageGroup.visibility = GONE
        binding.voiceMessage.visibility = VISIBLE
        binding.multipleFilesMessage.visibility = GONE
        binding.voiceMessage.setOnClickListener {
            binding.voiceMessage.callOnPlayerButtonClick()
        }
        ViewUtils.drawVoiceMessage(binding.voiceMessage, message)
    }

    private fun drawFileMessage(message: FileMessage) {
        binding.tvTextMessage.visibility = GONE
        binding.fileGroup.visibility = VISIBLE
        binding.imageGroup.visibility = GONE
        binding.voiceMessage.visibility = GONE
        binding.multipleFilesMessage.visibility = GONE
        ViewUtils.drawFilename(binding.tvFileName, message, parentMessageInfoUIConfig)
        ViewUtils.drawFileIcon(binding.ivFileIcon, message)
    }

    private fun drawImageMessage(message: FileMessage) {
        binding.tvTextMessage.visibility = GONE
        binding.fileGroup.visibility = GONE
        binding.imageGroup.visibility = VISIBLE
        binding.voiceMessage.visibility = GONE
        binding.multipleFilesMessage.visibility = GONE
        ViewUtils.drawThumbnail(binding.ivThumbnail, message)
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, message)
    }

    private fun drawMultipleFilesMessage(message: MultipleFilesMessage) {
        binding.tvTextMessage.visibility = GONE
        binding.fileGroup.visibility = GONE
        binding.imageGroup.visibility = GONE
        binding.voiceMessage.visibility = GONE
        binding.multipleFilesMessage.visibility = VISIBLE
        binding.multipleFilesMessage.bind(message)
        binding.multipleFilesMessage.onItemClickListener = OnItemClickListener { _, index, _ ->
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                val intent =
                    PhotoViewActivity.newIntent(context, ChannelType.GROUP, message, index)
                context.startActivity(intent)
            } else {
                binding.multipleFilesMessage.performClick()
            }
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ParentMessageInfoView, defStyle, 0)
        try {
            binding = SbViewParentMessageInfoBinding.inflate(
                LayoutInflater.from(context.toContextThemeWrapper(defStyle)),
                this,
                true
            )
            val nicknameAppearance = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_nickname_appearance,
                R.style.SendbirdH2OnLight01
            )
            val sentAtAppearance = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_sent_at_appearance,
                R.style.SendbirdCaption2OnLight03
            )
            val textMessageAppearance = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_text_message_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val moreIcon = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_more_icon,
                R.drawable.icon_more
            )
            val moreIconTint = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_more_icon_tint,
                R.color.onlight_02
            )
            val fileMessageBackground = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_file_message_background,
                R.drawable.sb_shape_chat_bubble
            )
            val fileMessageBackgroundTint = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_file_message_background_tint,
                R.color.background_100
            )
            val fileMessageAppearance = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_file_name_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val replyCountAppearance = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_reply_count_appearance,
                R.style.SendbirdBody3OnLight03
            )
            val dividerColor = a.getResourceId(
                R.styleable.ParentMessageInfoView_sb_parent_message_info_divider_line_color,
                R.color.onlight_04
            )
            val progressColor =
                a.getResourceId(
                    R.styleable.ParentMessageInfoView_sb_parent_message_info_voice_message_progress_color,
                    R.color.ondark_03
                )
            val progressTrackColor =
                a.getResourceId(
                    R.styleable.ParentMessageInfoView_sb_parent_message_info_voice_message_progress_track_color,
                    R.color.background_100
                )
            val timelineTextAppearance =
                a.getResourceId(
                    R.styleable.ParentMessageInfoView_sb_parent_message_info_voice_message_timeline_text_appearance,
                    R.style.SendbirdBody3OnLight01
                )

            binding.tvNickname.setAppearance(context, nicknameAppearance)
            binding.tvSentAt.setAppearance(context, sentAtAppearance)
            binding.tvTextMessage.setAppearance(context, textMessageAppearance)
            binding.ivMoreIcon.setImageDrawable(
                DrawableUtils.setTintList(context, moreIcon, moreIconTint)
            )
            binding.fileGroup.background =
                DrawableUtils.setTintList(context, fileMessageBackground, fileMessageBackgroundTint)
            binding.tvFileName.setAppearance(context, fileMessageAppearance)
            binding.tvReplyCount.setAppearance(context, replyCountAppearance)

            binding.rvEmojiReactionList.maxSpanSize = 6

            binding.contentDivider.setBackgroundResource(dividerColor)
            binding.threadInfoDivider.setBackgroundResource(dividerColor)

            parentMessageInfoUIConfig.myMessageTextUIConfig.mergeFromTextAppearance(context, textMessageAppearance)
            parentMessageInfoUIConfig.otherMessageTextUIConfig.mergeFromTextAppearance(context, textMessageAppearance)
            parentMessageInfoUIConfig.myEditedTextMarkUIConfig.mergeFromTextAppearance(
                context,
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdBody3OnDark02 else R.style.SendbirdBody3OnLight02
            )
            parentMessageInfoUIConfig.otherEditedTextMarkUIConfig.mergeFromTextAppearance(
                context,
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdBody3OnDark02 else R.style.SendbirdBody3OnLight02
            )
            parentMessageInfoUIConfig.myMentionUIConfig.mergeFromTextAppearance(
                context,
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdMentionLightMe else R.style.SendbirdMentionDarkMe
            )
            parentMessageInfoUIConfig.otherMentionUIConfig.mergeFromTextAppearance(
                context,
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdMentionLightMe else R.style.SendbirdMentionDarkMe
            )
            binding.tvTextMessage.setLinkTextColor(
                if (SendbirdUIKit.isDarkMode()) ContextCompat.getColor(
                    context,
                    R.color.ondark_02
                ) else ContextCompat.getColor(context, R.color.onlight_02)
            )
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
