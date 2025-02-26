package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewAdminMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.ui.messages.BaseMessageView
import com.jet.im.kit.model.message.FriendNotifyMessage
import com.jet.im.kit.model.message.GroupNotifyMessage
import com.juggle.im.JIM
import com.juggle.im.model.Message
import com.juggle.im.model.messages.RecallInfoMessage
import com.sendbird.android.message.BaseMessage

internal class AdminMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_admin_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewAdminMessageComponentBinding
    override val layout: View
        get() = binding.root

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_Admin, defStyle, 0)
        try {
            binding = SbViewAdminMessageComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val textAppearance = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_text_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            val backgroundResourceId = a.getResourceId(
                R.styleable.MessageView_Admin_sb_admin_message_background,
                android.R.color.transparent
            )
            binding.tvMessage.setAppearance(context, textAppearance)
            binding.tvMessage.setBackgroundResource(backgroundResourceId)
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(message: Message) {
        var text = ""
        val content = message.content
        if (content is FriendNotifyMessage) {
            text = friendNotifyText(message)
        } else if (content is GroupNotifyMessage) {
            text = content.description()
        } else if (content is RecallInfoMessage) {
            text = recallText(message)
        }
        binding.tvMessage.text = text
    }

    private fun recallText(message: Message): String {
        val tip: String
        if (message.direction == Message.MessageDirection.RECEIVE) {
            var userName = ""
            val senderId = message.senderUserId
            if (senderId != null) {
                userName = senderId
                val user = JIM.getInstance().userInfoManager.getUserInfo(senderId)
                if (user != null) {
                    userName = user.userName
                }
            }
            tip = "$userName 撤回了一条消息"
        } else {
            tip = "你 撤回了一条消息"
        }
        return tip
    }

    private fun friendNotifyText(message: Message): String {
        var tip = ""
        val content = message.content
        if (content is FriendNotifyMessage) {
            val opName = if (content.type == 0) "添加" else "通过"
            val userId = message.conversation.conversationId
            var userName = userId
            val user = JIM.getInstance().userInfoManager.getUserInfo(userId)
            if (user != null) {
                userName = user.userName
            }
            if (message.direction == Message.MessageDirection.SEND) {
                tip = "你" + opName + userName + "为好友"
            } else {
                tip = userName + opName + "你为好友"
            }
        }
        return tip
    }
}
