package com.jet.im.kit.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.message.Feedback
import com.sendbird.android.message.FeedbackRating
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewFeedbackBinding

internal class FeedbackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_feedback
) : FrameLayout(context, attrs, defStyleAttr) {
    val binding: SbViewFeedbackBinding = SbViewFeedbackBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    var onFeedbackRatingClickListener: ((rating: FeedbackRating) -> Unit)? = null

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Feedback,
            defStyleAttr,
            R.style.Widget_Sendbird_Feedback
        )

        try {
            val isDarkMode = SendbirdUIKit.isDarkMode()
            val goodFeedbackBackgroundResource = typedArray.getResourceId(
                R.styleable.Feedback_sb_feedback_good_background,
                if (isDarkMode) {
                    R.drawable.sb_feedback_background_dark
                } else {
                    R.drawable.sb_feedback_background_light
                }
            )

            val badFeedbackBackgroundResource = typedArray.getResourceId(
                R.styleable.Feedback_sb_feedback_bad_background,
                if (isDarkMode) {
                    R.drawable.sb_feedback_background_dark
                } else {
                    R.drawable.sb_feedback_background_light
                }
            )

            val goodFeedbackButtonResource = typedArray.getResourceId(
                R.styleable.Feedback_sb_feedback_good_button,
                if (isDarkMode) {
                    R.drawable.selector_feedback_good_button_dark
                } else {
                    R.drawable.selector_feedback_good_button_light
                }
            )

            val badFeedbackButtonResource = typedArray.getResourceId(
                R.styleable.Feedback_sb_feedback_bad_button,
                if (isDarkMode) {
                    R.drawable.selector_feedback_bad_button_dark
                } else {
                    R.drawable.selector_feedback_bad_button_light
                }
            )

            binding.goodFeedbackLayout.setBackgroundResource(goodFeedbackBackgroundResource)
            binding.badFeedbackLayout.setBackgroundResource(badFeedbackBackgroundResource)
            binding.ivGoodFeedback.setBackgroundResource(goodFeedbackButtonResource)
            binding.ivBadFeedback.setBackgroundResource(badFeedbackButtonResource)
        } finally {
            typedArray.recycle()
        }

        binding.goodFeedbackLayout.setOnClickListener {
            onFeedbackRatingClickListener?.invoke(FeedbackRating.Good)
        }

        binding.badFeedbackLayout.setOnClickListener {
            onFeedbackRatingClickListener?.invoke(FeedbackRating.Bad)
        }

        drawFeedback(null)
    }

    fun drawFeedback(feedback: Feedback?) {
        val (isGoodFeedbackEnabled, isGoodFeedbackSelected) = when (feedback?.rating) {
            null -> true to false // the feedback is submittable but not submitted state.
            FeedbackRating.Good -> true to true
            FeedbackRating.Bad -> false to false
        }

        val (isBadFeedbackEnabled, isBadFeedbackSelected) = when (feedback?.rating) {
            null -> true to false // the feedback is submittable but not submitted state.
            FeedbackRating.Good -> false to false
            FeedbackRating.Bad -> true to true
        }

        binding.goodFeedbackLayout.isEnabled = isGoodFeedbackEnabled
        binding.goodFeedbackLayout.isSelected = isGoodFeedbackSelected
        binding.ivGoodFeedback.isEnabled = isGoodFeedbackEnabled
        binding.ivGoodFeedback.isSelected = isGoodFeedbackSelected

        binding.badFeedbackLayout.isEnabled = isBadFeedbackEnabled
        binding.badFeedbackLayout.isSelected = isBadFeedbackSelected
        binding.ivBadFeedback.isEnabled = isBadFeedbackEnabled
        binding.ivBadFeedback.isSelected = isBadFeedbackSelected
    }
}
