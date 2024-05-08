package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewOpenChannelFileMessageComponentBinding
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils
import java.util.Locale

internal class OpenChannelFileMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_file_message
) : OpenChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOpenChannelFileMessageComponentBinding
    override val layout: View
        get() = binding.root

    private val nicknameAppearance: Int
    private val operatorAppearance: Int
    private val marginLeftEmpty: Int
    private val marginLeftNor: Int
    private val messageAppearance: Int
    private val sentAtAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            binding =
                SbViewOpenChannelFileMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val contentBackground = a.getResourceId(
                R.styleable.MessageView_sb_message_background,
                R.drawable.selector_open_channel_message_bg_light
            )
            messageAppearance =
                a.getResourceId(R.styleable.MessageView_sb_message_text_appearance, R.style.SendbirdBody3OnLight01)
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            operatorAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_operator_name_text_appearance,
                R.style.SendbirdCaption1Secondary300
            )
            binding.contentPanel.setBackgroundResource(contentBackground)
            binding.tvFileName.paintFlags = binding.tvFileName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            marginLeftEmpty = resources.getDimensionPixelSize(R.dimen.sb_size_40)
            marginLeftNor = resources.getDimensionPixelSize(R.dimen.sb_size_12)
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: OpenChannel, message: BaseMessage, params: MessageListUIParams) {
        val fileMessage = message as FileMessage
        val messageGroupType = params.messageGroupType

        messageUIConfig?.let {
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
            it.otherMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.operatorNicknameTextUIConfig.mergeFromTextAppearance(context, operatorAppearance)
            val isMine = MessageUtils.isMine(message)
            val background = if (isMine) it.myMessageBackground else it.otherMessageBackground
            background?.let { binding.contentPanel.background = background }
        }

        ViewUtils.drawFilename(binding.tvFileName, fileMessage, messageUIConfig)
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        val backgroundTint = if (SendbirdUIKit.isDarkMode()) R.color.background_600 else R.color.background_50
        val iconTint = SendbirdUIKit.getDefaultThemeMode().primaryTintResId
        val inset = context.resources.getDimension(R.dimen.sb_size_12).toInt()

        val background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_corner_24, backgroundTint)
        val isAudioMessage = fileMessage.type.lowercase(Locale.getDefault()).startsWith(StringSet.audio)
        val icon = DrawableUtils.setTintList(
            context, if (isAudioMessage) R.drawable.icon_file_audio else R.drawable.icon_file_document, iconTint
        )
        binding.ivIcon.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset))

        if (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) {
            binding.ivProfileView.visibility = VISIBLE
            binding.tvNickname.visibility = VISIBLE
            binding.tvSentAt.visibility = VISIBLE
            ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
            ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, channel.isOperator(message.sender))
            ViewUtils.drawProfile(binding.ivProfileView, message)
            val layoutParams = binding.contentPanel.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.leftMargin = marginLeftNor
            binding.contentPanel.layoutParams = layoutParams
        } else {
            binding.ivProfileView.visibility = GONE
            binding.tvNickname.visibility = GONE
            binding.tvSentAt.visibility = INVISIBLE
            val layoutParams = binding.contentPanel.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.leftMargin = marginLeftEmpty
            binding.contentPanel.layoutParams = layoutParams
        }
    }
}
