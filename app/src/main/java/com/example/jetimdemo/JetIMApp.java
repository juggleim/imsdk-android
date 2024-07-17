package com.example.jetimdemo;

import android.app.Application;
import android.net.SSLCertificateSocketFactory;

import com.jet.im.JetIM;
import com.jet.im.internal.logger.JLogConfig;
import com.jet.im.internal.logger.JLogLevel;
import com.jet.im.push.PushConfig;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class JetIMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        List<String> serverList = new ArrayList<>();
        serverList.add("http://120.48.178.248:8083");
        JetIM.getInstance().setServer(serverList);
        JetIM.InitConfig initConfig = new JetIM.InitConfig.Builder()
                .setPushConfig(new PushConfig.Builder().build())
                .setJLogConfig(new JLogConfig.Builder(getApplicationContext()).setLogConsoleLevel(JLogLevel.JLogLevelVerbose).build())
                .build();
        JetIM.getInstance().init(this, "appkey", initConfig);
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // 忽略主机名验证
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
