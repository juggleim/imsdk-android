package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message

internal abstract class GroupChannelMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    abstract fun drawMessage(
        channel: GroupChannel,
        message: BaseMessage,
        params: MessageListUIParams
    )

    abstract fun drawMessage(
        channel: ConversationInfo,
        message: Message,
        params: MessageListUIParams
    )
}
