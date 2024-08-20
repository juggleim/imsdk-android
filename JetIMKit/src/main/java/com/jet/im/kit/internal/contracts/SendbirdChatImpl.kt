package com.jet.im.kit.internal.contracts

import android.content.Context
import com.juggle.im.JIM
import com.juggle.im.JIMConst
import com.juggle.im.interfaces.IConnectionManager.IConnectionStatusListener
import com.juggle.im.interfaces.IMessageManager
import com.jet.im.kit.SendbirdUIKit
import com.sendbird.android.AppInfo
import com.sendbird.android.ConnectionState
import com.sendbird.android.handler.AuthenticationHandler
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.handler.UIKitConfigurationHandler
import com.sendbird.android.internal.sb.SendbirdSdkInfo
import com.sendbird.android.params.InitParams
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.android.user.User

internal class SendbirdChatImpl : SendbirdChatContract {

    override fun addChannelHandler(identifier: String, handler: IMessageManager.IMessageListener) {
        JIM.getInstance().messageManager.addListener(identifier, handler)
    }

    override fun removeChannelHandler(identifier: String) {
        JIM.getInstance().messageManager.removeListener(identifier)
    }


    override fun init(context: Context, params: InitParams, handler: InitResultHandler) {
//        SendbirdChat.init(params, handler)
    }

    private var mUser: User? = null;
    override fun connect(
        userId: String,
        accessToken: String?,
        handler: com.jet.im.kit.interfaces.ConnectHandler?
    ) {
        val listener = object : IConnectionStatusListener {
            override fun onStatusChange(
                status: JIMConst.ConnectionStatus?,
                code: Int,
                extra: String
            ) {
                if (status == JIMConst.ConnectionStatus.CONNECTED) {
                    handler?.onConnected(null);
                    JIM.getInstance().connectionManager.removeConnectionStatusListener("kit")
                } else if (status == JIMConst.ConnectionStatus.FAILURE) {
                    handler?.onConnected(RuntimeException());
                    JIM.getInstance().connectionManager.removeConnectionStatusListener("kit")
                }
            }

            override fun onDbOpen() {
//                        TODO("Not yet implemented")
            }

            override fun onDbClose() {
//                        TODO("Not yet implemented")
            }
        }
        JIM.getInstance().connectionManager.addConnectionStatusListener("kit", listener)
        JIM.getInstance().connectionManager.connect(SendbirdUIKit.token)
    }

    override fun updateCurrentUserInfo(params: UserUpdateParams, handler: CompletionHandler?) {
    }

    override fun addExtension(key: String, version: String) {
    }

    override fun addSendbirdExtensions(
        extensions: List<SendbirdSdkInfo>,
        customData: Map<String, String>?
    ) {
    }




    override fun authenticateFeed(
        userId: String,
        accessToken: String?,
        apiHost: String?,
        handler: AuthenticationHandler?
    ) {
        val connectionStatus = JIM.getInstance().connectionManager.connectionStatus;
        if (connectionStatus == JIMConst.ConnectionStatus.CONNECTED) {
            handler?.onAuthenticated(mUser, null);
        } else {
            handler?.onAuthenticated(null, null);
        }
    }
}
