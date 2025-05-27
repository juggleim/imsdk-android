package com.jet.im.kit.activities.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.UserInfo;
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
    private final List<Message> oldMessageList;
    @NonNull
    private final List<Message> newMessageList;
    @NonNull
    private final List<MessageReaction> oldReactionList;
    @NonNull
    private final List<MessageReaction> newReactionList;

    public MessageDiffCallback(@NonNull List<Message> oldMessageList, @NonNull List<Message> newMessageList,
                               @NonNull List<MessageReaction> oldReactionList, @NonNull List<MessageReaction> newReactionList
                               ) {
        this.oldReactionList = oldReactionList;
        this.newReactionList = newReactionList;
        this.oldMessageList = oldMessageList;
        this.newMessageList = newMessageList;
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
        Message oldMessage = oldMessageList.get(oldItemPosition);
        Message newMessage = newMessageList.get(newItemPosition);
        return oldMessage.getClientMsgNo() == newMessage.getClientMsgNo();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Message oldMessage = oldMessageList.get(oldItemPosition);
        Message newMessage = newMessageList.get(newItemPosition);

        if (oldMessage.getContentType() != null && newMessage.getContentType() != null && !oldMessage.getContentType().equals(newMessage.getContentType())) {
            return false;
        }
        if (oldMessage.getState() != newMessage.getState()) {
            return false;
        }
        if (oldMessage.isHasRead() != newMessage.isHasRead()) {
            return false;
        }
        if (oldMessage.getTimestamp() != newMessage.getTimestamp()) {
            return false;
        }
        if (oldMessage.getContent()!= null && newMessage.getContent()!= null && oldMessage.getContent().hashCode() != newMessage.getContent().hashCode()) {
            return false;
        }
        if (oldMessage.isDelete() != newMessage.isDelete()) {
            return false;
        }
        if (oldMessage.isEdit() != newMessage.isEdit()) {
            return false;
        }
        MessageReaction oldReaction = getMessageReaction(oldMessage.getMessageId(), oldReactionList);
        MessageReaction newReaction = getMessageReaction(newMessage.getMessageId(), newReactionList);
        if (oldReaction != null || newReaction != null) {
            return false;
        }
//        if (oldReaction == null && newReaction != null) {
//            return false;
//        }
//        if (oldReaction != null && newReaction == null) {
//            return false;
//        }
//        if (oldReaction != null && newReaction != null && !isSameReaction(oldReaction, newReaction)) {
//            return false;
//        }
        return true;
    }

    private MessageReaction getMessageReaction(String messageId, List<MessageReaction> reactionList) {
        for (MessageReaction reaction : reactionList) {
            if (reaction.getMessageId().equals(messageId)) {
                return reaction;
            }
        }
        return null;
    }

    private boolean isSameReaction(MessageReaction oldReaction, MessageReaction newReaction) {
        if (!oldReaction.getMessageId().equals(newReaction.getMessageId())) {
            return false;
        }
        List<MessageReactionItem> oldItemList = oldReaction.getItemList();
        List<MessageReactionItem> newItemList = newReaction.getItemList();
        if (oldItemList.size() != newItemList.size()) {
            return false;
        }
        boolean hasMatched;
        for (MessageReactionItem oldItem : oldItemList) {
            hasMatched = false;
            for (MessageReactionItem newItem : newItemList) {
                if (oldItem.getReactionId().equals(newItem.getReactionId())) {
                    hasMatched = true;
                    if (!isSameUserList(oldItem.getUserInfoList(), newItem.getUserInfoList())) {
                        return false;
                    }
                    break;
                }
            }
            if (!hasMatched) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameUserList(List<UserInfo> oldUserList, List<UserInfo> newUserList) {
        if (oldUserList.size() != newUserList.size()) {
            return false;
        }
        boolean hasMatched;
        for (UserInfo oldUser : oldUserList) {
            hasMatched = false;
            for (UserInfo newUser : newUserList) {
                if (oldUser.getUserId().equals(newUser.getUserId())) {
                    hasMatched = true;
                    break;
                }
            }
            if (!hasMatched) {
                return false;
            }
        }
        return true;
    }
}
