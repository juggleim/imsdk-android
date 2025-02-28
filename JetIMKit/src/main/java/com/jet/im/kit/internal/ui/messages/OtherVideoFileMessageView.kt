package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewOtherFileVideoMessageComponentBinding
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.messages.VideoMessage
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus

internal class OtherVideoFileMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_file_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherFileVideoMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int

    override fun drawMessage(
        channel: ConversationInfo,
        message: Message,
        params: MessageListUIParams
    ) {
        val messageGroupType = params.messageGroupType
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
        ViewUtils.drawThumbnail(binding.ivThumbnail, (message.content as VideoMessage), message)
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, StringSet.video)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewOtherFileVideoMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
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
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            val bg =
                if (SendbirdUIKit.isDarkMode()) R.drawable.sb_shape_image_message_background_dark else R.drawable.sb_shape_image_message_background
            binding.ivThumbnail.setBackgroundResource(bg)
        } finally {
            a.recycle()
        }
    }
}
