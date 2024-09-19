package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewMemberListItemBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.UserUtils
import com.jet.im.kit.utils.ViewUtils

internal class UserPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_user_preview
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewMemberListItemBinding
    val layout: View
        get() = this

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.vgMemberItem.setOnClickListener(listener)
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        binding.vgMemberItem.setOnLongClickListener(listener)
    }

    fun useActionMenu(use: Boolean) {
        binding.ivAction.visibility = if (use) VISIBLE else GONE
    }

    fun setOnActionMenuClickListener(listener: OnClickListener?) {
        binding.ivAction.setOnClickListener(listener)
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

    fun setImageFromUrl(url: String?, plainUrl: String?) {
        ViewUtils.drawProfile(binding.ivProfile, url, plainUrl)
    }

    fun setVisibleOverlay(visibility: Int) {
        binding.ivProfileOverlay.visibility = visibility
    }

    fun enableActionMenu(enabled: Boolean) {
        binding.ivAction.isEnabled = enabled
    }

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun drawUser(preview: UserPreview, user: User, description: String, isMuted: Boolean) {
            val context = preview.context
            val isMe = user.userId == SendbirdUIKit.getAdapter()?.userInfo?.userId
            val nickname = UserUtils.getDisplayName(context, user)
            preview.setName(nickname)
            preview.setDescription(description)
            preview.setImageFromUrl(user.profileUrl, user.plainProfileImageUrl)
            preview.enableActionMenu(!isMe)
            preview.setVisibleOverlay(if (isMuted) VISIBLE else GONE)
            if (isMe) {
                val meBadge = nickname + context.resources.getString(R.string.sb_text_user_list_badge_me)
                val spannable: Spannable = SpannableString(meBadge)
                val badgeAppearance =
                    if (SendbirdUIKit.isDarkMode()) R.style.SendbirdSubtitle2OnDark02 else R.style.SendbirdSubtitle2OnLight02
                val originLen = nickname.length
                spannable.setSpan(
                    TextAppearanceSpan(context, badgeAppearance),
                    originLen, meBadge.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                preview.setName(spannable)
            }
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UserPreview, defStyle, 0)
        try {
            binding = SbViewMemberListItemBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val background = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_background,
                R.drawable.selector_rectangle_light
            )
            val nicknameAppearance = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_nickname_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            val descAppearance = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_description_appearance,
                R.style.SendbirdBody2OnLight02
            )
            val actionMenuBgResId = a.getResourceId(
                R.styleable.UserPreview_sb_member_preview_action_menu_background,
                R.drawable.sb_button_uncontained_background_light
            )
            binding.root.setBackgroundResource(background)
            binding.tvNickname.setAppearance(context, nicknameAppearance)
            binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
            binding.tvNickname.maxLines = 1
            binding.tvDescription.setAppearance(context, descAppearance)
            binding.ivAction.setBackgroundResource(actionMenuBgResId)
            val moreTint =
                if (SendbirdUIKit.isDarkMode()) R.color.sb_selector_icon_more_color_dark else R.color.sb_selector_icon_more_color_light
            binding.ivAction.setImageDrawable(
                DrawableUtils.setTintList(
                    binding.ivAction.drawable,
                    AppCompatResources.getColorStateList(context, moreTint)
                )
            )
            val muteDrawable = DrawableUtils.createOvalIcon(
                context,
                SendbirdUIKit.getDefaultThemeMode().primaryTintResId,
                255 / 2,
                R.drawable.icon_mute,
                R.color.background_50
            )
            binding.ivProfileOverlay.setImageDrawable(muteDrawable)
        } finally {
            a.recycle()
        }
    }
}
