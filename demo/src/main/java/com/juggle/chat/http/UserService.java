package com.juggle.chat.http;

import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.QRCodeBean;
import com.juggle.chat.bean.UserInfoBean;
import com.juggle.chat.bean.UserInfoRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {
    @POST("/jim/users/update")
    Call<HttpResult<Object>> updateUserInfo(@Body UserInfoRequest userInfo);

    @GET("/jim/users/info")
    Call<HttpResult<UserInfoBean>> getUserInfo(@Query("user_id") String userId);

    @GET("/jim/users/qrcode")
    Call<HttpResult<QRCodeBean>> getQRCode();
}
