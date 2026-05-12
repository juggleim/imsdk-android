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
        //判空
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        //从缓存中查找
        UserInfo userInfoCache = mUserInfoCache.getUserInfo(userId);
        //缓存命中，直接返回缓存数据
        if (userInfoCache != null) {
            return userInfoCache;
        }
        //缓存未命中，从数据库中查询
        UserInfo userInfoDB = mCore.getDbManager().getUserInfo(userId);
        //更新缓存
        mUserInfoCache.insertUserInfo(userInfoDB);
        //返回数据
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
        //判空
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        //从缓存中查找
        GroupInfo groupInfoCache = mUserInfoCache.getGroupInfo(groupId);
        //缓存命中，直接返回缓存数据
        if (groupInfoCache != null) {
            return groupInfoCache;
        }
        //缓存未命中，从数据库中查询
        GroupInfo groupInfoDB = mCore.getDbManager().getGroupInfo(groupId);
        //更新缓存
        mUserInfoCache.insertGroupInfo(groupInfoDB);
        //返回数据
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
        //判空
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return null;
        }
        //从缓存中查找
        GroupMember groupMember = mUserInfoCache.getGroupMember(groupId, userId);
        //缓存命中，直接返回缓存数据
        if (groupMember != null) {
            return groupMember;
        }
        //缓存未命中，从数据库中查询
        groupMember = mCore.getDbManager().getGroupMember(groupId, userId);
        //更新缓存
        mUserInfoCache.insertGroupMember(groupMember);
        //返回数据
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
        //判空
        if (list == null || list.isEmpty()) {
            return;
        }
        //更新数据库
        mCore.getDbManager().insertUserInfoList(list);
        //更新缓存
        mUserInfoCache.insertUserInfoList(list);
    }

    void insertGroupInfoList(List<GroupInfo> list) {
        //判空
        if (list == null || list.isEmpty()) {
            return;
        }
        //更新数据库
        mCore.getDbManager().insertGroupInfoList(list);
        //更新缓存
        mUserInfoCache.insertGroupInfoList(list);
    }

    void insertGroupMemberList(List<GroupMember> list) {
        //判空
        if (list == null || list.isEmpty()) {
            return;
        }
        //更新数据库
        mCore.getDbManager().insertGroupMembers(list);
        //更新缓存
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
