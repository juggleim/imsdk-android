package com.juggle.chat.http;

import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.UserInfoRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("/jim/users/update")
    Call<HttpResult<Object>> updateUserInfo(@Body UserInfoRequest userInfo);
}
