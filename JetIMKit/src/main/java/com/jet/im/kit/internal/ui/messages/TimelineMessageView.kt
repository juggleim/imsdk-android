package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewTimeLineMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.utils.DateUtils
import com.jet.im.kit.utils.DrawableUtils
import com.juggle.im.model.Message

internal class TimelineMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_timeline_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewTimeLineMessageComponentBinding
    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            binding = SbViewTimeLineMessageComponentBinding.inflate(
                LayoutInflater.from(getContext()),
                this,
                true
            )
            val textAppearance = a.getResourceId(
                R.styleable.MessageView_sb_message_timeline_text_appearance,
                R.style.SendbirdCaption1OnDark01
            )
            val backgroundResourceId = a.getResourceId(
                R.styleable.MessageView_sb_message_timeline_background,
                R.drawable.sb_shape_timeline_background
            )
            binding.tvTimeline.setAppearance(context, textAppearance)
            binding.tvTimeline.setBackgroundResource(backgroundResourceId)
        } finally {
            a.recycle()
        }
    }

    fun drawTimeline(message: BaseMessage) {
        binding.tvTimeline.text = DateUtils.formatTimelineMessage(message.createdAt)
    }
    fun drawTimeline(message: Message) {
        binding.tvTimeline.text = DateUtils.formatTimelineMessage(message.timestamp)
    }

    fun drawTimeline(message: BaseMessage, uiConfig: NotificationConfig?) {
        binding.tvTimeline.run {
            text = DateUtils.formatTimelineMessage(message.createdAt)
            uiConfig?.let {
                val themeMode = it.themeMode
                it.theme.listTheme.timeline.apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    setTypeface(fontWeight.value)
                    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

                    DrawableUtils.createRoundedShapeDrawable(
                        backgroundColor.getColor(themeMode),
                        (measuredHeight / 2).toFloat()
                    ).apply { background = this }
                    setTextColor(textColor.getColor(themeMode))
                }
            }
        }
    }
}
