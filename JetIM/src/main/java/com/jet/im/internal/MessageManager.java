package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.SendMessageCallback;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.messages.TextMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements IMessageManager {

    public MessageManager(JetIMCore core) {
        this.mCore = core;
        ContentTypeCenter.getInstance().registerContentType(TextMessage.class);
    }
    private final JetIMCore mCore;

    @Override
    public void sendMessage(MessageContent content, Conversation conversation, ISendMessageCallback callback) {
        ConcreteMessage message = new ConcreteMessage();
        message.setContent(content);
        message.setConversation(conversation);
        message.setContentType(content.getContentType());
        message.setDirection(Message.MessageDirection.SEND);
        message.setState(Message.MessageState.SENDING);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());

        //todo db clientMsgNo
        SendMessageCallback messageCallback = new SendMessageCallback(0) {
            @Override
            public void onSuccess(long clientMsgNo, String msgId, long timestamp, long msgIndex) {
                //todo sync time
                //todo update message
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

        // mCore.
        mCore.getWebSocket().sendIMMessage(content, conversation, 0, message.getClientUid(), messageCallback);
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction) {
        return null;
    }

    @Override
    public void deleteMessageByClientMsgNo(long clientMsgNo) {

    }

    @Override
    public void deleteMessageByMessageId(String messageId) {

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


    void syncMessage() {
        mCore.getWebSocket().setMessageListener(new JWebSocket.IWebSocketMessageListener() {
            @Override
            public void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished) {
                //todo 排重
                //todo cmd message 吞掉
                mCore.getDbManager().insertMessages(messages);
                boolean sendDirection = false;
                boolean receiveDirection = false;
                for (int i = messages.size() - 1; i >= 0; i--) {
                    ConcreteMessage message = messages.get(i);
                    if (!sendDirection) {
                        if (message.getDirection() == Message.MessageDirection.SEND) {
                            sendDirection = true;
                            mCore.setMessageSendSyncTime(message.getTimestamp());
                        }
                    }
                    if (!receiveDirection) {
                        if (message.getDirection() == Message.MessageDirection.RECEIVE) {
                            receiveDirection = true;
                            mCore.setMessageReceiveTime(message.getTimestamp());
                        }
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageReceive(message);
                        }
                    }
                }
                if (!isFinished) {
                    sync();
                }
            }

            @Override
            public void onSyncNotify(long syncTime) {

            }
        });
        sync();
    }

    private void sync() {
        mCore.getWebSocket().syncMessages(mCore.getMessageReceiveTime(), mCore.getMessageSendSyncTime(), mCore.getUserId());
    }

    private String createClientUid() {
        long result = System.currentTimeMillis();
        result = result % 1000000;
        result = result * 1000 + mIncreaseId++;
        return Long.toString(result);
    }
    private int mIncreaseId = 0;
    private ConcurrentHashMap<String, IMessageListener> mListenerMap;
}
