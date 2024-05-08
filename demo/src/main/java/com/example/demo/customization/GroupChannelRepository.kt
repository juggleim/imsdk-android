package com.example.demo.customization

import android.app.Activity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.GroupChannelListQueryParams
import com.example.demo.common.widgets.WaitingDialog
import com.jet.im.kit.utils.ContextUtils
import java.util.concurrent.Executors

internal object GroupChannelRepository {
    private val worker = Executors.newSingleThreadExecutor()
    private var channelCache = mutableListOf<GroupChannel>()

    fun getRandomChannel(activity: Activity, callback: (GroupChannel) -> Unit) {
        if (channelCache.isNotEmpty()) {
            callback(channelCache.random())
            return
        }
        WaitingDialog.show(activity)
        worker.submit {
            GroupChannel.createMyGroupChannelListQuery(GroupChannelListQueryParams()).next { channels, e ->
                WaitingDialog.dismiss()
                if (e != null || channels.isNullOrEmpty()) {
                    ContextUtils.toastError(activity, "No channels")
                    return@next
                }
                channelCache.addAll(channels)
                activity.runOnUiThread { callback(channelCache.random()) }
            }
        }
    }
}
