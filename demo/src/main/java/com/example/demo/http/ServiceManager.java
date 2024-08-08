package com.example.demo.http;

import com.example.demo.utils.SSLHelper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceManager {
    private static LoginService loginService;

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(SSLHelper.getTrustAllSSLSocketFactory(), SSLHelper.getTrustAllManager())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://appserver.jugglechat.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        loginService = retrofit.create(LoginService.class);
    }

    public static LoginService loginService() {
        return loginService;
    }
}
