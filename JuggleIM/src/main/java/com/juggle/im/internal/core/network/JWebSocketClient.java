package com.juggle.im.internal.core.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class JWebSocketClient extends WebSocketClient {
    JWebSocketClient(URI serverUri, IWebSocketClientListener listener) {
        super(serverUri);
        mWebSocketClientListener = listener;
    }

    interface IWebSocketClientListener {
        void onOpen(JWebSocketClient client, ServerHandshake handshakedata);
        void onMessage(JWebSocketClient client, String message);
        void onMessage(JWebSocketClient client, ByteBuffer bytes);
        void onClose(JWebSocketClient client, int code, String reason, boolean remote);
        void onError(JWebSocketClient client, Exception ex);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (mWebSocketClientListener != null) {
            mWebSocketClientListener.onOpen(this, handshakedata);
        }
    }

    @Override
    public void onMessage(String message) {
        if (mWebSocketClientListener != null) {
            mWebSocketClientListener.onMessage(this, message);
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        if (mWebSocketClientListener != null) {
            mWebSocketClientListener.onMessage(this, bytes);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (mWebSocketClientListener != null) {
            mWebSocketClientListener.onClose(this, code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        if (mWebSocketClientListener != null) {
            mWebSocketClientListener.onError(this, ex);
        }
    }

    private final IWebSocketClientListener mWebSocketClientListener;
}
