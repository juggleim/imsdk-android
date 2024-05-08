package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewChatNotificationComponentBinding
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.internal.extensions.addRipple
import com.jet.im.kit.internal.extensions.loadCircle
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.model.notifications.NotificationThemeMode
import com.jet.im.kit.utils.DateUtils
import com.jet.im.kit.utils.MessageUtils

internal class ChatNotificationView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_chat_notification
) : BaseNotificationView(context, attrs, defStyle) {
    override val binding: SbViewChatNotificationComponentBinding
    override val layout: View
        get() = binding.root

    var onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_ChatNotification, defStyle, 0)
        try {
            binding = SbViewChatNotificationComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_background,
                R.color.background_100
            )
            val leftCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_category_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val rightCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_sent_at_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val bubbleRadius = a.getDimensionPixelSize(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_radius,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8)
            )

            binding.contentPanel.setBackgroundResource(messageBackground)
            binding.tvLabel.setAppearance(context, leftCaptionAppearance)
            binding.tvSentAt.setAppearance(context, rightCaptionAppearance)
            binding.contentPanel.radius = bubbleRadius.toFloat()
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(channel: GroupChannel, message: BaseMessage, config: NotificationConfig? = null) {
        binding.tvLabel.text = MessageUtils.getNotificationLabel(message)
        binding.tvLabel.visibility = if (channel.isTemplateLabelEnabled) View.VISIBLE else View.INVISIBLE
        binding.tvSentAt.text = DateUtils.formatDateTime(context, message.createdAt)
        binding.ivProfileView.loadCircle(channel.coverUrl)

        // apply config
        config?.let {
            it.theme.notificationTheme.apply {
                val themeMode = config.themeMode
                val fontStyle = label ?: category
                fontStyle.apply {
                    binding.tvLabel.setTextColor(textColor.getColor(themeMode))
                    binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    binding.tvLabel.setTypeface(fontWeight.value)
                }
                sentAt.apply {
                    binding.tvSentAt.setTextColor(textColor.getColor(themeMode))
                    binding.tvSentAt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    binding.tvSentAt.setTypeface(fontWeight.value)
                }

                val theme = this
                binding.contentPanel.apply {
                    isClickable = true
                    isFocusable = true
                    setRadiusIntSize(theme.radius)
                    setBackgroundColor(theme.backgroundColor.getColor(themeMode))
                    addRipple(theme.pressedColor.getColor(themeMode))
                }
            }
        }

        makeTemplateView(
            message,
            binding.contentPanel,
            config?.themeMode ?: NotificationThemeMode.Default,
            onNotificationTemplateActionHandler
        )
    }
}
