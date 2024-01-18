package com.jet.im.internal;

import com.jet.im.JetIMConst;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IMessageManager;
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
}
