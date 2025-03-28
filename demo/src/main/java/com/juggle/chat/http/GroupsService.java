package com.juggle.chat.http;

import com.juggle.chat.bean.CreateGroupResult;
import com.juggle.chat.bean.GroupAnnouncementBean;
import com.juggle.chat.bean.GroupBean;
import com.juggle.chat.bean.GroupDetailBean;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.bean.QRCodeBean;

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

    @POST("/jim/groups/invite")
    Call<HttpResult<Object>> addMember(@Body RequestBody body);

    @POST("/jim/groups/members/del")
    Call<HttpResult<Object>> removeMember(@Body RequestBody body);

    @GET("/jim/groups/info")
    Call<HttpResult<GroupDetailBean>> getGroupDetail(@Query("group_id") String groupId);

    @GET("/jim/groups/members/list")
    Call<HttpResult<ListResult<GroupMemberBean>>> getGroupMembers(@Query("group_id") String groupId);

    @POST("/jim/groups/update")
    Call<HttpResult<Object>> updateGroupInfo(@Body GroupBean group);

    @POST("/jim/groups/setdisplayname")
    Call<HttpResult<Object>> updateGroupDisplayName(@Body RequestBody body);

    @POST("/jim/groups/setgrpannouncement")
    Call<HttpResult<Object>> updateGroupAnnouncement(@Body RequestBody body);

    @GET("/jim/groups/getgrpannouncement")
    Call<HttpResult<GroupAnnouncementBean>> getGroupAnnouncement(@Query("group_id") String groupId);

    @POST("/jim/groups/quit")
    Call<HttpResult<Object>> quitGroup(@Body RequestBody body);

    @POST("/jim/groups/management/setmute")
    Call<HttpResult<Object>> setMute(@Body RequestBody body);

    @POST("/jim/groups/management/sethismsgvisible")
    Call<HttpResult<Object>> setHistoryMessageVisible(@Body RequestBody body);

    @POST("/jim/groups/management/chgowner")
    Call<HttpResult<Object>> changeOwner(@Body RequestBody body);

    @GET("/jim/groups/qrcode")
    Call<HttpResult<QRCodeBean>> getQRCode(@Query("group_id") String groupId);
}
