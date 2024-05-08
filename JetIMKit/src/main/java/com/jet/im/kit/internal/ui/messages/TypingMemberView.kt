
package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewTypingMemberComponentBinding
import com.jet.im.kit.utils.ViewUtils
import kotlin.math.min

internal const val MAX_TYPING_MEMBER_COUNT = 99

internal class TypingMemberView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewTypingMemberComponentBinding = SbViewTypingMemberComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    override val layout: View
        get() = binding.root

    var typingMemberViewType: TypingMemberViewType = TypingMemberViewType.MEMBER
        set(value) {
            when (value) {
                TypingMemberViewType.MEMBER -> binding.ivTypingMember.visibility = VISIBLE
                TypingMemberViewType.COUNTER -> binding.cvTypingMemberCount.visibility = VISIBLE
            }
            field = value
        }

    init {
        @DrawableRes val backgroundResourceId: Int
        @ColorRes val textColorResourceId: Int
        @ColorRes val textBackgroundResourceId: Int

        if (SendbirdUIKit.isDarkMode()) {
            backgroundResourceId = R.drawable.sb_typing_member_message_background_light
            textColorResourceId = R.color.ondark_02
            textBackgroundResourceId = R.color.background_400
        } else {
            backgroundResourceId = R.drawable.sb_typing_member_message_background_dark
            textColorResourceId = R.color.onlight_02
            textBackgroundResourceId = R.color.background_100
        }

        binding.root.setBackgroundResource(backgroundResourceId)
        binding.cvTypingMemberCount.setCardBackgroundColor(ContextCompat.getColor(context, textBackgroundResourceId))
        binding.tvTypingMemberCount.setTextColor(ContextCompat.getColor(context, textColorResourceId))
    }

    fun drawTypingMemberCount(memberCount: Int) {
        binding.tvTypingMemberCount.text = memberCount.toMemberCountText()
    }

    fun drawTypingMember(user: User) {
        ViewUtils.drawProfile(binding.ivTypingMember, user.profileUrl, user.plainProfileImageUrl)
    }
}

internal fun Int.toMemberCountText() = "+${min(this, MAX_TYPING_MEMBER_COUNT)}"

internal enum class TypingMemberViewType {
    MEMBER,
    COUNTER,
}
