package com.example.demo.common.extensions

import android.content.Intent
import android.os.Build
import com.sendbird.android.SendbirdChat
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.providers.AdapterProviders
import com.jet.im.kit.providers.FragmentProviders
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.providers.ViewModelProviders
import com.example.demo.common.consts.StringSet
import com.example.demo.common.preferences.PreferenceUtils
import com.sendbird.android.handler.ConnectHandler
import java.io.Serializable

/**
 * Clean up previous sample settings.
 */
internal fun cleanUpPreviousSampleSettings() {
    // clear providers
    AdapterProviders.resetToDefault()
    FragmentProviders.resetToDefault()
    ModuleProviders.resetToDefault()
    ViewModelProviders.resetToDefault()

    // clear custom params handler
    SendbirdUIKit.setCustomParamsHandler(null)

    // clear custom user list query handler to use default user list query
    SendbirdUIKit.setCustomUserListQueryHandler(null)
}

internal fun authenticate(handler: ConnectHandler) {
    if (PreferenceUtils.isUsingFeedChannelOnly) {
        SendbirdUIKit.authenticateFeed(handler::onConnected)
        return
    }
    SendbirdUIKit.connect(handler)
}

internal fun SendbirdUIKit.ThemeMode.isUsingDarkTheme() = this == SendbirdUIKit.ThemeMode.Dark

internal fun getFeedChannelUrl(): String {
    return SendbirdChat.appInfo?.let {
        val feedChannels = it.notificationInfo?.feedChannels
        feedChannels?.get(StringSet.feed)
    } ?: ""
}

internal fun <T : Serializable?> Intent.getSerializable(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(key, clazz)
    } else {
        @Suppress("UNCHECKED_CAST")
        this.getSerializableExtra(key) as? T
    }
}
