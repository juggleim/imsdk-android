package com.jet.im.kit.internal.contracts

import com.sendbird.android.AppInfo
import com.sendbird.android.ConnectionState
import com.sendbird.android.SendbirdChat
import com.sendbird.android.handler.AuthenticationHandler
import com.sendbird.android.handler.BaseChannelHandler
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.handler.ConnectHandler
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.handler.UIKitConfigurationHandler
import com.sendbird.android.internal.sb.SendbirdSdkInfo
import com.sendbird.android.params.InitParams
import com.sendbird.android.params.UserUpdateParams

internal class SendbirdChatImpl : SendbirdChatContract {
    override fun addChannelHandler(identifier: String, handler: BaseChannelHandler) {
        SendbirdChat.addChannelHandler(identifier, handler)
    }

    override fun addConnectionHandler(identifier: String, handler: ConnectionHandler) {
        SendbirdChat.addConnectionHandler(identifier, handler)
    }

    override fun removeChannelHandler(identifier: String): BaseChannelHandler? =
        SendbirdChat.removeChannelHandler(identifier)

    override fun removeConnectionHandler(identifier: String): ConnectionHandler? =
        SendbirdChat.removeConnectionHandler(identifier)

    override fun init(params: InitParams, handler: InitResultHandler) {
        SendbirdChat.init(params, handler)
    }

    override fun connect(userId: String, accessToken: String?, handler: ConnectHandler?) {
        SendbirdChat.connect(userId, accessToken, handler)
    }

    override fun updateCurrentUserInfo(params: UserUpdateParams, handler: CompletionHandler?) {
        SendbirdChat.updateCurrentUserInfo(params, handler)
    }

    override fun addExtension(key: String, version: String) {
        SendbirdChat.addExtension(key, version)
    }

    override fun addSendbirdExtensions(
        extensions: List<SendbirdSdkInfo>,
        customData: Map<String, String>?
    ) {
        SendbirdChat.addSendbirdExtensions(extensions, customData)
    }

    override fun getAppInfo(): AppInfo? = SendbirdChat.appInfo

    override fun getConnectionState(): ConnectionState = SendbirdChat.connectionState

    override fun getUIKitConfiguration(handler: UIKitConfigurationHandler?) {
        SendbirdChat.getUIKitConfiguration(handler)
    }

    override fun authenticateFeed(
        userId: String,
        accessToken: String?,
        apiHost: String?,
        handler: AuthenticationHandler?
    ) {
        SendbirdChat.authenticateFeed(userId, accessToken, apiHost, handler)
    }
}
