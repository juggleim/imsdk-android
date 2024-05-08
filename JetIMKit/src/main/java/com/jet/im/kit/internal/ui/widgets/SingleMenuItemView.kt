package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewSingleMenuItemBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils

internal class SingleMenuItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewSingleMenuItemBinding
    val layout: View
        get() = this

    enum class Type(var value: Int) {
        /**
         * A type that has an action button to redirect next page.
         */
        NEXT(0),

        /**
         * A type that has a switch button to toggle some action.
         */
        SWITCH(1),

        /**
         * A type that has no next action.
         */
        NONE(2);

        companion object {
            // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
            @JvmStatic
            fun from(value: Int): Type = values().firstOrNull { it.value == value } ?: NONE
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) = binding.vgMenuItem.setOnClickListener(listener)
    override fun setOnLongClickListener(listener: OnLongClickListener?) =
        binding.vgMenuItem.setOnLongClickListener(listener)

    fun setNextActionDrawable(@DrawableRes drawableResId: Int) = binding.ivNext.setImageResource(drawableResId)
    fun setOnActionMenuClickListener(listener: OnClickListener) {
        binding.scSwitch.setOnClickListener(listener)
        binding.ivNext.setOnClickListener(listener)
    }

    fun setIcon(@DrawableRes resId: Int) = binding.ivIcon.setImageResource(resId)
    fun setIconTint(@ColorRes tintResId: Int) {
        binding.ivIcon.imageTintList = AppCompatResources.getColorStateList(binding.ivIcon.context, tintResId)
    }

    fun setIconTint(tint: ColorStateList) {
        binding.ivIcon.imageTintList = tint
    }

    fun setName(name: String) {
        binding.tvName.text = name
    }

    fun setChecked(checked: Boolean) {
        binding.scSwitch.isChecked = checked
    }

    fun setMenuType(type: Type) {
        when (type) {
            Type.NEXT -> {
                binding.scSwitch.visibility = GONE
                binding.ivNext.visibility = VISIBLE
                binding.tvDescription.visibility = VISIBLE
            }
            Type.SWITCH -> {
                binding.scSwitch.visibility = VISIBLE
                binding.ivNext.visibility = GONE
                binding.tvDescription.visibility = GONE
            }
            else -> {
                binding.scSwitch.visibility = GONE
                binding.ivNext.visibility = GONE
                binding.tvDescription.visibility = GONE
            }
        }
    }

    fun setDescription(description: String) {
        binding.tvDescription.text = description
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SingleMenuItemView, defStyle, 0)
        try {
            binding = SbViewSingleMenuItemBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val itemBackground = a.getResourceId(
                R.styleable.SingleMenuItemView_sb_menu_item_background,
                R.drawable.selector_rectangle_light
            )
            val nicknameAppearance = a.getResourceId(
                R.styleable.SingleMenuItemView_sb_menu_item_name_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            val descriptionAppearance = a.getResourceId(
                R.styleable.SingleMenuItemView_sb_menu_item_description_appearance,
                R.style.SendbirdSubtitle2OnLight02
            )
            val iconTintRes = a.getColorStateList(R.styleable.SingleMenuItemView_sb_menu_item_icon_tint)
            val type = a.getInteger(R.styleable.SingleMenuItemView_sb_menu_item_type, 0)
            binding.tvName.setAppearance(context, nicknameAppearance)
            binding.tvName.ellipsize = TextUtils.TruncateAt.END
            binding.tvName.maxLines = 1
            binding.tvDescription.setAppearance(context, descriptionAppearance)
            binding.vgMenuItem.setBackgroundResource(itemBackground)
            val useDarkTheme = SendbirdUIKit.isDarkMode()
            val nextTint = if (useDarkTheme) R.color.ondark_01 else R.color.onlight_01
            val divider = if (useDarkTheme) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light
            val switchTrackTint = if (useDarkTheme) R.color.sb_switch_track_dark else R.color.sb_switch_track_light
            val switchThumbTint = if (useDarkTheme) R.color.sb_switch_thumb_dark else R.color.sb_switch_thumb_light
            binding.divider.setBackgroundResource(divider)
            iconTintRes?.let { setIconTint(it) }
            val nextResId = a.getResourceId(
                R.styleable.SingleMenuItemView_sb_menu_item_action_drawable,
                R.drawable.icon_chevron_right
            )
            binding.ivNext.setImageResource(nextResId)
            binding.ivNext.setImageDrawable(
                DrawableUtils.setTintList(
                    binding.ivNext.drawable,
                    AppCompatResources.getColorStateList(context, nextTint)
                )
            )
            binding.scSwitch.trackTintList = AppCompatResources.getColorStateList(context, switchTrackTint)
            binding.scSwitch.thumbTintList = AppCompatResources.getColorStateList(context, switchThumbTint)
            setMenuType(Type.from(type))
        } finally {
            a.recycle()
        }
    }
}
