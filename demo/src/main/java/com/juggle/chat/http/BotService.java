package com.juggle.chat.http;

import com.juggle.chat.bean.BotBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BotService {
    @GET("/jim/bots/list")
    Call<HttpResult<ListResult<BotBean>>> getBotList(@Query("offset") String offset, @Query("count") int count);
}
