package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewSuggestedReplyComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class SuggestedReplyView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewSuggestedReplyComponentBinding = SbViewSuggestedReplyComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    override val layout: View
        get() = binding.root

    init {
        val textAppearance = if (SendbirdUIKit.isDarkMode()) {
            R.style.SendbirdBody3Primary200
        } else {
            R.style.SendbirdBody3Primary300
        }

        val backgroundResourceId = if (SendbirdUIKit.isDarkMode()) {
            R.drawable.sb_suggested_replies_button_dark
        } else {
            R.drawable.sb_suggested_replies_button_light
        }

        binding.tvSuggestedReply.setAppearance(context, textAppearance)
        binding.tvSuggestedReply.setBackgroundResource(backgroundResourceId)
    }

    fun drawSuggestedReplies(suggestedReply: String) {
        binding.tvSuggestedReply.text = suggestedReply
    }
}
