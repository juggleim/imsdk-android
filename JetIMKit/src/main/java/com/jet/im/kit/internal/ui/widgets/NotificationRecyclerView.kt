package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintSet
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewChatNotificationRecyclerViewBinding
import com.jet.im.kit.internal.extensions.addRipple
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.utils.NotificationViewedTracker
import com.jet.im.kit.utils.SoftInputUtils

internal class NotificationRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private var categoryMenuTextColor: Int
    private var categoryMenuTextAppearance: Int
    private var categoryMenuBackground: Int
    private val binding: SbViewChatNotificationRecyclerViewBinding
    private val notificationScrollViewedTracker by lazy {
        NotificationViewedTracker(recyclerView).apply {
            onNotificationViewedDetected = {
                recyclerView.layoutManager?.let {
                    val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = it.findLastVisibleItemPosition()
                    onNotificationViewedDetectedListener?.invoke(firstVisibleItemPosition, lastVisibleItemPosition)
                }
            }
        }
    }
    var onNotificationViewedDetectedListener: ((firstVisibleItemPosition: Int, lastVisibleItemPosition: Int) -> Unit)? = null
    val recyclerView: PagerRecyclerView
        get() = binding.rvMessageList
    val isReverseLayout
        get() = recyclerView.isReverseLayout()
    val categoryFilterBox: RadioGroup
        get() = binding.categoryMenuBox

    override fun setBackground(background: Drawable?) {
        recyclerView.background = background
    }

    fun showTooltip(text: String) {
        binding.vgTooltipBox.visibility = VISIBLE
        binding.tooltip.text = text
        movePosition()
    }

    fun hideTooltip() {
        binding.vgTooltipBox.visibility = GONE
    }

    fun setTooltipTextColor(@ColorInt color: Int) {
        binding.tooltip.setTextColor(color)
    }

    fun setTooltipBackgroundColor(@ColorInt color: Int) {
        binding.vgTooltipBox.setBackgroundColor(color)
    }

    fun setTooltipTextSize(textSize: Int) {
        binding.tooltip.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
    }

    fun setTooltipTextStyle(textStyle: Int) {
        binding.tooltip.setTypeface(textStyle)
    }

    fun setOnTooltipClickListener(onTooltipClickListener: OnClickListener?) {
        binding.vgTooltipBox.setOnClickListener(onTooltipClickListener)
    }

    fun enableCategoryFilterView(enable: Boolean) {
        binding.svCategoryBox.visibility = if (enable) VISIBLE else GONE
    }

    fun setCategoryFilterBoxBackgroundColor(@ColorInt color: Int) {
        binding.svCategoryBox.setBackgroundColor(color)
    }

    fun createCategoryFilterItemView(): CompoundButton {
        return RadioButton(context).apply {
            setTextColor(ColorStateList.valueOf(categoryMenuTextColor))
            setAppearance(context, categoryMenuTextAppearance)
            setBackgroundResource(categoryMenuBackground)
            buttonDrawable = null
            setPadding(
                resources.intToDp(12),
                resources.intToDp(7),
                resources.intToDp(12),
                resources.intToDp(7)
            )

            layoutParams = RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                resources.getDimensionPixelSize(R.dimen.sb_size_30)
            ).apply {
                marginEnd = resources.intToDp(8)
            }
        }
    }

    private fun movePosition() {
        val reverse = recyclerView.isReverseLayout()
        val set = ConstraintSet()
        val rootView = binding.root
        set.clone(rootView)
        if (reverse) {
            set.clear(binding.vgTooltipBox.id, ConstraintSet.TOP)
            set.connect(
                binding.vgTooltipBox.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
        } else {
            set.clear(binding.vgTooltipBox.id, ConstraintSet.BOTTOM)
            set.connect(
                binding.vgTooltipBox.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
        }
        set.applyTo(rootView)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        notificationScrollViewedTracker.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        notificationScrollViewedTracker.stop()
    }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NotificationListView,
            defStyle,
            0
        )
        try {
            binding = SbViewChatNotificationRecyclerViewBinding.inflate(
                LayoutInflater.from(getContext()),
                this,
                true
            )
            val recyclerViewBackground =
                a.getResourceId(
                    R.styleable.NotificationListView_sb_notification_recyclerview_background,
                    R.color.background_50
                )
            val tooltipBackground = a.getResourceId(
                R.styleable.NotificationListView_sb_notification_recyclerview_tooltip_background,
                R.drawable.selector_tooltip_background_light
            )
            val tooltipTextAppearance = a.getResourceId(
                R.styleable.NotificationListView_sb_notification_recyclerview_tooltip_text_appearance,
                R.style.SendbirdBody2OnDark01
            )

            categoryMenuBackground = a.getResourceId(
                R.styleable.NotificationListView_sb_category_filter_background,
                R.drawable.selector_category_filter_menu_light
            )
            categoryMenuTextAppearance = a.getResourceId(
                R.styleable.NotificationListView_sb_category_filter_text_appearance,
                R.style.SendbirdCaption2OnLight01
            )
            categoryMenuTextColor = a.getResourceId(
                R.styleable.NotificationListView_sb_category_filter_text_color,
                R.drawable.selector_category_filter_menu_text_color_light
            )
            setBackgroundResource(android.R.color.transparent)
            binding.rvMessageList.setOnTouchListener { v: View, _: MotionEvent? ->
                SoftInputUtils.hideSoftKeyboard(this)
                v.performClick()
                false
            }
            binding.svCategoryBox.setBackgroundResource(recyclerViewBackground)
            binding.rvMessageList.setBackgroundResource(recyclerViewBackground)
            binding.rvMessageList.setUseDivider(false)
            binding.vgTooltipBox.radius = resources.intToDp(19).toFloat()
            binding.vgTooltipBox.setBackgroundResource(tooltipBackground)
            binding.vgTooltipBox.addRipple()
            binding.tooltip.setAppearance(context, tooltipTextAppearance)
        } finally {
            a.recycle()
        }
    }
}
