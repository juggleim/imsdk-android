package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.SendbirdChat
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewUserProfileBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.setAppearance

internal class UserProfile @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewUserProfileBinding
    var onItemClickListener: OnItemClickListener<User>? = null

    fun drawUserProfile(user: User) {
        binding.profileView.loadImage(user.profileUrl)
        binding.tvName.text = user.nickname
        binding.tvUserId.text = user.userId
        setUseChannelCreateButton(isMe(user.userId))
        binding.btCreateChannel.setOnClickListener {
            onItemClickListener?.onItemClick(binding.btCreateChannel, 0, user)
        }
    }

    private fun isMe(userId: String): Boolean = SendbirdChat.currentUser?.userId == userId

    fun setUseChannelCreateButton(channelCreatable: Boolean) {
        binding.btCreateChannel.visibility = if (!channelCreatable) GONE else VISIBLE
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UserProfile, defStyleAttr, 0)
        try {
            binding = SbViewUserProfileBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val background = a.getResourceId(R.styleable.UserProfile_sb_user_profile_background, R.color.background_50)
            val userNameAppearance = a.getResourceId(
                R.styleable.UserProfile_sb_user_profile_user_name_text_appearance,
                R.style.SendbirdH1OnLight01
            )
            val singleMessageButtonBg = a.getResourceId(
                R.styleable.UserProfile_sb_user_profile_button_background,
                R.drawable.selector_button_default_light
            )
            val singleMessageTextAppearance = a.getResourceId(
                R.styleable.UserProfile_sb_user_profile_button_text_appearance,
                R.style.SendbirdButtonOnLight01
            )
            val dividerColor =
                a.getResourceId(R.styleable.UserProfile_sb_user_profile_divider_color, R.color.onlight_04)
            val infoTitleTextAppearance = a.getResourceId(
                R.styleable.UserProfile_sb_user_profile_information_title_text_appearance,
                R.style.SendbirdBody2OnLight02
            )
            val infoContentTextAppearance = a.getResourceId(
                R.styleable.UserProfile_sb_user_profile_information_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            binding.parent.setBackgroundResource(background)
            binding.tvName.setAppearance(context, userNameAppearance)
            binding.btCreateChannel.setBackgroundResource(singleMessageButtonBg)
            binding.btCreateChannel.setAppearance(context, singleMessageTextAppearance)
            binding.ivDivider.setBackgroundResource(dividerColor)
            binding.tvTitleUserId.setAppearance(context, infoTitleTextAppearance)
            binding.tvUserId.setAppearance(context, infoContentTextAppearance)
        } finally {
            a.recycle()
        }
    }
}
