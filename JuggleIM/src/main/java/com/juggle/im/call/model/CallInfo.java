package com.juggle.im.call.model;

import com.juggle.im.call.CallConst;
import com.juggle.im.model.UserInfo;

import java.util.List;

public class CallInfo {
    public String getCallId() {
        return mCallId;
    }

    public void setCallId(String callId) {
        mCallId = callId;
    }

    public boolean isMultiCall() {
        return mIsMultiCall;
    }

    public void setMultiCall(boolean multiCall) {
        mIsMultiCall = multiCall;
    }

    public CallConst.CallMediaType getMediaType() {
        return mMediaType;
    }

    public void setMediaType(CallConst.CallMediaType mediaType) {
        mMediaType = mediaType;
    }

    public UserInfo getOwner() {
        return mOwner;
    }

    public void setOwner(UserInfo owner) {
        mOwner = owner;
    }

    public List<CallMember> getMembers() {
        return mMembers;
    }

    public void setMembers(List<CallMember> members) {
        mMembers = members;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    private String mCallId;
    private boolean mIsMultiCall;
    private CallConst.CallMediaType mMediaType;
    private UserInfo mOwner;
    private List<CallMember> mMembers;
    private String mExtra;
}
