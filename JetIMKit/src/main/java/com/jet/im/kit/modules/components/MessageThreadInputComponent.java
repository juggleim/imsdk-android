package com.jet.im.kit.modules.components;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.MutedState;
import com.jet.im.kit.R;
import com.jet.im.kit.internal.extensions.ChannelExtensionsKt;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.widgets.MessageInputView;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * This class creates and performs a view corresponding the message thread input area in Sendbird UIKit.
 *
 * since 3.3.0
 */
public class MessageThreadInputComponent extends MessageInputComponent {
    @NonNull
    private BaseMessage parentMessage;
    @NonNull
    private StatusFrameView.Status status = StatusFrameView.Status.NONE;

    /**
     * Constructor
     *
     * @param parentMessage The parent message of thread.
     * since 3.3.0
     */
    public MessageThreadInputComponent(@NonNull BaseMessage parentMessage) {
        super(new Params());
        this.parentMessage = parentMessage;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.3.0
     */
    @NonNull
    @Override
    public Params getParams() {
        return (Params) super.getParams();
    }

    /**
     * Notifies this component that the parent message has been changed.
     *
     * @param channel The latest group channel
     * @param parentMessage The latest parent message
     * since 3.3.0
     */
    public void notifyParentMessageUpdated(@NonNull GroupChannel channel, @NonNull BaseMessage parentMessage) {
        if (!(getRootView() instanceof MessageInputView)) return;
        final MessageInputView inputView = (MessageInputView) this.getRootView();

        this.parentMessage = parentMessage;
        setHintMessageTextInternal(inputView, channel);
    }

    /**
     * Notifies this component that the status has been changed.
     *
     * @param channel The latest group channel
     * @param status The latest status
     * since 3.3.0
     */
    public void notifyStatusUpdated(@NonNull GroupChannel channel, @NonNull StatusFrameView.Status status) {
        if (!(getRootView() instanceof MessageInputView)) return;
        final MessageInputView inputView = (MessageInputView) this.getRootView();

        this.status = status;
        setHintMessageTextInternal(inputView, channel);
    }

    @Override
    void setHintMessageTextInternal(@NonNull MessageInputView inputView, @NonNull GroupChannel channel) {
        boolean isOperator = channel.getMyRole() == Role.OPERATOR;
        boolean isMuted = channel.getMyMutedState() == MutedState.MUTED;
        boolean isFrozen = channel.isFrozen() && !isOperator;
        boolean shouldDisableInput = ChannelExtensionsKt.shouldDisableInput(channel, getParams().getChannelConfig());
        inputView.setEnabled(!isMuted && !isFrozen && !shouldDisableInput &&
            status != StatusFrameView.Status.ERROR &&
            status != StatusFrameView.Status.CONNECTION_ERROR);

        final MessageInputView.Mode mode = inputView.getInputMode();
        // set hint
        final Context context = inputView.getContext();
        String hintText;
        if (isMuted) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_muted);
        } else if (isFrozen) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint_frozen);
        } else if (MessageInputView.Mode.EDIT == mode) {
            hintText = context.getString(R.string.sb_text_channel_input_text_hint);
        } else if (parentMessage.getThreadInfo().getReplyCount() > 0) {
            hintText = context.getString(R.string.sb_text_channel_input_reply_to_thread_hint);
        } else {
            hintText = context.getString(R.string.sb_text_channel_input_reply_in_thread_hint);
        }
        Logger.dev("++ hint text : " + hintText);
        inputView.setInputTextHint(hintText);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.3.0
     */
    public static class Params extends MessageInputComponent.Params {}
}
