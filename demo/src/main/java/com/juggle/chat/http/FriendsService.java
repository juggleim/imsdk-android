package com.juggle.chat.http;

import com.juggle.chat.bean.FriendApplicationBean;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.bean.SearchUserBean;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FriendsService {
    @GET("/jim/friends/list")
    Call<HttpResult<ListResult<FriendBean>>> getFriendList(@Query("user_id") String userId, @Query("start_id") String startId, @Query("count") int count);

    @POST("/jim/friends/add")
    Call<HttpResult<Object>> addFriend(@Body RequestBody body);

    @POST("/jim/users/search")
    Call<HttpResult<ListResult<SearchUserBean>>> searchUsers(@Body RequestBody body);

    @POST("/jim/friends/apply")
    Call<HttpResult<Object>> applyFriend(@Body RequestBody body);

    @POST("/jim/friends/confirm")
    Call<HttpResult<Object>> confirmFriend(@Body RequestBody body);

    @GET("/jim/friends/applications")
    Call<HttpResult<ListResult<FriendApplicationBean>>> getFriendApplicationList(@Query("start") int start, @Query("count") int count);
}
