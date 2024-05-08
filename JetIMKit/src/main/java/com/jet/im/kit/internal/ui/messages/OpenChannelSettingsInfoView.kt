package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.channel.OpenChannel
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewOpenChannelSettingsInfoBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.ChannelUtils
import com.jet.im.kit.utils.TextUtils

internal class OpenChannelSettingsInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val binding: SbViewOpenChannelSettingsInfoBinding
    val layout: OpenChannelSettingsInfoView
        get() = this

    fun drawOpenChannelSettingsInfoView(channel: OpenChannel?) {
        if (channel == null) return
        val channelName = channel.name
        binding.tvChannelName.text = if (TextUtils.isEmpty(channelName)) "" else channelName
        ChannelUtils.loadChannelCover(binding.ccvChannelImage, channel)
        binding.tvInformationContent.text = channel.url
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelSettings, defStyleAttr, 0)
        try {
            binding = SbViewOpenChannelSettingsInfoBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background =
                a.getResourceId(R.styleable.ChannelSettings_sb_channel_settings_background, R.color.background_50)
            val nameAppearance = a.getResourceId(
                R.styleable.ChannelSettings_sb_channel_settings_name_appearance,
                R.style.SendbirdSubtitle1OnLight01
            )
            val infoTitleAppearance = a.getResourceId(
                R.styleable.ChannelSettings_sb_channel_settings_description_appearance,
                R.style.SendbirdBody2OnLight02
            )
            val infoContentAppearance = a.getResourceId(
                R.styleable.ChannelSettings_sb_channel_settings_description_appearance,
                R.style.SendbirdBody2OnLight02
            )
            val divider =
                if (SendbirdUIKit.isDarkMode()) R.drawable.sb_line_divider_dark else R.drawable.sb_line_divider_light
            setBackgroundResource(background)
            binding.tvChannelName.setAppearance(context, nameAppearance)
            // letterSpacing should be 0 to use ellipsize as TextUtils.TruncateAt.MIDDLE
            binding.tvChannelName.letterSpacing = 0f
            binding.tvChannelName.ellipsize = android.text.TextUtils.TruncateAt.MIDDLE
            binding.tvChannelName.isSingleLine = true
            binding.tvInformationTitle.setAppearance(context, infoTitleAppearance)
            binding.tvInformationContent.setAppearance(context, infoContentAppearance)
            binding.divider.setBackgroundResource(divider)
            binding.divider1.setBackgroundResource(divider)
        } finally {
            a.recycle()
        }
    }
}
