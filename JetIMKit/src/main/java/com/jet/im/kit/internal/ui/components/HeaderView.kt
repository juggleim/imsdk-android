package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewHeaderBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.ui.channels.ChannelCoverView

internal class HeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewHeaderBinding
    val titleTextView: TextView
        get() = binding.title
    val leftButton: ImageButton
        get() = binding.leftButton
    val rightButton: ImageButton
        get() = binding.rightButton

    fun setLeftButtonImageResource(@DrawableRes drawableRes: Int) {
        binding.leftButton.setImageResource(drawableRes)
    }

    fun setLeftButtonImageDrawable(drawable: Drawable?) {
        binding.leftButton.setImageDrawable(drawable)
    }

    fun setLeftButtonTint(tint: ColorStateList?) {
        binding.leftButton.imageTintList = tint
    }

    fun setOnLeftButtonClickListener(listener: OnClickListener?) {
        binding.leftButton.setOnClickListener(listener)
    }

    fun setUseLeftButton(useLeftButton: Boolean) {
        binding.leftButton.visibility = if (useLeftButton) VISIBLE else GONE
    }

    fun setRightButtonImageResource(@DrawableRes drawableRes: Int) {
        binding.rightButton.setImageResource(drawableRes)
    }

    fun setRightButtonImageDrawable(drawable: Drawable?) {
        binding.rightButton.setImageDrawable(drawable)
    }

    fun setRightButtonTint(tint: ColorStateList) {
        binding.rightButton.imageTintList = tint
    }

    fun setOnRightButtonClickListener(listener: OnClickListener?) {
        binding.rightButton.setOnClickListener(listener)
    }

    fun setUseRightButton(useRightButton: Boolean) {
        binding.rightButton.visibility = if (useRightButton) VISIBLE else GONE
    }

    fun setDividerColor(@ColorInt color: Int) {
        binding.elevationView.setBackgroundColor(color)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        binding.getRoot().setBackgroundColor(color)
    }

    val descriptionTextView: TextView
        get() = binding.description
    val profileView: ChannelCoverView
        get() = binding.profileView

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HeaderComponent,
            defStyleAttr, 0
        )
        try {
            binding = SbViewHeaderBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_background, R.color.background_50)
            val titleText: CharSequence? = a.getString(R.styleable.HeaderComponent_sb_appbar_title)
            val titleTextAppearance =
                a.getResourceId(R.styleable.HeaderComponent_sb_appbar_title_appearance, R.style.SendbirdH2OnLight01)
            val descText: CharSequence? = a.getString(R.styleable.HeaderComponent_sb_appbar_description)
            val descTextAppearance = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_description_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val leftButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_left_button_icon, 0)
            val leftButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_left_button_tint)
            val leftButtonBackground = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_left_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val rightButtonIconResId = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_right_button_icon, 0)
            val rightButtonTint = a.getColorStateList(R.styleable.HeaderComponent_sb_appbar_right_button_tint)
            val rightButtonBackground = a.getResourceId(
                R.styleable.HeaderComponent_sb_appbar_right_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val dividerColor = a.getResourceId(R.styleable.HeaderComponent_sb_appbar_divider_color, R.color.onlight_04)
            binding.getRoot().setBackgroundResource(background)
            binding.title.text = titleText
            binding.title.setAppearance(context, titleTextAppearance)
            binding.description.setAppearance(context, descTextAppearance)
            binding.description.setTextSize(Dimension.DP, resources.getDimension(R.dimen.sb_size_12))
            binding.leftButton.setImageResource(leftButtonIconResId)
            binding.leftButton.setBackgroundResource(leftButtonBackground)
            binding.leftButton.imageTintList = leftButtonTint
            binding.rightButton.setImageResource(rightButtonIconResId)
            binding.rightButton.setBackgroundResource(rightButtonBackground)
            binding.rightButton.imageTintList = rightButtonTint
            binding.elevationView.setBackgroundResource(dividerColor)
            descText?.let {
                binding.description.text = it
            } ?: run {
                binding.description.visibility = GONE
            }
        } finally {
            a.recycle()
        }
    }
}
