package com.jet.im.kit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;

import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.consts.KeyboardDisplayType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnInputModeChangedListener;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.interfaces.OnMentionEventListener;
import com.jet.im.kit.internal.ui.widgets.MessageInputDialogWrapper;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MessageUIConfig;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.model.UserMentionConfig;
import com.jet.im.kit.model.configurations.ChannelConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.utils.ViewUtils;
import com.jet.im.kit.widgets.MentionEditText;
import com.jet.im.kit.widgets.MessageInputView;

import org.jetbrains.annotations.TestOnly;

import java.util.List;

/**
 * This class creates and performs a view corresponding the message input area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class MessageInputComponent {
    @NonNull
    private final Params params;
    @Nullable
    private MessageInputView messageInputView;

    @Nullable
    private View.OnClickListener inputRightButtonClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private View.OnClickListener editModeCancelButtonClickListener;
    @Nullable
    private View.OnClickListener editModeSaveButtonClickListener;
    @Nullable
    private View.OnClickListener voiceRecorderButtonClickListener;
    @Nullable
    private View.OnClickListener replyModeCloseButtonClickListener;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnInputTextChangedListener editModeTextChangedListener;
    @Nullable
    private OnInputModeChangedListener inputModeChangedListener;
    @Nullable
    private CharSequence hintText;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public MessageInputComponent() {
        this.params = new Params();
    }

    MessageInputComponent(@NonNull Params params) {
        this.params = params;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.messageInputView;
    }

    /**
     * Returns the edit text view used in the input component bt default.
     *
     * @return {@link EditText} used in this component
     * since 3.0.0
     */
    @Nullable
    public EditText getEditTextView() {
        if (messageInputView == null) return null;
        return messageInputView.getInputEditText();
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) {
            params.apply(context, args);
        }
        this.messageInputView = createMessageInputView(context);
        if (params.leftButtonIcon != null) {
            this.messageInputView.setAddImageDrawable(params.leftButtonIcon);
        }
        if (params.leftButtonIconTint != null) {
            this.messageInputView.setAddImageButtonTint(params.leftButtonIconTint);
        }
        if (params.rightButtonIcon != null) {
            this.messageInputView.setSendImageDrawable(params.rightButtonIcon);
        }
        if (params.rightButtonIconTint != null) {
            this.messageInputView.setSendImageButtonTint(params.rightButtonIconTint);
        }
        if (params.hintText != null) {
            this.messageInputView.setInputTextHint(params.hintText);
        }
        hintText = this.messageInputView.getInputEditText().getHint();
        if (params.inputText != null) {
            this.messageInputView.setInputText(params.inputText);
        }
        if (params.textUIConfig != null) {
            this.messageInputView.applyTextUIConfig(params.textUIConfig);
        }
        this.messageInputView.setAddButtonVisibility(params.useLeftButton ? View.VISIBLE : View.GONE);

        if (params.alwaysShowRightButton) messageInputView.setSendButtonVisibility(View.VISIBLE);
        this.messageInputView.setShowSendButtonAlways(params.alwaysShowRightButton);
        this.messageInputView.setOnSendClickListener(this::onInputRightButtonClicked);
        this.messageInputView.setOnAddClickListener(this::onInputLeftButtonClicked);
        this.messageInputView.setOnEditCancelClickListener(this::onEditModeCancelButtonClicked);
        this.messageInputView.setOnEditSaveClickListener(this::onEditModeSaveButtonClicked);
        this.messageInputView.setOnInputTextChangedListener(this::onInputTextChanged);
        this.messageInputView.setOnEditModeTextChangedListener(this::onEditModeTextChanged);
        this.messageInputView.setOnReplyCloseClickListener(this::onQuoteReplyModeCloseButtonClicked);
        this.messageInputView.setOnInputModeChangedListener(this::onInputModeChanged);
        this.messageInputView.setOnVoiceRecorderButtonClickListener(this::onVoiceRecorderButtonClicked);
        this.messageInputView.setUseVoiceButton(params.getChannelConfig().getEnableVoiceMessage());
        this.setUseSuggestedMentionListDivider(params.useSuggestedMentionListDivider);
        if (params.keyboardDisplayType == KeyboardDisplayType.Dialog) {
            final MessageInputDialogWrapper messageInputDialogWrapper = new MessageInputDialogWrapper(context);
            messageInputDialogWrapper.initInputView(messageInputView);
            return messageInputDialogWrapper;
        } else {
            return messageInputView;
        }
    }

    /**
     * Binds the mention configuration and the event listener to the input component.
     *
     * @param mentionConfig The configuration to be applied for the mention in this input component.
     * @param handler The handler to be invoked when the mention text is detected in this input component.
     * since 3.0.0
     */
    public void bindUserMention(@NonNull UserMentionConfig mentionConfig, @NonNull OnMentionEventListener handler) {
        if (getEditTextView() instanceof MentionEditText) {
            if (messageInputView != null) {
                params.messageUIConfig.getMyMentionUIConfig().mergeFromTextAppearance(messageInputView.getContext(), messageInputView.getTextAppearance());
            }
            ((MentionEditText) getEditTextView()).bindUserMention(mentionConfig, params.messageUIConfig.getMyMentionUIConfig(), handler);
        }
    }


    /**
     * Sets whether to use divider in suggested mention list.
     *
     * @param useDivider If <code>true</code> the divider will be used at suggested mention list, <code>false</code> other wise.
     * since 3.0.0
     */
    public void setUseSuggestedMentionListDivider(boolean useDivider) {
        if (getEditTextView() instanceof MentionEditText) {
            ((MentionEditText) getEditTextView()).setUseSuggestedMentionListDivider(useDivider);
        }
    }

    /**
     * Register a callback to be invoked when the right button of the input is clicked.
     *
     * @param inputRightButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputRightButtonClickListener(@Nullable View.OnClickListener inputRightButtonClickListener) {
        this.inputRightButtonClickListener = inputRightButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the left button of the input is clicked.
     *
     * @param inputLeftButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputLeftButtonClickListener(@Nullable View.OnClickListener inputLeftButtonClickListener) {
        this.inputLeftButtonClickListener = inputLeftButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the cancel button is clicked, when the input is the edited mode.
     *
     * @param editModeCancelButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeCancelButtonClickListener(@Nullable View.OnClickListener editModeCancelButtonClickListener) {
        this.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the save button is clicked, when the input is the edited mode.
     *
     * @param editModeSaveButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeSaveButtonClickListener(@Nullable View.OnClickListener editModeSaveButtonClickListener) {
        this.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the input text is changed, when the input is the edited mode.
     *
     * @param editModeTextChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnEditModeTextChangedListener(@Nullable OnInputTextChangedListener editModeTextChangedListener) {
        this.editModeTextChangedListener = editModeTextChangedListener;
    }

    /**
     * Register a callback to be invoked when the input text is changed.
     *
     * @param inputTextChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputTextChangedListener(@Nullable OnInputTextChangedListener inputTextChangedListener) {
        this.inputTextChangedListener = inputTextChangedListener;
    }

    /**
     * Register a callback to be invoked when the close button is clicked, when the input is the quote reply mode.
     *
     * @param replyModeCloseButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnQuoteReplyModeCloseButtonClickListener(@Nullable View.OnClickListener replyModeCloseButtonClickListener) {
        this.replyModeCloseButtonClickListener = replyModeCloseButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the input mode is changed.
     *
     * @param inputModeChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputModeChangedListener(@Nullable OnInputModeChangedListener inputModeChangedListener) {
        this.inputModeChangedListener = inputModeChangedListener;
    }

    /**
     * Register a callback to be invoked when the voice recorder button is clicked.
     *
     * @param voiceRecorderButtonClickListener The callback that will run
     * since 3.4.0
     */
    public void setOnVoiceRecorderButtonClickListener(@Nullable View.OnClickListener voiceRecorderButtonClickListener) {
        this.voiceRecorderButtonClickListener = voiceRecorderButtonClickListener;
    }

    /**
     * Called when the left button of the input is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onInputLeftButtonClicked(@NonNull View view) {
        if (inputLeftButtonClickListener != null) inputLeftButtonClickListener.onClick(view);
    }

    /**
     * Called when the right button of the input is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onInputRightButtonClicked(@NonNull View view) {
        if (inputRightButtonClickListener != null) inputRightButtonClickListener.onClick(view);
    }

    /**
     * Called when the input text is changed, when the input is the edited mode.
     * <p>
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     * </p>
     *
     * since 3.0.0
     */
    protected void onEditModeTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (editModeTextChangedListener != null)
            editModeTextChangedListener.onInputTextChanged(s, start, before, count);
    }

    /**
     * Called when the input text is changed.
     * <p>
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     * </p>
     *
     * since 3.0.0
     */
    protected void onInputTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (inputTextChangedListener != null) {
            inputTextChangedListener.onInputTextChanged(s, start, before, count);
        }
    }

    /**
     * Called when the close button is clicked, when the input is the quote reply mode.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onQuoteReplyModeCloseButtonClicked(@NonNull View view) {
        if (replyModeCloseButtonClickListener != null)
            replyModeCloseButtonClickListener.onClick(view);
    }

    /**
     * Called when the input mode is changed.
     *
     * @param before  Input mode before change
     * @param current The latest input mode
     * since 3.0.0
     */
    protected void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current) {
        if (inputModeChangedListener != null)
            inputModeChangedListener.onInputModeChanged(before, current);
    }

    /**
     * Called when the cancel button is clicked, when the input is the edited mode.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onEditModeCancelButtonClicked(@NonNull View view) {
        if (editModeCancelButtonClickListener != null)
            editModeCancelButtonClickListener.onClick(view);
    }

    /**
     * Called when the save button is clicked, when the input is the edited mode.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onEditModeSaveButtonClicked(@NonNull View view) {
        if (editModeSaveButtonClickListener != null) editModeSaveButtonClickListener.onClick(view);
    }

    /**
     * Called when the voice recorder button is clicked.
     *
     * @param view The View clicked
     * since 3.4.0
     */
    protected void onVoiceRecorderButtonClicked(@NonNull View view) {
        if (voiceRecorderButtonClickListener != null) voiceRecorderButtonClickListener.onClick(view);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull ConversationInfo channel) {
        if (messageInputView == null) return;
        final MessageInputView inputView = this.messageInputView;
        setHintMessageTextInternal(inputView, channel);
    }

    /**
     * Notifies this component that the data needed to draw the input has changed.
     *
     * @param message Message required for current input information
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyDataChanged(@Nullable Message message, @NonNull ConversationInfo channel) {
        notifyDataChanged(message, channel, "");
    }

    /**
     * Notifies this component that the data needed to draw the input has changed.
     *
     * @param message     Message required for current input information
     * @param channel     The latest group channel
     * @param defaultText Text set as initial value for input
     * since 3.0.0
     */
    public void notifyDataChanged(
        @Nullable Message message,
        @NonNull ConversationInfo channel,
        @NonNull String defaultText
    ) {
        if (messageInputView == null) return;
        final MessageInputView inputView = this.messageInputView;

        final MessageInputView.Mode mode = inputView.getInputMode();
        if (MessageInputView.Mode.EDIT == mode) {
            if (message != null) {
                final CharSequence text = ViewUtils.getDisplayableText(
                    inputView.getContext(),
                    message,
                    params.messageUIConfig,
                    null,
                    true,
                    null,
                    params.getChannelConfig().getEnableMention()
                );
                inputView.setInputText(text);
            }
            inputView.showKeyboard();
        }  else {
            inputView.setInputText(defaultText);
            final CharSequence text = inputView.getInputText();
            if (text != null) {
                inputView.getInputEditText().setSelection(text.length());
            }
        }

        setHintMessageTextInternal(inputView, channel);
    }

    /**
     * Notifies changes of suggested mention list.
     *
     * @param suggestedMentionList The updated suggested mention list.
     * since 3.0.0
     */
    public void notifySuggestedMentionDataChanged(@NonNull List<User> suggestedMentionList) {
        Logger.d(">> MessageInputComponent::notifySuggestedMentionDataChanged()");
        if (getEditTextView() instanceof MentionEditText) {
            ((MentionEditText) getEditTextView()).notifySuggestedMentionDataChanged(suggestedMentionList);
        }
    }

    /**
     * Requests to set the input mode.
     *
     * @param mode Input mode to be set to this component
     * @see MessageInputView.Mode
     * since 3.0.0
     */
    public void requestInputMode(@NonNull MessageInputView.Mode mode) {
        if (messageInputView == null) return;
        this.messageInputView.setInputMode(mode);
    }

    void setHintMessageTextInternal(@NonNull MessageInputView inputView, @NonNull ConversationInfo channel) {
        inputView.setEnabled(true);

        final MessageInputView.Mode mode = inputView.getInputMode();
        // set hint
        final Context context = inputView.getContext();
        String hintText = this.hintText != null ? this.hintText.toString() : null;
        Logger.dev("++ hint text : " + hintText);
        inputView.setInputTextHint(hintText);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        private boolean useLeftButton = true;
        private boolean alwaysShowRightButton = false;
        private boolean useSuggestedMentionListDivider = true;
        @Nullable
        private Drawable leftButtonIcon;
        @Nullable
        private Drawable rightButtonIcon;
        @Nullable
        private ColorStateList leftButtonIconTint;
        @Nullable
        private ColorStateList rightButtonIconTint;

        @Nullable
        private String hintText;

        @Nullable
        private String inputText;

        @NonNull
        private KeyboardDisplayType keyboardDisplayType = KeyboardDisplayType.Plane;

        @NonNull
        private final MessageUIConfig messageUIConfig;

        @Nullable
        private TextUIConfig textUIConfig;
        @NonNull
        private ChannelConfig channelConfig = UIKitConfig.getGroupChannelConfig();

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
            this.messageUIConfig = new MessageUIConfig();
            this.messageUIConfig.getMyMentionUIConfig().apply(new TextUIConfig.Builder().setTextStyle(Typeface.BOLD).build());
        }

        /**
         * Sets the icon on the left button of the input view.
         *
         * @param leftButtonIcon The Drawable to be displayed on the left button of the input view
         * since 3.0.0
         */
        public void setLeftButtonIcon(@Nullable Drawable leftButtonIcon) {
            this.leftButtonIcon = leftButtonIcon;
        }

        /**
         * Sets the icon on the right button of the input view.
         *
         * @param rightButtonIcon The Drawable to be displayed on the right button of the input view
         * since 3.0.0
         */
        public void setRightButtonIcon(@Nullable Drawable rightButtonIcon) {
            this.rightButtonIcon = rightButtonIcon;
        }

        /**
         * Sets whether the left button of the input view is used.
         *
         * @param useLeftButton <code>true</code> if the left button of the input view is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseLeftButton(boolean useLeftButton) {
            this.useLeftButton = useLeftButton;
        }

        /**
         * Sets the color of the icon on the left button of the input view.
         *
         * @param leftButtonIconTint Color state list to be applied to the left button icon
         * since 3.0.0
         */
        public void setLeftButtonIconTint(@Nullable ColorStateList leftButtonIconTint) {
            this.leftButtonIconTint = leftButtonIconTint;
        }

        /**
         * Sets the color of the icon on the right button of the input view.
         *
         * @param rightButtonIconTint Color state list to be applied to the right button icon
         * since 3.0.0
         */
        public void setRightButtonIconTint(@Nullable ColorStateList rightButtonIconTint) {
            this.rightButtonIconTint = rightButtonIconTint;
        }

        /**
         * Shows always the right button of the input view.
         *
         * since 3.0.0
         */
        public void showInputRightButtonAlways() {
            this.alwaysShowRightButton = true;
        }

        /**
         * Sets the hint of the input view.
         *
         * @param hint The String displayed as a hint message
         * since 3.0.0
         */
        public void setInputHint(@Nullable String hint) {
            this.hintText = hint;
        }

        /**
         * Sets the input text.
         *
         * @param inputText The String to be set on the input view
         * since 3.0.0
         */
        public void setInputText(@Nullable String inputText) {
            this.inputText = inputText;
        }

        /**
         * Sets the keyboard display type. (Refer to {@link KeyboardDisplayType})
         *
         * @param type Keyboard display type to be used in this component
         * @see KeyboardDisplayType
         * since 3.0.0
         */
        public void setKeyboardDisplayType(@NonNull KeyboardDisplayType type) {
            this.keyboardDisplayType = type;
        }

        /**
         * Sets whether to use divider in suggested mention list.
         *
         * @param useDivider If <code>true</code> the divider will be used at suggested mention list, <code>false</code> other wise.
         * since 3.0.0
         */
        public void setUseSuggestedMentionListDivider(boolean useDivider) {
            this.useSuggestedMentionListDivider = useDivider;
        }

        /**
         * Sets {@link ChannelConfig} that will be used in this component.
         * Use {@code UIKitConfig.groupChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val messageInputComponent = MessageInputComponent()
         * messageInputComponent.params.channelConfig = UIKitConfig.groupChannelConfig.clone().apply {
         *     this.enableMention = true
         * }
         * </pre>
         *
         * @param channelConfig Channel config to be used in this component.
         * since 3.6.0
         */
        public void setChannelConfig(@NonNull ChannelConfig channelConfig) {
            this.channelConfig = channelConfig;
        }

        /**
         * Returns the keyboard display type. (Refer to {@link KeyboardDisplayType})
         *
         * @return Keyboard display type used in this component
         * since 3.0.0
         */
        @NonNull
        public KeyboardDisplayType getKeyboardDisplayType() {
            return keyboardDisplayType;
        }

        /**
         * Returns whether the left button of the input view is used.
         *
         * @return <code>true</code> if the left button of the input view is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseLeftButton() {
            return useLeftButton;
        }

        /**
         * Returns whether the right button of the input view is shown always.
         *
         * @return <code>true</code> if the right button of the input view is shown always, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean isAlwaysShowRightButton() {
            return alwaysShowRightButton;
        }

        /**
         * Returns the icon on the left button of the input view.
         *
         * @return The Drawable to be displayed on the left button of the input view
         * since 3.0.0
         */
        @Nullable
        public Drawable getLeftButtonIcon() {
            return leftButtonIcon;
        }

        /**
         * Returns the icon on the right button of the input view.
         *
         * @return The Drawable to be displayed on the right button of the input view
         * since 3.0.0
         */
        @Nullable
        public Drawable getRightButtonIcon() {
            return rightButtonIcon;
        }

        /**
         * Returns the color of the icon on the left button of the input view.
         *
         * @return Color state list to be applied to the left button icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getLeftButtonIconTint() {
            return leftButtonIconTint;
        }

        /**
         * Returns the color of the icon on the right button of the input view.
         *
         * @return Color state list to be applied to the right button icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getRightButtonIconTint() {
            return rightButtonIconTint;
        }

        /**
         * Returns the hint of the input view.
         *
         * @return The String displayed as a hint message
         * since 3.0.0
         */
        @Nullable
        public String getHintText() {
            return hintText;
        }

        /**
         * Returns the input text.
         *
         * @return The String to be set on the input view
         * since 3.0.0
         */
        @Nullable
        public String getInputText() {
            return inputText;
        }

        /**
         * Returns whether to use divider in suggested mention list.
         *
         * @return If <code>true</code> the divider is used at suggested mention list, <code>false</code> other wise.
         * since 3.0.0
         */
        public boolean shouldUseSuggestedMentionListDivider() {
            return useSuggestedMentionListDivider;
        }

        /**
         * Sets the UI configuration of mentioned text.
         *
         * @param configSentFromMe     the UI configuration of mentioned text in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of mentioned text in the message that was sent from others.
         * since 3.0.0
         */
        public void setMentionUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyMentionUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherMentionUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of message input text.
         *
         * @param textUIConfig the UI configuration of the message input text.
         * since 3.2.1
         */
        public void setMessageInputTextUIConfig(@Nullable TextUIConfig textUIConfig) {
            this.textUIConfig = textUIConfig;
        }

        /**
         * Returns the UI configuration of message input text.
         *
         * @return the UI configuration of message input text.
         * since 3.2.1
         */
        @Nullable
        public TextUIConfig getMessageInputTextUIConfig() {
            return textUIConfig;
        }

        /**
         * Returns {@link ChannelConfig} that will be used in this component.
         *
         * @return Channel config to be used in this component.
         * since 3.6.0
         */
        @NonNull
        public ChannelConfig getChannelConfig() {
            return channelConfig;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_INPUT_LEFT_BUTTON_ICON_RES_ID} is mapped to {@link #setLeftButtonIcon(Drawable)}
         * {@code KEY_INPUT_LEFT_BUTTON_ICON_TINT} is mapped to {@link #setLeftButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID} is mapped to {@link #setRightButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_RIGHT_BUTTON_ICON_TINT} is mapped to {@link #setRightButtonIconTint(ColorStateList)}
         * {@code KEY_INPUT_HINT} is mapped to {@link #setInputHint(String)}
         * {@code KEY_INPUT_TEXT} is mapped to {@link #setInputText(String)}
         * {@code KEY_USE_INPUT_LEFT_BUTTON} is mapped to {@link #setUseLeftButton(boolean)}
         * {@code KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS} is mapped to {@link #showInputRightButtonAlways()}
         * {@code KEY_KEYBOARD_DISPLAY_TYPE} is mapped to {@link #setKeyboardDisplayType(KeyboardDisplayType)}
         * {@code KEY_MENTION_UI_CONFIG_SENT_FROM_ME} and {@code KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setMentionUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_MESSAGE_INPUT_TEXT_UI_CONFIG} and {@code KEY_MESSAGE_INPUT_TEXT_UI_CONFIG} are mapped to {@link #setMessageInputTextUIConfig(TextUIConfig)}
         * {@code KEY_USE_SUGGESTED_MENTION_LIST_DIVIDER} is mapped to {@link #setUseSuggestedMentionListDivider(boolean)}
         * {@code KEY_CHANNEL_CONFIG} is mapped to {@link #setChannelConfig(ChannelConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID)) {
                setLeftButtonIcon(ContextCompat.getDrawable(context, args.getInt(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT)) {
                setLeftButtonIconTint(args.getParcelable(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID)) {
                setRightButtonIcon(ContextCompat.getDrawable(context, args.getInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT)) {
                setRightButtonIconTint(args.getParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_HINT)) {
                setInputHint(args.getString(StringSet.KEY_INPUT_HINT));
            }
            if (args.containsKey(StringSet.KEY_INPUT_TEXT)) {
                setInputText(args.getString(StringSet.KEY_INPUT_TEXT, ""));
            }
            if (args.containsKey(StringSet.KEY_USE_INPUT_LEFT_BUTTON)) {
                setUseLeftButton(args.getBoolean(StringSet.KEY_USE_INPUT_LEFT_BUTTON));
            }
            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS)) {
                if (args.getBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS)) {
                    showInputRightButtonAlways();
                }
            }
            if (args.containsKey(StringSet.KEY_KEYBOARD_DISPLAY_TYPE)) {
                final KeyboardDisplayType displayType = (KeyboardDisplayType) args.getSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE);
                if (displayType != null) {
                    setKeyboardDisplayType(displayType);
                }
            }
            setMentionUIConfig(args.getParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS));
            if (args.containsKey(StringSet.KEY_USE_SUGGESTED_MENTION_LIST_DIVIDER)) {
                setUseSuggestedMentionListDivider(args.getBoolean(StringSet.KEY_USE_SUGGESTED_MENTION_LIST_DIVIDER));
            }
            if (args.containsKey(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG)) {
                setMessageInputTextUIConfig(args.getParcelable(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG));
            }
            if (args.containsKey(StringSet.KEY_CHANNEL_CONFIG)) {
                setChannelConfig(args.getParcelable(StringSet.KEY_CHANNEL_CONFIG));
            }
            return this;
        }

        @TestOnly
        @NonNull
        MessageUIConfig getMessageUIConfig() {
            return messageUIConfig;
        }
    }

    @VisibleForTesting
    @NonNull
    MessageInputView createMessageInputView(@NonNull Context context) {
        return new MessageInputView(context, null, R.attr.sb_component_channel_message_input);
    }
}
