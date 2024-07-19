package com.example.demo.common.extensions

import com.example.demo.common.consts.StringSet
import com.jet.im.kit.SendbirdUIKit
import com.sendbird.android.SendbirdChat


internal fun SendbirdUIKit.ThemeMode.isUsingDarkTheme() = this == SendbirdUIKit.ThemeMode.Dark

internal fun getFeedChannelUrl(): String {
    return SendbirdChat.appInfo?.let {
        val feedChannels = it.notificationInfo?.feedChannels
        feedChannels?.get(StringSet.feed)
    } ?: ""
}

