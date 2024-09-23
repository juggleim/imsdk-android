package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewToastBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class ToastView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewToastBinding

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ToastView, defStyleAttr, 0)
        try {
            binding = SbViewToastBinding.inflate(LayoutInflater.from(getContext()), this, true).apply {
                val background =
                    a.getResourceId(R.styleable.ToastView_sb_toast_background, R.drawable.sb_toast_background_light)
                val textAppearance =
                    a.getResourceId(R.styleable.ToastView_sb_toast_text_appearance, R.style.SendbirdBody3OnDark01)
                val successTint = if (SendbirdUIKit.isDarkMode()) R.color.secondary_500 else R.color.secondary_200
                val errorTint = if (SendbirdUIKit.isDarkMode()) R.color.error_300 else R.color.error_200
                toastPanel.setBackgroundResource(background)
                toastPanel.background.alpha = 163
                tvToastText.setAppearance(context, textAppearance)
                ivSuccess.imageTintList = AppCompatResources.getColorStateList(context, successTint)
                ivError.imageTintList = AppCompatResources.getColorStateList(context, errorTint)
            }
        } finally {
            a.recycle()
        }
    }

    fun setText(@StringRes text: Int) {
        binding.tvToastText.setText(text)
    }

    fun setText(text: CharSequence?) {
        binding.tvToastText.text = text
    }

    enum class ToastStatus {
        SUCCESS, ERROR
    }

    fun setStatus(status: ToastStatus) {
        if (status == ToastStatus.SUCCESS) {
            binding.ivError.visibility = GONE
            binding.ivSuccess.visibility = VISIBLE
        } else if (status == ToastStatus.ERROR) {
            binding.ivError.visibility = VISIBLE
            binding.ivSuccess.visibility = GONE
        }
    }
}
