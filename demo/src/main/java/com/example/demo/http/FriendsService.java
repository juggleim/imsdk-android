package com.example.demo.http;

import com.example.demo.bean.FriendBean;
import com.example.demo.bean.HttpResult;
import com.example.demo.bean.ListResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FriendsService {
    @GET("/friends/list")
    Call<HttpResult<ListResult<FriendBean>>> getFriendList(@Query("user_id") String userId, @Query("start_id") String startId, @Query("count") int count);
}
