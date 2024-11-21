package com.juggle.im.call.model;

import com.juggle.im.call.CallConst;
import com.juggle.im.model.UserInfo;

public class CallMember {
    public long getFinishTime() {
        return mFinishTime;
    }

    public void setFinishTime(long finishTime) {
        mFinishTime = finishTime;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public CallConst.CallStatus getCallStatus() {
        return mCallStatus;
    }

    public void setCallStatus(CallConst.CallStatus callStatus) {
        mCallStatus = callStatus;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public long getConnectTime() {
        return mConnectTime;
    }

    public void setConnectTime(long connectTime) {
        mConnectTime = connectTime;
    }

    public UserInfo getInviter() {
        return mInviter;
    }

    public void setInviter(UserInfo inviter) {
        mInviter = inviter;
    }

    private UserInfo mUserInfo;
    private CallConst.CallStatus mCallStatus;
    private long mStartTime;
    private long mConnectTime;
    private long mFinishTime;
    private UserInfo mInviter;
}
