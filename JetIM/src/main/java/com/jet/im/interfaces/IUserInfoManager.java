package com.jet.im.interfaces;

import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;

public interface IUserInfoManager {

    UserInfo getUserInfo(String userId);

    GroupInfo getGroupInfo(String groupId);
}
