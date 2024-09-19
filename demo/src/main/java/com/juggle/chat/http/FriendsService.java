package com.juggle.chat.http;

import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FriendsService {
    @GET("friends/list")
    Call<HttpResult<ListResult<FriendBean>>> getFriendList(@Query("user_id") String userId, @Query("start_id") String startId, @Query("count") int count);

    @POST("friends/add")
    Call<HttpResult<Object>> addFriend(@Body RequestBody body);

    @POST("users/search")
    Call<HttpResult<ListResult<FriendBean>>> searchUsers(@Body RequestBody body);
}
