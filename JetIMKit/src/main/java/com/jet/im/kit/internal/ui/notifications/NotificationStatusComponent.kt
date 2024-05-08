package com.jet.im.kit.internal.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.FeedChannel
import com.jet.im.kit.R
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.modules.components.StatusComponent

@JvmSuppressWildcards
internal class NotificationStatusComponent(
    private val uiConfig: NotificationConfig? = null
) : StatusComponent() {

    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        val layout = super.onCreateView(context, inflater, parent, args)
        uiConfig?.let {
            val themeMode = it.themeMode
            it.theme.listTheme.apply {
                layout.setBackgroundColor(backgroundColor.getColor(themeMode))
            }
        }
        return layout
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * since 3.8.0
     */
    fun notifyChannelChanged(channel: BaseChannel) {
        if (channel is FeedChannel && channel.isCategoryFilterEnabled) {
            rootView?.let {
                val frameLayout: View = it
                val layoutParams = frameLayout.layoutParams as ViewGroup.MarginLayoutParams
                val marginTop = it.resources.getDimensionPixelSize(R.dimen.sb_size_62)
                layoutParams.topMargin = marginTop
                frameLayout.layoutParams = layoutParams
            }
        }
    }
}
