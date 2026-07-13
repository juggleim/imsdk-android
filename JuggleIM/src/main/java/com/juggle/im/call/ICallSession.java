package com.juggle.im.call;

import android.view.View;

import com.juggle.im.call.model.CallMember;
import com.juggle.im.call.model.CallVideoDenoiseParams;
import com.juggle.im.model.Conversation;

import java.util.HashMap;
import java.util.List;

public interface ICallSession {
    interface ICallSessionListener {
        // Call connected.
        void onCallConnect();

        // Call finished.
        void onCallFinish(CallConst.CallFinishReason finishReason);

        // Error callback during a call.
        void onErrorOccur(CallConst.CallErrorCode errorCode);

        // Users were invited (used in multi-party calls).
        void onUsersInvite(String inviterId, List<String> userIdList);

        // Users joined the call (used in multi-party calls).
        void onUsersConnect(List<String> userIdList);

        // Users left the call (used in multi-party calls).
        void onUsersLeave(List<String> userIdList);

        // User enabled or disabled the camera.
        void onUserCameraEnable(String userId, boolean enable);

        // User enabled or disabled the microphone.
        void onUserMicrophoneEnable(String userId, boolean enable);

        // User sound levels changed.
        // userId as the key, and the sound level as the value.
        void onSoundLevelUpdate(HashMap<String, Float> soundLevels);

        // Callback for the first rendered video frame.
        void onVideoFirstFrameRender(String userId);
    }

    void addListener(String key, ICallSessionListener listener);
    void removeListener(String key);

    // Accept an incoming call.
    void accept();
    // Hang up the call.
    void hangup();
    // Enable the camera.
    void enableCamera(boolean isEnable);
    // Set the user's video view.
    void setVideoView(String userId, View view);
    // Start preview.
    void startPreview(View view);
    // Stop preview.
    void stopPreview();
    // Mute or unmute the microphone.
    void muteMicrophone(boolean isMute);
    // Mute or unmute the speaker.
    void muteSpeaker(boolean isMute);
    // Set speakerphone output.
    // true uses the speakerphone; false uses the earpiece.
    void setSpeakerEnable(boolean isEnable);
    // Switch the camera. true uses the front camera by default.
    void useFrontCamera(boolean isEnable);
    // Invite users to join the call (not supported when isMultiCall is false).
    void inviteUsers(List<String> userIdList);
    // Enable echo cancellation.
    void enableAEC(boolean isEnable);
    // Set video denoise parameters.
    void setVideoDenoiseParams(CallVideoDenoiseParams params);

    // Call ID.
    String getCallId();
    // Whether this is a multi-party call. false means a one-to-one call.
    boolean isMultiCall();
    // Media type (voice or video).
    CallConst.CallMediaType getMediaType();
    // Call status.
    CallConst.CallStatus getCallStatus();
    // Call start time. In a multi-party call, this is the time when current user was invited and may differ from the overall call start time.
    long getStartTime();
    // Time when current user joined the call.
    long getConnectTime();
    // Time when current user finished the call.
    long getFinishTime();
    // ID of the call initiator.
    String getOwner();
    // ID of the user who invited current user to the call.
    String getInviter();
    // Call finish reason.
    CallConst.CallFinishReason getFinishReason();
    // Call participants other than current user.
    List<CallMember> getMembers();
    // Current user.
    CallMember getCurrentCallMember();
    // Extra field.
    String getExtra();
    // Owning conversation.
    Conversation getConversation();
}
