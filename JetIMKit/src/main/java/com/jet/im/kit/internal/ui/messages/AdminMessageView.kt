package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewAdminMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class AdminMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_admin_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewAdminMessageComponentBinding
    override val layout: View
        get() = binding.root

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_Admin, defStyle, 0)
        try {
            binding = SbViewAdminMessageComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val textAppearance = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_text_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val backgroundResourceId = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_background,
                android.R.color.transparent
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
