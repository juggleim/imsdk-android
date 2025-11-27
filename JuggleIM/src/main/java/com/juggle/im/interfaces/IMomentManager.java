package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.GetMomentCommentOption;
import com.juggle.im.model.GetMomentOption;
import com.juggle.im.model.Moment;
import com.juggle.im.model.MomentComment;
import com.juggle.im.model.MomentMedia;
import com.juggle.im.model.MomentReaction;

import java.util.List;

public interface IMomentManager {
    void addMoment(String content, List<MomentMedia> mediaList, JIMConst.IResultCallback<Moment> callback);
    void removeMoment(String momentId, IMessageManager.ISimpleCallback callback);
    List<Moment> getCachedMomentList(GetMomentOption option);
    void getMomentList(GetMomentOption option, JIMConst.IResultListCallback<Moment> callback);
    void getMoment(String momentId, JIMConst.IResultCallback<Moment> callback);
    void addComment(String momentId, String parentCommentId, String content, JIMConst.IResultCallback<MomentComment> callback);
    void removeComment(String momentId, String commentId, IMessageManager.ISimpleCallback callback);
    void getCommentList(GetMomentCommentOption option, JIMConst.IResultListCallback<MomentComment> callback);
    void addReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);
    void removeReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);
    void getReactionList(String momentId, JIMConst.IResultListCallback<MomentReaction> callback);
}
