package com.juggle.chat.http;

import com.juggle.chat.bean.CreateGroupResult;
import com.juggle.chat.bean.GroupBean;
import com.juggle.chat.bean.GroupDetailBean;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GroupsService {
    @GET("/jim/groups/mygroups")
    Call<HttpResult<ListResult<GroupBean>>> getGroupList(@Query("start_id") String startId, @Query("count") int count);

    @POST("/jim/groups/add")
    Call<HttpResult<CreateGroupResult>> createGroup(@Body RequestBody body);

    @POST("/jim/groups/members/add")
    Call<HttpResult<Object>> addMember(@Body RequestBody body);

    @GET("/jim/groups/info")
    Call<HttpResult<GroupDetailBean>> getGroupDetail(@Query("group_id") String groupId);

    @GET("/jim/groups/members/list")
    Call<HttpResult<ListResult<GroupMemberBean>>> getGroupMembers(@Query("group_id") String groupId);

    @POST("/jim/groups/update")
    Call<HttpResult<Object>> updateGroupInfo(@Body GroupBean group);

    @POST("/jim/groups/setdisplayname")
    Call<HttpResult<Object>> updateGroupDisplayName(@Body RequestBody body);
}
