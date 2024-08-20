package com.jet.im.kit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.JIM;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory;
import com.jet.im.kit.consts.MessageGroupType;
import com.jet.im.kit.consts.ReplyType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.TimelineMessage;
import com.juggle.im.model.Message;
import com.sendbird.android.channel.NotificationData;
import com.sendbird.android.message.AdminMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.CustomizableMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MessageMetaArray;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    public static boolean isMine(@NonNull BaseMessage message) {
        if (message.getSender() == null) {
            return false;
        }
        return isMine(message.getSender().getUserId());
    }

    public static boolean isMine(@NonNull Message message) {
        if (message.getSenderUserId() == null) {
            return false;
        }
        return isMine(message.getSenderUserId());
    }

    public static boolean isMine(@Nullable String senderId) {
        String currentUser = JIM.getInstance().getCurrentUserId();
        if (currentUser != null) {
            return currentUser.equals(senderId);
        }
        return false;
    }

    public static boolean isDeletableMessage(@NonNull BaseMessage message) {
        if (message instanceof UserMessage || message instanceof FileMessage) {
            return isMine(message.getSender().getUserId()) && !hasThread(message);
        }
        return false;
    }

    public static boolean isUnknownType(@NonNull BaseMessage message) {
        MessageType messageType = MessageViewHolderFactory.getMessageType(message);
        return messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME || messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
    }
    public static boolean isUnknownType(@NonNull Message message) {
        MessageType messageType = MessageViewHolderFactory.getMessageType(message);
        return messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME || messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
    }

    public static boolean isFailed(@NonNull Message message) {
        final Message.MessageState status = message.getState();
        return status == Message.MessageState.FAIL;
    }

    public static boolean isSucceed(@NonNull Message message) {
        final Message.MessageState status = message.getState();
        return status == Message.MessageState.SENT;
    }

    public static boolean isGroupChanged(@Nullable Message frontMessage, @Nullable Message backMessage, @NonNull MessageListUIParams messageListUIParams) {
        return frontMessage == null ||
                frontMessage.getSenderUserId() == null ||
                backMessage == null ||
                !backMessage.getState().equals(Message.MessageState.SENT) ||
                !frontMessage.getState().equals(Message.MessageState.SENT) ||
                !frontMessage.getSenderUserId().equals(backMessage.getSenderUserId()) ||
                !DateUtils.hasSameTimeInMinute(frontMessage.getTimestamp(), backMessage.getTimestamp());
    }

    public static boolean isGroupChanged(@Nullable BaseMessage frontMessage, @Nullable BaseMessage backMessage, @NonNull MessageListUIParams messageListUIParams) {
        return frontMessage == null ||
            frontMessage.getSender() == null ||
            frontMessage instanceof AdminMessage ||
            (hasParentMessage(frontMessage)) ||
            backMessage == null ||
            backMessage.getSender() == null ||
            backMessage instanceof AdminMessage ||
            (hasParentMessage(backMessage)) ||
            !backMessage.getSendingStatus().equals(SendingStatus.SUCCEEDED) ||
            !frontMessage.getSendingStatus().equals(SendingStatus.SUCCEEDED) ||
            !frontMessage.getSender().equals(backMessage.getSender()) ||
            !DateUtils.hasSameTimeInMinute(frontMessage.getCreatedAt(), backMessage.getCreatedAt());
    }

    @NonNull
    public static MessageGroupType getMessageGroupType(@Nullable BaseMessage prevMessage,
                                                       @NonNull BaseMessage message,
                                                       @Nullable BaseMessage nextMessage,
                                                       @NonNull MessageListUIParams messageListUIParams) {
        if (!messageListUIParams.shouldUseMessageGroupUI()) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        if (!message.getSendingStatus().equals(SendingStatus.SUCCEEDED)) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        if (hasParentMessage(message)) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        MessageGroupType messageGroupType = MessageGroupType.GROUPING_TYPE_BODY;
        boolean isHead = messageListUIParams.shouldUseReverseLayout() ? MessageUtils.isGroupChanged(prevMessage, message, messageListUIParams) : MessageUtils.isGroupChanged(message, nextMessage, messageListUIParams);
        boolean isTail = messageListUIParams.shouldUseReverseLayout() ? MessageUtils.isGroupChanged(message, nextMessage, messageListUIParams) : MessageUtils.isGroupChanged(prevMessage, message, messageListUIParams);

        if (!isHead && isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_TAIL;
        } else if (isHead && !isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_HEAD;
        } else if (isHead) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        return messageGroupType;
    }

    @NonNull
    public static MessageGroupType getMessageGroupType(@Nullable Message prevMessage,
                                                       @NonNull Message message,
                                                       @Nullable Message nextMessage,
                                                       @NonNull MessageListUIParams messageListUIParams) {
        if (!messageListUIParams.shouldUseMessageGroupUI()) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        if (!Message.MessageState.SENT.equals(message.getState())) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        MessageGroupType messageGroupType = MessageGroupType.GROUPING_TYPE_BODY;
        boolean isHead = messageListUIParams.shouldUseReverseLayout() ? MessageUtils.isGroupChanged(prevMessage, message, messageListUIParams) : MessageUtils.isGroupChanged(message, nextMessage, messageListUIParams);
        boolean isTail = messageListUIParams.shouldUseReverseLayout() ? MessageUtils.isGroupChanged(message, nextMessage, messageListUIParams) : MessageUtils.isGroupChanged(prevMessage, message, messageListUIParams);

        if (!isHead && isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_TAIL;
        } else if (isHead && !isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_HEAD;
        } else if (isHead) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        return messageGroupType;
    }
    public static boolean hasParentMessage(@NonNull BaseMessage message) {
        return message.getParentMessageId() != 0L;
    }

    public static boolean hasParentMessage(@NonNull Message message) {
        return false;
    }

    public static boolean hasThread(@NonNull BaseMessage message) {
        if (message instanceof CustomizableMessage) return false;
        return message.getThreadInfo().getReplyCount() > 0;
    }

    public static boolean isVoiceMessage(@Nullable FileMessage fileMessage) {
        if (fileMessage == null) return false;
        final String[] typeParts = fileMessage.getType().split(";");
        if (typeParts.length > 1) {
            for (final String typePart : typeParts) {
                if (typePart.startsWith(StringSet.sbu_type)) {
                    final String[] paramKeyValue = typePart.split("=");
                    if (paramKeyValue.length > 1) {
                        if (paramKeyValue[1].equals(StringSet.voice)) {
                            return true;
                        }
                    }
                }
            }
        }

        final List<String> typeArrayKeys = new ArrayList<>();
        typeArrayKeys.add(StringSet.KEY_INTERNAL_MESSAGE_TYPE);
        final List<MessageMetaArray> typeArray = fileMessage.getMetaArrays(typeArrayKeys);
        final String type = typeArray.isEmpty() ? "" : typeArray.get(0).getValue().get(0);
        return type.startsWith(StringSet.voice);
    }
    @NonNull
    public static String getVoiceMessageKey(@NonNull FileMessage fileMessage) {
        if (fileMessage.getSendingStatus() == SendingStatus.PENDING) {
            return fileMessage.getRequestId();
        } else {
            return String.valueOf(fileMessage.getMessageId());
        }
    }
    @NonNull
    public static String getVoiceMessageKey(@NonNull Message message) {
        return String.valueOf(message.getClientMsgNo());
    }

    @NonNull
    public static String getVoiceFilename(@NonNull FileMessage message) {
        String key = message.getRequestId();
        if (key.isEmpty() || key.equals("0")) {
            key = String.valueOf(message.getMessageId());
        }
        return "Voice_file_" + key + "." + StringSet.m4a;
    }
    @NonNull
    public static String getVoiceFilename(@NonNull Message message) {
        String key = message.getMessageId();
        if (key.isEmpty() || key.equals("0")) {
            key = String.valueOf(message.getMessageId());
        }
        return "Voice_file_" + key + "." + StringSet.m4a;
    }

    public static int extractDuration(@NonNull FileMessage message) {
        final List<String> durationArrayKeys = new ArrayList<>();
        durationArrayKeys.add(StringSet.KEY_VOICE_MESSAGE_DURATION);
        final List<MessageMetaArray> durationArray = message.getMetaArrays(durationArrayKeys);
        final String duration = durationArray.isEmpty() ? "" : durationArray.get(0).getValue().get(0);
        try {
            return Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            Logger.w(e);
        }
        return 0;
    }

    /**
     * Get notification label from message.
     * If the sub_type is 0, get the label from sub_data.
     * If the sub_data doesn't include label data, get the label from custom_type.
     */
    @NonNull
    public static String getNotificationLabel(@NonNull BaseMessage message) {
        final NotificationData notificationData = message.getNotificationData();
        if (notificationData != null) {
            return notificationData.getLabel();
        }
        return message.getCustomType();
    }
}
