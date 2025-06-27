package com.jet.im.kit.call;

import android.view.View;

import com.juggle.im.model.UserInfo;

public interface ICallScrollView {
    void setScrollViewOverScrollMode(int mode);
    void removeChild(String childId);
    View findChildById(String childId);
    void updateChildState(String childId, boolean visible);
    void updateChildState(String childId, String state);
    void setChildPortraitSize(int size);
    void enableShowState(boolean enable);
    void addChild(String childId, UserInfo userInfo);
    void addChild(String childId, UserInfo userInfo, String state);
    void updateChildInfo(String childId, UserInfo userInfo);
    int dip2pix(int dipValue);
}
