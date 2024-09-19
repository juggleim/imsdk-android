package com.juggle.chat.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.juggle.chat.utils.SSLHelper;
import com.google.gson.Gson;
import com.jet.im.kit.SendbirdUIKit;

import java.io.IOException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceManager {
    public static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json;charset=UTF-8");
    private static LoginService loginService;
    private static FriendsService friendsService;
    private static GroupsService groupsService;

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(SSLHelper.getTrustAllSSLSocketFactory(), SSLHelper.getTrustAllManager())
                .addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        Request request = chain.request();
                        if (!TextUtils.isEmpty(SendbirdUIKit.authorization)) {
                            request = request.newBuilder().addHeader("authorization", SendbirdUIKit.authorization).build();
                        }
                        return chain.proceed(request);
                    }
                })
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
        friendsService = retrofit.create(FriendsService.class);
        groupsService = retrofit.create(GroupsService.class);
    }

    /**
     * 通过参数 Map 合集
     *
     * @param paramsMap
     * @return
     */
    public static RequestBody createJsonRequest(HashMap<String, Object> paramsMap) {
        Gson gson = new Gson();
        String strEntity = gson.toJson(paramsMap);
        return RequestBody.create(MEDIA_TYPE_JSON, strEntity);
    }

    public static LoginService loginService() {
        return loginService;
    }

    public static FriendsService friendsService() {
        return friendsService;
    }

    public static GroupsService getGroupsService() {
        return groupsService;
    }
}
