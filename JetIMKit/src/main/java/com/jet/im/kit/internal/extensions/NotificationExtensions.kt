package com.jet.im.kit.internal.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sendbird.android.params.MessageListParams
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.singleton.NotificationChannelManager.checkAndInit
import com.jet.im.kit.internal.singleton.NotificationChannelManager.getGlobalNotificationChannelSettings
import com.jet.im.kit.internal.ui.notifications.ChatNotificationChannelModule
import com.jet.im.kit.internal.ui.notifications.FeedNotificationChannelModule
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.providers.ViewModelProviders
import com.jet.im.kit.vm.ChatNotificationChannelViewModel
import com.jet.im.kit.vm.FeedNotificationChannelViewModel

internal fun Fragment.createFeedNotificationChannelModule(args: Bundle): FeedNotificationChannelModule {
    checkAndInit(requireContext())
    val config = getGlobalNotificationChannelSettings()?.let {
        NotificationConfig.from(it)
    }
    return ModuleProviders.feedNotificationChannel.provide(requireContext(), args, config)
}

internal fun Fragment.createChatNotificationChannelModule(args: Bundle): ChatNotificationChannelModule {
    checkAndInit(requireContext())
    val config = getGlobalNotificationChannelSettings()?.let {
        NotificationConfig.from(it)
    }
    return ModuleProviders.chatNotificationChannel.provide(requireContext(), args, config)
}

internal fun Fragment.createFeedNotificationChannelViewModel(
    channelUrl: String,
    params: MessageListParams?
): FeedNotificationChannelViewModel {
    return ViewModelProviders.feedNotificationChannel.provide(this, channelUrl, params)
}

internal fun Fragment.createChatNotificationChannelViewModel(
    channelUrl: String,
    params: MessageListParams?
): ChatNotificationChannelViewModel {
    return ViewModelProviders.chatNotificationChannel.provide(this, channelUrl, params)
}
