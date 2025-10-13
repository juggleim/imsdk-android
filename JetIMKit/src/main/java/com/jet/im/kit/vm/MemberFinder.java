package com.jet.im.kit.vm;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.IGroupMemberProvider;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MentionSuggestion;
import com.jet.im.kit.model.UserMentionConfig;
import com.jet.im.kit.utils.ClearableScheduledExecutorService;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MemberFinder {
    private final long debounceTime;
    @NonNull
    private final ClearableScheduledExecutorService executor = new ClearableScheduledExecutorService();
    @NonNull
    private final MutableLiveData<MentionSuggestion> userList = new MutableLiveData<>();
    @Nullable
    private List<UserInfo> totalUserList;
    private volatile boolean isLive = true;
    @Nullable
    private String lastNicknameStartWith;
    @Nullable
    private String lastEmptyResultKeyword;
    private final int maxSuggestionCount;
    @SuppressWarnings("ComparatorCombinators")
    private static final Comparator<UserInfo> ALPHABETICAL_COMPARATOR = (member1, member2) -> member1.getUserName().toLowerCase().compareTo(member2.getUserName().toLowerCase());

    MemberFinder(@NonNull UserMentionConfig mentionConfig, String groupId) {
        this.debounceTime = mentionConfig.getDebounceTime();
        this.maxSuggestionCount = mentionConfig.getMaxSuggestionCount();
        if (sGroupMemberProvider == null) {
            return;
        }
        sGroupMemberProvider.getGroupMembers(groupId, new IGroupMemberProvider.GroupMemberCallback() {
            @Override
            public void onMembersFetch(List<UserInfo> members, int code) {
                MemberFinder.this.totalUserList = members;
            }
        });
    }

    @NonNull
    public LiveData<MentionSuggestion> getMentionSuggestion() {
        return userList;
    }

    public synchronized void dispose() {
        this.executor.cancelAllJobs(true);
        this.isLive = false;
    }

    public synchronized void find(@Nullable String nicknameStartWith) {
        Logger.d(">> ChannelMemberFinder::request( nicknameStartWith=%s )", nicknameStartWith);
        if (!isLive) return;

        if (TextUtils.isNotEmpty(lastEmptyResultKeyword) && nicknameStartWith != null && nicknameStartWith.startsWith(lastEmptyResultKeyword)) {
            Logger.d("++ skip search because [%s] keyword must be empty.", nicknameStartWith);
            return;
        }

        // all previous requests must be cancel.
        executor.cancelAllJobs(true);
        executor.schedule(() -> {
            if (!isLive) return;
            if (nicknameStartWith == null) return;
            try {
                this.lastNicknameStartWith = nicknameStartWith;
                List<UserInfo> users = getFilteredMembers(nicknameStartWith, maxSuggestionCount);
                notifyMemberListChanged(nicknameStartWith, users);
            } catch (Throwable ignore) {
            }
        }, debounceTime, TimeUnit.MILLISECONDS);
    }

    @NonNull
    private List<UserInfo> getFilteredMembers(@NonNull String nicknameStartWith, int maxMemberCount) {
        Logger.d(">> MemberFinder::getFilteredMembers() nicknameStartWith=%s", nicknameStartWith);
        final List<UserInfo> filteredList = new ArrayList<>();
        final List<UserInfo> members = totalUserList;
        if (members == null) {
            return filteredList;
        }
        members.sort(ALPHABETICAL_COMPARATOR);
            final String myUserId = SendbirdUIKit.userId;
            for (UserInfo member : members) {
                final String nickname = member.getUserName();
                if (nickname.toLowerCase().startsWith(nicknameStartWith.toLowerCase()) && !myUserId.equalsIgnoreCase(member.getUserId())) {
                    if (filteredList.size() >= maxMemberCount) {
                        return filteredList;
                    }
                    filteredList.add(member);
                }
            }
        return filteredList;
    }

    @AnyThread
    private synchronized void notifyMemberListChanged(@NonNull String nicknameStartWith, @NonNull List<UserInfo> userList) {
        if (!isLive) return;

        // if the result of query is a previous request, it has not to delivery to the listener.
        if (lastNicknameStartWith != null && !lastNicknameStartWith.equals(nicknameStartWith)) return;

        // set the last empty keyword to avoid unnecessary request.
        this.lastEmptyResultKeyword = userList.isEmpty() ? nicknameStartWith : null;

        final MentionSuggestion mentionSuggestion = new MentionSuggestion(nicknameStartWith);
        if (!userList.isEmpty()) {
            mentionSuggestion.append(userList);
        }
        this.userList.postValue(mentionSuggestion);
    }

    public static void setGroupMemberProvider(IGroupMemberProvider provider) {
        sGroupMemberProvider = provider;
    }

    private static IGroupMemberProvider sGroupMemberProvider;

}
