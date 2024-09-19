package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewMentionLimitAlertBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils
import java.util.Locale

internal class ThemeableSnackbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_themeable_snackbar
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewMentionLimitAlertBinding
    private var snackbar: Snackbar? = null

    fun init(anchorView: View) {
        if (this.parent != null) (this.parent as ViewGroup).removeAllViews()
        val snackbar = Snackbar.make(anchorView, "", Snackbar.LENGTH_INDEFINITE).apply {
            this.view.setBackgroundColor(Color.TRANSPARENT)
            this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            this.anchorView = anchorView
        }
        val snackbarLayout = snackbar.view as SnackbarLayout
        snackbarLayout.removeAllViews()
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(this)
        this.snackbar = snackbar
    }

    fun setMaxMentionCount(maxMentionCount: Int) {
        val alertText =
            String.format(Locale.US, context.getString(R.string.sb_text_exceed_mention_limit_count), maxMentionCount)
        binding.tvText.text = alertText
    }

    fun show() {
        snackbar?.let {
            if (!it.isShown) {
                it.show()
            }
        }
    }

    fun dismiss() {
        snackbar?.let {
            if (it.isShown) {
                it.dismiss()
            }
        }
    }

    override fun isShown(): Boolean = snackbar?.isShown ?: false

    init {
        val a = context.theme.obtainStyledAttributes(null, R.styleable.ThemeableSnackbar, defStyleAttr, 0)
        try {
            binding = SbViewMentionLimitAlertBinding.inflate(LayoutInflater.from(context))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val background =
                a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_background, R.color.background_50)
            val rooflineColor =
                a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_roof_line_color, R.color.onlight_04)
            val textAppearance = a.getResourceId(
                R.styleable.ThemeableSnackbar_sb_snackbar_text_appearance,
                R.style.SendbirdBody3OnLight02
            )
            val icon = a.getResourceId(R.styleable.ThemeableSnackbar_sb_snackbar_icon, R.drawable.icon_info)
            val iconTint = a.getColorStateList(R.styleable.ThemeableSnackbar_sb_snackbar_icon_tint)
            binding.ivIcon.setImageResource(icon)
            if (iconTint != null) {
                binding.ivIcon.setImageDrawable(DrawableUtils.setTintList(binding.ivIcon.context, icon, iconTint))
            }
            binding.tvText.setAppearance(context, textAppearance)
            binding.ivRoofLine.setBackgroundResource(rooflineColor)
            binding.root.setBackgroundResource(background)
        } finally {
            a.recycle()
        }
    }
}
