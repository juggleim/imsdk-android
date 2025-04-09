package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.jet.im.kit.R
import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.databinding.SbViewMyContactCardMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.message.ContactCardMessage
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.PortraitGenerator
import com.jet.im.kit.utils.TextUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem
import com.juggle.im.model.messages.TextMessage
import java.io.File

internal class MyContactCardMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_user_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewMyContactCardMessageComponentBinding
    override val layout: View
        get() = binding.root
    private val sentAtAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewMyContactCardMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val messageBackground =
                a.getResourceId(R.styleable.MessageView_File_sb_message_me_background, R.drawable.sb_shape_chat_bubble)
            val messageBackgroundTint = ContextCompat.getColorStateList(context, R.color.sb_message_other_tint_light) // a.getColorStateList(R.color.sb_message_other_tint_light)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
            binding.tvSentAt.setAppearance(context, sentAtAppearance)
            binding.contentPanelWithReactions.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
            binding.emojiReactionListBackground.setBackgroundResource(emojiReactionListBackground)
            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)
        } finally {
            a.recycle()
        }
    }

    override fun drawMessage(
        channel: ConversationInfo,
        message: Message,
        reactionItemList: List<MessageReactionItem>,
        params: MessageListUIParams
    ) {
        val messageGroupType = params.messageGroupType
        val isSent = message.state == Message.MessageState.SENT
        val enableReactions = reactionItemList.isNotEmpty()
        binding.emojiReactionListBackground.visibility = if (enableReactions) VISIBLE else GONE
        binding.rvEmojiReactionList.visibility = if (enableReactions) VISIBLE else GONE
        binding.tvSentAt.visibility =
            if (isSent && (messageGroupType === MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType === MessageGroupType.GROUPING_TYPE_SINGLE)) VISIBLE else GONE
        binding.ivStatus.drawStatus(message, channel, params.shouldUseMessageReceipt())
        messageUIConfig?.let {
            it.mySentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.myMessageBackground?.let { background ->
                binding.contentPanel.background = background
            }
            it.myReactionListBackground?.let { reactionListBackground ->
                binding.emojiReactionListBackground.background = reactionListBackground
            }
        }
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList)
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
        val contactCardMessage = message.content as ContactCardMessage
        if (TextUtils.isNotEmpty(contactCardMessage.portrait)) {
            Glide.with(context).load(contactCardMessage.portrait).circleCrop().into(binding.ivPortrait)
        } else {
            val uri = Uri.parse(PortraitGenerator.generateDefaultAvatar(context, contactCardMessage.userId, contactCardMessage.name))
            Glide.with(context).load(uri).circleCrop().into(binding.ivPortrait)
        }
        binding.tvName.text = contactCardMessage.name
        binding.contentPanel.setOnClickListener {
            onClick(contactCardMessage)
        }
    }

    fun onClick(contactCard: ContactCardMessage) {
        val intent = Intent("com.jet.im.action.user_detail")
        intent.putExtra("userId", contactCard.userId)
        intent.putExtra("name", contactCard.name)
        intent.putExtra("portrait", contactCard.portrait)
        context.startActivity(intent)
    }
}