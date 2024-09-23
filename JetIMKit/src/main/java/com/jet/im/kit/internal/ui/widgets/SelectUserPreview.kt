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
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.CompoundButtonCompat
import com.sendbird.android.SendbirdChat
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewUserListItemBinding
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.ChannelUtils
import com.jet.im.kit.utils.UserUtils

internal class SelectUserPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_select_user_preview
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewUserListItemBinding
    val layout: View
        get() = this
    var onSelectedStateChangedListener: CompoundButton.OnCheckedChangeListener? = null
    var onItemClickListener: OnClickListener? = null
    var onItemLongClickListener: OnLongClickListener? = null

    override fun isSelected(): Boolean = binding.cbUserPreview.isChecked
    fun setUserSelected(isSelected: Boolean) {
        binding.cbUserPreview.isChecked = isSelected
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.vgUserItem.isEnabled = enabled
        binding.cbUserPreview.isEnabled = enabled
        binding.tvNickname.isEnabled = enabled
    }

    fun drawUser(userInfo: UserInfo, isSelected: Boolean, isEnabled: Boolean) {
        val nickname = UserUtils.getDisplayName(context, userInfo)
        binding.tvNickname.text = nickname
        ChannelUtils.loadImage(binding.ivUserCover, userInfo.profileUrl)
        if (userInfo.userId == SendbirdChat.currentUser?.userId) {
            val meBadge = resources.getString(R.string.sb_text_user_list_badge_me)
            val spannable: Spannable = SpannableString(meBadge)
            val badgeAppearance =
                if (SendbirdUIKit.isDarkMode()) R.style.SendbirdSubtitle2OnDark02 else R.style.SendbirdSubtitle2OnLight02
            spannable.setSpan(
                TextAppearanceSpan(context, badgeAppearance),
                0, meBadge.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvNickname.append(spannable)
        }
        setUserSelected(isSelected)
        setEnabled(isEnabled)
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SelectUserPreview, defStyle, 0)
        try {
            binding = SbViewUserListItemBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val background = a.getResourceId(
                R.styleable.SelectUserPreview_sb_select_user_preview_background,
                R.drawable.selector_rectangle_light
            )
            val nicknameAppearance = a.getResourceId(
                R.styleable.SelectUserPreview_sb_select_user_preview_nickname_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            binding.root.setBackgroundResource(background)
            binding.cbUserPreview.visibility = VISIBLE
            binding.tvNickname.setAppearance(context, nicknameAppearance)
            binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
            binding.tvNickname.maxLines = 1
            val checkBoxTint =
                if (SendbirdUIKit.isDarkMode()) R.color.sb_checkbox_tint_dark else R.color.sb_checkbox_tint_light
            CompoundButtonCompat.setButtonTintList(
                binding.cbUserPreview,
                AppCompatResources.getColorStateList(context, checkBoxTint)
            )
            binding.vgUserItem.setOnClickListener { v: View? ->
                binding.cbUserPreview.toggle()
                onItemClickListener?.onClick(v)
                onSelectedStateChangedListener?.onCheckedChanged(binding.cbUserPreview, !isSelected)
            }
            binding.cbUserPreview.setOnClickListener { v: View? ->
                onItemClickListener?.onClick(v)
                onSelectedStateChangedListener?.onCheckedChanged(binding.cbUserPreview, !isSelected)
            }
            binding.vgUserItem.setOnLongClickListener { v: View? ->
                onItemLongClickListener?.onLongClick(v)
                false
            }
        } finally {
            a.recycle()
        }
    }
}
