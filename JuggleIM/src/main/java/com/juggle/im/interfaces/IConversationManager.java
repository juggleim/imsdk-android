package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.ConversationTagInfo;
import com.juggle.im.model.GetConversationOptions;

import java.util.List;

public interface IConversationManager {

    interface ISimpleCallback {
        void onSuccess();

        void onError(int errorCode);
    }

    interface ICreateConversationInfoCallback {
        void onSuccess(ConversationInfo conversationInfo);

        void onError(int errorCode);
    }

    void createConversationInfo(Conversation conversation, ICreateConversationInfoCallback callback);

    List<ConversationInfo> getConversationInfoList();

    List<ConversationInfo> getConversationInfoList(int[] conversationTypes,
                                                   int count,
                                                   long timestamp,
                                                   JIMConst.PullDirection direction);

    /**
     * Gets the conversation information list by query options.
     * @param options Query options.
     * @return Conversation information list.
     */
    List<ConversationInfo> getConversationInfoList(GetConversationOptions options);

    /**
     * Gets the conversation information list by page. Results are sorted by conversation time
     * in descending order, with newer items first and older items later.
     * @param count Number of items to fetch.
     * @param timestamp Fetch timestamp. Pass 0 to use the current time.
     * @param direction Fetch direction.
     * @return Conversation information list.
     */
    List<ConversationInfo> getConversationInfoList(int count,
                                                   long timestamp,
                                                   JIMConst.PullDirection direction);

    ConversationInfo getConversationInfo(Conversation conversation);

    void deleteConversationInfo(Conversation conversation, ISimpleCallback callback);

    void setDraft(Conversation conversation, String draft);

    void clearDraft(Conversation conversation);

    void setMute(Conversation conversation,
                 boolean isMute,
                 ISimpleCallback callback);

    void setTop(Conversation conversation, boolean isTop, ISimpleCallback callback);

    List<ConversationInfo> getTopConversationInfoList(int count,
                                                      long timestamp,
                                                      JIMConst.PullDirection direction);

    int getTotalUnreadCount();

    /**
     * Gets the total unread message count by conversation types.
     * @param conversationTypes Conversation type list.
     * @return Total unread message count.
     */
    int getUnreadCountWithTypes(int[] conversationTypes);

    /**
     * Gets the total unread message count by tag ID.
     * @param tagId Tag ID.
     * @return Total unread message count.
     */
    int getUnreadCountWithTag(String tagId);

    void clearUnreadCount(Conversation conversation, ISimpleCallback callback);

    void clearTotalUnreadCount(ISimpleCallback callback);

    void setUnread(Conversation conversation, ISimpleCallback callback);

    /**
     * Adds a conversation tag.
     * @param tagId Tag ID.
     * @param tagName Tag name.
     * @param callback Result callback.
     */
    void createConversationTag(String tagId, String tagName, ISimpleCallback callback);

    /**
     * Deletes a conversation tag.
     * @param tagId Tag ID.
     * @param callback Result callback.
     */
    void destroyConversationTag(String tagId, ISimpleCallback callback);

    /**
     * Updates a conversation tag name.
     * @param tagId Tag ID.
     * @param tagName Tag name.
     * @param callback Result callback.
     */
    void updateConversationTagName(String tagId, String tagName, ISimpleCallback callback);

    /**
     * Gets the cached conversation tag list.
     * @return Conversation tag list.
     */
    List<ConversationTagInfo> getCachedConversationTagList();

    /**
     * Gets the conversation tag list.
     * @param callback Result callback.
     */
    void getConversationTagList(JIMConst.IResultListCallback<ConversationTagInfo> callback);

    /**
     * Gets all tags for a specific conversation.
     * @param conversation Conversation identifier.
     * @return Tag list for the specific conversation.
     */
    List<ConversationTagInfo> getTagsForConversation(Conversation conversation);

    /**
     * Adds conversations to a tag.
     * @param conversations Conversation list.
     * @param tagId Tag ID.
     * @param callback Result callback.
     */
    void addConversationsToTag(List<Conversation> conversations, String tagId, ISimpleCallback callback);

    /**
     * Removes conversations from a tag.
     * @param conversations Conversation list.
     * @param tagId Tag ID.
     * @param callback Result callback.
     */
    void removeConversationsFromTag(List<Conversation> conversations, String tagId, ISimpleCallback callback);

    void setTopConversationsOrderType(JIMConst.TopConversationsOrderType type);

    void addListener(String key, IConversationListener listener);

    void removeListener(String key);

    void addSyncListener(String key, IConversationSyncListener listener);

    void removeSyncListener(String key);

    void addTagListener(String key, IConversationTagListener listener);

    void removeTagListener(String key);

    interface IConversationListener {
        void onConversationInfoAdd(List<ConversationInfo> conversationInfoList);

        void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList);

        void onConversationInfoDelete(List<ConversationInfo> conversationInfoList);

        void onTotalUnreadMessageCountUpdate(int count);
    }

    interface IConversationSyncListener {
        void onConversationSyncComplete();
    }

    interface IConversationTagListener {
        void onTagCreate(ConversationTagInfo tagInfo);
        void onTagDestroy(String tagId);
        void onTagNameUpdate(String tagId, String tagName);
        void onConversationsAddToTag(String tagId, List<Conversation> conversations);
        void onConversationsRemoveFromTag(String tagId, List<Conversation> conversations);
    }
}
