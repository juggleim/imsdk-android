package com.jet.im.kit.internal.ui.messages

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewTypingIndicatorDotsComponentBinding
import com.jet.im.kit.utils.DrawableUtils

internal class TypingIndicatorDotsView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewTypingIndicatorDotsComponentBinding = SbViewTypingIndicatorDotsComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )

    override val layout: View
        get() = binding.root
    private var animatorSet: AnimatorSet? = null

    init {
        val messageBackground = R.drawable.sb_shape_chat_bubble
        val messageBackgroundTint = if (SendbirdUIKit.isDarkMode()) {
            AppCompatResources.getColorStateList(context, R.color.ondark_04)
        } else {
            AppCompatResources.getColorStateList(context, R.color.onlight_04)
        }
        val dotImageTintList = if (SendbirdUIKit.isDarkMode()) {
            AppCompatResources.getColorStateList(context, R.color.ondark_01)
        } else {
            AppCompatResources.getColorStateList(context, R.color.onlight_01)
        }

        binding.root.background =
            DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)

        setDotsImageTintList(dotImageTintList)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    private fun setDotsImageTintList(dotImageTintList: ColorStateList) {
        with(binding) {
            ivLeftTypingDot.imageTintList = dotImageTintList
            ivCenterTypingDot.imageTintList = dotImageTintList
            ivRightTypingDot.imageTintList = dotImageTintList
        }
    }

    private fun startAnimation() {
        with(binding) {
            setAnimation(ivLeftTypingDot, 400L)
            setAnimation(ivCenterTypingDot, 600L)
            setAnimation(ivRightTypingDot, 800L)
        }
    }

    private fun stopAnimation() {
        animatorSet?.cancel()
        animatorSet = null
    }

    private fun setAnimation(targetView: View, startDelay: Long) {
        val startAlphaAnimator = ObjectAnimator.ofFloat(targetView, "alpha", 0f, 0.12f)
        val scaleXAnimator = ObjectAnimator.ofFloat(targetView, "scaleX", 1.0f, 1.2f).apply {
            this.startDelay = startDelay
            this.duration = 400
            this.repeatCount = ObjectAnimator.INFINITE
            this.repeatMode = ObjectAnimator.REVERSE
        }
        val scaleYAnimator = ObjectAnimator.ofFloat(targetView, "scaleY", 1.0f, 1.2f).apply {
            this.startDelay = startDelay
            this.duration = 400
            this.repeatCount = ObjectAnimator.INFINITE
            this.repeatMode = ObjectAnimator.REVERSE
        }
        val alphaAnimator = ObjectAnimator.ofFloat(targetView, "alpha", 0.12f, 0.38f).apply {
            this.startDelay = startDelay
            this.duration = 400
            this.repeatCount = ObjectAnimator.INFINITE
            this.repeatMode = ObjectAnimator.REVERSE
        }

        animatorSet = AnimatorSet().apply {
            playTogether(startAlphaAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator)
            start()
        }
    }
}
