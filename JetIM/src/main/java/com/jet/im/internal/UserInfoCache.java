package com.jet.im.internal;

import android.text.TextUtils;
import android.util.LruCache;

import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserInfoCache {
    private static final int MAX_CACHED_COUNT = 100;
    private final Lock mLock = new ReentrantLock();
    private final LruCache<String, UserInfo> mUserInfoCache = new LruCache<>(MAX_CACHED_COUNT);
    private final LruCache<String, GroupInfo> mGroupInfoCache = new LruCache<>(MAX_CACHED_COUNT);

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

    //获取userInfo缓存
    public UserInfo getUserInfo(String userId) {
        mLock.lock();
        try {
            //判空
            if (TextUtils.isEmpty(userId)) {
                return null;
            }
            //从缓存中查找
            return mUserInfoCache.get(userId);
        } finally {
            mLock.unlock();
        }
    }

    //更新userInfo缓存
    public void insertUserInfo(UserInfo userInfo) {
        mLock.lock();
        try {
            //判空
            if (userInfo == null || TextUtils.isEmpty(userInfo.getUserId())) {
                return;
            }
            //更新缓存
            mUserInfoCache.put(userInfo.getUserId(), userInfo);
        } finally {
            mLock.unlock();
        }
    }

    //更新userInfoList缓存
    public void insertUserInfoList(List<UserInfo> list) {
        mLock.lock();
        try {
            //判空
            if (list == null || list.isEmpty()) {
                return;
            }
            //更新缓存
            for (UserInfo userInfo : list) {
                mUserInfoCache.put(userInfo.getUserId(), userInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

    //获取groupInfo缓存
    public GroupInfo getGroupInfo(String groupId) {
        mLock.lock();
        try {
            //判空
            if (TextUtils.isEmpty(groupId)) {
                return null;
            }
            //从缓存中查找
            return mGroupInfoCache.get(groupId);
        } finally {
            mLock.unlock();
        }
    }

    //更新groupInfo缓存
    public void insertGroupInfo(GroupInfo groupInfo) {
        mLock.lock();
        try {
            //判空
            if (groupInfo == null || TextUtils.isEmpty(groupInfo.getGroupId())) {
                return;
            }
            //更新缓存
            mGroupInfoCache.put(groupInfo.getGroupId(), groupInfo);
        } finally {
            mLock.unlock();
        }
    }

    //更新groupInfoList缓存
    public void insertGroupInfoList(List<GroupInfo> list) {
        mLock.lock();
        try {
            //判空
            if (list == null || list.isEmpty()) {
                return;
            }
            //更新缓存
            for (GroupInfo groupInfo : list) {
                mGroupInfoCache.put(groupInfo.getGroupId(), groupInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

}

