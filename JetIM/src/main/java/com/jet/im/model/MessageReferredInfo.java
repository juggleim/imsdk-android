package com.jet.im.model;

/**
 * @author Ye_Guli
 * @create 2024-06-07 9:59
 */
public class MessageReferredInfo {
    //被引用消息的 messageId
    private String messageId;
    //发送者 id
    private String senderId;
    //被引用消息的 messageContent
    private MessageContent content;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public MessageContent getContent() {
        return content;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }
}