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
     * Publishes a moment.
     * @param content Text content of the moment.
     * @param mediaList Media content list for the moment, such as images or videos.
     * @param callback Result callback.
     */
    void addMoment(String content, List<MomentMedia> mediaList, JIMConst.IResultCallback<Moment> callback);

    /**
     * Deletes a moment.
     * @param momentId Moment ID.
     * @param callback Result callback.
     */
    void removeMoment(String momentId, IMessageManager.ISimpleCallback callback);

    /**
     * Gets the cached moment list. Cached data may not be the latest version and can be
     * used to render the UI immediately for a better user experience.
     * @param option Fetch options.
     * @return Cached moment list.
     */
    List<Moment> getCachedMomentList(GetMomentOption option);

    /**
     * Gets the moment list.
     * @param option Fetch options.
     * @param callback Result callback.
     */
    void getMomentList(GetMomentOption option, JIMConst.IResultListCallback<Moment> callback);

    /**
     * Gets moment details.
     * @param momentId Moment ID.
     * @param callback Result callback.
     */
    void getMoment(String momentId, JIMConst.IResultCallback<Moment> callback);

    /**
     * Publishes a comment.
     * @param momentId ID of the moment being commented on.
     * @param parentCommentId Parent comment ID.
     * @param content Comment content.
     * @param callback Result callback.
     */
    void addComment(String momentId, String parentCommentId, String content, JIMConst.IResultCallback<MomentComment> callback);

    /**
     * Deletes a comment.
     * @param momentId Moment ID.
     * @param commentId Comment ID.
     * @param callback Result callback.
     */
    void removeComment(String momentId, String commentId, IMessageManager.ISimpleCallback callback);

    /**
     * Gets the comment list.
     * @param option Fetch options.
     * @param callback Result callback.
     */
    void getCommentList(GetMomentCommentOption option, JIMConst.IResultListCallback<MomentComment> callback);

    /**
     * Adds a reaction.
     * @param momentId Moment ID.
     * @param key Reaction type.
     * @param callback Result callback.
     */
    void addReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);

    /**
     * Removes a reaction.
     * @param momentId Moment ID.
     * @param key Reaction type.
     * @param callback Result callback.
     */
    void removeReaction(String momentId, String key, IMessageManager.ISimpleCallback callback);

    /**
     * Gets the reaction list.
     * @param momentId Moment ID.
     * @param callback Result callback.
     */
    void getReactionList(String momentId, JIMConst.IResultListCallback<MomentReaction> callback);
}
