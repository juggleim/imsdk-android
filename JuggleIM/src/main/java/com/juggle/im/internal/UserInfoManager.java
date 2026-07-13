package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.internal.core.network.wscallback.WebSocketDataCallback;
import com.juggle.im.model.FriendInfo;
import com.juggle.im.model.GroupMember;
import com.juggle.im.interfaces.IUserInfoManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.UserStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfoManager implements IUserInfoManager {
    public UserInfoManager(JIMCore core) {
        this.mCore = core;
    }

    @Override
    public UserInfo getUserInfo(String userId) {
        //Validate empty input
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        //Look up from the cache
        UserInfo userInfoCache = mUserInfoCache.getUserInfo(userId);
        //Cache hit; return cached data directly
        if (userInfoCache != null) {
            return userInfoCache;
        }
        //Cache miss; query the database
        UserInfo userInfoDB = mCore.getDbManager().getUserInfo(userId);
        //Update the cache
        mUserInfoCache.insertUserInfo(userInfoDB);
        //Return data
        return userInfoDB;
    }

    @Override
    public List<UserInfo> getUserInfoList(List<String> userIdList) {
        List<UserInfo> userInfoList = mCore.getDbManager().getUserInfoList(userIdList);
        for (UserInfo userInfo : userInfoList) {
            mUserInfoCache.insertUserInfo(userInfo);
        }
        return userInfoList;
    }

    @Override
    public GroupInfo getGroupInfo(String groupId) {
        //Validate empty input
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        //Look up from the cache
        GroupInfo groupInfoCache = mUserInfoCache.getGroupInfo(groupId);
        //Cache hit; return cached data directly
        if (groupInfoCache != null) {
            return groupInfoCache;
        }
        //Cache miss; query the database
        GroupInfo groupInfoDB = mCore.getDbManager().getGroupInfo(groupId);
        //Update the cache
        mUserInfoCache.insertGroupInfo(groupInfoDB);
        //Return data
        return groupInfoDB;
    }

    @Override
    public List<GroupInfo> getGroupInfoList(List<String> groupIdList) {
        List<GroupInfo> groupList = mCore.getDbManager().getGroupInfoList(groupIdList);
        for (GroupInfo group : groupList) {
            mUserInfoCache.insertGroupInfo(group);
        }
        return groupList;
    }

    @Override
    public GroupMember getGroupMember(String groupId, String userId) {
        //Validate empty input
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return null;
        }
        //Look up from the cache
        GroupMember groupMember = mUserInfoCache.getGroupMember(groupId, userId);
        //Cache hit; return cached data directly
        if (groupMember != null) {
            return groupMember;
        }
        //Cache miss; query the database
        groupMember = mCore.getDbManager().getGroupMember(groupId, userId);
        //Update the cache
        mUserInfoCache.insertGroupMember(groupMember);
        //Return data
        return groupMember;
    }

    @Override
    public FriendInfo getFriendInfo(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        FriendInfo friendInfo = mUserInfoCache.getFriendInfo(userId);
        if (friendInfo != null) {
            return friendInfo;
        }
        friendInfo = mCore.getDbManager().getFriendInfo(userId);
        mUserInfoCache.insertFriendInfo(friendInfo);
        return friendInfo;
    }

    @Override
    public void fetchUserInfo(String userId, JIMConst.IResultCallback<UserInfo> callback) {
        if (TextUtils.isEmpty(userId)) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.INVALID_PARAM);
                }
            });
            return;
        }
        mCore.getWebSocket().fetchUserInfo(userId, new WebSocketDataCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                mUserInfoCache.insertUserInfo(userInfo);
                mCore.getDbManager().insertUserInfoList(Collections.singletonList(userInfo));
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(userInfo);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void fetchGroupInfo(String groupId, JIMConst.IResultCallback<GroupInfo> callback) {
        if (TextUtils.isEmpty(groupId)) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.INVALID_PARAM);
                }
            });
            return;
        }
        mCore.getWebSocket().fetchGroupInfo(groupId, new WebSocketDataCallback<GroupInfo>() {
            @Override
            public void onSuccess(GroupInfo groupInfo) {
                mUserInfoCache.insertGroupInfo(groupInfo);
                mCore.getDbManager().insertGroupInfoList(Collections.singletonList(groupInfo));
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(groupInfo);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void fetchFriendInfo(String userId, JIMConst.IResultCallback<FriendInfo> callback) {
        if (TextUtils.isEmpty(userId)) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.INVALID_PARAM);
                }
            });
            return;
        }
        if (TextUtils.isEmpty(mCore.getUserId())) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.CONNECTION_UNAVAILABLE);
                }
            });
            return;
        }

        mCore.getWebSocket().fetchFriendInfo(userId, mCore.getUserId(), new WebSocketDataCallback<FriendInfo>() {
            @Override
            public void onSuccess(FriendInfo friendInfo) {
                if (friendInfo == null) {
                    mCore.getCallbackHandler().post(() -> {
                        if (callback != null) {
                            callback.onError(JErrorCode.FRIEND_NOT_EXIST);
                        }
                    });
                    return;
                }
                mUserInfoCache.insertFriendInfo(friendInfo);
                mCore.getDbManager().insertFriendInfoList(Collections.singletonList(friendInfo));
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(friendInfo);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void getUserStatus(List<String> userIdList, JIMConst.IResultListCallback<UserStatus> callback) {
        if (userIdList == null || userIdList.isEmpty()) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.INVALID_PARAM);
                }
            });
            return;
        }
        if (TextUtils.isEmpty(mCore.getUserId()) || mCore.getWebSocket() == null) {
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onError(JErrorCode.CONNECTION_UNAVAILABLE);
                }
            });
            return;
        }
        mCore.getWebSocket().getUserStatus(userIdList, mCore.getUserId(), new WebSocketDataCallback<List<UserStatus>>() {
            @Override
            public void onSuccess(List<UserStatus> data) {
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(data, true);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void addUserStatusListener(String key, IUserStatusListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mUserStatusListenerMap == null) {
            mUserStatusListenerMap = new ConcurrentHashMap<>();
        }
        mUserStatusListenerMap.put(key, listener);
    }

    @Override
    public void removeUserStatusListener(String key) {
        if (!TextUtils.isEmpty(key) && mUserStatusListenerMap != null) {
            mUserStatusListenerMap.remove(key);
        }
    }

    public void clearCache() {
        mUserInfoCache.clearCache();
    }

    public void insertUserInfoList(List<UserInfo> list) {
        //Validate empty input
        if (list == null || list.isEmpty()) {
            return;
        }
        //Update the database
        mCore.getDbManager().insertUserInfoList(list);
        //Update the cache
        mUserInfoCache.insertUserInfoList(list);
    }

    void insertGroupInfoList(List<GroupInfo> list) {
        //Validate empty input
        if (list == null || list.isEmpty()) {
            return;
        }
        //Update the database
        mCore.getDbManager().insertGroupInfoList(list);
        //Update the cache
        mUserInfoCache.insertGroupInfoList(list);
    }

    void insertGroupMemberList(List<GroupMember> list) {
        //Validate empty input
        if (list == null || list.isEmpty()) {
            return;
        }
        //Update the database
        mCore.getDbManager().insertGroupMembers(list);
        //Update the cache
        mUserInfoCache.insertGroupMemberList(list);
    }

    void insertFriendInfoList(List<FriendInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mCore.getDbManager().insertFriendInfoList(list);
        mUserInfoCache.insertFriendInfoList(list);
    }

    void userStatusChange(UserStatus userStatus) {
        if (mUserStatusListenerMap != null) {
            for (Map.Entry<String, IUserStatusListener> entry : mUserStatusListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onUserStatusChange(userStatus);
                });
            }
        }

    }

    private final JIMCore mCore;
    private final UserInfoCache mUserInfoCache = new UserInfoCache();
    private ConcurrentHashMap<String, IUserStatusListener> mUserStatusListenerMap;
}
