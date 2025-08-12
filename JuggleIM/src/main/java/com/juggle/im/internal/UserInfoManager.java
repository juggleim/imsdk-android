package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.model.GroupMember;
import com.juggle.im.interfaces.IUserInfoManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

import java.util.List;

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

    private final JIMCore mCore;
    private final UserInfoCache mUserInfoCache = new UserInfoCache();
}
