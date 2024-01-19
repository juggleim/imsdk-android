package com.jet.im.internal;

import com.jet.im.JetIMConst;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.internal.core.network.WebSocketSendMessageCallback;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;

import java.util.List;

public class MessageManager implements IMessageManager {

    public MessageManager(JetIMCore core) {
        this.mCore = core;
    }
    private JetIMCore mCore;

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
        WebSocketSendMessageCallback messageCallback = new WebSocketSendMessageCallback(0) {
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

    }

    @Override
    public void setListener(IMessageListener listener) {

    }

    private String createClientUid() {
        long result = System.currentTimeMillis();
        result = result % 1000000;
        result = result * 1000 + mIncreaseId++;
        return Long.toString(result);
    }
    private int mIncreaseId = 0;
}
