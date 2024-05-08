package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.exception.SendbirdError;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.GetMessageChangeLogsHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.MessageChangeLogsParams;
import com.sendbird.android.params.MessageListParams;
import com.jet.im.kit.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class MessageChangeLogsPager {
    interface MessageChangeLogsResultHandler {
        void onError(@NonNull SendbirdException e);

        void onResult(@NonNull List<BaseMessage> addedMessageList, @NonNull List<BaseMessage> updatedMessageList, @NonNull List<Long> deletedMessageIdList);
    }

    private final BaseChannel channel;
    private final long lastSyncAt;
    private final MessageChangeLogsParams params;
    private final MessageListParams messageListParams;

    MessageChangeLogsPager(@NonNull BaseChannel channel, long lastSyncAt, @NonNull MessageListParams params) {
        this.channel = channel;
        this.lastSyncAt = lastSyncAt;
        this.params = MessageChangeLogsParams.from(params);
        this.messageListParams = params.clone();
        this.messageListParams.setPreviousResultSize(0);
        this.messageListParams.setNextResultSize(100);
    }

    void load(@Nullable final MessageChangeLogsResultHandler handler) {
        load(true, handler);
    }

    void load(boolean fetchAll, @Nullable final MessageChangeLogsResultHandler handler) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<BaseMessage> updatedMessageList = new ArrayList<>();
            final List<BaseMessage> addedMessageList = new ArrayList<>();
            final List<Long> deletedMessageIdList = new ArrayList<>();

            CountDownLatch lock = new CountDownLatch(1);
            final AtomicReference<String> tokenRef = new AtomicReference<>();
            final AtomicReference<SendbirdException> error = new AtomicReference<>();
            final AtomicBoolean hasMoreRef = new AtomicBoolean();

            try {
                load(lastSyncAt, params, (updated, deletedMessageIds, hasMore, token, e) -> {
                    try {
                        if (e != null) {
                            error.set(e);
                            if (handler != null) {
                                handler.onError(e);
                            }
                            return;
                        }
                        tokenRef.set(token);
                        hasMoreRef.set(hasMore);

                        if (updated != null) {
                            updatedMessageList.addAll(updated);
                        }
                        if (deletedMessageIds != null) {
                            deletedMessageIdList.addAll(deletedMessageIds);
                        }
                    } finally {
                        lock.countDown();
                    }
                });
                lock.await();

                while (hasMoreRef.get() && error.get() == null) {
                    CountDownLatch moreLock = new CountDownLatch(1);
                    more(tokenRef.get(), params, (updated, deletedMessageIds, hasMore, token, e) -> {
                        try {
                            if (e != null) {
                                error.set(e);
                                if (handler != null) {
                                    handler.onError(e);
                                }
                                return;
                            }

                            tokenRef.set(token);
                            hasMoreRef.set(hasMore);

                            if (updated != null) {
                                updatedMessageList.addAll(updated);
                            }
                            if (deletedMessageIds != null) {
                                deletedMessageIdList.addAll(deletedMessageIds);
                            }
                        } finally {
                            moreLock.countDown();
                        }
                    });
                    moreLock.await();
                }

                if (error.get() != null) {
                    throw error.get();
                }

                boolean hasMore;
                long timeStamp = lastSyncAt;
                do {
                    int totalSize = addedMessageList.size();
                    if (totalSize > 0) {
                        timeStamp = addedMessageList.get(0).getCreatedAt();
                    }
                    final List<BaseMessage> list = loadAddedMessageFromTimestamp(timeStamp);
                    addedMessageList.addAll(0, list);
                    hasMore = list.size() > 0;
                } while (hasMore && fetchAll);

                if (handler != null) {
                    handler.onResult(addedMessageList, updatedMessageList, deletedMessageIdList);
                }
            } catch (SendbirdException e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(e);
                }
            } catch (Exception e) {
                Logger.e(e);
                if (handler != null) {
                    handler.onError(new SendbirdException(e.getMessage(), SendbirdError.ERR_REQUEST_FAILED));
                }
            }
        });
    }

    private void load(long ts, MessageChangeLogsParams params, GetMessageChangeLogsHandler handler) {
        this.channel.getMessageChangeLogsSinceTimestamp(ts, params, handler);
    }

    private void more(String token, MessageChangeLogsParams params, GetMessageChangeLogsHandler handler) {
        this.channel.getMessageChangeLogsSinceToken(token, params, handler);
    }

    private List<BaseMessage> loadAddedMessageFromTimestamp(long ts) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        channel.getMessagesByTimestamp(ts, messageListParams, (messages, e) -> {
            try {
                if (e != null) {
                    error.set(e);
                    return;
                }
                result.set(messages);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) throw error.get();
        return result.get();
    }
}
