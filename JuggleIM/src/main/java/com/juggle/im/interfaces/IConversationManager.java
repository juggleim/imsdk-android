package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
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
     * 根据查询条件获取会话信息列表
     * @param options 查询条件
     * @return 会话信息列表
     */
    List<ConversationInfo> getConversationInfoList(GetConversationOptions options);

    /**
     * 分页获取会话信息列表，结果按照会话时间倒序排列（新的在前，旧的在后）
     * @param count 拉取数量
     * @param timestamp 拉取时间戳（传 0 表示当前时间）
     * @param direction 拉取方向
     * @return 会话信息列表
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
     * 根据会话类型获取消息未读总数
     * @param conversationTypes 会话类型列表
     * @return 消息未读总数
     */
    int getUnreadCountWithTypes(int[] conversationTypes);

    /**
     * 根据标签 id 获取消息未读总数
     * @param tagId 标签 id
     * @return 消息未读总数
     */
    int getUnreadCountWithTag(String tagId);

    void clearUnreadCount(Conversation conversation, ISimpleCallback callback);

    void clearTotalUnreadCount(ISimpleCallback callback);

    void setUnread(Conversation conversation, ISimpleCallback callback);

    /**
     * 将会话添加到标签
     * @param conversations 会话列表
     * @param tagId 标签 id
     * @param callback 结果回调
     */
    void addConversationsToTag(List<Conversation> conversations, String tagId, ISimpleCallback callback);

    /**
     * 将会话从标签中删除
     * @param conversations 会话列表
     * @param tagId 标签 id
     * @param callback 结果回调
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
        void onConversationsAddToTag(String tagId, List<Conversation> conversations);
        void onConversationsRemoveFromTag(String tagId, List<Conversation> conversations);
    }
}
