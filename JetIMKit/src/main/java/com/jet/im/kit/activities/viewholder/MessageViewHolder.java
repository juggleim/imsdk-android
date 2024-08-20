package com.jet.im.kit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.consts.MessageGroupType;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.MessageUIConfig;
import com.jet.im.kit.utils.DateUtils;
import com.jet.im.kit.utils.MessageUtils;

import org.jetbrains.annotations.TestOnly;

import java.util.Map;

/**
 * A ViewHolder describes an item view and Message about its place within the RecyclerView.
 */
public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
    @Nullable
    protected MessageUIConfig messageUIConfig;
    @NonNull
    private final MessageListUIParams messageListUIParams;
    private boolean isNewDate = false;
    private boolean isMine = false;
    private boolean isShowProfile = false;

    /**
     * Constructor
     *
     * @param view View to be displayed.
     */
    public MessageViewHolder(@NonNull View view) {
        this(view, new MessageListUIParams.Builder().build());
    }

    public MessageViewHolder(@NonNull View view, @NonNull MessageListUIParams messageListUIParams) {
        super(view);
        this.messageListUIParams = messageListUIParams;
    }

    public void onBindViewHolder(@NonNull ConversationInfo channel,
                                 @Nullable Message prevMessage,
                                 @NonNull Message message,
                                 @Nullable Message nextMessage) {
        if (prevMessage != null) {
            this.isNewDate = !DateUtils.hasSameDate(message.getTimestamp(), prevMessage.getTimestamp());
        } else {
            this.isNewDate = true;
        }

        this.isMine = MessageUtils.isMine(message);
        this.isShowProfile = !isMine;

        final MessageGroupType messageGroupType = MessageUtils.getMessageGroupType(prevMessage, message, nextMessage, messageListUIParams);
        final MessageListUIParams params = new MessageListUIParams.Builder(messageListUIParams)
            .setMessageGroupType(messageGroupType)
            .build();
        bind(channel, message, params);

        // for backward compatibility.
        // This function was deprecated, but it was called again for the backword compatibility.
        bind(channel, message, messageGroupType);
        itemView.requestLayout();
    }
    /**
     * Sets the configurations of the message's properties to highlight text.
     *
     * @param messageUIConfig the configurations of the message's properties to highlight text.
     * @see com.jet.im.kit.model.TextUIConfig
     * since 3.0.0
     */
    public void setMessageUIConfig(@Nullable MessageUIConfig messageUIConfig) {
        this.messageUIConfig = messageUIConfig;
    }

    /**
     * Return whether a day has passed since the previous message was created.
     *
     * @return <code>true</code> if a day has passed since the previous message was created ,
     * <code>false</code> otherwise.
     */
    protected boolean isNewDate() {
        return isNewDate;
    }

    /**
     * Return whether the profile is visible or not.
     *
     * @return <code>true</code> if the profile is visible, <code>false</code> otherwise.
     */
    protected boolean isShowProfile() {
        return isShowProfile;
    }

    /**
     * Called whether the message is from the current user.
     *
     * @return <code>true</code> if the message is from the current user, <code>false</code> otherwise.
     */
    protected boolean isMine() {
        return isMine;
    }

    /**
     * Binds as item view and data.
     *
     * @param channel          Channel used for as item view.
     * @param message          Message used for as item view.
     * @param messageGroupType The type of message group UI.
     * since 1.2.1
     * @deprecated 3.3.0
     * <p> Use {@link #bind(ConversationInfo, Message, MessageListUIParams)} instead.
     * When binding view holders, this method is still invoked.
     * We recommend you implement only one bind() method.
     */
    @Deprecated
    public void bind(@NonNull ConversationInfo channel, @NonNull Message message, @NonNull MessageGroupType messageGroupType) {}

    /**
     * Binds as item view and data.
     *
     * @param channel Channel used for as item view.
     * @param message Message used for as item view.
     * @param params  Params used for as item view.
     * since 3.3.0
     */
    public void bind(@NonNull ConversationInfo channel, @NonNull Message message, @NonNull MessageListUIParams params) {}

    /**
     * Returns a Map containing views to register a click event with an identifier.
     *
     * @return A Map containing views to register a click event with an identifier.
     * since 2.2.0
     */
    @NonNull
    abstract public Map<String, View> getClickableViewMap();

    @TestOnly
    @Nullable
    MessageUIConfig getMessageUIConfig() {
        return this.messageUIConfig;
    }

    @TestOnly
    @NonNull
    MessageListUIParams getMessageListUIParams() {
        return this.messageListUIParams;
    }
}

