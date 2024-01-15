package com.jet.im.internal;

import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.utils.LoggerUtils;

import java.net.URI;

public class ConnectionManager implements IConnectionManager {
    @Override
    public void connect(String token) {
        LoggerUtils.i("connect, token is " + token);
        //todo 校验，是否有连接，mCore.getWebSocket 是不是为空

        if (mCore.getWebSocket() == null) {
            URI uri = JWebSocket.createWebSocketUri(mCore.getServers()[0]);
            mCore.setWebSocket(new JWebSocket(mCore.getAppKey(), token, uri, mCore.getContext()));
        } else {
            mCore.getWebSocket().setToken(token);
        }
        mCore.getWebSocket().connect();
    }

    @Override
    public void disconnect(boolean receivePush) {

    }

    public ConnectionManager(JetIMCore core, ConversationManager conversationManager, MessageManager messageManager) {
        this.mCore = core;
        this.mConversationManager = conversationManager;
        this.mMessageManager = messageManager;
    }

    private JetIMCore mCore;
    private ConversationManager mConversationManager;
    private MessageManager mMessageManager;
}
