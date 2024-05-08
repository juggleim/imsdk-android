package com.jet.im.kit.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewStatusFrameBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils

class StatusFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewStatusFrameBinding
    var errorIconTint: ColorStateList?
    var emptyIconTint: ColorStateList?
    var actionIconTint: ColorStateList?
    var errorIcon: Drawable?
    var errorText: String?
    var emptyIcon: Drawable?
    var emptyText: String?
    var showAction: Boolean = false
    var actionText: Int

    enum class Status {
        LOADING, CONNECTION_ERROR, ERROR, EMPTY, NONE
    }

    fun setStatus(status: Status) {
        this.visibility = VISIBLE
        binding.progressPanel.visibility = GONE
        when (status) {
            Status.LOADING -> binding.progressPanel.visibility = VISIBLE
            Status.CONNECTION_ERROR -> {
                actionText = R.string.sb_text_button_retry
                showAction = true
                setAlert(context.getString(R.string.sb_text_error_retry_request), errorIcon, errorIconTint)
            }
            Status.ERROR -> {
                showAction = false
                setAlert(errorText, errorIcon, errorIconTint)
            }
            Status.EMPTY -> {
                showAction = false
                setAlert(emptyText, emptyIcon, emptyIconTint)
            }
            Status.NONE -> this.visibility = GONE
        }
    }

    fun setOnActionEventListener(listener: OnClickListener) {
        binding.actionPanel.setOnClickListener(listener)
    }

    override fun setBackgroundColor(color: Int) {
        binding.frameParentPanel.setBackgroundColor(color)
    }

    override fun setBackground(drawable: Drawable) {
        binding.frameParentPanel.background = drawable
    }

    private fun setAlert(text: String?, icon: Drawable?, iconTint: ColorStateList?) {
        this.visibility = VISIBLE
        if (!isValidDrawable(icon)) {
            binding.ivAlertIcon.visibility = GONE
        } else {
            binding.ivAlertIcon.visibility = VISIBLE
            binding.ivAlertIcon.setImageDrawable(DrawableUtils.setTintList(icon, iconTint))
        }
        binding.ivAlertText.text = text
        binding.tvAction.setText(actionText)
        binding.actionPanel.visibility = if (showAction) VISIBLE else GONE
        if (showAction) {
            val actionIcon = binding.ivAction.drawable
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(actionIcon, actionIconTint))
        }
    }

    private fun isValidDrawable(drawable: Drawable?): Boolean {
        val isTransparentColor = if (drawable is ColorDrawable) {
            val transparent = ContextCompat.getColor(context, android.R.color.transparent)
            drawable.color == transparent
        } else false
        return drawable != null && !isTransparentColor
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StatusComponent, defStyleAttr, 0)
        try {
            binding = SbViewStatusFrameBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background =
                a.getResourceId(R.styleable.StatusComponent_sb_status_frame_background, R.color.background_50)
            val alertTextAppearance = a.getResourceId(
                R.styleable.StatusComponent_sb_status_frame_text_appearance,
                R.style.SendbirdBody3OnLight03
            )
            actionIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_action_icon_tint)
            errorIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_error_icon_tint)
            emptyIconTint = a.getColorStateList(R.styleable.StatusComponent_sb_status_frame_empty_icon_tint)
            errorIcon = a.getDrawable(R.styleable.StatusComponent_sb_status_frame_error_icon)
            errorText = a.getString(R.styleable.StatusComponent_sb_status_frame_error_text)
            emptyIcon = a.getDrawable(R.styleable.StatusComponent_sb_status_frame_empty_icon)
            emptyText = a.getString(R.styleable.StatusComponent_sb_status_frame_empty_text)
            actionText =
                a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_text, R.string.sb_text_button_retry)
            val actionBackground = a.getResourceId(
                R.styleable.StatusComponent_sb_status_frame_action_background,
                R.drawable.selector_button_retry_light
            )
            val actionIcon =
                a.getResourceId(R.styleable.StatusComponent_sb_status_frame_action_icon, R.drawable.icon_refresh)
            val actionTextAppearance = a.getResourceId(
                R.styleable.StatusComponent_sb_status_frame_action_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            binding.ivAlertText.setAppearance(getContext(), alertTextAppearance)
            binding.actionPanel.visibility = if (showAction) VISIBLE else GONE
            binding.actionPanel.setBackgroundResource(actionBackground)
            binding.ivAction.setImageDrawable(DrawableUtils.setTintList(getContext(), actionIcon, actionIconTint))
            binding.tvAction.setText(actionText)
            binding.tvAction.setAppearance(context, actionTextAppearance)
            binding.frameParentPanel.setBackgroundResource(background)
            binding.progressPanel.setBackgroundResource(background)
            setStatus(Status.NONE)
        } finally {
            a.recycle()
        }
    }
}
