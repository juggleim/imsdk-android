package com.jet.im.kit.internal.contracts

internal interface SendbirdUIKitContract {
    fun connect(handler: com.jet.im.kit.interfaces.ConnectHandler?)
    fun runOnUIThread(runnable: Runnable)
}
