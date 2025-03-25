package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

public class GroupManagementBean {
    public int getGroupMute() {
        return groupMute;
    }

    public void setGroupMute(int groupMute) {
        this.groupMute = groupMute;
    }

    public int getMaxAdminCount() {
        return maxAdminCount;
    }

    public void setMaxAdminCount(int maxAdminCount) {
        this.maxAdminCount = maxAdminCount;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(int adminCount) {
        this.adminCount = adminCount;
    }

    public int getGroupVerifyType() {
        return groupVerifyType;
    }

    public void setGroupVerifyType(int groupVerifyType) {
        this.groupVerifyType = groupVerifyType;
    }

    public int getHistoryMessageVisible() {
        return historyMessageVisible;
    }

    public void setHistoryMessageVisible(int historyMessageVisible) {
        this.historyMessageVisible = historyMessageVisible;
    }

    @SerializedName("group_mute")
    private int groupMute;
    @SerializedName("max_admin_count")
    private int maxAdminCount;
    @SerializedName("admin_count")
    private int adminCount;
    @SerializedName("group_verify_type")
    private int groupVerifyType;
    @SerializedName("group_his_msg_visible")
    private int historyMessageVisible;

}
