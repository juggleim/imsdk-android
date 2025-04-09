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
import com.jet.im.kit.databinding.SbViewOtherContactCardMessageComponentBinding
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.message.ContactCardMessage
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.PortraitGenerator
import com.jet.im.kit.utils.TextUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem

internal class OtherContactCardMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_user_message
) : GroupChannelMessageView(context, attrs, defStyle) {
    override val binding: SbViewOtherContactCardMessageComponentBinding
    override val layout: View
        get() = this
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyle, 0)
        try {
            binding = SbViewOtherContactCardMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val messageBackgroundTint = ContextCompat.getColorStateList(context, R.color.sb_message_other_tint_light)
//            val messageBackgroundTint =
//                a.getColorStateList(R.styleable.MessageView_File_sb_message_other_background_tint)
            val emojiReactionListBackground = a.getResourceId(
                R.styleable.MessageView_File_sb_message_emoji_reaction_list_background,
                R.drawable.sb_shape_chat_bubble_reactions_light
            )
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
        val enableReactions =
            reactionItemList.isNotEmpty()
        val showProfile =
            messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL
        val showNickname =
            (messageGroupType == MessageGroupType.GROUPING_TYPE_SINGLE || messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD) &&
                    (!MessageUtils.hasParentMessage(message))

        binding.ivProfileView.visibility = if (showProfile) VISIBLE else INVISIBLE
        binding.tvNickname.visibility = if (showNickname) VISIBLE else GONE
        binding.emojiReactionListBackground.visibility = if (enableReactions) VISIBLE else GONE
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
        ViewUtils.drawReactionEnabled(binding.rvEmojiReactionList)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
        val paddingTop =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_TAIL || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        val paddingBottom =
            resources.getDimensionPixelSize(if (messageGroupType == MessageGroupType.GROUPING_TYPE_HEAD || messageGroupType == MessageGroupType.GROUPING_TYPE_BODY) R.dimen.sb_size_1 else R.dimen.sb_size_8)
        binding.root.setPadding(binding.root.paddingLeft, paddingTop, binding.root.paddingRight, paddingBottom)
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