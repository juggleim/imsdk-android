package com.juggle.chat.http;

import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.CodeRequest;
import com.juggle.chat.bean.LoginRequest;
import com.juggle.chat.bean.LoginResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginService {
    @POST("/jim/sms/send")
    Call<HttpResult<Void>> getVerificationCode(@Body CodeRequest phone);
    @POST("jim/sms_login")
    Call<HttpResult<LoginResult>> login(@Body LoginRequest phone);
    @POST("jim/login/qrcode/confirm")
    Call<HttpResult<Void>> qrcodeConfirm(@Body RequestBody body);
}
