package com.juggle.im.interfaces;

import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMember;
import com.juggle.im.model.UserInfo;

import java.util.List;

public interface IUserInfoManager {

    UserInfo getUserInfo(String userId);

    List<UserInfo> getUserInfoList(List<String> userIdList);

    GroupInfo getGroupInfo(String groupId);

    List<GroupInfo> getGroupInfoList(List<String> groupIdList);

    GroupMember getGroupMember(String groupId, String userId);
}
