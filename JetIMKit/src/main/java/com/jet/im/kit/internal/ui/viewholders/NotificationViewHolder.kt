package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.internal.model.notifications.NotificationConfig

internal abstract class NotificationViewHolder internal constructor(
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?)
}
