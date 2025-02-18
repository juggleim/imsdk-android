package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IConversationManager;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.internal.contracts.TaskQueueContract;
import com.jet.im.kit.internal.contracts.TaskQueueImpl;
import com.jet.im.kit.internal.tasks.JobTask;
import com.jet.im.kit.internal.testmodel.ChannelListViewModelDataContract;
import com.jet.im.kit.log.Logger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.sendbird.android.channel.GroupChannel;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel preparing and managing data related with the list of channels
 * <p>
 * since 3.0.0
 */
public class ChannelListViewModel extends BaseViewModel implements OnPagedDataLoader<List<ConversationInfo>>, IConversationManager.IConversationListener {

    @NonNull
    private final IConversationManager query;
    @NonNull
    private final MutableLiveData<List<ConversationInfo>> channelList;

    @NonNull
    private final TaskQueueContract taskQueue;

    private boolean mHasNext = true;

    @Nullable
    @VisibleForTesting
    ChannelListViewModelDataContract contract;

    /**
     * Constructor
     *
     * @param query A query to retrieve {@code GroupChannel} list for the current user
     */
    public ChannelListViewModel(@Nullable IConversationManager query) {
        this(query, new SendbirdUIKitImpl(), new TaskQueueImpl());
    }

    @VisibleForTesting
    ChannelListViewModel(@Nullable IConversationManager query, @NonNull SendbirdUIKitContract sendbirdUIKit, @NonNull TaskQueueContract taskQueue) {
        super(sendbirdUIKit);
        this.query = query == null ? createGroupChannelListQuery() : query;
        this.channelList = new MutableLiveData<>();
        this.taskQueue = taskQueue;
    }

    @TestOnly
    ChannelListViewModel(@NonNull ChannelListViewModelDataContract contract) {
        super(contract.getSendbirdUIKit());
        this.query = contract.getQuery();
        this.channelList = contract.getChannelList();
        this.taskQueue = contract.getTaskQueue();
        this.contract = contract;
    }

    @TestOnly
    boolean isSameProperties(@NonNull ChannelListViewModelDataContract contract) {
        // It's enough to check the instance's reference.
        return contract.getSendbirdUIKit() == this.sendbirdUIKit
                && contract.getQuery() == query
                && contract.getChannelList() == channelList
                && contract.getTaskQueue() == taskQueue;
    }

    /**
     * Live data that can be observed for a list of channels.
     *
     * @return LiveData holding the list of {@code GroupChannel} for the current user
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<ConversationInfo>> getChannelList() {
        return channelList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        JIM.getInstance().getConversationManager().removeListener("ChannelListViewModel");
    }

    /**
     * Returns {@code false} as the channel list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Returns the empty list as the channel list do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<ConversationInfo> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNext() {
        return mHasNext;
    }

    /**
     * Requests the list of <code>GroupChannel</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     * <p>
     * since 3.0.0
     */
    public void loadInitial() {
        this.channelList.postValue(new ArrayList<>());
        JIM.getInstance().getConversationManager().addListener("ChannelListViewModel", this);

        taskQueue.addTask(new JobTask<List<ConversationInfo>>() {
            @Override
            protected List<ConversationInfo> call() throws Exception {
                return loadNext();
            }
        });
    }

    /**
     * Requests the list of <code>GroupChannel</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * @return Returns the queried list of <code>GroupChannel</code>s if no error occurs
     */
    @NonNull
    @Override
    public List<ConversationInfo> loadNext() {
        if (!hasNext()) return Collections.emptyList();

        List<ConversationInfo> conversationInfoList = loadMoreBlocking();
        conversationInfoList = mergeConversations(channelList.getValue(), conversationInfoList);
        channelList.postValue(conversationInfoList);
        return conversationInfoList;
    }

    @NonNull
    private List<ConversationInfo> loadMoreBlocking() {
        long timestamp = 0;
        if (channelList.getValue() != null && !channelList.getValue().isEmpty()) {
            timestamp = channelList.getValue().get(channelList.getValue().size() - 1).getSortTime();
        }
        int[] types = {Conversation.ConversationType.PRIVATE.getValue(), Conversation.ConversationType.GROUP.getValue()};
        List<ConversationInfo> conversationInfoList = JIM.getInstance().getConversationManager().getConversationInfoList(types, 20, timestamp, JIMConst.PullDirection.OLDER);
        if (conversationInfoList != null && conversationInfoList.size() < 20) {
            mHasNext = false;
        }
        if (conversationInfoList == null) {
            conversationInfoList = new ArrayList<>();
        }
        return conversationInfoList;
    }

    /**
     * Sets push notification settings of this channel.
     *
     * @param channel Target GroupChannel
     * @param enable  Whether the push notification turns on
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void setPushNotification(@NonNull GroupChannel channel, boolean enable, @Nullable OnCompleteHandler handler) {
        channel.setMyPushTriggerOption(enable ? GroupChannel.PushTriggerOption.ALL :
                        GroupChannel.PushTriggerOption.OFF,
                e -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ setPushNotification enable : %s result : %s", enable, e == null ? "success" : "error");
                });
    }

    public void setRead(ConversationInfo conversationInfo) {
        JIM.getInstance().getConversationManager().clearUnreadCount(conversationInfo.getConversation(), new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    public void setUnread(ConversationInfo conversationInfo) {
        JIM.getInstance().getConversationManager().setUnread(conversationInfo.getConversation(), new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    public void mute(ConversationInfo conversationInfo, boolean isMute) {
        JIM.getInstance().getConversationManager().setMute(conversationInfo.getConversation(), isMute, new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    public void delete(ConversationInfo conversationInfo) {
        JIM.getInstance().getConversationManager().deleteConversationInfo(conversationInfo.getConversation(), new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    /**
     * Leaves the targeted channel.
     *
     * @param channel Target GroupChannel
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void leaveChannel(@NonNull final GroupChannel channel, @Nullable OnCompleteHandler handler) {
        channel.leave(false, e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }

    /**
     * Tries to connect Sendbird Server.
     *
     * @param handler Callback notifying the result of authentication
     *                since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((e) -> {
            if (e == null) {
                handler.onAuthenticated();
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates group channel list query.
     *
     * @return {@code GroupChannelListQuery} to retrieve the list of channels
     * since 3.0.0
     */
    @NonNull
    protected IConversationManager createGroupChannelListQuery() {
        return JIM.getInstance().getConversationManager();
    }

    @Override
    public void onConversationInfoAdd(List<ConversationInfo> conversationInfoList) {
        List<ConversationInfo> mergeList = mergeConversations(channelList.getValue(), conversationInfoList);
        channelList.postValue(mergeList);
    }

    @Override
    public void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList) {
        List<ConversationInfo> mergeList = mergeConversations(channelList.getValue(), conversationInfoList);
        channelList.postValue(mergeList);
    }

    @Override
    public void onConversationInfoDelete(List<ConversationInfo> conversationInfoList) {
        List<ConversationInfo> list = channelList.getValue();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ConversationInfo newInfo : conversationInfoList) {
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                ConversationInfo oldInfo = list.get(i);
                if (newInfo.getConversation().equals(oldInfo.getConversation())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                list.remove(index);
            }
        }
        channelList.postValue(list);
    }

    @Override
    public void onTotalUnreadMessageCountUpdate(int count) {

    }

    private List<ConversationInfo> mergeConversations(List<ConversationInfo> oldList, List<ConversationInfo> newList) {
        if (newList == null) {
            return oldList;
        }
        if (oldList == null) {
            oldList = new ArrayList<>();
        }
        ConversationInfo toBeRemove;
        for (ConversationInfo newInfo : newList) {
            toBeRemove = null;
            for (ConversationInfo oldInfo : oldList) {
                if (oldInfo.getConversation().equals(newInfo.getConversation())) {
                    toBeRemove = oldInfo;
                    break;
                }
            }
            if (toBeRemove != null) {
                oldList.remove(toBeRemove);
            }
            oldList.add(newInfo);
        }
        List<ConversationInfo> topConversationInfoList = new ArrayList<>();
        List<ConversationInfo> notTopConversationInfoList = new ArrayList<>();
        for (ConversationInfo info : oldList) {
            if (info.isTop()) {
                topConversationInfoList.add(info);
            } else {
                notTopConversationInfoList.add(info);
            }
        }
        Comparator<ConversationInfo> topComparator = Comparator.comparingLong(ConversationInfo::getTopTime).reversed();
        Comparator<ConversationInfo> notTopComparator = Comparator.comparingLong(ConversationInfo::getSortTime).reversed();
        topConversationInfoList.sort(topComparator);
        notTopConversationInfoList.sort(notTopComparator);
        List<ConversationInfo> result = new ArrayList<>();
        result.addAll(topConversationInfoList);
        result.addAll(notTopConversationInfoList);
        return result;
    }
}
