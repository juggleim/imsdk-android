package com.jet.im.kit.vm;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.params.MemberListQueryParams;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.User;
import com.sendbird.android.user.query.MemberListQuery;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MentionSuggestion;
import com.jet.im.kit.model.UserMentionConfig;
import com.jet.im.kit.utils.ClearableScheduledExecutorService;
import com.jet.im.kit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class MemberFinder {
    @NonNull
    private final GroupChannel channel;
    private final long debounceTime;
    @NonNull
    private final ClearableScheduledExecutorService executor = new ClearableScheduledExecutorService();
    @NonNull
    private final MutableLiveData<MentionSuggestion> userList = new MutableLiveData<>();
    @Nullable
    private MemberListQuery query;
    private volatile boolean isLive = true;
    @Nullable
    private String lastNicknameStartWith;
    @Nullable
    private String lastEmptyResultKeyword;
    private final int maxSuggestionCount;
    @SuppressWarnings("ComparatorCombinators")
    private static final Comparator<Member> ALPHABETICAL_COMPARATOR = (member1, member2) -> member1.getNickname().toLowerCase().compareTo(member2.getNickname().toLowerCase());

    MemberFinder(@NonNull GroupChannel channel, @NonNull UserMentionConfig mentionConfig) {
        this.debounceTime = mentionConfig.getDebounceTime();
        this.maxSuggestionCount = mentionConfig.getMaxSuggestionCount();
        this.channel = channel;
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
                List<User> users;
                if (!channel.isSuper()) {
                    users = getFilteredMembers(channel, nicknameStartWith, maxSuggestionCount);
                } else {
                    this.query = createMemberListQuery(channel, nicknameStartWith, maxSuggestionCount + 1);
                    users = getFilteredMembers(query);
                }
                notifyMemberListChanged(nicknameStartWith, users);
            } catch (Throwable ignore) {
            }
        }, debounceTime, TimeUnit.MILLISECONDS);
    }

    @NonNull
    private List<User> getFilteredMembers(@NonNull GroupChannel channel, @NonNull String nicknameStartWith, int maxMemberCount) {
        Logger.d(">> MemberFinder::getFilteredMembers() nicknameStartWith=%s", nicknameStartWith);
        final List<User> filteredList = new ArrayList<>();
        final List<Member> members = channel.getMembers();
        Collections.sort(members, ALPHABETICAL_COMPARATOR);
        if (SendbirdUIKit.getAdapter() != null) {
            final String myUserId = SendbirdUIKit.getAdapter().getUserInfo().getUserId();
            for (Member member : members) {
                if (!member.isActive()) continue;
                final String nickname = member.getNickname();
                if (nickname.toLowerCase().startsWith(nicknameStartWith.toLowerCase()) && !myUserId.equalsIgnoreCase(member.getUserId())) {
                    if (filteredList.size() >= maxMemberCount) {
                        return filteredList;
                    }
                    filteredList.add(member);
                }
            }
        }
        return filteredList;
    }

    @NonNull
    private List<User> getFilteredMembers(@NonNull MemberListQuery query) throws Exception {
        Logger.d(">> MemberFinder::requestNext() nicknameStartWith=%s", lastNicknameStartWith);
        if (channel.isBroadcast()) return Collections.emptyList();

        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<List<Member>> results = new AtomicReference<>();
        final AtomicReference<SendbirdException> error = new AtomicReference<>();
        query.next((queryResult, e) -> {
            try {
                error.set(e);
                results.set(queryResult);
            } finally {
                lock.countDown();
            }
        });
        lock.await();
        if (error.get() != null) throw new SendbirdException("Error");

        final List<User> filteredList = new ArrayList<>();
        if (SendbirdUIKit.getAdapter() != null) {
            final String myUserId = SendbirdUIKit.getAdapter().getUserInfo().getUserId();
            for (Member member : results.get()) {
                if (!member.isActive()) continue;
                if (!myUserId.equalsIgnoreCase(member.getUserId())) {
                    if (filteredList.size() >= maxSuggestionCount) {
                        return filteredList;
                    }
                    filteredList.add(member);
                }
            }
        }
        Logger.d("____ result size=%s", results.get().size());
        return filteredList;
    }

    @NonNull
    private static MemberListQuery createMemberListQuery(@NonNull GroupChannel channel, @NonNull String nicknameStartWith, int maxMemberCount) {
        MemberListQueryParams memberListQueryParams = new MemberListQueryParams();
        memberListQueryParams.setLimit(maxMemberCount);
        memberListQueryParams.setNicknameStartsWithFilter(nicknameStartWith);
        return channel.createMemberListQuery(memberListQueryParams);
    }

    @AnyThread
    private synchronized void notifyMemberListChanged(@NonNull String nicknameStartWith, @NonNull List<User> userList) {
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
}
