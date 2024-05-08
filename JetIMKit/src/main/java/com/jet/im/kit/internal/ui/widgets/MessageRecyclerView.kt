package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewMessageRecyclerViewBinding
import com.jet.im.kit.interfaces.OnConsumableClickListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.SoftInputUtils
import com.jet.im.kit.utils.TextUtils

internal class MessageRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding: SbViewMessageRecyclerViewBinding
    fun showTypingIndicator(text: String) {
        binding.tvTypingIndicator.visibility = VISIBLE
        binding.tvTypingIndicator.text = text
    }

    fun hideTypingIndicator() {
        binding.tvTypingIndicator.visibility = GONE
    }

    fun showNewMessageTooltip(text: String) {
        binding.vgTooltipBox.visibility = VISIBLE
        binding.tvTooltipText.text = text
    }

    fun showScrollFirstButton() {
        binding.ivScrollFirstIcon.visibility = VISIBLE
    }

    fun hideScrollFirstButton() {
        binding.ivScrollFirstIcon.visibility = GONE
    }

    fun hideNewMessageTooltip() {
        binding.vgTooltipBox.visibility = GONE
    }

    fun rotateScrollFirstButton(rotation: Float) {
        scrollFirstView.rotation = rotation
    }

    val layout: View
        get() = binding.root
    val recyclerView: PagerRecyclerView
        get() = binding.rvMessageList
    val tooltipView: View
        get() = binding.tvTooltipText
    val scrollFirstView: View
        get() = binding.ivScrollFirstIcon
    val typingIndicator: View
        get() = binding.tvTypingIndicator
    val bannerView: View
        get() = binding.tvBanner
    var onScrollFirstButtonClickListener: OnConsumableClickListener? = null

    fun setBannerText(text: String?) {
        binding.tvBanner.visibility = if (TextUtils.isEmpty(text)) GONE else VISIBLE
        binding.tvBanner.text = text
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageListView, defStyle, 0)
        try {
            binding = SbViewMessageRecyclerViewBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val dividerColor = a.getColor(
                R.styleable.MessageListView_sb_recycler_view_divide_line_color,
                ContextCompat.getColor(context, android.R.color.transparent)
            )
            val dividerHeight = a.getDimension(R.styleable.MessageListView_sb_recycler_view_divide_line_height, 0f)
            val recyclerViewBackground =
                a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_background, R.color.background_50)
            val tooltipBackground = a.getResourceId(
                R.styleable.MessageListView_sb_message_recyclerview_tooltip_background,
                R.drawable.selector_tooltip_background_light
            )
            val tooltipTextAppearance = a.getResourceId(
                R.styleable.MessageListView_sb_message_recyclerview_tooltip_textappearance,
                R.style.SendbirdCaption1Primary300
            )
            val typingIndicatorTextAppearance = a.getResourceId(
                R.styleable.MessageListView_sb_message_typing_indicator_textappearance,
                R.style.SendbirdCaption1OnLight02
            )
            val bannerBackground = a.getResourceId(
                R.styleable.MessageListView_sb_message_recyclerview_banner_background,
                R.drawable.sb_shape_channel_information_bg
            )
            val bannerTextAppearance = a.getResourceId(
                R.styleable.MessageListView_sb_message_recyclerview_banner_textappearance,
                R.style.SendbirdCaption2OnLight01
            )
            val scrollBottomBackground = a.getResourceId(
                R.styleable.MessageListView_sb_message_scroll_bottom_background,
                R.drawable.selector_scroll_bottom_light
            )
            val scrollBottomIcon =
                a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_icon, R.drawable.icon_chevron_down)
            val scrollBottomTintColor =
                a.getColorStateList(R.styleable.MessageListView_sb_message_scroll_bottom_icon_tint)
            setBackgroundResource(android.R.color.transparent)
            binding.rvMessageList.setBackgroundResource(recyclerViewBackground)
            binding.rvMessageList.setOnTouchListener { v: View, _: MotionEvent? ->
                SoftInputUtils.hideSoftKeyboard(this)
                v.performClick()
                false
            }
            binding.rvMessageList.setUseDivider(false)
            binding.rvMessageList.setDividerColor(dividerColor)
            binding.rvMessageList.setDividerHeight(dividerHeight)
            binding.tvTooltipText.setBackgroundResource(tooltipBackground)
            binding.tvTooltipText.setAppearance(context, tooltipTextAppearance)
            binding.tvTypingIndicator.setAppearance(context, typingIndicatorTextAppearance)
            binding.ivScrollFirstIcon.setBackgroundResource(scrollBottomBackground)
            binding.ivScrollFirstIcon.setImageResource(scrollBottomIcon)
            binding.ivScrollFirstIcon.imageTintList = scrollBottomTintColor
            binding.ivScrollFirstIcon.setOnClickListener {
                if (onScrollFirstButtonClickListener?.onClick(it) == true) return@setOnClickListener
                recyclerView.stopScroll()
                recyclerView.scrollToPosition(0)
            }
            binding.tvBanner.setBackgroundResource(bannerBackground)
            binding.tvBanner.setAppearance(context, bannerTextAppearance)
        } finally {
            a.recycle()
        }
    }
}
