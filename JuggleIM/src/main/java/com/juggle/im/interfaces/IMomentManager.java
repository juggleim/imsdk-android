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
    /**
     * 发布朋友圈
     * @param content 朋友圈的文本内容
     * @param mediaList 朋友圈的媒体内容列表（图片或者视频）
     * @param callback 结果回调
     */
    void addMoment(String content, List<MomentMedia> mediaList, JIMConst.IResultCallback<Moment> callback);

    /**
     * 删除朋友圈
     * @param momentId 朋友圈 id
     * @param callback 结果回调
     */
    void removeMoment(String momentId, IMessageManager.ISimpleCallback callback);

    /**
     * 获取缓存的朋友圈列表（缓存的数据不一定是最新版本，可用于第一时间渲染界面，优化用户体验）
     * @param option 获取参数
     * @return 缓存的朋友圈列表
     */
    List<Moment> getCachedMomentList(GetMomentOption option);

    /**
     * 获取朋友圈列表
     * @param option 获取参数
     * @param callback 结果回调
     */
    void getMomentList(GetMomentOption option, JIMConst.IResultListCallback<Moment> callback);

    /**
     * 获取朋友圈详情
     * @param momentId 朋友圈 id
     * @param callback 结果回调
     */
    void getMoment(String momentId, JIMConst.IResultCallback<Moment> callback);

    /**
     * 发布评论
     * @param momentId 评论的朋友圈 id
     * @param parentCommentId 父级评论 id
     * @param content 评论内容
     * @param callback 结果回调
     */
    void addComment(String momentId, String parentCommentId, String content, JIMConst.IResultCallback<MomentComment> callback);

    /**
     * 删除评论
     * @param momentId 朋友圈 id
     * @param commentId 评论 id
     * @param callback 结果回调
     */
    void removeComment(String momentId, String commentId, IMessageManager.ISimpleCallback callback);

    /**
     * 获取评论列表
     * @param option 获取参数
     * @param callback 结果回调
     */
    void getCommentList(GetMomentCommentOption option, JIMConst.IResultListCallback<MomentComment> callback);

    /**
     * 添加点赞
     * @param momentId 朋友圈 id
     * @param key 点赞类型
     * @param callback 结果回调
     */
    void addReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);

    /**
     * 取消点赞
     * @param momentId 朋友圈 id
     * @param key 点赞类型
     * @param callback 结果回调
     */
    void removeReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);

    /**
     * 获取点赞列表
     * @param momentId 朋友圈 id
     * @param callback 结果回调
     */
    void getReactionList(String momentId, JIMConst.IResultListCallback<MomentReaction> callback);
}
