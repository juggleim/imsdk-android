package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseFileMessage
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.message.UserMessage
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.ReplyType
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewOtherQuotedMessageBinding
import com.jet.im.kit.internal.extensions.getCacheKey
import com.jet.im.kit.internal.extensions.getDisplayMessage
import com.jet.im.kit.internal.extensions.getName
import com.jet.im.kit.internal.extensions.getType
import com.jet.im.kit.internal.extensions.hasParentMessage
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.TextUIConfig
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.UserUtils
import com.jet.im.kit.utils.ViewUtils
import java.util.Locale

internal class OtherQuotedMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_other_message
) : BaseQuotedMessageView(context, attrs, defStyleAttr) {
    override val binding: SbViewOtherQuotedMessageBinding
    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyleAttr, 0)
        try {
            binding = SbViewOtherQuotedMessageBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundResId = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val backgroundTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_other_background_tint)
            val titleIconId = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_other_title_icon,
                R.drawable.icon_reply_filled
            )
            val titleIconTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_other_title_icon_tint)
            val titleTextAppearance = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_other_title_text_appearance,
                R.style.SendbirdCaption1OnLight01
            )
            val messageIconTintId = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_other_file_icon_tint)
            val messageTextAppearance = a.getResourceId(
                R.styleable.MessageView_sb_quoted_message_other_text_appearance,
                R.style.SendbirdCaption2OnLight03
            )
            backgroundTint?.let {
                binding.quoteReplyMessagePanel.background =
                    DrawableUtils.setTintList(context, backgroundResId, backgroundTint.withAlpha(0x80))
            } ?: binding.quoteReplyMessagePanel.setBackgroundResource(backgroundResId)
            binding.ivQuoteReplyIcon.setImageResource(titleIconId)
            binding.ivQuoteReplyIcon.imageTintList = titleIconTint
            binding.tvQuoteReplyTitle.setAppearance(context, titleTextAppearance)
            binding.tvQuoteReplyMessage.setAppearance(context, messageTextAppearance)
            binding.ivQuoteReplyMessageIcon.imageTintList = messageIconTintId
            if (SendbirdUIKit.isDarkMode()) {
                binding.ivQuoteReplyThumbnail.setBackgroundResource(R.drawable.sb_shape_quoted_message_thumbnail_background_dark)
            } else {
                binding.ivQuoteReplyThumbnail.setBackgroundResource(R.drawable.sb_shape_quoted_message_thumbnail_background)
            }
        } finally {
            a.recycle()
        }
    }

    override fun drawQuotedMessage(
        channel: GroupChannel,
        message: BaseMessage,
        textUIConfig: TextUIConfig?,
        messageListUIParams: MessageListUIParams
    ) {
        binding.quoteReplyPanel.visibility = GONE
        if (!message.hasParentMessage()) return

        val parentMessage = message.parentMessage
        binding.quoteReplyPanel.visibility = VISIBLE
        binding.quoteReplyMessagePanel.visibility = GONE
        binding.ivQuoteReplyMessageIcon.visibility = GONE
        binding.quoteReplyThumbnailPanel.visibility = GONE
        binding.ivQuoteReplyThumbnailOverlay.visibility = GONE

        parentMessage?.let {
            val replyType = messageListUIParams.channelConfig.replyType
            if (replyType == ReplyType.THREAD && it.createdAt < channel.messageOffsetTimestamp) {
                val messageUnavailable = context.getString(R.string.sb_text_channel_message_unavailable)
                binding.quoteReplyMessagePanel.visibility = VISIBLE
                binding.tvQuoteReplyMessage.text =
                    textUIConfig?.apply(context, messageUnavailable) ?: messageUnavailable
                binding.tvQuoteReplyMessage.isSingleLine = true
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.END
                binding.tvQuoteReplyTitle.text = String.format(
                    context.getString(R.string.sb_text_replied_to),
                    UserUtils.getDisplayName(context, message.sender, true),
                    UserUtils.getDisplayName(context, null, true)
                )
                return
            }
        }

        binding.tvQuoteReplyTitle.text = String.format(
            context.getString(R.string.sb_text_replied_to),
            UserUtils.getDisplayName(context, message.sender, true, 10),
            UserUtils.getDisplayName(context, parentMessage?.sender, true)
        )

        when (parentMessage) {
            is UserMessage -> {
                binding.quoteReplyMessagePanel.visibility = VISIBLE
                val text = parentMessage.getDisplayMessage()
                binding.tvQuoteReplyMessage.text = textUIConfig?.apply(context, text) ?: text
                binding.tvQuoteReplyMessage.isSingleLine = false
                binding.tvQuoteReplyMessage.maxLines = 2
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.END
            }

            is BaseFileMessage -> {
                val requestListener: RequestListener<Drawable?> = object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.ivQuoteReplyThumbnailOverlay.visibility = GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.ivQuoteReplyThumbnailOverlay.visibility = VISIBLE
                        return false
                    }
                }
                val type = parentMessage.getType()
                binding.ivQuoteReplyThumbnail.radius = resources.getDimensionPixelSize(R.dimen.sb_size_16).toFloat()
                binding.tvQuoteReplyMessage.isSingleLine = true
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.MIDDLE

                if (type == StringSet.voice) {
                    val text = context.getString(R.string.sb_text_voice_message)
                    binding.quoteReplyMessagePanel.visibility = VISIBLE
                    binding.tvQuoteReplyMessage.text = textUIConfig?.apply(context, text) ?: text
                    binding.tvQuoteReplyMessage.isSingleLine = true
                    binding.tvQuoteReplyMessage.maxLines = 1
                    binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.END
                } else if (type.lowercase(Locale.getDefault()).contains(StringSet.gif)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageDrawable(
                        DrawableUtils.createOvalIcon(
                            context, R.color.background_50, R.drawable.icon_gif, R.color.onlight_03
                        )
                    )
                    when (parentMessage) {
                        is FileMessage -> {
                            ViewUtils.drawQuotedMessageThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage,
                                requestListener
                            )
                        }

                        is MultipleFilesMessage -> {
                            val firstImage = parentMessage.files.firstOrNull() ?: return
                            ViewUtils.drawThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage.getCacheKey(0),
                                firstImage.url,
                                firstImage.plainUrl,
                                firstImage.fileType,
                                firstImage.thumbnails,
                                requestListener,
                                R.dimen.sb_size_24
                            )
                        }
                    }
                } else if (type.lowercase(Locale.getDefault()).contains(StringSet.video)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageDrawable(
                        DrawableUtils.createOvalIcon(
                            context, R.color.background_50, R.drawable.icon_play, R.color.onlight_03
                        )
                    )
                    when (parentMessage) {
                        is FileMessage -> {
                            ViewUtils.drawQuotedMessageThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage,
                                requestListener
                            )
                        }

                        is MultipleFilesMessage -> {
                            val firstImage = parentMessage.files.firstOrNull() ?: return
                            ViewUtils.drawThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage.getCacheKey(0),
                                firstImage.url,
                                firstImage.plainUrl,
                                firstImage.fileType,
                                firstImage.thumbnails,
                                requestListener,
                                R.dimen.sb_size_24
                            )
                        }
                    }
                } else if (type.lowercase(Locale.getDefault()).startsWith(StringSet.audio)) {
                    binding.quoteReplyMessagePanel.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_audio)
                    binding.tvQuoteReplyMessage.text =
                        textUIConfig?.apply(context, parentMessage.getName(context)) ?: parentMessage.getName(context)
                } else if (type.startsWith(StringSet.image) && !type.contains(StringSet.svg)) {
                    binding.quoteReplyThumbnailPanel.visibility = VISIBLE
                    binding.ivQuoteReplyThumbnailIcon.setImageResource(android.R.color.transparent)
                    when (parentMessage) {
                        is FileMessage -> {
                            ViewUtils.drawQuotedMessageThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage,
                                requestListener
                            )
                        }

                        is MultipleFilesMessage -> {
                            val firstImage = parentMessage.files.firstOrNull() ?: return
                            ViewUtils.drawThumbnail(
                                binding.ivQuoteReplyThumbnail,
                                parentMessage.getCacheKey(0),
                                firstImage.url,
                                firstImage.plainUrl,
                                firstImage.fileType,
                                firstImage.thumbnails,
                                requestListener,
                                R.dimen.sb_size_24
                            )
                        }
                    }
                } else {
                    binding.quoteReplyMessagePanel.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.visibility = VISIBLE
                    binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_document)
                    binding.tvQuoteReplyMessage.text =
                        textUIConfig?.apply(context, parentMessage.getName(context)) ?: parentMessage.getName(context)
                }
            }

            else -> {
                if (parentMessage == null) return
                binding.quoteReplyMessagePanel.visibility = VISIBLE
                ViewUtils.drawUnknownMessage(binding.tvQuoteReplyMessage, false)
                binding.tvQuoteReplyMessage.isSingleLine = false
                binding.tvQuoteReplyMessage.maxLines = 2
                binding.tvQuoteReplyMessage.ellipsize = TextUtils.TruncateAt.END
            }
        }
    }
}
