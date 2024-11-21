package com.juggle.im.call.internal.model;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.model.UserInfo;

import java.util.List;

public class RtcRoom {
    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public UserInfo getOwner() {
        return mOwner;
    }

    public void setOwner(UserInfo owner) {
        mOwner = owner;
    }

    public boolean isMultiCall() {
        return mIsMultiCall;
    }

    public void setMultiCall(boolean multiCall) {
        mIsMultiCall = multiCall;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public CallConst.CallStatus getCallStatus() {
        return mCallStatus;
    }

    public void setCallStatus(CallConst.CallStatus callStatus) {
        mCallStatus = callStatus;
    }

    public List<CallMember> getMembers() {
        return mMembers;
    }

    public void setMembers(List<CallMember> members) {
        mMembers = members;
    }

    private String mRoomId;
    private UserInfo mOwner;
    private boolean mIsMultiCall;
    private String mDeviceId;
    private CallConst.CallStatus mCallStatus;
    private List<CallMember> mMembers;
}
