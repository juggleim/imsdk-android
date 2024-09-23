package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewStateHeaderBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class StateHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewStateHeaderBinding
    val titleTextView: TextView
        get() = binding.title

    fun setLeftButtonImageDrawable(drawable: Drawable?) = binding.leftButton.setImageDrawable(drawable)
    fun setRightButtonText(@StringRes testResId: Int) = binding.rightButton.setText(testResId)
    fun setOnRightButtonClickListener(listener: OnClickListener?) = binding.rightButton.setOnClickListener(listener)
    fun setOnLeftButtonClickListener(listener: OnClickListener?) = binding.leftButton.setOnClickListener(listener)
    fun setLeftButtonTint(tint: ColorStateList?) {
        binding.leftButton.imageTintList = tint
    }

    fun setUseLeftButton(useLeftButton: Boolean) {
        binding.leftButton.visibility = if (useLeftButton) VISIBLE else GONE
    }

    fun setRightButtonText(text: String?) {
        binding.rightButton.text = text
    }

    fun setEnabledRightButton(enabled: Boolean) {
        binding.rightButton.isEnabled = enabled
    }

    fun setUseRightButton(useRightButton: Boolean) {
        binding.rightButton.visibility = if (useRightButton) VISIBLE else GONE
    }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HeaderComponent,
            defStyleAttr, 0
        )
        try {
            binding = SbViewStateHeaderBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_background, R.color.background_50)
            val titleText: CharSequence? = a.getString(R.styleable.HeaderComponent_sb_appbar_title)
            val titleTextAppearance =
                a.getResourceId(R.styleable.HeaderComponent_sb_appbar_title_appearance, R.style.SendbirdH2OnLight01)
            val leftButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_icon, 0)
            val leftButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_left_button_tint)
            val leftButtonBackground = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_left_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val rightButtonText: CharSequence? = a.getString(R.styleable.HeaderComponent_sb_appbar_right_button_text)
            val rightButtonTextAppearance = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_right_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val rightButtonTextColor =
                a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_right_button_text_color)
            val rightButtonBackground = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_right_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val dividerColor = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_divider_color, R.color.onlight_04)
            binding.root.setBackgroundResource(background)
            binding.title.text = titleText
            binding.title.setAppearance(context, titleTextAppearance)
            binding.leftButton.setImageResource(leftButtonIconResId)
            binding.leftButton.setBackgroundResource(leftButtonBackground)
            binding.leftButton.imageTintList = leftButtonTint
            binding.rightButton.text = rightButtonText
            binding.rightButton.setAppearance(context, rightButtonTextAppearance)
            if (rightButtonTextColor != null) {
                binding.rightButton.setTextColor(rightButtonTextColor)
            }
            binding.rightButton.setBackgroundResource(rightButtonBackground)
            binding.elevationView.setBackgroundResource(dividerColor)
        } finally {
            a.recycle()
        }
    }
}
