package com.juggle.im.call.internal.media;

import android.view.View;

import java.util.List;

public interface ICallMediaListener {
    View viewForUserId(String userId);
    void onUsersJoin(List<String> userIdList);
    void onUserCameraChange(String userId, boolean enable);
}
