package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.LiveDataEx;
import com.jet.im.kit.model.MentionSuggestion;
import com.jet.im.kit.model.MessageList;
import com.jet.im.kit.model.MutableLiveDataEx;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.VoiceMessage;
import com.sendbird.android.collection.Traceable;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.UserMessageCreateParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class BaseMessageListViewModel extends BaseViewModel implements OnPagedDataLoader<List<Message>> {
    @Nullable
    ConversationInfo channel;
    @NonNull
    protected final Conversation conversation;
    @NonNull
    final MessageList cachedMessages = new MessageList();
    @NonNull
    final MutableLiveDataEx<ChannelViewModel.ChannelMessageData> messageList = new MutableLiveDataEx<>();

    @VisibleForTesting
    BaseMessageListViewModel(@NonNull Conversation conversation, @NonNull SendbirdUIKitContract sendbirdUIKitContract) {
        super(sendbirdUIKitContract);
        this.channel = null;
        this.conversation = conversation;
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * since 3.0.0
     */
    @Nullable
    public ConversationInfo getChannel() {
        return channel;
    }


    /**
     * Returns LiveData that can be observed for the list of messages.
     *
     * @return LiveData holding the latest {@link ChannelViewModel.ChannelMessageData}
     * since 3.0.0
     */
    @NonNull
    public LiveDataEx<ChannelViewModel.ChannelMessageData> getMessageList() {
        return messageList;
    }

    /**
     * Returns LiveData that can be observed for suggested information from mention.
     *
     * @return LiveData holding {@link MentionSuggestion} for this view model
     * since 3.0.0
     */
    @NonNull
    public LiveData<MentionSuggestion> getMentionSuggestion() {
        //todo mention
        return new MutableLiveData<>();
    }

    @Override
    abstract public boolean hasNext();

    @Override
    abstract public boolean hasPrevious();

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
    }

    /**
     * Sets whether the current user is typing.
     *
     * @param isTyping {@code true} if the current user is typing, {@code false} otherwise
     */
    public void setTyping(boolean isTyping) {
//        if (channel != null) {
//            if (isTyping) {
//                channel.startTyping();
//            } else {
//                channel.endTyping();
//            }
//        }
    }

    /**
     * Sends a text message to the channel.
     *
     * @param params Parameters to be applied to the message
     *               since 3.0.0
     */
    public void sendUserMessage(@NonNull UserMessageCreateParams params) {
        Logger.i("++ request send message : %s", params);
        TextMessage textMessage = new TextMessage(params.getMessage());
        if (channel != null) {
            JIM.getInstance().getMessageManager().sendMessage(textMessage, channel.getConversation(), new IMessageManager.ISendMessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    Logger.i("++ sent message : %s", message);
                    onMessagesUpdated(channel, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    Logger.e("send message error : %s", errorCode);
                    onMessagesUpdated(channel, message);
                }
            });
        }

    }

    public void sendVoiceMessage(@NonNull String localPath, int duration) {
        if (channel != null) {
            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.setLocalPath(localPath);
            voiceMessage.setDuration(duration);
            JIM.getInstance().getMessageManager().sendMediaMessage(voiceMessage, channel.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {
                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(channel, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(channel, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(channel, message);
                }
            });
        }
    }


    public void sendImageMessage(@NonNull String localPath) {
        if (channel != null) {
            ImageMessage imageMessage = new ImageMessage();
            imageMessage.setLocalPath(localPath);
            imageMessage.setThumbnailLocalPath(localPath);
            JIM.getInstance().getMessageManager().sendMediaMessage(imageMessage, channel.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {

                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(channel, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(channel, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(channel, message);
                }
            });
        }
    }

    /**
     * Sends a file message to the channel.
     *
     * @param params   Parameters to be applied to the message
     * @param fileInfo File information to send to the channel
     *                 since 3.0.0
     */
    public void sendFileMessage(@NonNull FileMessageCreateParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
//        if (channel != null) {
//            VoiceMessage voiceMessage = new VoiceMessage();
//            voiceMessage.setLocalPath(fileInfo.getPath());
//            voiceMessage.setDuration();
//            JIM.getInstance().getMessageManager().sendMediaMessage(textMessage, channel.getConversation(), new IMessageManager.ISendMessageCallback() {
//                @Override
//                public void onSuccess(Message message) {
//                    Logger.i("++ sent message : %s", message);
//                }
//
//                @Override
//                public void onError(Message message, int errorCode) {
//                    Logger.e("send message error : %s", errorCode);
//                }
//            });
//        }
    }

    /**
     * Resends a message to the channel.
     *
     * @param message Message to resend
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void resendMessage(@NonNull Message message, @Nullable OnCompleteHandler handler) {
//        if (channel == null) return;
//        if (message instanceof UserMessage) {
//            channel.resendMessage((UserMessage) message, (userMessage, e) -> {
//                if (handler != null) handler.onComplete(e);
//                Logger.i("__ resent message : %s", userMessage);
//            });
//        } else if (message instanceof FileMessage) {
//            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
//            final File file = info == null ? null : info.getFile();
//            channel.resendMessage((FileMessage) message, file, (fileMessage, e) -> {
//                if (handler != null) handler.onComplete(e);
//                Logger.i("__ resent file message : %s", fileMessage);
//            });
//        } else if (message instanceof MultipleFilesMessage) {
//            channel.resendMessage((MultipleFilesMessage) message, null, (multipleFilesMessage, e) -> {
//                if (handler != null) handler.onComplete(e);
//                Logger.i("__ resent multiple files message : %s", multipleFilesMessage);
//            });
//        }
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void deleteMessage(@NonNull Message message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        final Message.MessageState status = message.getState();
        if (status == Message.MessageState.SENT) {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(message.getMessageId());
            JIM.getInstance().getMessageManager().deleteMessagesByMessageIdList(channel.getConversation(), ids, new IMessageManager.ISimpleCallback() {
                @Override
                public void onSuccess() {
                    if (handler != null) handler.onComplete(null);

                }

                @Override
                public void onError(int errorCode) {
                    if (handler != null)
                        handler.onComplete(new RuntimeException("erroCode:" + errorCode));
                    Logger.i("++ deleted message : %s", message);
                }
            });
        }
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     *                since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((e) -> {
            if (e == null) {
                ConversationInfo info = getChannel(conversation);
                if (info == null) {
                    info = new ConversationInfo();
                    info.setConversation(conversation);
                }
                this.channel = info;
                if (channel == null) {
                    handler.onAuthenticationFailed();
                } else {
                    handler.onAuthenticated();
                }

            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    @VisibleForTesting
    ConversationInfo getChannel(@NonNull Conversation conversation) {
        return JIM.getInstance().getConversationManager().getConversationInfo(conversation);
    }

    /**
     * Loads the list of members whose nickname starts with startWithFilter.
     *
     * @param startWithFilter The filter to be used to load a list of members with nickname that starts with a specific text.
     *                        since 3.0.0
     */
    public synchronized void loadMemberList(@Nullable String startWithFilter) {
    }

    void onMessagesUpdated(@NonNull ConversationInfo channel, @NonNull Message message) {
        if (message == null || message.getClientMsgNo() == 0) return;
        Message find = cachedMessages.getById(message.getClientMsgNo());
        String name;
        if (find == null) {
            cachedMessages.add(message);
            name = StringSet.ACTION_PENDING_MESSAGE_ADDED;
        } else {
            name = StringSet.EVENT_MESSAGE_SENT;
            cachedMessages.update(message);
        }
        notifyDataSetChanged(name);
    }

    @UiThread
    synchronized void notifyDataSetChanged(@NonNull Traceable trace) {
        notifyDataSetChanged(trace.getTraceName());
    }

    @UiThread
    synchronized void notifyDataSetChanged(@NonNull String traceName) {
    }

    /**
     * Processes a list of messages to be passed to the view. The return value of this function is delivered to the view through {@link LiveData}.
     * If you want to customize the message list to be delivered to the view, you can override this function as shown below.
     *
     * <pre>
     * class CustomChannelViewModel(
     *     channelUrl: String
     * ) : ChannelViewModel(channelUrl, null) {
     *     override fun buildMessageList(): List&lt;BaseMessage&gt; {
     *         return super.buildMessageList().map { message ->
     *             // customize the message here
     *             message
     *         }
     *     }
     * }
     * </pre>
     * To provide custom {@link ChannelViewModel} to Sendbird UIKit, Check out <a href="https://sendbird.com/docs/chat/uikit/v3/android/customizations/global-customization/viewmodels#2-apply-custom-viewmodels">here</a> for more details.
     *
     * @return List of messages to be passed to the view through LiveData.
     * since 3.12.0
     */
    @UiThread
    @NonNull
    public List<Message> buildMessageList() {
        return Collections.emptyList();
    }

}

