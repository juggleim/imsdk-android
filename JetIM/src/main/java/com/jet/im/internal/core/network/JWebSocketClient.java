package com.jet.im.internal.core.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

class JWebSocketClient extends WebSocketClient {
    JWebSocketClient(URI serverUri, IWebSocketClientListener listener) {
        super(serverUri);
        mWebSocketClientListener = listener;
        setSocketFactory(createSSLSocketFactory());
    }

    private javax.net.ssl.SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class TrustAllCerts implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
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
