package com.jet.im.kit.internal.ui.reactions

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
import com.sendbird.android.SendbirdChat
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewEmojiReactionUserComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.UserUtils
import com.juggle.im.model.UserInfo

internal class EmojiReactionUserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewEmojiReactionUserComponentBinding = SbViewEmojiReactionUserComponentBinding.inflate(LayoutInflater.from(getContext()))
    val layout: View
        get() = this
    var onProfileClickListener: OnClickListener? = null

    init {
        addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val nicknameAppearance = if (SendbirdUIKit.isDarkMode()) R.style.SendbirdSubtitle2OnDark01 else R.style.SendbirdSubtitle2OnLight01
        binding.tvNickname.setAppearance(context, nicknameAppearance)
        binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
        binding.tvNickname.maxLines = 1
        binding.ivUserCover.setOnClickListener {
            onProfileClickListener?.onClick(it)
        }
    }

    fun drawUser(user: UserInfo?) {
        val context = binding.ivUserCover.context
        val nickname = UserUtils.getDisplayName(context, user)
        val urls: MutableList<String> = ArrayList()
        binding.tvNickname.text = nickname
        user?.let {
            urls.add(user.portrait)
            binding.ivUserCover.loadImages(urls)
            if (user.userId == SendbirdChat.currentUser?.userId) {
                val meBadge = context.resources.getString(R.string.sb_text_user_list_badge_me)
                val spannable: Spannable = SpannableString(meBadge)
                val badgeAppearance =
                    if (SendbirdUIKit.isDarkMode()) R.style.SendbirdSubtitle2OnDark02 else R.style.SendbirdSubtitle2OnLight02
                spannable.setSpan(
                    TextAppearanceSpan(context, badgeAppearance),
                    0,
                    meBadge.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvNickname.append(spannable)
            }
        }
    }
}
