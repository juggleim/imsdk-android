package com.example.demo.http;

import com.example.demo.bean.HttpResult;
import com.example.demo.bean.CodeRequest;
import com.example.demo.bean.LoginRequest;
import com.example.demo.bean.LoginResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginService {
    @POST("/sms/send")
    Call<HttpResult<Void>> getVerificationCode(@Body CodeRequest phone);
    @POST("/sms_login")
    Call<HttpResult<LoginResult>> login(@Body LoginRequest phone);
}
