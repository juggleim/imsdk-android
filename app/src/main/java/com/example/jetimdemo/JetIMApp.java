package com.example.jetimdemo;

import android.app.Application;

import com.juggle.im.JIM;
import com.juggle.im.internal.logger.JLogConfig;
import com.juggle.im.internal.logger.JLogLevel;
import com.juggle.im.push.PushConfig;

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
        serverList.add("https://nav.routechat.im");
        JIM.getInstance().setServer(serverList);
        JIM.InitConfig initConfig = new JIM.InitConfig.Builder()
                .setPushConfig(new PushConfig.Builder().build())
                .setJLogConfig(new JLogConfig.Builder(getApplicationContext()).setLogConsoleLevel(JLogLevel.JLogLevelVerbose).build())
                .build();
        JIM.getInstance().init(this, "kefukey", initConfig);
//        try {
//            TrustManager[] trustAllCerts = new TrustManager[]{
//                    new X509TrustManager() {
//                        public X509Certificate[] getAcceptedIssuers() {
//                            return null;
//                        }
//
//                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                        }
//
//                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                        }
//                    }
//            };
//
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//            // 忽略主机名验证
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
