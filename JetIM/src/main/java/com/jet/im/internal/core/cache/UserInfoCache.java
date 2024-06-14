package com.jet.im.internal.core.cache;

import android.text.TextUtils;
import android.util.LruCache;

import com.jet.im.internal.core.db.DBManager;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserInfoCache {
    private static final int MAX_CACHED_COUNT = 100;
    private final DBManager mDBManager;
    private final Lock mLock = new ReentrantLock();
    private final LruCache<String, UserInfo> mUserInfoCache = new LruCache<>(MAX_CACHED_COUNT);
    private final LruCache<String, GroupInfo> mGroupInfoCache = new LruCache<>(MAX_CACHED_COUNT);

    public UserInfoCache(DBManager core) {
        this.mDBManager = core;
    }

    //清空缓存
    public void clearCache() {
        mLock.lock();
        try {
            mUserInfoCache.evictAll();
            mGroupInfoCache.evictAll();
        } finally {
            mLock.unlock();
        }
    }

    //获取userInfo
    public UserInfo getUserInfo(String userId) {
        mLock.lock();
        try {
            //判空
            if (TextUtils.isEmpty(userId)) {
                return null;
            }
            //从缓存中查找
            UserInfo userInfo = mUserInfoCache.get(userId);
            //缓存命中，直接返回缓存数据
            if (userInfo != null) {
                return userInfo;
            }
            //缓存未命中，从数据库中查询
            UserInfo userInfoDB = mDBManager.getUserInfo(userId);
            //更新缓存
            if (userInfoDB != null) {
                mUserInfoCache.put(userId, userInfoDB);
            }
            //返回数据
            return userInfoDB;
        } finally {
            mLock.unlock();
        }
    }

    //更新userInfo
    public void insertUserInfoList(List<UserInfo> list) {
        mLock.lock();
        try {
            //判空
            if (list == null || list.isEmpty()) {
                return;
            }
            //更新数据库
            mDBManager.insertUserInfoList(list);
            //更新缓存
            for (UserInfo userInfo : list) {
                mUserInfoCache.put(userInfo.getUserId(), userInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

    //获取groupInfo
    public GroupInfo getGroupInfo(String groupId) {
        mLock.lock();
        try {
            //判空
            if (TextUtils.isEmpty(groupId)) {
                return null;
            }
            //从缓存中查找
            GroupInfo groupInfo = mGroupInfoCache.get(groupId);
            //缓存命中，直接返回缓存数据
            if (groupInfo != null) {
                return groupInfo;
            }
            //GroupInfo，从数据库中查询
            GroupInfo groupInfoDB = mDBManager.getGroupInfo(groupId);
            //更新缓存
            if (groupInfoDB != null) {
                mGroupInfoCache.put(groupId, groupInfoDB);
            }
            //返回数据
            return groupInfoDB;
        } finally {
            mLock.unlock();
        }
    }

    //更新groupInfo
    public void insertGroupInfoList(List<GroupInfo> list) {
        mLock.lock();
        try {
            //判空
            if (list == null || list.isEmpty()) {
                return;
            }
            //更新数据库
            mDBManager.insertGroupInfoList(list);
            //更新缓存
            for (GroupInfo groupInfo : list) {
                mGroupInfoCache.put(groupInfo.getGroupId(), groupInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

}

