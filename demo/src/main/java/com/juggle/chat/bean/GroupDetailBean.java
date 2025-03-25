package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupDetailBean {
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public List<GroupMemberBean> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberBean> members) {
        this.members = members;
    }

    public GroupMemberBean getOwner() {
        return owner;
    }

    public void setOwner(GroupMemberBean owner) {
        this.owner = owner;
    }

    public int getMyRole() {
        return myRole;
    }

    public void setMyRole(int myRole) {
        this.myRole = myRole;
    }

    public GroupManagementBean getGroupManagement() {
        return groupManagement;
    }

    public void setGroupManagement(GroupManagementBean groupManagement) {
        this.groupManagement = groupManagement;
    }

    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    @SerializedName("group_id")
    private String groupId;
    @SerializedName("group_name")
    private String groupName;
    @SerializedName("group_portrait")
    private String portrait;
    @SerializedName("member_count")
    private int memberCount;
    private List<GroupMemberBean> members;
    private GroupMemberBean owner;
    @SerializedName("my_role")
    private int myRole;
    @SerializedName("group_management")
    private GroupManagementBean groupManagement;
    @SerializedName("grp_display_name")
    private String groupDisplayName;
}
