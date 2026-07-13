package com.juggle.im.internal;

import android.text.TextUtils;
import android.util.LruCache;

import com.juggle.im.model.FriendInfo;
import com.juggle.im.model.GroupMember;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserInfoCache {
    private static final int MAX_CACHED_COUNT = 100;
    private final Lock mLock = new ReentrantLock();
    private final LruCache<String, UserInfo> mUserInfoCache = new LruCache<>(MAX_CACHED_COUNT);
    private final LruCache<String, GroupInfo> mGroupInfoCache = new LruCache<>(MAX_CACHED_COUNT);
    private final LruCache<String, GroupMember> mGroupMemberCache = new LruCache<>(MAX_CACHED_COUNT);
    private final LruCache<String, FriendInfo> mFriendInfoLruCache = new LruCache<>(MAX_CACHED_COUNT);

    //Clear the cache
    public void clearCache() {
        mLock.lock();
        try {
            mUserInfoCache.evictAll();
            mGroupInfoCache.evictAll();
            mGroupMemberCache.evictAll();
            mFriendInfoLruCache.evictAll();
        } finally {
            mLock.unlock();
        }
    }

    //Get the userInfo cache
    public UserInfo getUserInfo(String userId) {
        mLock.lock();
        try {
            //Validate empty input
            if (TextUtils.isEmpty(userId)) {
                return null;
            }
            //Look up from the cache
            return mUserInfoCache.get(userId);
        } finally {
            mLock.unlock();
        }
    }

    //Update the userInfo cache
    public void insertUserInfo(UserInfo userInfo) {
        mLock.lock();
        try {
            //Validate empty input
            if (userInfo == null || TextUtils.isEmpty(userInfo.getUserId())) {
                return;
            }
            //Update the cache
            UserInfo old = mUserInfoCache.get(userInfo.getUserId());
            if (old == null || userInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                mUserInfoCache.put(userInfo.getUserId(), userInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

    //Update the userInfoList cache
    public void insertUserInfoList(List<UserInfo> list) {
        mLock.lock();
        try {
            //Validate empty input
            if (list == null || list.isEmpty()) {
                return;
            }
            //Update the cache
            for (UserInfo userInfo : list) {
                UserInfo old = mUserInfoCache.get(userInfo.getUserId());
                if (old == null || userInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                    mUserInfoCache.put(userInfo.getUserId(), userInfo);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    //Get the groupInfo cache
    public GroupInfo getGroupInfo(String groupId) {
        mLock.lock();
        try {
            //Validate empty input
            if (TextUtils.isEmpty(groupId)) {
                return null;
            }
            //Look up from the cache
            return mGroupInfoCache.get(groupId);
        } finally {
            mLock.unlock();
        }
    }

    //Update the groupInfo cache
    public void insertGroupInfo(GroupInfo groupInfo) {
        mLock.lock();
        try {
            //Validate empty input
            if (groupInfo == null || TextUtils.isEmpty(groupInfo.getGroupId())) {
                return;
            }
            //Update the cache
            GroupInfo old = mGroupInfoCache.get(groupInfo.getGroupId());
            if (old == null || groupInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                mGroupInfoCache.put(groupInfo.getGroupId(), groupInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

    //Update the groupInfoList cache
    public void insertGroupInfoList(List<GroupInfo> list) {
        mLock.lock();
        try {
            //Validate empty input
            if (list == null || list.isEmpty()) {
                return;
            }
            //Update the cache
            for (GroupInfo groupInfo : list) {
                GroupInfo old = mGroupInfoCache.get(groupInfo.getGroupId());
                if (old == null || groupInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                    mGroupInfoCache.put(groupInfo.getGroupId(), groupInfo);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    public GroupMember getGroupMember(String groupId, String userId) {
        mLock.lock();
        try {
            //Validate empty input
            if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
                return null;
            }
            //Look up from the cache
            return mGroupMemberCache.get(keyForGroupMember(groupId, userId));
        } finally {
            mLock.unlock();
        }
    }

    public void insertGroupMember(GroupMember groupMember) {
        mLock.lock();
        try {
            //Validate empty input
            if (groupMember == null || TextUtils.isEmpty(groupMember.getGroupId()) || TextUtils.isEmpty(groupMember.getUserId())) {
                return;
            }
            //Update the cache
            GroupMember old = mGroupMemberCache.get(keyForGroupMember(groupMember.getGroupId(), groupMember.getUserId()));
            if (old == null || groupMember.getUpdatedTime() >= old.getUpdatedTime()) {
                mGroupMemberCache.put(keyForGroupMember(groupMember.getGroupId(), groupMember.getUserId()), groupMember);
            }
        } finally {
            mLock.unlock();
        }
    }

    public void insertGroupMemberList(List<GroupMember> list) {
        mLock.lock();
        try {
            //Validate empty input
            if (list == null || list.isEmpty()) {
                return;
            }
            //Update the cache
            for (GroupMember member : list) {
                GroupMember old = mGroupMemberCache.get(keyForGroupMember(member.getGroupId(), member.getUserId()));
                if (old == null || member.getUpdatedTime() >= old.getUpdatedTime()) {
                    mGroupMemberCache.put(keyForGroupMember(member.getGroupId(), member.getUserId()), member);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    public FriendInfo getFriendInfo(String userId) {
        mLock.lock();
        try {
            //Validate empty input
            if (TextUtils.isEmpty(userId)) {
                return null;
            }
            //Look up from the cache
            return mFriendInfoLruCache.get(userId);
        } finally {
            mLock.unlock();
        }
    }

    public void insertFriendInfo(FriendInfo friendInfo) {
        mLock.lock();
        try {
            //Validate empty input
            if (friendInfo == null || TextUtils.isEmpty(friendInfo.getUserId())) {
                return;
            }
            //Update the cache
            FriendInfo old = mFriendInfoLruCache.get(friendInfo.getUserId());
            if (old == null || friendInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                mFriendInfoLruCache.put(friendInfo.getUserId(), friendInfo);
            }
        } finally {
            mLock.unlock();
        }
    }

    public void insertFriendInfoList(List<FriendInfo> friendInfoList) {
        mLock.lock();
        try {
            //Validate empty input
            if (friendInfoList == null || friendInfoList.isEmpty()) {
                return;
            }
            //Update the cache
            for (FriendInfo friendInfo : friendInfoList) {
                FriendInfo old = mFriendInfoLruCache.get(friendInfo.getUserId());
                if (old == null || friendInfo.getUpdatedTime() >= old.getUpdatedTime()) {
                    mFriendInfoLruCache.put(friendInfo.getUserId(), friendInfo);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    private String keyForGroupMember(String groupId, String userId) {
        return groupId + sSeparator + userId;
    }

    private static final String sSeparator = "+++";

}

