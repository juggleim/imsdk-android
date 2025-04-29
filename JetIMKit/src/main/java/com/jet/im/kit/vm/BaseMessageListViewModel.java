package com.jet.im.kit.vm;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.IGroupMemberProvider;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.LiveDataEx;
import com.jet.im.kit.model.MentionSuggestion;
import com.jet.im.kit.model.MessageList;
import com.jet.im.kit.model.MutableLiveDataEx;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.MessageOptions;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.model.messages.VoiceMessage;
import com.sendbird.android.collection.Traceable;
import com.sendbird.android.params.FileMessageCreateParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class BaseMessageListViewModel extends BaseViewModel implements OnPagedDataLoader<List<Message>> {
    @Nullable
    ConversationInfo mConversationInfo;
    @NonNull
    protected final Conversation mConversation;
    @Nullable
    MemberFinder mMemberFinder;
    @NonNull
    final MessageList cachedMessages = new MessageList();
    @NonNull
    final List<MessageReaction> mMessageReactions = new ArrayList<>();
    @NonNull
    final MutableLiveDataEx<ChannelViewModel.ChannelMessageData> messageList = new MutableLiveDataEx<>();

    @VisibleForTesting
    BaseMessageListViewModel(@NonNull Conversation conversation, @NonNull SendbirdUIKitContract sendbirdUIKitContract) {
        super(sendbirdUIKitContract);
        this.mConversationInfo = null;
        this.mConversation = conversation;
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * since 3.0.0
     */
    @Nullable
    public ConversationInfo getConversationInfo() {
        return mConversationInfo;
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
        if (mMemberFinder == null) return new MutableLiveData<>();
        return mMemberFinder.getMentionSuggestion();
    }

    @Override
    abstract public boolean hasNext();

    @Override
    abstract public boolean hasPrevious();

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        if (mMemberFinder != null) mMemberFinder.dispose();
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

    public void sendMessage(MessageContent content) {
        if (mConversationInfo != null) {
            Message m = JIM.getInstance().getMessageManager().sendMessage(content, mConversationInfo.getConversation(), new IMessageManager.ISendMessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    Logger.i("++ sent message : %s", message);
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    Logger.e("send message error : %s", errorCode);
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void sendMessage(MessageContent content, @NonNull Conversation conversation) {
        if (mConversationInfo == null) {
            return;
        }
        Message m = JIM.getInstance().getMessageManager().sendMessage(content, conversation, new IMessageManager.ISendMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                Logger.i("++ sent message : %s", message);
                if (conversation.equals(mConversation)) {
                    if (mConversationInfo != null) {
                        onMessagesUpdated(mConversationInfo, message);
                    }
                }
            }

            @Override
            public void onError(Message message, int errorCode) {
                Logger.e("send message error : %s", errorCode);
                if (conversation.equals(mConversation)) {
                    if (mConversationInfo != null) {
                        onMessagesUpdated(mConversationInfo, message);
                    }
                }
            }
        });
        onMessagesUpdated(mConversationInfo, m);
    }

    public void sendTextMessage(String content, String parentMessageId, MessageMentionInfo mentionInfo) {
        Logger.i("++ request send message : %s", content);
        TextMessage textMessage = new TextMessage(content);
        MessageOptions options = new MessageOptions();
        if (TextUtils.isNotEmpty(parentMessageId)) {
            options.setReferredMessageId(parentMessageId);
        }
        if (mentionInfo != null) {
            options.setMentionInfo(mentionInfo);
        }
        if (mConversationInfo != null) {
            Message m = JIM.getInstance().getMessageManager().sendMessage(textMessage, mConversationInfo.getConversation(), options, new IMessageManager.ISendMessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    Logger.i("++ sent message : %s", message);
                    Handler mH = new Handler(Looper.getMainLooper());
                    mH.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onMessagesUpdated(mConversationInfo, message);
                        }
                    }, 200);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    Logger.e("send message error : %s", errorCode);
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void sendVoiceMessage(@NonNull String localPath, int duration) {
        if (mConversationInfo != null) {
            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.setLocalPath(localPath);
            voiceMessage.setDuration(duration);
            Message m = JIM.getInstance().getMessageManager().sendMediaMessage(voiceMessage, mConversationInfo.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {
                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void sendImageMessage(@NonNull ImageMessage imageMessage) {
        if (mConversationInfo != null) {
            Message m = JIM.getInstance().getMessageManager().sendMediaMessage(imageMessage, mConversationInfo.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {

                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void sendVideoMessage(@NonNull VideoMessage videoMessage) {
        if (mConversationInfo != null) {
            Message m = JIM.getInstance().getMessageManager().sendMediaMessage(videoMessage, mConversationInfo.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {

                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void sendFileMessage(@NonNull FileMessage fileMessage) {
        if (mConversationInfo != null) {
            Message m = JIM.getInstance().getMessageManager().sendMediaMessage(fileMessage, mConversationInfo.getConversation(), new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {

                }

                @Override
                public void onSuccess(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onCancel(Message message) {
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
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
        if (mConversationInfo == null) {
            if (handler != null) {
                handler.onComplete(new RuntimeException());
            }
            return;
        }
        MessageContent content = message.getContent();
        if (content == null) {
            if (handler != null) {
                handler.onComplete(new RuntimeException());
            }
            return;
        }
        boolean needUpload = false;
        if (content instanceof MediaMessageContent) {
            MediaMessageContent mediaMessageContent = (MediaMessageContent) content;
            if (TextUtils.isEmpty(mediaMessageContent.getUrl())) {
                needUpload = true;
            }
        }
        if (needUpload) {
            Message m = JIM.getInstance().getMessageManager().resendMediaMessage(message, new IMessageManager.ISendMediaMessageCallback() {
                @Override
                public void onProgress(int progress, Message message) {

                }

                @Override
                public void onSuccess(Message message) {
                    if (handler != null) {
                        handler.onComplete(null);
                    }
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    if (handler != null) {
                        handler.onComplete(new RuntimeException());
                    }
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onCancel(Message message) {

                }
            });
            onMessagesUpdated(mConversationInfo, m);
        } else {
            Message m = JIM.getInstance().getMessageManager().resendMessage(message, new IMessageManager.ISendMessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    if (handler != null) {
                        handler.onComplete(null);
                    }
                    onMessagesUpdated(mConversationInfo, message);
                }

                @Override
                public void onError(Message message, int errorCode) {
                    if (handler != null) {
                        handler.onComplete(new RuntimeException());
                    }
                    onMessagesUpdated(mConversationInfo, message);
                }
            });
            onMessagesUpdated(mConversationInfo, m);
        }
    }

    public void updateUserMessage(String messageId, String content, Conversation conversation, OnCompleteHandler handler) {
        TextMessage t = new TextMessage(content);
        JIM.getInstance().getMessageManager().updateMessage(messageId, t, conversation, new IMessageManager.IMessageCallback() {
            @Override
            public void onSuccess(Message message) {
            }

            @Override
            public void onError(int errorCode) {
                if (handler != null) handler.onComplete(new Exception());
            }
        });
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     *                since 3.0.0
     */
    public void deleteMessage(@NonNull Message message, @Nullable OnCompleteHandler handler) {
        if (mConversationInfo == null) return;
        final Message.MessageState status = message.getState();
        if (status == Message.MessageState.SENT) {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(message.getMessageId());
            JIM.getInstance().getMessageManager().deleteMessagesByMessageIdList(mConversationInfo.getConversation(), ids, new IMessageManager.ISimpleCallback() {
                @Override
                public void onSuccess() {
                    if (handler != null) handler.onComplete(null);
                }

                @Override
                public void onError(int errorCode) {
                    if (handler != null) {
                        handler.onComplete(new RuntimeException("errorCode:" + errorCode));
                    }
                    Logger.i("++ deleted message : %s", message);
                }
            });
        } else {
            if (handler != null) {
                handler.onComplete(new RuntimeException("Message not sent"));
            }
        }
    }

    public void recallMessage(@NonNull Message message, @NonNull OnCompleteHandler handler) {
        if (mConversationInfo == null) return;
        final Message.MessageState status = message.getState();
        if (status == Message.MessageState.SENT) {
            JIM.getInstance().getMessageManager().recallMessage(message.getMessageId(), null, new IMessageManager.IRecallMessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    handler.onComplete(null);
                }

                @Override
                public void onError(int errorCode) {
                    handler.onComplete(new RuntimeException("errorCode:" + errorCode));
                    Logger.i("++ recall message : %s", message);
                }
            });
        } else {
            handler.onComplete(new RuntimeException("Message not sent"));
        }
    }

    /**
     * Adds the reaction with {@code key} if the current user doesn't add it, otherwise the reaction will be deleted
     *
     * @param view View displaying the reaction with {@code key}
     * @param message Message to which the reaction will be applieds
     * @param key Key of reaction
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void toggleReaction(@NonNull View view, @NonNull Message message, @NonNull String key, @Nullable OnCompleteHandler handler) {
        if (mConversationInfo == null) return;
        if (!view.isSelected()) {
            Logger.i("__ add reaction : %s", key);
            JIM.getInstance().getMessageManager().addMessageReaction(message.getMessageId(), mConversationInfo.getConversation(), key, new IMessageManager.ISimpleCallback() {
                @Override
                public void onSuccess() {
                    if (handler != null) {
                        handler.onComplete(null);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (handler != null) {
                        handler.onComplete(new RuntimeException());
                    }
                }
            });
        } else {
            Logger.i("__ delete reaction : %s", key);
            JIM.getInstance().getMessageManager().removeMessageReaction(message.getMessageId(), mConversationInfo.getConversation(), key, new IMessageManager.ISimpleCallback() {
                @Override
                public void onSuccess() {
                    if (handler != null) {
                        handler.onComplete(null);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (handler != null) {
                        handler.onComplete(new RuntimeException());
                    }
                }
            });
        }
    }

    public MessageReaction getReactionByMessageId(@NonNull String messageId) {
        MessageReaction result = null;
        for (MessageReaction reaction : mMessageReactions) {
            if (reaction.getMessageId().equals(messageId)) {
                result = reaction;
            }
        }
        return result;
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     *                since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {

        Handler mh = new Handler(Looper.getMainLooper());
        mh.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConversationInfo info = getChannel(mConversation);
                if (info == null) {
                    info = new ConversationInfo();
                    info.setConversation(mConversation);
                }
                BaseMessageListViewModel.this.mConversationInfo = info;
                BaseMessageListViewModel.this.mMemberFinder = new MemberFinder(SendbirdUIKit.getUserMentionConfig(), mConversation.getConversationId());
                if (mConversationInfo == null) {
                    handler.onAuthenticationFailed();
                } else {
                    handler.onAuthenticated();
                }
            }
        }, 50);
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
        if (mMemberFinder != null) mMemberFinder.find(startWithFilter);
    }

    void onMessagesUpdated(@NonNull ConversationInfo channel, @NonNull Message message) {
        if (message.getClientMsgNo() == 0) return;
        Message find = cachedMessages.getByClientNo(message.getClientMsgNo());
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

