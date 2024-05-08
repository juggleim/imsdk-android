package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewSuggestedMentionListItemBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.UserUtils
import com.jet.im.kit.utils.ViewUtils

internal class SuggestedMentionPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_suggested_mention_preview
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewSuggestedMentionListItemBinding
    val layout: View
        get() = this
    private var nicknameTextAppearance: Int
    private var emptyNicknameTextAppearance: Int

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.vgMemberItem.setOnClickListener(listener)
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        binding.vgMemberItem.setOnLongClickListener(listener)
    }

    fun setOnProfileClickListener(listener: OnClickListener?) {
        binding.ivProfile.setOnClickListener(listener)
    }

    fun setDescription(text: CharSequence?) {
        binding.tvDescription.text = text
    }

    fun setName(name: CharSequence?) {
        binding.tvNickname.text = name
    }

    private fun setImageFromUrl(url: String?, plainUrl: String?) {
        ViewUtils.drawProfile(binding.ivProfile, url, plainUrl)
    }

    fun drawUser(user: User, showUserId: Boolean) {
        val context = context
        val nickname = UserUtils.getDisplayName(getContext(), user)
        if (TextUtils.isEmpty(user.nickname)) {
            binding.tvNickname.setAppearance(context, emptyNicknameTextAppearance)
        } else {
            binding.tvNickname.setAppearance(context, nicknameTextAppearance)
        }
        setName(nickname)
        if (showUserId) {
            val description = user.userId
            setDescription(description)
        }
        setImageFromUrl(user.profileUrl, user.plainProfileImageUrl)
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UserPreview, defStyle, 0)
        try {
            binding = SbViewSuggestedMentionListItemBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val background = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_background,
                R.drawable.selector_rectangle_light
            )
            val descAppearance = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_description_appearance,
                R.style.SendbirdBody2OnLight02
            )
            nicknameTextAppearance = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_nickname_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            emptyNicknameTextAppearance = a.getResourceId(
                R.styleable.UserPreview_sb_mention_empty_nickname_appearance,
                R.style.SendbirdBody2OnLight03
            )
            binding.root.setBackgroundResource(background)
            binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
            binding.tvNickname.maxLines = 1
            binding.tvDescription.setAppearance(context, descAppearance)
        } finally {
            a.recycle()
        }
    }
}
