package com.juggle.im.interfaces;

import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

public interface IUserInfoManager {

    UserInfo getUserInfo(String userId);

    GroupInfo getGroupInfo(String groupId);
}
