package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.user.User
import com.jet.im.kit.databinding.SbViewTypingIndicatorMessageComponentBinding

internal const val TYPING_INDICATOR_MEMBER_VIEW_COUNT = 3
internal const val TYPING_INDICATOR_MEMBER_COUNT_VIEW_INDEX = 3

internal class TypingIndicatorMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    private val typingMemberViews: List<TypingMemberView>

    override val binding: SbViewTypingIndicatorMessageComponentBinding = SbViewTypingIndicatorMessageComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )

    override val layout: View
        get() = binding.root

    init {
        typingMemberViews = listOf(
            binding.typingUserView1.apply { typingMemberViewType = TypingMemberViewType.MEMBER },
            binding.typingUserView2.apply { typingMemberViewType = TypingMemberViewType.MEMBER },
            binding.typingUserView3.apply { typingMemberViewType = TypingMemberViewType.MEMBER },
            binding.typingUserView4.apply { typingMemberViewType = TypingMemberViewType.COUNTER },
        )
    }

    @Synchronized
    fun updateTypingMembers(typingMembers: List<User>) {
        for (index: Int in 0 until TYPING_INDICATOR_MEMBER_VIEW_COUNT) {
            updateTypingMemberView(index, typingMembers.getOrNull(index))
        }
        updateTypingMemberCountView(typingMembers.size)
    }

    private fun updateTypingMemberView(index: Int, user: User?) {
        val typingMemberView = typingMemberViews.getOrNull(index) ?: return
        if (user != null) {
            typingMemberView.drawTypingMember(user)
            typingMemberView.visibility = VISIBLE
        } else {
            typingMemberView.visibility = GONE
        }
    }

    private fun updateTypingMemberCountView(size: Int) {
        val typingMemberView = typingMemberViews[TYPING_INDICATOR_MEMBER_COUNT_VIEW_INDEX]
        if (size > TYPING_INDICATOR_MEMBER_VIEW_COUNT) {
            typingMemberView.drawTypingMemberCount(size - TYPING_INDICATOR_MEMBER_VIEW_COUNT)
            typingMemberView.visibility = VISIBLE
        } else {
            typingMemberView.visibility = GONE
        }
    }
}
