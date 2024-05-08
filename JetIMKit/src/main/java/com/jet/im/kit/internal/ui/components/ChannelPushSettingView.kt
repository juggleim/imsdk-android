package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewChannelPushSettingBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class ChannelPushSettingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewChannelPushSettingBinding
    var onPushOptionAllClickListener: OnClickListener? = null
    var onPushOptionMentionsOnlyClickListener: OnClickListener? = null

    fun notifyChannelPushOptionChanged(option: GroupChannel.PushTriggerOption) {
        binding.vgOptionContainer.visibility = VISIBLE
        when (option) {
            GroupChannel.PushTriggerOption.OFF -> {
                binding.scSwitch.isChecked = false
                binding.vgOptionContainer.visibility = GONE
            }
            GroupChannel.PushTriggerOption.ALL, GroupChannel.PushTriggerOption.DEFAULT -> {
                binding.scSwitch.isChecked = true
                binding.all.isChecked = true
                binding.mentionsOnly.isChecked = false
            }
            GroupChannel.PushTriggerOption.MENTION_ONLY -> {
                binding.scSwitch.isChecked = true
                binding.all.isChecked = false
                binding.mentionsOnly.isChecked = true
            }
        }
    }

    fun setTitle(title: CharSequence) {
        this.binding.tvTitle.text = title
    }

    fun setDescription(description: CharSequence) {
        this.binding.tvDescription.text = description
    }

    fun setOnSwitchButtonClickListener(listener: OnClickListener) {
        this.binding.scSwitch.setOnClickListener(listener)
    }

    private fun onSubmenuClicked(view: View) {
        val id = view.id
        if (id == R.id.vgOptionAll || id == R.id.all) {
            onPushOptionAllClickListener?.onClick(view)
        } else if (id == R.id.vgMentionsOnly || id == R.id.mentionsOnly) {
            onPushOptionMentionsOnlyClickListener?.onClick(view)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelPushSettings, defStyle, 0)
        try {
            binding = SbViewChannelPushSettingBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val background = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_settings_background,
                R.color.background_50
            )
            val optionBackground = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_settings_option_item_background,
                R.drawable.selector_rectangle_light
            )
            val titleAppearance = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_settings_title_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            val descriptionAppearance = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_settings_description_appearance,
                R.style.SendbirdBody3OnLight02
            )
            val optionAppearance = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_option_item_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val dividerColor = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_push_option_item_divider_color,
                R.color.onlight_04
            )
            val switchTrackTint = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_switch_track_tint,
                R.color.sb_switch_track_light
            )
            val switchThumbTint = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_switch_thumb_tint,
                R.color.sb_switch_thumb_light
            )
            val radioButtonBackground = a.getResourceId(
                R.styleable.ChannelPushSettings_sb_channel_radio_button_background,
                R.drawable.selector_radio_button_light
            )
            binding.rootView.setBackgroundResource(background)
            binding.tvTitle.setAppearance(context, titleAppearance)
            binding.tvDescription.setAppearance(context, descriptionAppearance)
            binding.tvOptionAll.setAppearance(context, optionAppearance)
            binding.all.setBackgroundResource(radioButtonBackground)
            binding.tvOptionMentionsOnly.setAppearance(context, optionAppearance)
            binding.mentionsOnly.setBackgroundResource(radioButtonBackground)
            binding.scSwitch.trackTintList = AppCompatResources.getColorStateList(context, switchTrackTint)
            binding.scSwitch.thumbTintList = AppCompatResources.getColorStateList(context, switchThumbTint)
            binding.vgOptionAll.setBackgroundResource(optionBackground)
            binding.vgMentionsOnly.setBackgroundResource(optionBackground)
            binding.divider1.setBackgroundResource(dividerColor)
            binding.divider2.setBackgroundResource(dividerColor)
            binding.divider3.setBackgroundResource(dividerColor)
            binding.all.setOnClickListener { view: View -> onSubmenuClicked(view) }
            binding.vgOptionAll.setOnClickListener { view: View -> onSubmenuClicked(view) }
            binding.mentionsOnly.setOnClickListener { view: View -> onSubmenuClicked(view) }
            binding.vgMentionsOnly.setOnClickListener { view: View -> onSubmenuClicked(view) }
            binding.scSwitch.setOnClickListener { view: View -> onSubmenuClicked(view) }
        } finally {
            a.recycle()
        }
    }
}
