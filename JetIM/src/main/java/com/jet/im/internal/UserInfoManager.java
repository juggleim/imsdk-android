package com.jet.im.internal;

import com.jet.im.interfaces.IUserInfoManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;

public class UserInfoManager implements IUserInfoManager {
    public UserInfoManager(JetIMCore core) {
        this.mCore = core;
    }
    @Override
    public UserInfo getUserInfo(String userId) {
        return mCore.getDbManager().getUserInfo(userId);
    }

    @Override
    public GroupInfo getGroupInfo(String groupId) {
        return mCore.getDbManager().getGroupInfo(groupId);
    }

    private final JetIMCore mCore;
}
