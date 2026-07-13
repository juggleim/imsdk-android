package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.FriendInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMember;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.UserStatus;

import java.util.List;

public interface IUserInfoManager {

    /**
     * Gets user information.
     * @param userId User ID.
     * @return User information.
     */
    UserInfo getUserInfo(String userId);

    /**
     * Gets user information in batches.
     * @param userIdList List of user IDs.
     * @return User information list.
     */
    List<UserInfo> getUserInfoList(List<String> userIdList);

    /**
     * Gets group information.
     * @param groupId Group ID.
     * @return Group information.
     */
    GroupInfo getGroupInfo(String groupId);

    /**
     * Gets group information in batches.
     * @param groupIdList List of group IDs.
     * @return Group information list.
     */
    List<GroupInfo> getGroupInfoList(List<String> groupIdList);

    /**
     * Gets a group member.
     * @param groupId Group ID.
     * @param userId User ID.
     * @return Group member information.
     */
    GroupMember getGroupMember(String groupId, String userId);

    /**
     * Gets friend information.
     * @param userId User ID.
     * @return Friend information.
     */
    FriendInfo getFriendInfo(String userId);

    /**
     * Fetches the latest user information from the server.
     * @param userId User ID.
     * @param callback Result callback.
     */
    void fetchUserInfo(String userId, JIMConst.IResultCallback<UserInfo> callback);

    /**
     * Fetches the latest group information from the server.
     * @param groupId Group ID.
     * @param callback Result callback.
     */
    void fetchGroupInfo(String groupId, JIMConst.IResultCallback<GroupInfo> callback);

    /**
     * Fetches the latest friend information from the server.
     * @param userId User ID.
     * @param callback Result callback.
     */
    void fetchFriendInfo(String userId, JIMConst.IResultCallback<FriendInfo> callback);

    /**
     * Queries user status.
     * @param userIdList List of user IDs to fetch.
     * @param callback Result callback.
     */
    void getUserStatus(List<String> userIdList, JIMConst.IResultListCallback<UserStatus> callback);

    interface IUserStatusListener {
        void onUserStatusChange(UserStatus userStatus);
    }

    void addUserStatusListener(String key, IUserStatusListener listener);
    void removeUserStatusListener(String key);

}
