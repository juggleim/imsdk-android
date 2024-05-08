package com.jet.im.kit.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.MultipleFilesMessageCreateParams;
import com.sendbird.android.params.ThreadMessageListParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.activities.adapter.SuggestedMentionListAdapter;
import com.jet.im.kit.activities.adapter.ThreadListAdapter;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory;
import com.jet.im.kit.consts.KeyboardDisplayType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.MessageDisplayDataProvider;
import com.jet.im.kit.interfaces.OnEmojiReactionClickListener;
import com.jet.im.kit.interfaces.OnEmojiReactionLongClickListener;
import com.jet.im.kit.interfaces.OnInputModeChangedListener;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.internal.model.VoicePlayerManager;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.model.configurations.ChannelConfig;
import com.jet.im.kit.modules.MessageThreadModule;
import com.jet.im.kit.modules.components.MessageInputComponent;
import com.jet.im.kit.modules.components.MessageThreadHeaderComponent;
import com.jet.im.kit.modules.components.MessageThreadInputComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.modules.components.ThreadListComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.MessageUtils;
import com.jet.im.kit.utils.SoftInputUtils;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.vm.FileDownloader;
import com.jet.im.kit.vm.MessageThreadViewModel;
import com.jet.im.kit.widgets.MentionEditText;
import com.jet.im.kit.widgets.MessageInputView;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that provides thread list of the parent message
 *
 * since 3.3.0
 */
public class MessageThreadFragment extends BaseMessageListFragment<ThreadListAdapter, ThreadListComponent, MessageThreadModule, MessageThreadViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private View.OnClickListener headerDescriptionClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> parentMessageMenuClickListener;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnInputTextChangedListener editModeTextChangedListener;
    @Nullable
    private View.OnClickListener inputRightButtonClickListener;
    @Nullable
    private View.OnClickListener editModeCancelButtonClickListener;
    @Nullable
    private View.OnClickListener editModeSaveButtonClickListener;
    @Nullable
    private OnInputModeChangedListener inputModeChangedListener;
    @Nullable
    private View.OnClickListener voiceRecorderButtonClickListener;

    @Nullable
    private ThreadMessageListParams params;
    @NonNull
    private final AtomicBoolean isInitCallFinished = new AtomicBoolean(false);

    @NonNull
    @Override
    protected MessageThreadModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getMessageThread().provide(requireContext(), args, getParentMessage());
    }

    @NonNull
    @Override
    protected MessageThreadViewModel onCreateViewModel() {
        return ViewModelProviders.getMessageThread().provide(this, getChannelUrl(), getParentMessage(), params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MessageThreadModule module, @NonNull MessageThreadViewModel viewModel) {
        Logger.d(">> MessageThreadFragment::onBeforeReady()");
        super.onBeforeReady(status, module, viewModel);
        final GroupChannel channel = viewModel.getChannel();
        onBindMessageThreadHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindThreadListComponent(module.getMessageListComponent(), viewModel, channel);
        onBindMessageInputComponent(module.getMessageInputComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull MessageThreadModule module, @NonNull MessageThreadViewModel viewModel) {
        shouldDismissLoadingDialog();
        final GroupChannel channel = viewModel.getChannel();
        if (status != ReadyStatus.READY || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }

        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getMessageListComponent().notifyChannelChanged(channel);
        module.getMessageInputComponent().notifyChannelChanged(channel);
        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), channelUrl -> shouldActivityFinish());
        viewModel.onParentMessageDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted && isFragmentAlive()) {
                toastError(R.string.sb_text_original_message_deleted);
                shouldActivityFinish();
            }
        });
        viewModel.onReconnected().observe(getViewLifecycleOwner(), aBoolean -> {
            viewModel.loadInitial(module.getMessageListComponent().getCurrentViewPoint());
        });
        loadInitial(module.getMessageListComponent().getParams().getInitialStartingPoint());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isInitCallFinished.get()) {
            shouldDismissLoadingDialog();
        }
    }

    /**
     * Called to bind events to the MessageThreadHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageThreadModule, MessageThreadViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.3.0
     */
    protected void onBindMessageThreadHeaderComponent(@NonNull MessageThreadHeaderComponent headerComponent, @NonNull MessageThreadViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageThreadFragment::onBindMessageThreadHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener);
        headerComponent.setOnDescriptionClickListener(headerDescriptionClickListener != null ? headerDescriptionClickListener : v -> {
            if (!isFragmentAlive()) return;
            Intent intent = new ChannelActivity.IntentBuilder(requireContext(), getChannelUrl())
                .setStartingPoint(getParentMessage().getCreatedAt())
                .build();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            shouldActivityFinish();
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), headerComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the MessageListComponent and also bind ChannelViewModel.
     * This is called from {@link #onBeforeReady(ReadyStatus, MessageThreadModule, MessageThreadViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param messageListComponent The component to which the event will be bound
     * @param viewModel            A view model that provides the data needed for the fragment
     * @param channel              The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.3.0
     */
    protected void onBindThreadListComponent(@NonNull ThreadListComponent messageListComponent, @NonNull MessageThreadViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageThreadFragment::onBindMessageListComponent()");
        if (channel == null) return;
        messageListComponent.setOnMessageClickListener(this::onMessageClicked);
        messageListComponent.setOnMessageProfileLongClickListener(this::onMessageProfileLongClicked);
        messageListComponent.setOnMessageProfileClickListener(this::onMessageProfileClicked);
        messageListComponent.setOnMessageLongClickListener(this::onMessageLongClicked);
        messageListComponent.setOnMessageMentionClickListener(this::onMessageMentionClicked);
        messageListComponent.setOnEmojiReactionClickListener(emojiReactionClickListener != null ? emojiReactionClickListener : (view, position, message, reactionKey) -> toggleReaction(view, message, reactionKey));
        messageListComponent.setOnEmojiReactionLongClickListener(emojiReactionLongClickListener != null ? emojiReactionLongClickListener : (view, position, message, reactionKey) -> showEmojiReactionDialog(message, position));
        messageListComponent.setOnEmojiReactionMoreButtonClickListener(emojiReactionMoreButtonClickListener != null ? emojiReactionMoreButtonClickListener : (view, position, message) -> showEmojiListDialog(message));
        messageListComponent.setOnParentMessageMenuClickListener(this::onParentMessageMenuClicked);
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), messageListComponent::notifyChannelChanged);

        viewModel.getMessageList().observeAlways(getViewLifecycleOwner(), receivedMessageData -> {
            boolean isInitialCallFinished = isInitCallFinished.getAndSet(true);
            if (!isInitialCallFinished && isFragmentAlive()) shouldDismissLoadingDialog();

            final List<BaseMessage> messageList = receivedMessageData.getMessages();
            if (messageList.isEmpty()) return;
            final String eventSource = receivedMessageData.getTraceName();
            // The callback coming from setItems is worked asynchronously. So `isInitCallFinished` flag has to mark in advance.
            messageListComponent.notifyDataSetChanged(messageList, channel, messages -> {
                if (!isFragmentAlive()) return;

                if (eventSource != null) {
                    Logger.d("++ Message action : %s", eventSource);
                    final RecyclerView recyclerView = messageListComponent.getRecyclerView();

                    final ThreadListAdapter adapter = messageListComponent.getAdapter();
                    if (recyclerView == null || adapter == null) return;

                    final Context context = recyclerView.getContext();
                    switch (eventSource) {
                        case StringSet.ACTION_FAILED_MESSAGE_ADDED:
                        case StringSet.ACTION_PENDING_MESSAGE_ADDED:
                            getModule().getMessageInputComponent().requestInputMode(MessageInputView.Mode.DEFAULT);
                            if (viewModel.hasNext()) {
                                loadInitial(Long.MAX_VALUE);
                            } else {
                                messageListComponent.scrollToFirst();
                            }
                            break;
                        case StringSet.EVENT_MESSAGE_RECEIVED:
                        case StringSet.EVENT_MESSAGE_SENT:
                            messageListComponent.notifyOtherMessageReceived(false);
                            if (eventSource.equals(StringSet.EVENT_MESSAGE_SENT)) {
                                final BaseMessage latestMessage = adapter.getItem(adapter.getItemCount() - 1);
                                if (latestMessage instanceof FileMessage) {
                                    // Download from files already sent for quick image loading.
                                    FileDownloader.downloadThumbnail(context, (FileMessage) latestMessage);
                                }
                            }
                            break;
                        case StringSet.ACTION_INIT_FROM_REMOTE:
                        case StringSet.MESSAGE_CHANGELOG:
                        case StringSet.MESSAGE_FILL:
                            messageListComponent.notifyMessagesFilled(true);
                            break;
                    }
                }

                if (!isInitialCallFinished) {
                    messageListComponent.moveToFocusedMessage(viewModel.getStartingPoint(), null);
                }
            });
        });
        viewModel.onThreadMessageDeleted().observe(getViewLifecycleOwner(), deletedMessageId -> {
            if (String.valueOf(deletedMessageId).equals(VoicePlayerManager.getCurrentKey())) {
                VoicePlayerManager.pause();
            }
        });
    }

    /**
     * Called to bind events to the MessageInputComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageThreadModule, MessageThreadViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param inputComponent The component to which the event will be bound
     * @param viewModel      A view model that provides the data needed for the fragment
     * @param channel        The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.3.0
     */
    protected void onBindMessageInputComponent(@NonNull MessageInputComponent inputComponent, @NonNull MessageThreadViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageThreadFragment::onBindMessageInputComponent()");
        if (channel == null) return;
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), inputComponent::notifyChannelChanged);
        viewModel.onParentMessageUpdated().observe(getViewLifecycleOwner(), parentMessage -> {
            if (inputComponent instanceof MessageThreadInputComponent) {
                ((MessageThreadInputComponent) inputComponent).notifyParentMessageUpdated(channel, parentMessage);
            }
        });
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), status -> {
            if (inputComponent instanceof MessageThreadInputComponent) {
                ((MessageThreadInputComponent) inputComponent).notifyStatusUpdated(channel, status);
            }
        });

        inputComponent.setOnInputLeftButtonClickListener(inputLeftButtonClickListener != null ? inputLeftButtonClickListener : v -> showMediaSelectDialog());
        inputComponent.setOnInputRightButtonClickListener(inputRightButtonClickListener != null ? inputRightButtonClickListener : this::onInputRightButtonClicked);
        inputComponent.setOnEditModeSaveButtonClickListener(editModeSaveButtonClickListener != null ? editModeSaveButtonClickListener : v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                if (null != targetMessage) {
                    UserMessageUpdateParams params = new UserMessageUpdateParams(inputText.getText().toString());
                    if (inputText instanceof MentionEditText) {
                        final List<User> mentionedUsers = ((MentionEditText) inputText).getMentionedUsers();
                        final CharSequence mentionedTemplate = ((MentionEditText) inputText).getMentionedTemplate();
                        Logger.d("++ mentioned template text=%s", mentionedTemplate);
                        params.setMentionedMessageTemplate(mentionedTemplate.toString());
                        params.setMentionedUsers(mentionedUsers);
                    }
                    updateUserMessage(targetMessage.getMessageId(), params);
                } else {
                    Logger.d("Target message for update is missing");
                }
            }
            inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
        });

        inputComponent.setOnEditModeTextChangedListener(editModeTextChangedListener != null ? editModeTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnEditModeCancelButtonClickListener(editModeCancelButtonClickListener != null ? editModeCancelButtonClickListener : v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnInputTextChangedListener(inputTextChangedListener != null ? inputTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnInputModeChangedListener(inputModeChangedListener != null ? inputModeChangedListener : this::onInputModeChanged);
        inputComponent.setOnVoiceRecorderButtonClickListener((voiceRecorderButtonClickListener != null) ? voiceRecorderButtonClickListener : v -> takeVoiceRecorder());

        if (channelConfig.getEnableMention()) {
            inputComponent.bindUserMention(SendbirdUIKit.getUserMentionConfig(), text -> viewModel.loadMemberList(text != null ? text.toString() : null));

            // observe suggestion list
            viewModel.getMentionSuggestion().observe(getViewLifecycleOwner(), suggestion -> inputComponent.notifySuggestedMentionDataChanged(suggestion.getSuggestionList()));
        }
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageThreadModule, MessageThreadViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.3.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull MessageThreadViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageThreadFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    private synchronized void loadInitial(long startingPoint) {
        if (!isFragmentAlive()) return;
        isInitCallFinished.set(false);
        getViewModel().loadInitial(startingPoint);
    }

    private void onInputRightButtonClicked(@NonNull View view) {
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        final EditText inputText = inputComponent.getEditTextView();
        if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
            final Editable editableText = inputText.getText();
            UserMessageCreateParams params = new UserMessageCreateParams(editableText.toString());
            params.setParentMessageId(getViewModel().getParentMessage().getMessageId());
            params.setReplyToChannel(true);
            if (channelConfig.getEnableMention()) {
                if (inputText instanceof MentionEditText) {
                    final List<User> mentionedUsers = ((MentionEditText) inputText).getMentionedUsers();
                    final CharSequence mentionedTemplate = ((MentionEditText) inputText).getMentionedTemplate();
                    Logger.d("++ mentioned template text=%s", mentionedTemplate);
                    params.setMentionedMessageTemplate(mentionedTemplate.toString());
                    params.setMentionedUsers(mentionedUsers);
                }
            }
            sendUserMessage(params);
        }
    }

    private void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current) {
        final GroupChannel channel = getViewModel().getChannel();
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        if (channel == null) return;

        if (current == MessageInputView.Mode.EDIT) {
            inputComponent.notifyDataChanged(targetMessage, channel);
        } else {
            inputComponent.notifyDataChanged(null, channel);
            targetMessage = null;
        }
    }

    /**
     * Called when the parent message menu is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The parent message clicked
     * since 3.3.0
     */
    protected void onParentMessageMenuClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (parentMessageMenuClickListener != null) {
            parentMessageMenuClickListener.onItemClick(view, position, message);
            return;
        }
        showMessageContextMenu(view, message, makeMessageContextMenu(message));
    }

    @NonNull
    @Override
    protected List<DialogListItem> makeMessageContextMenu(@NonNull BaseMessage message) {
        final List<DialogListItem> items = new ArrayList<>();
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return items;

        MessageType type = MessageViewHolderFactory.getMessageType(message);
        DialogListItem copy = new DialogListItem(R.string.sb_text_channel_anchor_copy, R.drawable.icon_copy);
        DialogListItem edit = new DialogListItem(R.string.sb_text_channel_anchor_edit, R.drawable.icon_edit);
        DialogListItem save = new DialogListItem(R.string.sb_text_channel_anchor_save, R.drawable.icon_download);
        DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete, false, MessageUtils.hasThread(message));
        DialogListItem retry = new DialogListItem(R.string.sb_text_channel_anchor_retry, 0);
        DialogListItem deleteFailed = new DialogListItem(R.string.sb_text_channel_anchor_delete, 0);

        DialogListItem[] actions = null;
        switch (type) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                if (status == SendingStatus.SUCCEEDED) {
                    actions = new DialogListItem[]{copy, edit, delete};
                } else if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                }
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                actions = new DialogListItem[]{copy};
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    actions = new DialogListItem[]{delete, save};
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                actions = new DialogListItem[]{save};
                break;
            case VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME:
            case VIEW_TYPE_VOICE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    actions = new DialogListItem[]{delete};
                }
                break;
            case VIEW_TYPE_UNKNOWN_MESSAGE_ME:
                actions = new DialogListItem[]{delete};
            default:
                break;
        }

        if (actions != null) {
            items.addAll(Arrays.asList(actions));
        }
        return items;
    }

    @Override
    protected boolean onMessageContextMenuItemClicked(@NonNull BaseMessage message, @NonNull View view, int position, @NonNull DialogListItem item) {
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        int key = item.getKey();
        if (key == R.string.sb_text_channel_anchor_copy) {
            copyTextToClipboard(message.getMessage());
            return true;
        } else if (key == R.string.sb_text_channel_anchor_edit) {
            targetMessage = message;
            inputComponent.requestInputMode(MessageInputView.Mode.EDIT);
            return true;
        } else if (key == R.string.sb_text_channel_anchor_delete) {
            if (MessageUtils.isFailed(message)) {
                Logger.dev("delete");
                deleteMessage(message);
            } else {
                showWarningDialog(message);
            }
            return true;
        } else if (key == R.string.sb_text_channel_anchor_save) {
            if (message instanceof FileMessage) {
                saveFileMessage((FileMessage) message);
            }
            return true;
        }
        if (key == R.string.sb_text_channel_anchor_retry) {
            resendMessage(message);
            return true;
        }
        return false;
    }

    @Override
    void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {
        int size = items.size();
        final DialogListItem[] actions = items.toArray(new DialogListItem[size]);
        if (!(getViewModel().getChannel() != null && ChannelConfig.canSendReactions(channelConfig, getViewModel().getChannel())) ||
            MessageUtils.isUnknownType(message) ||
            !MessageUtils.isSucceed(message)
        ) {
            if (getContext() == null || size == 0) return;
            hideKeyboard();
            DialogUtils.showListBottomDialog(requireContext(), actions, createMessageActionListener(message));
        } else {
            showEmojiActionsDialog(message, actions);
        }
    }

    @Override
    void sendFileMessageInternal(@NonNull FileInfo fileInfo, @NonNull FileMessageCreateParams params) {
        params.setParentMessageId(getViewModel().getParentMessage().getMessageId());
        params.setReplyToChannel(true);
        getViewModel().sendFileMessage(params, fileInfo);
    }

    @Override
    void sendMultipleFilesMessageInternal(@NonNull List<FileInfo> fileInfos, @NonNull MultipleFilesMessageCreateParams params) {
        params.setParentMessageId(getViewModel().getParentMessage().getMessageId());
        params.setReplyToChannel(true);
        getViewModel().sendMultipleFilesMessage(fileInfos, params);
    }

    /**
     * Returns the parent message to use this fragment.
     *
     * @return The parent message this fragment is currently associated with
     * since 3.3.0
     */
    @NonNull
    protected BaseMessage getParentMessage() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return Objects.requireNonNull(BaseMessage.buildFromSerializedData(args.getByteArray(StringSet.KEY_PARENT_MESSAGE)));
    }

    @Override
    protected void shouldActivityFinish() {
        final View view = getModule().getMessageInputComponent().getRootView();
        if (view != null) {
            SoftInputUtils.hideSoftKeyboard(view);
        }
        super.shouldActivityFinish();
    }

    @SuppressWarnings("unused")
    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private ThreadListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private View.OnClickListener headerDescriptionClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> messageClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> messageProfileClickListener;
        @Nullable
        private OnItemClickListener<User> emojiReactionUserListProfileClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageLongClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> parentMessageMenuClickListener;
        @Nullable
        private View.OnClickListener inputLeftButtonListener;
        @Nullable
        private OnEmojiReactionClickListener emojiReactionClickListener;
        @Nullable
        private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
        @Nullable
        private ThreadMessageListParams params;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private OnInputTextChangedListener inputTextChangedListener;
        @Nullable
        private OnInputTextChangedListener editModeTextChangedListener;
        @Nullable
        private SuggestedMentionListAdapter suggestedMentionListAdapter;
        @Nullable
        private View.OnClickListener inputRightButtonClickListener;
        @Nullable
        private View.OnClickListener editModeCancelButtonClickListener;
        @Nullable
        private View.OnClickListener editModeSaveButtonClickListener;
        @Nullable
        private OnInputModeChangedListener inputModeChangedListener;
        @Nullable
        private View.OnClickListener voiceRecorderButtonClickListener;
        @Nullable
        private OnItemClickListener<User> messageMentionClickListener;
        @Nullable
        private MessageThreadFragment customFragment;


        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param parentMessage the parent message of thread.
         * since 3.3.0
         */
        public Builder(@NonNull String channelUrl, @NonNull BaseMessage parentMessage) {
            this(channelUrl, parentMessage, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param parentMessage the parent message of thread.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * since 3.3.0
         */
        public Builder(@NonNull String channelUrl, @NonNull BaseMessage parentMessage, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, parentMessage, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param parentMessage    the parent message of thread.
         * @param customThemeResId the resource identifier for custom theme. The theme resource id must be `sb_module_message_thread`.
         * since 3.3.0
         */
        public Builder(@NonNull String channelUrl, @NonNull BaseMessage parentMessage, @StyleRes int customThemeResId) {
            this.bundle = new Bundle();
            if (customThemeResId != 0) {
                this.bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            this.bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
            this.bundle.putByteArray(StringSet.KEY_PARENT_MESSAGE, parentMessage.serialize());
        }

        /**
         * Sets the custom fragment. It must inherit {@link MessageThreadFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public <T extends MessageThreadFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startTimemillis The timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setStartingPoint(long startTimemillis) {
            bundle.putLong(StringSet.KEY_STARTING_POINT, startTimemillis);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseHeaderRightButton(boolean useHeaderRightButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useHeaderRightButton);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setHeaderRightButtonIconResId(@DrawableRes int resId) {
            return setHeaderRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets whether the left button of the input is used.
         *
         * @param useInputLeftButton <code>true</code> if the left button of the input is used,
         *                           <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseInputLeftButton(boolean useInputLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_INPUT_LEFT_BUTTON, useInputLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputLeftButtonIconResId(@DrawableRes int resId) {
            return setInputLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon on the right button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputRightButtonIconResId(@DrawableRes int resId) {
            return setInputRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets whether showing the right button of the input always.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder showInputRightButtonAlways() {
            bundle.putBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS, true);
            return this;
        }

        /**
         * Sets the hint of the input text.
         *
         * @param hint text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputHint(@NonNull String hint) {
            bundle.putString(StringSet.KEY_INPUT_HINT, hint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the description of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnHeaderDescriptionClickListener(@NonNull View.OnClickListener listener) {
            this.headerDescriptionClickListener = listener;
            return this;
        }

        /**
         * Sets the thread list adapter.
         *
         * @param adapter the adapter for the thread list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setThreadListAdapter(@Nullable ThreadListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the thread list adapter and the message display data provider.
         * The message display data provider is used to generate the data to display the message.
         *
         * @param adapter the adapter for the thread list.
         * @param provider the provider for the message display data.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.7
         */
        @NonNull
        public Builder setThreadListAdapter(@Nullable ThreadListAdapter adapter, @Nullable MessageDisplayDataProvider provider) {
            this.adapter = adapter;
            if (this.adapter != null) this.adapter.setMessageDisplayDataProvider(provider);
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnMessageClickListener(@NonNull OnItemClickListener<BaseMessage> itemClickListener) {
            this.messageClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnMessageLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> itemLongClickListener) {
            this.messageLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the input.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnInputLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.inputLeftButtonListener = listener;
            return this;
        }

        /**
         * Sets the thread message list params for this channel.
         * The reverse, the nextResultSize, and the previousResultSize properties in the ThreadMessageListParams are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param params The ThreadMessageListParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setThreadMessageListParams(@NonNull ThreadMessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Sets the click listener on the emoji reaction of the message.
         *
         * @param emojiReactionClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEmojiReactionClickListener(@NonNull OnEmojiReactionClickListener emojiReactionClickListener) {
            this.emojiReactionClickListener = emojiReactionClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the emoji reaction of the message.
         *
         * @param emojiReactionLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEmojiReactionLongClickListener(@NonNull OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
            this.emojiReactionLongClickListener = emojiReactionLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the emoji reaction more button.
         *
         * @param emojiReactionMoreButtonClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEmojiReactionMoreButtonClickListener(@NonNull OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
            this.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
            return this;
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseMessageGroupUI(boolean useMessageGroupUI) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, useMessageGroupUI);
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnMessageProfileClickListener(@NonNull OnItemClickListener<BaseMessage> profileClickListener) {
            this.messageProfileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets the click listener on the profile of emoji reaction user list.
         *
         * @param emojiReactionUserListProfileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.9.2
         */
        @NonNull
        public Builder setOnEmojiReactionUserListProfileClickListener(@NonNull OnItemClickListener<User> emojiReactionUserListProfileClickListener) {
            this.emojiReactionUserListProfileClickListener = emojiReactionUserListProfileClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the profile of message.
         *
         * @param messageProfileLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnMessageProfileLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
            this.messageProfileLongClickListener = messageProfileLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the parent message menu.
         *
         * @param parentMessageMenuClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnParentMessageMenuClickListener(@NonNull OnItemClickListener<BaseMessage> parentMessageMenuClickListener) {
            this.parentMessageMenuClickListener = parentMessageMenuClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * The message input displays as a dialog type. (Refer to {@link KeyboardDisplayType})
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setKeyboardDisplayType(@NonNull KeyboardDisplayType type) {
            bundle.putSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE, type);
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 3.3.0
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_EMPTY_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_EMPTY_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the text when the data is not exists
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the text when error occurs
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is edited.
         *
         * @param editModeTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnEditModeTextChangedListener(@NonNull OnInputTextChangedListener editModeTextChangedListener) {
            this.editModeTextChangedListener = editModeTextChangedListener;
            return this;
        }

        /**
         * Sets the input text
         *
         * @param inputText the message text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setInputText(@NonNull String inputText) {
            bundle.putString(StringSet.KEY_INPUT_TEXT, inputText);
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is changed..
         *
         * @param inputTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@NonNull OnInputTextChangedListener inputTextChangedListener) {
            this.inputTextChangedListener = inputTextChangedListener;
            return this;
        }

        /**
         * Sets the suggested mention list adapter.
         *
         * @param adapter the adapter for the mentionable user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setSuggestedMentionListAdapter(@Nullable SuggestedMentionListAdapter adapter) {
            this.suggestedMentionListAdapter = adapter;
            return this;
        }

        /**
         * Sets the UI configuration of mentioned text.
         *
         * @param configSentFromMe     the UI configuration of mentioned text in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of mentioned text in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setMentionUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of edited text mark.
         *
         * @param configSentFromMe     the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of edited text mark in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setEditedTextMarkUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message text.
         *
         * @param configSentFromMe       the UI configuration of the message text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message text that was sent from others.\
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setMessageTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message sentAt text.
         *
         * @param configSentFromMe       the UI configuration of the message sentAt text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message sentAt text that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setSentAtTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of sender nickname text.
         *
         * @param configSentFromOthers   the UI configuration of the sender nickname text that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setNicknameTextUIConfig(@NonNull TextUIConfig configSentFromOthers) {
            bundle.putParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message input text.
         *
         * @param textUIConfig the UI configuration of the message input text.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setMessageInputTextUIConfig(@NonNull TextUIConfig textUIConfig) {
            bundle.putParcelable(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG, textUIConfig);
            return this;
        }

        /**
         * Sets the UI configuration of message background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the message background that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the message background that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setMessageBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message reaction list background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the message reaction list background drawable that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the message reaction list background drawable that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setReactionListBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of ogtag message background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the ogtag message background drawable that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the ogtag message background drawable that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOgtagBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of the linked text color in the message text.
         *
         * @param colorRes  the UI configuration of the linked text color.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setLinkedTextColor(@ColorRes int colorRes) {
            bundle.putInt(StringSet.KEY_LINKED_TEXT_COLOR, colorRes);
            return this;
        }

        /**
         * Register a callback to be invoked when the right button of the input is clicked.
         *
         * @param inputRightButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnInputRightButtonClickListener(@Nullable View.OnClickListener inputRightButtonClickListener) {
            this.inputRightButtonClickListener = inputRightButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the cancel button is clicked, when the input is the edited mode.
         *
         * @param editModeCancelButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnEditModeCancelButtonClickListener(@Nullable View.OnClickListener editModeCancelButtonClickListener) {
            this.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the save button is clicked, when the input is the edited mode.
         *
         * @param editModeSaveButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnEditModeSaveButtonClickListener(@Nullable View.OnClickListener editModeSaveButtonClickListener) {
            this.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the input mode is changed.
         *
         * @param inputModeChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnInputModeChangedListener(@Nullable OnInputModeChangedListener inputModeChangedListener) {
            this.inputModeChangedListener = inputModeChangedListener;
            return this;
        }

        /**
         * Sets whether to use divider in suggested mention list.
         *
         * @param useDivider If <code>true</code> the divider will be used at suggested mention list, <code>false</code> other wise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseSuggestedMentionListDivider(boolean useDivider) {
            bundle.putBoolean(StringSet.KEY_USE_SUGGESTED_MENTION_LIST_DIVIDER, useDivider);
            return this;
        }

        /**
         * Sets whether the message list banner is used.
         *
         * @param useBanner <code>true</code> if the message list banner is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseMessageListBanner(boolean useBanner) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_LIST_BANNER, useBanner);
            return this;
        }

        /**
         * Register a callback to be invoked when the button to show voice recorder is clicked.
         *
         * @param voiceRecorderButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.4.0
         */
        @NonNull
        public Builder setOnVoiceRecorderButtonClickListener(@Nullable View.OnClickListener voiceRecorderButtonClickListener) {
            this.voiceRecorderButtonClickListener = voiceRecorderButtonClickListener;
            return this;
        }

        /**
         * Sets the click listener on the mentioned user of message.
         *
         * @param mentionClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.3
         */
        @NonNull
        public Builder setOnMessageMentionClickListener(@NonNull OnItemClickListener<User> mentionClickListener) {
            this.messageMentionClickListener = mentionClickListener;
            return this;
        }

        /**
         * Sets channel configuration for this fragment.
         * Use {@code UIKitConfig.groupChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val fragment = MessageThreadFragment.Builder(CHANNEL_URL, parentMessage)
         *     .setChannelConfig(
         *         UIKitConfig.groupChannelConfig.clone().apply {
         *             this.enableMention = true
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param channelConfig The channel config.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         */
        @NonNull
        public Builder setChannelConfig(@NonNull ChannelConfig channelConfig) {
            this.bundle.putParcelable(StringSet.KEY_CHANNEL_CONFIG, channelConfig);
            return this;
        }

        /**
         * Creates an {@link MessageThreadFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link MessageThreadFragment} applied to the {@link Bundle}.
         * since 3.3.0
         */
        @NonNull
        public MessageThreadFragment build() {
            final MessageThreadFragment fragment = customFragment != null ? customFragment : new MessageThreadFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.headerDescriptionClickListener = headerDescriptionClickListener;
            fragment.setOnMessageClickListener(messageClickListener);
            fragment.setOnMessageLongClickListener(messageLongClickListener);
            fragment.inputLeftButtonClickListener = inputLeftButtonListener;
            fragment.emojiReactionClickListener = emojiReactionClickListener;
            fragment.emojiReactionLongClickListener = emojiReactionLongClickListener;
            fragment.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
            fragment.setOnMessageProfileClickListener(messageProfileClickListener);
            fragment.setOnEmojiReactionUserListProfileClickListener(emojiReactionUserListProfileClickListener);
            fragment.setOnMessageProfileLongClickListener(messageProfileLongClickListener);
            fragment.parentMessageMenuClickListener = parentMessageMenuClickListener;
            fragment.setOnLoadingDialogHandler(loadingDialogHandler);
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.editModeTextChangedListener = editModeTextChangedListener;
            fragment.setSuggestedMentionListAdapter(suggestedMentionListAdapter);
            fragment.inputRightButtonClickListener = inputRightButtonClickListener;
            fragment.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
            fragment.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
            fragment.inputModeChangedListener = inputModeChangedListener;
            fragment.voiceRecorderButtonClickListener = voiceRecorderButtonClickListener;
            fragment.setOnMessageMentionClickListener(messageMentionClickListener);
            fragment.setAdapter(adapter);
            fragment.params = params;
            return fragment;
        }
    }
}
