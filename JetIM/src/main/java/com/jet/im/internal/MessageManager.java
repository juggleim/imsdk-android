package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.SendMessageCallback;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.TextMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;
import com.jet.im.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements IMessageManager {

    public MessageManager(JetIMCore core) {
        this.mCore = core;
        ContentTypeCenter.getInstance().registerContentType(TextMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ImageMessage.class);
        ContentTypeCenter.getInstance().registerContentType(FileMessage.class);
        ContentTypeCenter.getInstance().registerContentType(VoiceMessage.class);
        ContentTypeCenter.getInstance().registerContentType(VideoMessage.class);
    }
    private final JetIMCore mCore;

    @Override
    public Message sendMessage(MessageContent content, Conversation conversation, ISendMessageCallback callback) {
        ConcreteMessage message = new ConcreteMessage();
        message.setContent(content);
        message.setConversation(conversation);
        message.setContentType(content.getContentType());
        message.setDirection(Message.MessageDirection.SEND);
        message.setState(Message.MessageState.SENDING);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());

        List<ConcreteMessage> list = new ArrayList<>(1);
        list.add(message);
        mCore.getDbManager().insertMessages(list);
        SendMessageCallback messageCallback = new SendMessageCallback(message.getClientMsgNo()) {
            @Override
            public void onSuccess(long clientMsgNo, String msgId, long timestamp, long msgIndex) {
                if (mSyncProcessing) {
                    mCachedSendTime = timestamp;
                } else {
                    mCore.setMessageSendSyncTime(timestamp);
                }
                mCore.getDbManager().updateMessageAfterSend(clientMsgNo, msgId, timestamp, msgIndex);
                message.setClientMsgNo(clientMsgNo);
                message.setMessageId(msgId);
                message.setTimestamp(timestamp);
                message.setMsgIndex(msgIndex);
                message.setState(Message.MessageState.SENT);
                if (callback != null) {
                    callback.onSuccess(message);
                }
            }

            @Override
            public void onError(int errorCode, long clientMsgNo) {
                if (callback != null) {
                    message.setClientMsgNo(clientMsgNo);
                    callback.onError(message, errorCode);
                }
            }
        };
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().sendIMMessage(content, conversation, message.getClientUid(), messageCallback);
        }
        return message;
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction) {
        return getMessages(conversation, count, timestamp, direction, new ArrayList<>());
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return mCore.getDbManager().getMessages(conversation, count, timestamp, direction, contentTypes);
    }

    @Override
    public List<Message> getMessagesByMessageIds(List<String> messageIdList) {
        return mCore.getDbManager().getMessagesByMessageIds(messageIdList);
    }

    @Override
    public List<Message> getMessagesByClientMsgNos(long[] clientMsgNos) {
        return mCore.getDbManager().getMessagesByClientMsgNos(clientMsgNos);
    }

    @Override
    public void deleteMessageByClientMsgNo(long clientMsgNo) {
        mCore.getDbManager().deleteMessageByClientMsgNo(clientMsgNo);
    }

    @Override
    public void deleteMessageByMessageId(String messageId) {
        mCore.getDbManager().deleteMessageByMessageId(messageId);
    }

    @Override
    public void clearMessages(Conversation conversation) {
        mCore.getDbManager().clearMessages(conversation);
    }

    @Override
    public void registerContentType(Class<? extends MessageContent> messageContentClass) {
        ContentTypeCenter.getInstance().registerContentType(messageContentClass);
    }

    @Override
    public void addListener(String key, IMessageListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mListenerMap == null) {
            mListenerMap = new ConcurrentHashMap<>();
        }
        mListenerMap.put(key, listener);
    }

    @Override
    public void removeListener(String key) {
        if (!TextUtils.isEmpty(key) && mListenerMap != null) {
            mListenerMap.remove(key);
        }
    }

    @Override
    public void addSyncListener(String key, IMessageSyncListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mSyncListenerMap == null) {
            mSyncListenerMap = new ConcurrentHashMap<>();
        }
        mSyncListenerMap.put(key, listener);
    }

    @Override
    public void removeSyncListener(String key) {
        if (!TextUtils.isEmpty(key) && mSyncListenerMap != null) {
            mSyncListenerMap.remove(key);
        }
    }


    void syncMessage() {
        mSyncProcessing = true;
        if (!mHasSetMessageListener) {
            mHasSetMessageListener = true;
            if (mCore.getWebSocket() != null) {
                mCore.getWebSocket().setMessageListener(new JWebSocket.IWebSocketMessageListener() {
                    @Override
                    public void onMessageReceive(ConcreteMessage message) {
                        List<ConcreteMessage> list = new ArrayList<>();
                        list.add(message);
                        handleReceiveMessages(list);
                    }

                    @Override
                    public void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished) {
                        handleReceiveMessages(messages);

                        if (!isFinished) {
                            sync();
                        } else {
                            mSyncProcessing = false;
                            if (mCachedSendTime > 0) {
                                mCore.setMessageSendSyncTime(mCachedSendTime);
                                mCachedSendTime = -1;
                            }
                            if (mCachedReceiveTime > 0) {
                                mCore.setMessageReceiveTime(mCachedReceiveTime);
                                mCachedReceiveTime = -1;
                            }
                            if (mSyncListenerMap != null) {
                                for (Map.Entry<String, IMessageSyncListener> entry : mSyncListenerMap.entrySet()) {
                                    entry.getValue().onMessageSyncComplete();
                                }
                            }
                        }
                    }

                    @Override
                    public void onSyncNotify(long syncTime) {
                        LoggerUtils.d("onSyncNotify, syncTime is " + syncTime + ", receiveSyncTime is " + mCore.getMessageReceiveTime());
                        if (syncTime > mCore.getMessageReceiveTime()) {
                            mSyncProcessing = true;
                            sync();
                        }

                    }
                });
            }
        }
        sync();
    }

    private void handleReceiveMessages(List<ConcreteMessage> messages) {
        //todo 排重
        //todo cmd message 吞掉
        mCore.getDbManager().insertMessages(messages);

        long sendTime = 0;
        long receiveTime = 0;
        for (ConcreteMessage message : messages) {
            if (message.getDirection() == Message.MessageDirection.SEND) {
                sendTime = message.getTimestamp();
            } else if (message.getDirection() == Message.MessageDirection.RECEIVE) {
                receiveTime = message.getTimestamp();
            }
            if (mListenerMap != null) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onMessageReceive(message);
                }
            }
        }
        if (mSyncProcessing) {
            if (sendTime > 0) {
                mCachedSendTime = sendTime;
            }
            if (receiveTime > 0) {
                mCachedReceiveTime = receiveTime;
            }
        } else {
            if (sendTime > 0) {
                mCore.setMessageSendSyncTime(sendTime);
            }
            if (receiveTime > 0) {
                mCore.setMessageReceiveTime(receiveTime);
            }
        }
    }

    private void sync() {
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().syncMessages(mCore.getMessageReceiveTime(), mCore.getMessageSendSyncTime(), mCore.getUserId());
        }
    }

    private String createClientUid() {
        long result = System.currentTimeMillis();
        result = result % 1000000;
        result = result * 1000 + mIncreaseId++;
        return Long.toString(result);
    }
    private int mIncreaseId = 0;
    private boolean mSyncProcessing = false;
    private long mCachedReceiveTime = -1;
    private long mCachedSendTime = -1;
    private boolean mHasSetMessageListener = false;
    private ConcurrentHashMap<String, IMessageListener> mListenerMap;
    private ConcurrentHashMap<String, IMessageSyncListener> mSyncListenerMap;
}
