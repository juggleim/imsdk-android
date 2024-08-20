package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.jet.im.kit.R
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewMyFileMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus

internal class MyFileMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_file_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyFileMessageComponentBinding
    override val layout: View
        get() = binding.root

    private val messageTextAppearance: Int
    private val sentAtAppearance: Int

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewMyFileMessageComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            messageTextAppearance = a.getResourceId(
                R.styleable.MessageView_File_sb_message_me_text_appearance,
                R.style.SendbirdBody3OnDark01
            )
            val messageBackground =
                a.getResourceId(
                    R.styleable.MessageView_File_sb_message_me_background,
                    R.drawable.sb_shape_chat_bubble
                )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_File_sb_message_me_background_tint)
            binding.tvSentAt.setAppearance(context, sentAtAppearance)
            binding.tvFileName.setAppearance(context, messageTextAppearance)
            binding.tvFileName.paintFlags =
                binding.tvFileName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.contentPanelWithReactions.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(
        channel: GroupChannel,
        message: BaseMessage,
        params: MessageListUIParams
    ) {
        val fileMessage = message as FileMessage
        val isSent = message.sendingStatus == SendingStatus.SUCCEEDED
        val messageGroupType = params.messageGroupType
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        messageUIConfig?.let {
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageTextAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myMessageBackground?.let { background ->
                binding.contentPanel.background = background
            }

        }

        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawFilename(binding.tvFileName, fileMessage, messageUIConfig)
        ViewUtils.drawFileIcon(binding.ivIcon, fileMessage)

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(
            binding.root.paddingLeft,
            paddingTop,
            binding.root.paddingRight,
            paddingBottom
        )
    }

    override fun drawMessage(
        channel: ConversationInfo,
        message: Message,
        params: MessageListUIParams
    ) {
        val fileMessage = message.content as com.juggle.im.model.messages.FileMessage
        val isSent = message.state == Message.MessageState.SENT
        val messageGroupType = params.messageGroupType
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        messageUIConfig?.let {
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageTextAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myMessageBackground?.let { background ->
                binding.contentPanel.background = background
            }

        }

        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawFilename(binding.tvFileName, message, fileMessage, messageUIConfig)
        ViewUtils.drawFileIcon(binding.ivIcon, "file")

        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(
            binding.root.paddingLeft,
            paddingTop,
            binding.root.paddingRight,
            paddingBottom
        )
    }
}
