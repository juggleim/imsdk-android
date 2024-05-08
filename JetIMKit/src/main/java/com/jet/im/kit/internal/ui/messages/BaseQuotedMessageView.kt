package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.TextUIConfig

abstract class BaseQuotedMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    abstract val layout: View
    abstract val binding: ViewBinding
    abstract fun drawQuotedMessage(
        channel: GroupChannel,
        message: BaseMessage,
        textUIConfig: TextUIConfig?,
        messageListUIParams: MessageListUIParams
    )
}
