package com.jet.im.kit.internal.ui.messages

import android.content.ActivityNotFoundException
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewOpenChannelUserMessageComponentBinding
import com.jet.im.kit.internal.ui.widgets.OnLinkLongClickListener
import com.jet.im.kit.log.Logger
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.configurations.OpenChannelConfig
import com.jet.im.kit.utils.IntentUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.ViewUtils

internal class OpenChannelUserMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_user_message
) : OpenChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOpenChannelUserMessageComponentBinding
    override val layout: View
        get() = binding.root

    private var operatorAppearance: Int
    private var nicknameAppearance: Int
    private var editedAppearance: Int
    private var marginLeftEmpty: Int
    private var marginLeftNor: Int
    private val messageAppearance: Int
    private val sentAtAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            binding =
                SbViewOpenChannelUserMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            messageAppearance =
                a.getResourceId(R.styleable.MessageView_sb_message_text_appearance, R.style.SendbirdBody3OnLight01)
            val contentBackground =
                a.getResourceId(R.styleable.MessageView_sb_message_background, R.drawable.selector_rectangle_light)
            val linkTextColor = a.getResourceId(R.styleable.MessageView_sb_message_link_text_color, R.color.ondark_01)
            val ogtagBackground = a.getResourceId(
                R.styleable.MessageView_sb_message_ogtag_background,
                R.drawable.selector_open_channel_message_bg_light
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            operatorAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_operator_name_text_appearance,
                R.style.SendbirdCaption1Secondary300
            )
            editedAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_edited_mark_text_appearance,
                R.style.SendbirdBody3OnLight02
            )
            binding.ogTag.setBackgroundResource(ogtagBackground)
            binding.tvMessage.setLinkTextColor(ContextCompat.getColor(context, linkTextColor))
            binding.tvMessage.clickedLinkTextColor = ContextCompat.getColor(context, linkTextColor)
            binding.contentPanel.setBackgroundResource(contentBackground)
            binding.tvMessage.setOnClickListener { binding.contentPanel.performClick() }
            binding.tvMessage.setOnLongClickListener { binding.contentPanel.performLongClick() }
            binding.tvMessage.onLinkLongClickListener = object : OnLinkLongClickListener {
                override fun onLongClick(textView: TextView, link: String): Boolean {
                    return binding.contentPanel.performLongClick()
                }
            }
            binding.tvMessage.clickedLinkTextColor = ContextCompat.getColor(context, linkTextColor)
            binding.ogTag.setOnLongClickListener { binding.contentPanel.performLongClick() }
            marginLeftEmpty = resources.getDimensionPixelSize(R.dimen.sb_size_40)
            marginLeftNor = resources.getDimensionPixelSize(R.dimen.sb_size_12)
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(channel: OpenChannel, message: BaseMessage, params: MessageListUIParams) {
        val messageGroupType = params.messageGroupType
        val enableOgTag = message.ogMetaData != null && OpenChannelConfig.getEnableOgTag(params.openChannelConfig)

        binding.ogTag.visibility = if (enableOgTag) VISIBLE else GONE

        messageUIConfig?.let {
            it.myEditedTextMarkUIConfig.mergeFromTextAppearance(context, editedAppearance)
            it.otherEditedTextMarkUIConfig.mergeFromTextAppearance(context, editedAppearance)
            it.myMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
            it.otherMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.operatorNicknameTextUIConfig.mergeFromTextAppearance(context, operatorAppearance)
            val isMine = MessageUtils.isMine(message)
            val background = if (isMine) it.myMessageBackground else it.otherMessageBackground
            val ogtagBackground = if (isMine) it.myOgtagBackground else it.otherOgtagBackground
            val linkedTextColor = it.linkedTextColor
            background?.let { binding.contentPanel.background = background }
            ogtagBackground?.let { binding.ogTag.background = ogtagBackground }
            linkedTextColor?.let {
                binding.tvMessage.setLinkTextColor(linkedTextColor)
                binding.tvMessage.clickedLinkTextColor = linkedTextColor.defaultColor
            }
        }
        ViewUtils.drawTextMessage(binding.tvMessage, message, messageUIConfig, false)

        if (enableOgTag) binding.ogTag.drawOgtag(message.ogMetaData)
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())

        if (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) {
            binding.ivProfileView.visibility = VISIBLE
            binding.tvNickname.visibility = VISIBLE
            binding.tvSentAt.visibility = VISIBLE
            ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
            ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, channel.isOperator(message.sender))
            ViewUtils.drawProfile(binding.ivProfileView, message)
            val layoutParams = binding.tvMessage.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.leftMargin = marginLeftNor
            binding.tvMessage.layoutParams = layoutParams
        } else {
            binding.ivProfileView.visibility = GONE
            binding.tvNickname.visibility = GONE
            binding.tvSentAt.visibility = INVISIBLE
            val layoutParams = binding.tvMessage.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.leftMargin = marginLeftEmpty
            binding.tvMessage.layoutParams = layoutParams
        }

        if (enableOgTag) {
            binding.ogTag.setOnClickListener {
                message.ogMetaData?.url?.let {
                    val intent = IntentUtils.getWebViewerIntent(it)
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Logger.e(e)
                    }
                }
            }
        }
    }
}
