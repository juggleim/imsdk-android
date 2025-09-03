package com.juggle.im.call.internal.model;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.model.CallFinishNotifyMessage;
import com.juggle.im.call.model.CallInfo;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CallActiveCallMessage extends MessageContent {
    public CallActiveCallMessage() {
        this.mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        //不会往外发
        return new byte[0];
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "CallActiveCallMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            CallInfo callInfo = new CallInfo();
            callInfo.setCallId(jsonObject.optString("room_id"));
            int roomType = jsonObject.optInt("room_type");
            callInfo.setMultiCall(roomType == 1);
            JSONObject ownerObject = jsonObject.optJSONObject("owner");
            callInfo.setOwner(decodeUserInfo(ownerObject));
            callInfo.setMediaType(CallConst.CallMediaType.setValue(jsonObject.optInt("rtc_media_type")));
            JSONArray members = jsonObject.optJSONArray("members");
            List<CallMember> l = new ArrayList<>();
            if (members != null) {
                for (int i = 0; i < members.length(); i++) {
                    JSONObject memberObject = members.optJSONObject(i);
                    UserInfo userInfo = decodeUserInfo(memberObject);
                    CallMember callMember = new CallMember();
                    callMember.setUserInfo(userInfo);
                    l.add(callMember);
                }
            }
            callInfo.setMembers(l);
            mCallInfo = callInfo;
            mIsFinished = jsonObject.optBoolean("finished");
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "CallActiveCallMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_CMD.getValue();
    }

    public CallInfo getCallInfo() {
        return mCallInfo;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    private UserInfo decodeUserInfo(JSONObject userInfoObj) {
        if (userInfoObj == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        if (userInfoObj.has(USER_ID)) {
            userInfo.setUserId(userInfoObj.optString(USER_ID));
        }
        if (userInfoObj.has(USER_NAME)) {
            userInfo.setUserName(userInfoObj.optString(USER_NAME));
        }
        if (userInfoObj.has(USER_PORTRAIT)) {
            userInfo.setPortrait(userInfoObj.optString(USER_PORTRAIT));
        }
        return userInfo;
    }

    public static final String CONTENT_TYPE = "jg:activedcall";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "nickname";
    private static final String USER_PORTRAIT = "user_portrait";

    private CallInfo mCallInfo;
    private boolean mIsFinished;
}
