package com.jet.im.kit.interfaces;

import com.juggle.im.model.UserInfo;

import java.util.List;

public interface IGroupMemberProvider {
    void getGroupMembers(String groupId, GroupMemberCallback callback);

    interface GroupMemberCallback {
        void onMembersFetch(List<UserInfo> members, int code);
    }
}
