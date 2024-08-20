package com.jet.im.kit.activities.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.databinding.SbViewMyFileImageMessageBinding;
import com.jet.im.kit.databinding.SbViewMyFileMessageBinding;
import com.jet.im.kit.databinding.SbViewMyFileVideoMessageBinding;
import com.jet.im.kit.databinding.SbViewMyUserMessageBinding;
import com.jet.im.kit.databinding.SbViewMyVoiceMessageBinding;
import com.jet.im.kit.databinding.SbViewOtherFileImageMessageBinding;
import com.jet.im.kit.databinding.SbViewOtherFileMessageBinding;
import com.jet.im.kit.databinding.SbViewOtherFileVideoMessageBinding;
import com.jet.im.kit.databinding.SbViewOtherUserMessageBinding;
import com.jet.im.kit.databinding.SbViewOtherVoiceMessageBinding;
import com.jet.im.kit.databinding.SbViewTimeLineMessageBinding;
import com.jet.im.kit.internal.extensions.MessageExtensionsKt;
import com.jet.im.kit.internal.ui.viewholders.MyFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.MyImageFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.MyUserMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.MyVideoFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.MyVoiceMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherImageFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherUserMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherVideoFileMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherVoiceMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.TimelineViewHolder;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.TimelineMessage;
import com.jet.im.kit.utils.MessageUtils;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.model.messages.VoiceMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.MultipleFilesMessage;
import com.sendbird.android.message.UserMessage;

/**
 * A Factory manages a type of messages.
 */
public class MessageViewHolderFactory {


    /**
     * Create a view holder that matches {@link MessageType} for {@code GroupChannel}.
     *
     * @param inflater          Inflater that creates a view
     * @param parent            The parent view to which the view holder is attached
     * @param viewType          The type of message you want to create
     * @param useMessageGroupUI Whether to show the view holder as a grouped message UI
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * @deprecated 3.3.0
     */
    @NonNull
    @Deprecated
    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     @NonNull MessageType viewType,
                                                     boolean useMessageGroupUI) {
        return createViewHolder(
                inflater,
                parent,
                viewType,
                new MessageListUIParams.Builder().setUseMessageGroupUI(useMessageGroupUI).build()
        );
    }

    /**
     * Create a view holder that matches {@link MessageType} for {@code GroupChannel}.
     *
     * @param inflater            Inflater that creates a view
     * @param parent              The parent view to which the view holder is attached
     * @param viewType            The type of message you want to create
     * @param messageListUIParams The {@link MessageListUIParams} that contains drawing parameters
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * since 3.3.0
     */
    @NonNull
    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     @NonNull MessageType viewType,
                                                     @NonNull MessageListUIParams messageListUIParams) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
                holder = new MyFileMessageViewHolder(SbViewMyFileMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OtherFileMessageViewHolder(SbViewOtherFileMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                holder = new MyImageFileMessageViewHolder(SbViewMyFileImageMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OtherImageFileMessageViewHolder(SbViewOtherFileImageMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                holder = new MyVideoFileMessageViewHolder(SbViewMyFileVideoMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OtherVideoFileMessageViewHolder(SbViewOtherFileVideoMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_VOICE_MESSAGE_ME:
                holder = new MyVoiceMessageViewHolder(SbViewMyVoiceMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_VOICE_MESSAGE_OTHER:
                holder = new OtherVoiceMessageViewHolder(SbViewOtherVoiceMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            default:
                // unknown message type
                if (viewType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME) {
                    holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                } else {
                    holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                }
        }
        return holder;
    }

    /**
     * Return the type of message as an integer.
     *
     * @param message Message to know the type.
     * @return Type of message as an integer.
     */
    public static int getViewType(@NonNull Message message) {
        return getMessageType(message).getValue();
    }

    /**
     * Return the type of message as {@link MessageType}.
     *
     * @param message Message to know the type.
     * @return Type of message as {@link MessageType}.
     */
    @NonNull
    public static MessageType getMessageType(@NonNull Message message) {
        MessageType type;
        MessageContent content = message.getContent();
        if (content instanceof TextMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (content instanceof VoiceMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_VOICE_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_VOICE_MESSAGE_OTHER;
            }
        } else if (content instanceof ImageMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
            }
        } else if (content instanceof VideoMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
            } else {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
            }
        } else if (content instanceof FileMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
            }
        }
        else if (content instanceof FileMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
            }
        }
//        else if (message instanceof MultipleFilesMessage && MessageExtensionsKt.containsOnlyImageFiles((MultipleFilesMessage) message)) {
//            if (MessageUtils.isMine(message)) {
//                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME;
//            } else {
//                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER;
//            }
//        }
        else if (content instanceof TimelineMessage) {
            type = MessageType.VIEW_TYPE_TIME_LINE;
        } else {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
            }
        }

        return type;
    }

    /**
     * Return the type of message as an integer.
     *
     * @param message Message to know the type.
     * @return Type of message as an integer.
     */
    public static int getViewType(@NonNull BaseMessage message) {
        return getMessageType(message).getValue();
    }

    /**
     * Return the type of message as {@link MessageType}.
     *
     * @param message Message to know the type.
     * @return Type of message as {@link MessageType}.
     */
    @NonNull
    public static MessageType getMessageType(@NonNull BaseMessage message) {
        MessageType type;
        if (message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof com.sendbird.android.message.FileMessage) {
            com.sendbird.android.message.FileMessage fileMessage = (com.sendbird.android.message.FileMessage) message;
            String mimeType = fileMessage.getType().toLowerCase();
            if (MessageUtils.isVoiceMessage(fileMessage)) {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_VOICE_MESSAGE_ME;
                } else {
                    type = MessageType.VIEW_TYPE_VOICE_MESSAGE_OTHER;
                }
            } else if (mimeType.startsWith(StringSet.image)) {
                if (mimeType.contains(StringSet.svg)) {
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                    }
                } else {
                    // If the sender is current user
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                    }
                }
            } else if (mimeType.startsWith(StringSet.video)) {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
            }
        } else if (message instanceof MultipleFilesMessage && MessageExtensionsKt.containsOnlyImageFiles((MultipleFilesMessage) message)) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER;
            }
        } else {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
            }
        }

        return type;
    }
}
