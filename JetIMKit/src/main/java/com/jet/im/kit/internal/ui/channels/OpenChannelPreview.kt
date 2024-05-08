package com.jet.im.kit.internal.ui.channels

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.channel.OpenChannel
import com.jet.im.kit.R
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewOpenChannelListItemBinding
import com.jet.im.kit.internal.extensions.setAppearance

internal class OpenChannelPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_open_channel_preview
) : FrameLayout(context, attrs, defStyle) {

    private val binding: SbViewOpenChannelListItemBinding

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.OpenChannelPreview, defStyle, 0)
        try {
            binding = SbViewOpenChannelListItemBinding.inflate(LayoutInflater.from(getContext())).apply {
                val background = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_background,
                    R.drawable.selector_rectangle_light
                )
                val coverBg = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_cover_image_background,
                    R.drawable.sb_shape_circle_background_300
                )
                val titleAppearance = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_title_appearance,
                    R.style.SendbirdSubtitle1OnLight01
                )
                val participantsCountAppearance = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_participants_count_appearance,
                    R.style.SendbirdCaption1OnLight02
                )
                val participantsIcon = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_participants_icon,
                    R.drawable.icon_members
                )

                val participantsIconTint =
                    a.getColorStateList(R.styleable.OpenChannelPreview_sb_open_channel_preview_participants_icon_tint)
                val coverDefaultIcon = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_default_icon,
                    R.drawable.icon_channels
                )
                val coverDefaultIconTint =
                    a.getColorStateList(R.styleable.OpenChannelPreview_sb_open_channel_preview_default_icon_tint)
                val freezeIcon = a.getResourceId(
                    R.styleable.OpenChannelPreview_sb_open_channel_preview_frozen_icon,
                    R.drawable.icon_freeze
                )
                val freezeIconTint =
                    a.getColorStateList(R.styleable.OpenChannelPreview_sb_open_channel_preview_frozen_icon_tint)
                root.setBackgroundResource(background)
                tvTitle.setAppearance(context, titleAppearance)
                tvParticipants.setAppearance(context, participantsCountAppearance)
                ivParticipantsIcon.setImageResource(participantsIcon)
                if (participantsIconTint != null)
                    ivParticipantsIcon.imageTintList = participantsIconTint
                ivCoverIcon.setImageResource(coverDefaultIcon)
                if (coverDefaultIconTint != null) {
                    ivCoverIcon.imageTintList = coverDefaultIconTint
                }
                ivCoverImage.setBackgroundResource(coverBg)
                ivFrozenIcon.setImageResource(freezeIcon)
                if (freezeIconTint != null) {
                    ivFrozenIcon.imageTintList = freezeIconTint
                }
            }
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } finally {
            a.recycle()
        }
    }

    fun drawChannel(channel: OpenChannel) {
        binding.tvTitle.text = channel.name.trim().ifEmpty { StringSet.OPEN_CHANNEL }
        binding.tvParticipants.text = channel.participantCount.toString()
        binding.ivFrozenIcon.visibility = if (channel.isFrozen) VISIBLE else GONE
        if (channel.coverUrl.isNotEmpty()) {
            binding.ivCoverIcon.visibility = GONE
            Glide.with(context)
                .load(channel.coverUrl)
                .override(binding.ivCoverImage.width, binding.ivCoverImage.height)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivCoverImage)
        } else {
            binding.ivCoverImage.setImageDrawable(null)
            binding.ivCoverIcon.visibility = VISIBLE
        }
    }
}
