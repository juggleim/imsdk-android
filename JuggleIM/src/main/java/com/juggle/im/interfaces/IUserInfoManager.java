package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.FriendInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMember;
import com.juggle.im.model.UserInfo;

import java.util.List;

public interface IUserInfoManager {

    /**
     * 获取用户信息
     * @param userId 用户 id
     * @return 用户信息
     */
    UserInfo getUserInfo(String userId);

    /**
     * 批量获取用户信息
     * @param userIdList 用户 id 列表
     * @return 用户信息列表
     */
    List<UserInfo> getUserInfoList(List<String> userIdList);

    /**
     * 获取群组信息
     * @param groupId 群组 id
     * @return 群组信息
     */
    GroupInfo getGroupInfo(String groupId);

    /**
     * 批量获取群组信息
     * @param groupIdList 群组 id 列表
     * @return 群组信息列表
     */
    List<GroupInfo> getGroupInfoList(List<String> groupIdList);

    /**
     * 获取群成员
     * @param groupId 群组 id
     * @param userId 用户 id
     * @return 群成员信息
     */
    GroupMember getGroupMember(String groupId, String userId);

    /**
     * 获取好友信息
     * @param userId 用户 id
     * @return 好友信息
     */
    FriendInfo getFriendInfo(String userId);

    /**
     * 从服务端获取最新的用户信息
     * @param userId 用户 id
     * @param callback 结果回调
     */
    void fetchUserInfo(String userId, JIMConst.IResultCallback<UserInfo> callback);

    /**
     * 从服务端获取最新的群组信息
     * @param groupId 群组 id
     * @param callback 结果回调
     */
    void fetchGroupInfo(String groupId, JIMConst.IResultCallback<GroupInfo> callback);

    /**
     * 从服务端获取最新的好友信息
     * @param userId 用户 id
     * @param callback 结果回调
     */
    void fetchFriendInfo(String userId, JIMConst.IResultCallback<FriendInfo> callback);
}
