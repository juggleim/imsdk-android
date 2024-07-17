package com.jet.im.internal.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class NetUtils {
    private static SSLSocketFactory sslSocketFactory;
    private static SSLContext sslContext;

    private static HostnameVerifier hostnameVerifier;

    public static HttpURLConnection createURLConnection(String urlStr) throws IOException {
        HttpURLConnection conn;
        URL url;
        if (urlStr.toLowerCase().startsWith("https")) {
            url = new URL(urlStr);
            HttpsURLConnection c = null;
            c = (HttpsURLConnection) url.openConnection();
            // 优先使用SSLSocketFactory，未设置则尝试使用SSLContext的SSLSocketFactory
            if (sslSocketFactory != null) {
                c.setSSLSocketFactory(sslSocketFactory);
            } else if (sslContext != null) {
                c.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            if (hostnameVerifier != null) {
                c.setHostnameVerifier(hostnameVerifier);
            }
            conn = c;
        } else {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
        }
        return conn;
    }
}
