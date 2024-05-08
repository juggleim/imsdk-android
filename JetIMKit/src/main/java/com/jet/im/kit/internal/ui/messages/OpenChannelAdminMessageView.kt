package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewOpenChannelAdminMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class OpenChannelAdminMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_admin_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewOpenChannelAdminMessageComponentBinding
    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyle, 0)
        try {
            binding = SbViewOpenChannelAdminMessageComponentBinding.inflate(
                LayoutInflater.from(getContext()),
                this,
                true
            )
            val textAppearance = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_text_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val backgroundResourceId = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_background,
                R.drawable.sb_shape_admin_message_background_light
            )
            binding.tvMessage.setAppearance(context, textAppearance)
            binding.tvMessage.setBackgroundResource(backgroundResourceId)
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(message: BaseMessage) {
        binding.tvMessage.text = message.message
    }
}
