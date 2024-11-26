package com.juggle.im.internal.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

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

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        boolean isNetAvailable = false;
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            if (manager == null) {
                return false;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities networkCapabilities =
                        manager.getNetworkCapabilities(manager.getActiveNetwork());
                if (networkCapabilities != null
                        && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_BLUETOOTH)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_VPN)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                        || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_LOWPAN))) {

                    isNetAvailable = true;
                }
            } else {
                // 29以后弃用
                NetworkInfo networkInfo = null;
                try {
                    networkInfo = manager.getActiveNetworkInfo();
                } catch (Exception e) {
                    JLogger.e("Util-Net", "getActiveNetworkInfo Exception " + e);
                }
                if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable()) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            JLogger.e("Util-Net", "isNetWorkAvailable Exception " + e);
        }
        return isNetAvailable;
    }
}
