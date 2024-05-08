package com.jet.im.kit.internal.contracts

import com.sendbird.android.handler.ConnectHandler

internal interface SendbirdUIKitContract {
    fun connect(handler: ConnectHandler?)
    fun runOnUIThread(runnable: Runnable)
}
