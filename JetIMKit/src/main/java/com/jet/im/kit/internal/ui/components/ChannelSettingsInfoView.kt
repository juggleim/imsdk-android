package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewChannelSettingsInfoBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.ChannelUtils

internal class ChannelSettingsInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewChannelSettingsInfoBinding
    val layout: ChannelSettingsInfoView
        get() = this

    fun drawChannelSettingsInfoView(channel: GroupChannel?) {
        channel?.let {
            binding.tvChannelName.text = ChannelUtils.makeTitleText(context, channel)
            ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyle, 0)
        try {
            binding = SbViewChannelSettingsInfoBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background =
                a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50)
            val nameAppearance = a.getResourceId(
                R.styleable.ChannelSettings_sb_channel_settings_name_appearance,
                R.style.SendbirdSubtitle1OnLight01
            )
            val divider =
                if (SendbirdUIKit.isDarkMode()) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light
            setBackgroundResource(background)
            binding.tvChannelName.setAppearance(context, nameAppearance)
            // letterSpacing should be 0 to use ellipsize as TextUtils.TruncateAt.MIDDLE
            binding.tvChannelName.letterSpacing = 0f
            binding.tvChannelName.ellipsize = TextUtils.TruncateAt.MIDDLE
            binding.tvChannelName.isSingleLine = true
            binding.divider.setBackgroundResource(divider)
        } finally {
            a.recycle()
        }
    }
}
