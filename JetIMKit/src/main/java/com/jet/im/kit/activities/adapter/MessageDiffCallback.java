package com.jet.im.kit.activities.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.CustomizableMessage;
import com.sendbird.android.message.Reaction;
import com.sendbird.android.message.ThreadInfo;
import com.sendbird.android.user.User;
import com.jet.im.kit.consts.MessageGroupType;
import com.jet.im.kit.consts.ReplyType;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.TypingIndicatorMessage;
import com.jet.im.kit.utils.MessageUtils;

import java.util.List;
import java.util.Map;

class MessageDiffCallback extends DiffUtil.Callback {
    @NonNull
    private final List<BaseMessage> oldMessageList;
    @NonNull
    private final List<BaseMessage> newMessageList;
    @Nullable
    private final GroupChannel oldChannel;
    @NonNull
    private final GroupChannel newChannel;
    @NonNull
    private final MessageListUIParams messageListUIParams;

    public MessageDiffCallback(@Nullable GroupChannel oldChannel, @NonNull GroupChannel newChannel,
                               @NonNull List<BaseMessage> oldMessageList, @NonNull List<BaseMessage> newMessageList,
                               @NonNull MessageListUIParams messageListUIParams) {
        this.oldChannel = oldChannel;
        this.newChannel = newChannel;
        this.oldMessageList = oldMessageList;
        this.newMessageList = newMessageList;
        this.messageListUIParams = messageListUIParams;
    }

    @Override
    public int getOldListSize() {
        return oldMessageList.size();
    }

    @Override
    public int getNewListSize() {
        return newMessageList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
        BaseMessage newMessage = newMessageList.get(newItemPosition);
        String oldItemId = getItemId(oldMessage);
        String newItemId = getItemId(newMessage);
        return oldItemId.equals(newItemId);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldChannel == null) return false;
        BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
        BaseMessage newMessage = newMessageList.get(newItemPosition);

        if (oldMessage.getSendingStatus() != newMessage.getSendingStatus()) {
            return false;
        }

        if (oldMessage.getUpdatedAt() != newMessage.getUpdatedAt()) {
            return false;
        }

        // In the case of Bot message stream mode, the `message` is updated, but `updated_at` is not updated.
        boolean isBotOld = oldMessage.getSender() != null && oldMessage.getSender().isBot();
        boolean isBotNew = newMessage.getSender() != null && newMessage.getSender().isBot();
        if (isBotOld && isBotNew) {
            if (!oldMessage.getMessage().equals(newMessage.getMessage())) {
                return false;
            }
        }

        Map<String, String> oldExtendedMessagePayload = oldMessage.getExtendedMessagePayload();
        Map<String, String> newExtendedMessagePayload = newMessage.getExtendedMessagePayload();
        if (!oldExtendedMessagePayload.equals(newExtendedMessagePayload)) {
            return false;
        }

        if (messageListUIParams.shouldUseMessageReceipt()) {
            if (oldChannel.getUnreadMemberCount(newMessage) != newChannel.getUnreadMemberCount(newMessage)) {
                return false;
            }

            if (oldChannel.getUndeliveredMemberCount(newMessage) != newChannel.getUndeliveredMemberCount(newMessage)) {
                return false;
            }
        }

        if (oldChannel.isFrozen() != newChannel.isFrozen()) {
            return false;
        }

        if (oldChannel.getMyRole() != newChannel.getMyRole()) {
            return false;
        }

        List<Reaction> oldReactions = oldMessage.getReactions();
        List<Reaction> newReactions = newMessage.getReactions();

        if (oldReactions.size() != newReactions.size()) {
            return false;
        }

        for (int i = 0; i < oldReactions.size(); i++) {
            Reaction oldReaction = oldReactions.get(i);
            Reaction newReaction = newReactions.get(i);

            if (!oldReaction.equals(newReaction)) {
                return false;
            }

            if (!oldReaction.getUserIds().equals(newReaction.getUserIds())) {
                return false;
            }
        }

        if (oldMessage.getOgMetaData() == null && newMessage.getOgMetaData() != null) {
            return false;
        } else if (oldMessage.getOgMetaData() != null && !oldMessage.getOgMetaData().equals(newMessage.getOgMetaData())) {
            return false;
        }

        if (oldMessage.getMyFeedbackStatus() != newMessage.getMyFeedbackStatus()) {
            return false;
        }

        if (oldMessage.getMyFeedback() != newMessage.getMyFeedback()) {
            return false;
        }

        if (oldMessage instanceof TypingIndicatorMessage && newMessage instanceof TypingIndicatorMessage) {
            return ((TypingIndicatorMessage) oldMessage).getTypingUsers().equals(((TypingIndicatorMessage) newMessage).getTypingUsers()) ;
        }
        if (messageListUIParams.shouldUseMessageGroupUI()) {
            BaseMessage oldPrevMessage = oldItemPosition - 1 < 0 ? null : oldMessageList.get(oldItemPosition - 1);
            BaseMessage newPrevMessage = newItemPosition - 1 < 0 ? null : newMessageList.get(newItemPosition - 1);
            BaseMessage oldNextMessage = oldItemPosition + 1 >= oldMessageList.size() ? null : oldMessageList.get(oldItemPosition + 1);
            BaseMessage newNextMessage = newItemPosition + 1 >= newMessageList.size() ? null : newMessageList.get(newItemPosition + 1);
            MessageGroupType oldMessageGroupType = MessageUtils.getMessageGroupType(oldPrevMessage, oldMessage, oldNextMessage, messageListUIParams);
            MessageGroupType newMessageGroupType = MessageUtils.getMessageGroupType(newPrevMessage, newMessage, newNextMessage, messageListUIParams);

            return oldMessageGroupType == newMessageGroupType;
        }

        return true;
    }

    private String getItemId(@NonNull BaseMessage item) {
        if (TextUtils.isEmpty(item.getRequestId())) {
            return String.valueOf(item.getMessageId());
        } else {
            return item.getRequestId();
        }
    }
}
