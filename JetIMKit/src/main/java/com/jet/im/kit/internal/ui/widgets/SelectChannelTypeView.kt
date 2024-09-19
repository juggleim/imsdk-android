package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.jet.im.kit.R
import com.jet.im.kit.consts.CreatableChannelType
import com.jet.im.kit.databinding.SbViewSelectChannelTypeBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils

internal class SelectChannelTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_select_channel_type
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewSelectChannelTypeBinding
    var onItemClickListener: OnItemClickListener<CreatableChannelType>? = null
    fun canCreateSuperGroupChannel(canCreateSuperGroupChannel: Boolean) {
        binding.vgSuperGroup.visibility = if (canCreateSuperGroupChannel) VISIBLE else GONE
    }

    fun canCreateBroadcastGroupChannel(canCreateBroadcastGroupChannel: Boolean) {
        binding.vgBroadcast.visibility = if (canCreateBroadcastGroupChannel) VISIBLE else GONE
    }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SelectChannelTypeView,
            defStyleAttr,
            0
        )
        try {
            binding = SbViewSelectChannelTypeBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background = a.getResourceId(
                R.styleable.SelectChannelTypeView_sb_select_channel_type_background,
                R.color.background_50
            )
            val titleTextAppearance = a.getResourceId(
                R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_title_appearance,
                R.style.SendbirdH1OnLight01
            )
            val menuBackgroundRes = a.getResourceId(
                R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val nameTextAppearance = a.getResourceId(
                R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_name_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val iconTint = a.getColorStateList(R.styleable.SelectChannelTypeView_sb_select_channel_type_menu_icon_tint)
            binding.getRoot().setBackgroundResource(background)
            binding.tvTitle.setAppearance(context, titleTextAppearance)
            binding.vgGroup.setBackgroundResource(menuBackgroundRes)
            binding.vgBroadcast.setBackgroundResource(menuBackgroundRes)
            binding.vgSuperGroup.setBackgroundResource(menuBackgroundRes)
            binding.tvMenuGroupChat.setAppearance(context, nameTextAppearance)
            binding.tvMenuSuperGroupChat.setAppearance(context, nameTextAppearance)
            binding.tvMenuBroadcastChant.setAppearance(context, nameTextAppearance)
            if (iconTint != null) {
                binding.ivIconGroup.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_chat, iconTint))
                binding.ivIconSuperGroup.setImageDrawable(
                    DrawableUtils.setTintList(
                        context,
                        R.drawable.icon_supergroup,
                        iconTint
                    )
                )
                binding.ivIconBroadcast.setImageDrawable(
                    DrawableUtils.setTintList(
                        context,
                        R.drawable.icon_broadcast,
                        iconTint
                    )
                )
            } else {
                binding.ivIconGroup.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.icon_chat))
                binding.ivIconSuperGroup.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.icon_supergroup
                    )
                )
                binding.ivIconBroadcast.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.icon_broadcast
                    )
                )
            }
            binding.vgGroup.setOnClickListener {
                onItemClickListener?.onItemClick(binding.vgGroup, 0, CreatableChannelType.Normal)
            }
            binding.vgSuperGroup.setOnClickListener {
                onItemClickListener?.onItemClick(binding.vgSuperGroup, 1, CreatableChannelType.Super)
            }
            binding.vgBroadcast.setOnClickListener {
                onItemClickListener?.onItemClick(binding.vgBroadcast, 2, CreatableChannelType.Broadcast)
            }
        } finally {
            a.recycle()
        }
    }
}
