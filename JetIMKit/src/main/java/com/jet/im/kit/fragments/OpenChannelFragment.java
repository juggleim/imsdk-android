package com.jet.im.kit.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.message.UserMessage;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.user.Sender;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.OpenChannelSettingsActivity;
import com.jet.im.kit.activities.ParticipantListActivity;
import com.jet.im.kit.activities.PhotoViewActivity;
import com.jet.im.kit.activities.adapter.OpenChannelMessageListAdapter;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory;
import com.jet.im.kit.consts.KeyboardDisplayType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.CustomParamsHandler;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.OnConsumableClickListener;
import com.jet.im.kit.interfaces.OnInputModeChangedListener;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemEventListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnResultHandler;
import com.jet.im.kit.internal.tasks.JobResultTask;
import com.jet.im.kit.internal.tasks.TaskQueue;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.model.configurations.OpenChannelConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.modules.OpenChannelModule;
import com.jet.im.kit.modules.components.OpenChannelHeaderComponent;
import com.jet.im.kit.modules.components.OpenChannelMessageInputComponent;
import com.jet.im.kit.modules.components.OpenChannelMessageListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.IntentUtils;
import com.jet.im.kit.utils.MessageUtils;
import com.jet.im.kit.utils.PermissionUtils;
import com.jet.im.kit.utils.SoftInputUtils;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.vm.FileDownloader;
import com.jet.im.kit.vm.OpenChannelViewModel;
import com.jet.im.kit.widgets.MessageInputView;
import com.jet.im.kit.widgets.StatusFrameView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that provides chat in {@code OpenChannel}
 */
public class OpenChannelFragment extends BaseModuleFragment<OpenChannelModule, OpenChannelViewModel> {
    @Nullable
    private OpenChannelMessageListAdapter adapter;
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private MessageListParams params;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;
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
    @Deprecated
    private View.OnClickListener scrollBottomButtonClickListener;
    @Nullable
    private OnConsumableClickListener scrollFirstButtonClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnItemEventListener<BaseMessage> messageInsertedListener;

    @Nullable
    private BaseMessage targetMessage;
    @NonNull
    final AtomicBoolean isInitCallFinished = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean anchorDialogShowing = new AtomicBoolean(false);
    @Nullable
    private Uri mediaUri;

    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null) return;
        final Uri mediaUri = intent.getData();
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        final Uri mediaUri = OpenChannelFragment.this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        final Uri mediaUri = OpenChannelFragment.this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onPickMediaResult);

    @NonNull
    private OpenChannelConfig openChannelConfig = UIKitConfig.getOpenChannelConfig();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null && args.containsKey(StringSet.KEY_OPEN_CHANNEL_CONFIG)) {
            openChannelConfig = args.getParcelable(StringSet.KEY_OPEN_CHANNEL_CONFIG);
        }
    }

    @NonNull
    @Override
    protected OpenChannelModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getOpenChannel().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull OpenChannelModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @Override
    public void onDestroy() {
        Logger.i(">> OpenChannelFragment::onDestroy()");
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);

        if (!isInitCallFinished.get()) {
            shouldDismissLoadingDialog();
        }
    }

    /**
     * Returns a {@link OpenChannelViewModel}.
     * It is a class that includes business logic such as data and networking to be used on the OpenChannel screen,
     * and uses Android's View Model.
     *
     * @return OpenChannelViewModel
     * @see OpenChannelViewModel
     * since 3.0.0
     */
    @NonNull
    @Override
    public OpenChannelViewModel onCreateViewModel() {
        return ViewModelProviders.getOpenChannel().provide(this, getChannelUrl(), params);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.i(">> OpenChannelFragment::onConfigurationChanged(%s)", newConfig.orientation);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelModule module, @NonNull OpenChannelViewModel viewModel) {
        Logger.d(">> OpenChannelFragment::onBeforeReady()");
        module.getMessageListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getMessageListComponent().setAdapter(adapter);
        }
        final OpenChannel channel = viewModel.getChannel();
        onBindChannelHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindMessageListComponent(module.getMessageListComponent(), viewModel, channel);
        onBindMessageInputComponent(module.getMessageInputComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull OpenChannelModule module, @NonNull OpenChannelViewModel viewModel) {
        Logger.d(">> OpenChannelFragment::onReady()");
        final OpenChannel channel = viewModel.getChannel();
        shouldDismissLoadingDialog();
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel, getModule().getParams().shouldUseOverlayMode());
                shouldActivityFinish();
            }
            return;
        }

        module.getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
        viewModel.enterChannel(channel, e -> {
            if (!isFragmentAlive()) return;
            module.getStatusComponent().notifyStatusChanged(StatusFrameView.Status.NONE);
            if (e != null) {
                toastError(R.string.sb_text_error_get_channel, getModule().getParams().shouldUseOverlayMode());
                shouldActivityFinish();
                return;
            }
            viewModel.loadInitial();
        });

        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getMessageListComponent().notifyChannelChanged(channel);
        module.getMessageInputComponent().notifyChannelChanged(channel);
        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), deleted -> shouldActivityFinish());
        viewModel.getMessageLoadState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case LOAD_STARTED:
                    break;
                case LOAD_ENDED:
                    if (isFragmentAlive() && isInitCallFinished.getAndSet(false)) {
                        shouldDismissLoadingDialog();
                    }
                    break;
            }
        });
    }

    /**
     * Called to bind events to the OpenChannelHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModule, OpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindChannelHeaderComponent(@NonNull OpenChannelHeaderComponent headerComponent, @NonNull OpenChannelViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> OpenChannelFragment::onBindChannelHeaderComponent()");
        if (channel == null) return;

        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            if (!isFragmentAlive()) return;
            boolean isOperator = channel.isOperator(SendbirdChat.getCurrentUser());
            if (isOperator) {
                Intent intent = OpenChannelSettingsActivity.newIntent(requireContext(), channel.getUrl());
                startActivity(intent);
            } else {
                startActivity(ParticipantListActivity.newIntent(requireContext(), channel.getUrl()));
            }
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), headerComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the OpenChannelMessageListComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModule, OpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param messageListComponent The component to which the event will be bound
     * @param viewModel            A view model that provides the data needed for the fragment
     * @param channel              The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMessageListComponent(@NonNull OpenChannelMessageListComponent messageListComponent, @NonNull OpenChannelViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> OpenChannelFragment::onBindMessageListComponent()");

        if (channel == null) return;
        messageListComponent.setOnMessageClickListener(this::onMessageClicked);
        messageListComponent.setOnMessageProfileClickListener(this::onMessageProfileClicked);
        messageListComponent.setOnMessageLongClickListener(this::onMessageLongClicked);
        messageListComponent.setOnMessageProfileLongClickListener(messageProfileLongClickListener);
        messageListComponent.setOnScrollBottomButtonClickListener(scrollBottomButtonClickListener);
        messageListComponent.setOnScrollFirstButtonClickListener(scrollFirstButtonClickListener);
        messageListComponent.setOnMessageInsertedListener(messageInsertedListener != null ? messageInsertedListener : data -> {
            if (!anchorDialogShowing.get()) {
                messageListComponent.scrollToFirst();
            }
        });

        viewModel.getMessageList().observe(getViewLifecycleOwner(), messageList -> {
            Logger.dev("++ result messageList size : %s", messageList.size());
            messageListComponent.notifyDataSetChanged(messageList, channel, null);
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), messageListComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the OpenChannelMessageInputComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModule, OpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param inputComponent The component to which the event will be bound
     * @param viewModel      A view model that provides the data needed for the fragment
     * @param channel        The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMessageInputComponent(@NonNull OpenChannelMessageInputComponent inputComponent, @NonNull OpenChannelViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> OpenChannelFragment::onBindMessageInputComponent()");
        if (channel == null) return;
        inputComponent.setOnInputLeftButtonClickListener(inputLeftButtonClickListener != null ? inputLeftButtonClickListener : v -> showMediaSelectDialog());
        inputComponent.setOnInputRightButtonClickListener(inputRightButtonClickListener != null ? inputRightButtonClickListener : v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                UserMessageCreateParams params = new UserMessageCreateParams(inputText.getText().toString());
                sendUserMessage(params);
            }
        });
        inputComponent.setOnEditModeSaveButtonClickListener(editModeSaveButtonClickListener != null ? editModeSaveButtonClickListener : v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                UserMessageUpdateParams params = new UserMessageUpdateParams(inputText.getText().toString());
                if (null != targetMessage) {
                    updateUserMessage(targetMessage.getMessageId(), params);
                } else {
                    Logger.d("Target message for update is missing");
                }
            }
            inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
        });
        inputComponent.setOnEditModeTextChangedListener(editModeTextChangedListener);
        inputComponent.setOnInputTextChangedListener(inputTextChangedListener);
        inputComponent.setOnEditModeCancelButtonClickListener(editModeCancelButtonClickListener != null ? editModeCancelButtonClickListener : v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnInputModeChangedListener(inputModeChangedListener != null ? inputModeChangedListener : (before, current) -> {
            if (current == MessageInputView.Mode.DEFAULT) {
                targetMessage = null;
            }
            inputComponent.notifyDataChanged(targetMessage, channel);
        });

        viewModel.onMessageDeleted().observe(getViewLifecycleOwner(), deletedMessageId -> {
            if (targetMessage != null && deletedMessageId.equals(targetMessage.getMessageId())) {
                targetMessage = null;
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), openChannel -> {
            inputComponent.notifyChannelChanged(openChannel);
            boolean isOperator = channel.isOperator(SendbirdChat.getCurrentUser());
            boolean isFrozen = channel.isFrozen() && !isOperator;
            if (isFrozen) {
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });
        viewModel.getMyMutedInfo().observe(getViewLifecycleOwner(), isMuted -> {
            if (viewModel.getChannel() == null) return;
            inputComponent.notifyMyMutedStateChanged(viewModel.getChannel(), isMuted);
            if (isMuted) {
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModule, OpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull OpenChannelViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> OpenChannelFragment::onBindStatusComponent()");
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    private void showUserProfile(@NonNull Sender sender) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, UIKitConfig.getCommon().getEnableUsingDefaultUserProfile());
        if (getContext() == null || !useUserProfile) return;
        hideKeyboard();
        DialogUtils.showUserProfileDialog(getContext(), sender, false, null, null, getModule().getParams().shouldUseOverlayMode());
    }

    /**
     * Make context menu items that are shown when the message is long clicked.
     *
     * @param message A clicked message.
     * @return Collection of {@link DialogListItem}
     * since 3.0.0
     */
    @NonNull
    protected List<DialogListItem> makeMessageContextMenu(@NonNull BaseMessage message) {
        final List<DialogListItem> items = new ArrayList<>();
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return items;

        MessageType type = MessageViewHolderFactory.getMessageType(message);
        DialogListItem copy = new DialogListItem(R.string.sb_text_channel_anchor_copy, R.drawable.icon_copy);
        DialogListItem edit = new DialogListItem(R.string.sb_text_channel_anchor_edit, R.drawable.icon_edit);
        DialogListItem save = new DialogListItem(R.string.sb_text_channel_anchor_save, R.drawable.icon_download);
        DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete);
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

    private void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {
        int size = items.size();
        final DialogListItem[] actions = items.toArray(new DialogListItem[size]);

        if (MessageUtils.isUnknownType(message)) {
            if (getContext() == null) return;
            DialogUtils.showListBottomDialog(requireContext(), actions, createMessageActionListener(message), getModule().getParams().shouldUseOverlayMode());
        } else {
            if (getContext() == null) return;
            final RecyclerView messageListView = getModule().getMessageListComponent().getRecyclerView();
            if (messageListView != null) {
                MessageAnchorDialog messageAnchorDialog = new MessageAnchorDialog.Builder(anchorView, messageListView, actions)
                    .setOnItemClickListener(createMessageActionListener(message))
                    .setOnDismissListener(() -> anchorDialogShowing.set(false))
                    .setUseOverlay(getModule().getParams().shouldUseOverlayMode())
                    .build();
                messageAnchorDialog.show();
                anchorDialogShowing.set(true);
            }
        }
    }

    /**
     * It will be called when the message context menu was clicked.
     *
     * @param message  A clicked message.
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param item     {@link DialogListItem} that was clicked.
     * @return <code>true</code> if long click event was handled, <code>false</code> otherwise.
     * since 3.0.0
     */
    @SuppressWarnings("UnusedReturnValue")
    protected boolean onMessageContextMenuItemClicked(@NonNull BaseMessage message, @NonNull View view, int position, @NonNull DialogListItem item) {
        final OpenChannelMessageInputComponent inputComponent = getModule().getMessageInputComponent();
        final int key = item.getKey();
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
        } else if (key == R.string.sb_text_channel_anchor_retry) {
            resendMessage(message);
            return true;
        }
        return false;
    }

    /**
     * It will be called when the input message's left button is clicked.
     * The default behavior is showing the menu, like, taking camera, gallery, and file.
     *
     * since 2.0.1
     */
    protected void showMediaSelectDialog() {
        if (getContext() == null) return;
        final List<DialogListItem> items = new ArrayList<>();
        if (openChannelConfig.getInput().getCamera().getEnablePhoto()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_camera, R.drawable.icon_camera));
        }
        if (openChannelConfig.getInput().getCamera().getEnableVideo()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_take_video, R.drawable.icon_camera));
        }
        if (openChannelConfig.getInput().getGallery().getEnablePhoto() || openChannelConfig.getInput().getGallery().getEnableVideo()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_gallery, R.drawable.icon_photo));
        }
        if (openChannelConfig.getInput().getEnableDocument()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_document, R.drawable.icon_document));
        }
        if (items.isEmpty()) return;
        hideKeyboard();
        DialogUtils.showListBottomDialog(requireContext(), items.toArray(new DialogListItem[0]), (view, position, item) -> {
            final int key = item.getKey();
            try {
                if (key == R.string.sb_text_channel_input_camera) {
                    takeCamera();
                } else if (key == R.string.sb_text_channel_input_take_video) {
                    takeVideo();
                } else if (key == R.string.sb_text_channel_input_gallery) {
                    takePhoto();
                } else {
                    takeFile();
                }
            } catch (Exception e) {
                Logger.e(e);
                if (key == R.string.sb_text_channel_input_camera) {
                    toastError(R.string.sb_text_error_open_camera, getModule().getParams().shouldUseOverlayMode());
                } else if (key == R.string.sb_text_channel_input_take_video) {
                    toastError(R.string.sb_text_error_open_camera, getModule().getParams().shouldUseOverlayMode());
                } else if (key == R.string.sb_text_channel_input_gallery) {
                    toastError(R.string.sb_text_error_open_gallery, getModule().getParams().shouldUseOverlayMode());
                } else {
                    toastError(R.string.sb_text_error_open_file, getModule().getParams().shouldUseOverlayMode());
                }
            }
        }, getModule().getParams().shouldUseOverlayMode());
    }

    /**
     * Call taking camera application.
     *
     * since 2.0.1
     */
    public void takeCamera() {
        SendbirdChat.setAutoBackgroundDetection(false);
        String[] permissions = PermissionUtils.CAMERA_PERMISSION;
        requestPermission(permissions, () -> {
            if (getContext() == null) return;
            this.mediaUri = FileUtils.createImageFileUri(getContext());
            if (mediaUri == null) return;
            Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
            if (IntentUtils.hasIntent(getContext(), intent)) {
                takeCameraLauncher.launch(intent);
            }
        });
    }

    /**
     * Call taking camera application for video capture.
     *
     * since 3.2.1
     */
    public void takeVideo() {
        SendbirdChat.setAutoBackgroundDetection(false);
        requestPermission(PermissionUtils.CAMERA_PERMISSION, () -> {
            if (getContext() == null) return;
            this.mediaUri = FileUtils.createVideoFileUri(getContext());
            if (mediaUri == null) return;

            Intent intent = IntentUtils.getVideoCaptureIntent(getContext(), mediaUri);
            if (IntentUtils.hasIntent(getContext(), intent)) {
                takeVideoLauncher.launch(intent);
            }
        });
    }

    /**
     * Call taking gallery application.
     *
     * since 2.0.1
     */
    public void takePhoto() {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType =
            openChannelConfig.getInput().getGallery().getPickVisualMediaType();
        if (mediaType != null) {
            SendbirdChat.setAutoBackgroundDetection(false);
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(mediaType)
                .build());
        }
    }

    private void onPickMediaResult(@Nullable Uri uri) {
        SendbirdChat.setAutoBackgroundDetection(true);
        if (uri != null && isFragmentAlive()) {
            sendFileMessage(uri);
        }
    }

    /**
     * Call taking file chooser application.
     *
     * since 2.0.1
     */
    public void takeFile() {
        SendbirdChat.setAutoBackgroundDetection(false);
        final String[] permissions = PermissionUtils.GET_CONTENT_PERMISSION;
        if (permissions.length > 0) {
            requestPermission(permissions, () -> {
                Intent intent = IntentUtils.getFileChooserIntent();
                getContentLauncher.launch(intent);
            });
        } else {
            Intent intent = IntentUtils.getFileChooserIntent();
            getContentLauncher.launch(intent);
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    protected boolean shouldShowLoadingDialog() {
        return getModule().shouldShowLoadingDialog();
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 1.2.5
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageCreateParams}.
     */
    protected void onBeforeSendUserMessage(@NonNull UserMessageCreateParams params) {
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageCreateParams}.
     */
    protected void onBeforeSendFileMessage(@NonNull FileMessageCreateParams params) {
    }

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageUpdateParams}.
     */
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams params) {
    }

    /**
     * Sends a user message.
     *
     * @param params Params of user message. Refer to {@link UserMessageCreateParams}.
     */
    protected void sendUserMessage(@NonNull UserMessageCreateParams params) {
        CustomParamsHandler customParamsHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customParamsHandler != null) {
            customParamsHandler.onBeforeSendUserMessage(params);
        }
        onBeforeSendUserMessage(params);
        getViewModel().sendUserMessage(params, filteredMessage -> toastError(R.string.sb_text_error_message_filtered, getModule().getParams().shouldUseOverlayMode()));
        final OpenChannelMessageInputComponent inputComponent = getModule().getMessageInputComponent();
        inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
    }

    /**
     * Sends a file with given file information.
     *
     * @param uri A file Uri
     */
    protected void sendFileMessage(@NonNull Uri uri) {
        if (getContext() != null) {
            FileInfo.fromUri(getContext(), uri, SendbirdUIKit.shouldUseImageCompression(), new OnResultHandler<FileInfo>() {
                @Override
                public void onResult(@NonNull FileInfo info) {
                    FileMessageCreateParams params = info.toFileParams();
                    CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
                    if (customHandler != null) {
                        customHandler.onBeforeSendFileMessage(params);
                    }
                    onBeforeSendFileMessage(params);
                    getViewModel().sendFileMessage(params, info, filteredMessage -> toastError(R.string.sb_text_error_send_message, getModule().getParams().shouldUseOverlayMode()));
                    final OpenChannelMessageInputComponent inputComponent = getModule().getMessageInputComponent();
                    inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
                }

                @Override
                public void onError(@Nullable SendbirdException e) {
                    Logger.w(e);
                    toastError(R.string.sb_text_error_send_message, getModule().getParams().shouldUseOverlayMode());
                }
            });
        }
    }

    /**
     * Updates a <code>UserMessage</code> that was previously sent in the channel.
     *
     * @param messageId The ID of the message. This must be a message that exists in the channel's history,
     *                  or an error will be returned.
     * @param params    Params of a message. Refer to {@link UserMessageUpdateParams}.
     */
    protected void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params) {
        CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeUpdateUserMessage(params);
        }
        onBeforeUpdateUserMessage(params);
        getViewModel().updateUserMessage(messageId, params, e -> toastError(R.string.sb_text_error_update_user_message, getModule().getParams().shouldUseOverlayMode()));
    }

    /**
     * Delete a message
     *
     * @param message Message to delete.
     */
    protected void deleteMessage(@NonNull BaseMessage message) {
        getViewModel().deleteMessage(message, e -> toastError(R.string.sb_text_error_delete_message, getModule().getParams().shouldUseOverlayMode()));
    }

    /**
     * Resends a failed message.
     *
     * @param message Failed message to resend.
     */
    protected void resendMessage(@NonNull BaseMessage message) {
        if (message.isResendable()) {
            getViewModel().resendMessage(message, e -> toastError(R.string.sb_text_error_resend_message, getModule().getParams().shouldUseOverlayMode()));
        } else {
            toastError(R.string.sb_text_error_not_possible_resend_message, getModule().getParams().shouldUseOverlayMode());
        }
    }

    private void showFile(@NonNull File file, @NonNull String mimeType) {
        TaskQueue.addTask(new JobResultTask<Intent>() {
            @Override
            @Nullable
            public Intent call() {
                if (!isFragmentAlive()) return null;
                Uri uri = FileUtils.fileToUri(requireContext(), file);
                return IntentUtils.getFileViewerIntent(uri, mimeType);
            }

            @Override
            public void onResultForUiThread(@Nullable Intent intent, @Nullable SendbirdException e) {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_open_file, getModule().getParams().shouldUseOverlayMode());
                    return;
                }
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageClickListener != null) {
            messageClickListener.onItemClick(view, position, message);
            return;
        }

        if (message.getSendingStatus() == SendingStatus.SUCCEEDED) {
            MessageType type = MessageViewHolderFactory.getMessageType(message);
            switch (type) {
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                    startActivity(PhotoViewActivity.newIntent(requireContext(), ChannelType.OPEN, (FileMessage) message));
                    break;
                case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                case VIEW_TYPE_FILE_MESSAGE_ME:
                case VIEW_TYPE_FILE_MESSAGE_OTHER:
                    final FileMessage fileMessage = (FileMessage) message;
                    FileDownloader.downloadFile(requireContext(), fileMessage, new OnResultHandler<File>() {
                        @Override
                        public void onResult(@NonNull File file) {
                            showFile(file, fileMessage.getType());
                        }

                        @Override
                        public void onError(@Nullable SendbirdException e) {
                            toastError(R.string.sb_text_error_download_file, getModule().getParams().shouldUseOverlayMode());
                        }
                    });
                    break;
                default:
            }
        } else {
            if (MessageUtils.isMine(message) && (message instanceof UserMessage || message instanceof FileMessage)) {
                resendMessage(message);
            }
        }
    }

    /**
     * Called when the profile view of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileClickListener != null) {
            messageProfileClickListener.onItemClick(view, position, message);
            return;
        }

        if (message.getSender() != null) {
            showUserProfile(message.getSender());
        }
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageLongClickListener != null) {
            messageLongClickListener.onItemLongClick(view, position, message);
            return;
        }
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return;
        showMessageContextMenu(view, message, makeMessageContextMenu(message));
    }

    @NonNull
    private OnItemClickListener<DialogListItem> createMessageActionListener(@NonNull BaseMessage message) {
        return (view, position, item) -> onMessageContextMenuItemClicked(message, view, position, item);
    }

    private void download(@NonNull FileMessage fileMessage) {
        toastSuccess(R.string.sb_text_toast_success_start_download_file, getModule().getParams().shouldUseOverlayMode());
        TaskQueue.addTask(new JobResultTask<Boolean>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            @NonNull
            public Boolean call() throws Exception {
                if (getContext() == null) return false;
                FileDownloader.getInstance().saveFile(getContext(), fileMessage.getUrl(),
                    fileMessage.getType(), fileMessage.getName());
                return true;
            }

            @Override
            public void onResultForUiThread(@Nullable Boolean result, @Nullable SendbirdException e) {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_download_file, getModule().getParams().shouldUseOverlayMode());
                    return;
                }
                toastSuccess(R.string.sb_text_toast_success_download_file, getModule().getParams().shouldUseOverlayMode());
            }
        });
    }

    private void copyTextToClipboard(@NonNull String text) {
        if (!isFragmentAlive()) return;
        ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(StringSet.LABEL_COPY_TEXT, text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
            toastSuccess(R.string.sb_text_toast_success_copy, getModule().getParams().shouldUseOverlayMode());
        } else {
            toastError(R.string.sb_text_error_copy_message, getModule().getParams().shouldUseOverlayMode());
        }
    }

    private void showWarningDialog(@NonNull BaseMessage message) {
        if (getContext() == null) return;
        DialogUtils.showWarningDialog(
            requireContext(),
            getString(R.string.sb_text_dialog_delete_message),
            getString(R.string.sb_text_button_delete),
            delete -> {
                Logger.dev("delete");
                deleteMessage(message);
            },
            getString(R.string.sb_text_button_cancel),
            cancel -> Logger.dev("cancel"),
            getModule().getParams().shouldUseOverlayMode());
    }

    /**
     * Download {@link FileMessage} into external storage.
     * It needs to have a permission.
     * If current application needs permission, the request of permission will call automatically.
     * After permission is granted, the download will be also called automatically.
     *
     * @param message A file message to download contents.
     * since 3.0.0
     */
    protected void saveFileMessage(@NonNull FileMessage message) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            download(message);
        } else {
            requestPermission(PermissionUtils.GET_CONTENT_PERMISSION, () -> download(message));
        }
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * since 3.0.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    /**
     * This is a Builder that is able to create the OpenChannel fragment.
     * The builder provides options how the channel is showing and working. Also you can set the event handler what you want to override.
     *
     * since 2.0.0
     */
    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private OpenChannelMessageListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> messageClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageLongClickListener;
        @Nullable
        private View.OnClickListener inputLeftButtonClickListener;
        @Nullable
        private MessageListParams params;
        @Nullable
        private OnItemClickListener<BaseMessage> messageProfileClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
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
        @Deprecated
        private View.OnClickListener scrollBottomButtonClickListener;
        @Nullable
        private OnConsumableClickListener scrollFirstButtonClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
        @Nullable
        private OnItemEventListener<BaseMessage> messageInsertedListener;
        @Nullable
        private OpenChannelFragment customFragment;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom fragment. It must inherit {@link OpenChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OpenChannelFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.1
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets the description of the header.
         *
         * @param description text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderDescription(@NonNull String description) {
            bundle.putString(StringSet.KEY_HEADER_DESCRIPTION, description);
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         * since 2.1.0
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
         * since 2.1.0
         */
        @NonNull
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets whether showing the right button of the input always.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.2
         */
        @NonNull
        public Builder showInputRightButtonAlways() {
            bundle.putBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS, true);
            return this;
        }

        /**
         * Sets whether the left button of the input is used.
         *
         * @param useInputLeftButton <code>true</code> if the left button of the input is used,
         *                           <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.1
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
         * since 2.1.0
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
         * since 2.1.0
         */
        @NonNull
        public Builder setInputRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the hint of the input text.
         *
         * @param hint text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setInputHint(@NonNull String hint) {
            bundle.putString(StringSet.KEY_INPUT_HINT, hint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param headerLeftButtonClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener headerLeftButtonClickListener) {
            this.headerLeftButtonClickListener = headerLeftButtonClickListener;
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param headerRightButtonClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener headerRightButtonClickListener) {
            this.headerRightButtonClickListener = headerRightButtonClickListener;
            return this;
        }

        /**
         * Sets the message list adapter.
         *
         * @param adapter the adapter for the message list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setOpenChannelMessageListAdapter(@NonNull OpenChannelMessageListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param messageClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageClickListener(@NonNull OnItemClickListener<BaseMessage> messageClickListener) {
            this.messageClickListener = messageClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param messageLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> messageLongClickListener) {
            this.messageLongClickListener = messageLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the input.
         *
         * @param inputLeftButtonClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setOnInputLeftButtonClickListener(@NonNull View.OnClickListener inputLeftButtonClickListener) {
            this.inputLeftButtonClickListener = inputLeftButtonClickListener;
            return this;
        }

        /**
         * Sets the message list params for this channel.
         * The reverse and the nextResultSize properties in the MessageListParams are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param params The MessageListParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setMessageListParams(@NonNull MessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseMessageGroupUI(boolean useMessageGroupUI) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, useMessageGroupUI);
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param messageProfileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnProfileClickListener(@NonNull OnItemClickListener<BaseMessage> messageProfileClickListener) {
            this.messageProfileClickListener = messageProfileClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * The channel displays as a overlay mode.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder useOverlayMode() {
            bundle.putBoolean(StringSet.KEY_USE_OVERLAY_MODE, true);
            return this;
        }

        /**
         * The message input displays as a dialog type. (Refer to {@link KeyboardDisplayType})
         *
         * @return This Builder object to allow for chaining of calls to set methods.
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
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.2
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId) {
            return setEmptyIcon(resId, null);
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
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
         * since 2.0.2
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
         * since 3.0.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the input text
         *
         * @param inputText the message text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
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
         * since 2.1.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@NonNull OnInputTextChangedListener inputTextChangedListener) {
            this.inputTextChangedListener = inputTextChangedListener;
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is edited.
         *
         * @param editModeTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setOnEditModeTextChangedListener(@NonNull OnInputTextChangedListener editModeTextChangedListener) {
            this.editModeTextChangedListener = editModeTextChangedListener;
            return this;
        }

        /**
         * Sets whether the profile image of the header is used.
         *
         * @param useHeaderProfileImage <code>true</code> if the profile image of the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.2.4
         */
        @NonNull
        public Builder setUseHeaderProfileImage(boolean useHeaderProfileImage) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE, useHeaderProfileImage);
            return this;
        }

        /**
         * Register a callback to be invoked when the right button of the input is clicked.
         *
         * @param inputRightButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
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
         * since 3.0.0
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
         * since 3.0.0
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
         * since 3.0.0
         */
        @NonNull
        public Builder setOnInputModeChangedListener(@Nullable OnInputModeChangedListener inputModeChangedListener) {
            this.inputModeChangedListener = inputModeChangedListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the button to scroll to the bottom is clicked.
         *
         * @param scrollBottomButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         * @deprecated 3.2.2
         * This method is no longer acceptable to invoke event.
         * <p> Use {@link #setOnScrollFirstButtonClickListener(OnConsumableClickListener)} instead.
         */
        @NonNull
        @Deprecated
        public Builder setOnScrollBottomButtonClickListener(@Nullable View.OnClickListener scrollBottomButtonClickListener) {
            this.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the button to scroll to the first position is clicked.
         *
         * @param scrollFirstButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.2
         */
        @NonNull
        public Builder setOnScrollFirstButtonClickListener(@Nullable OnConsumableClickListener scrollFirstButtonClickListener) {
            this.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the profile view of the message is long-clicked.
         *
         * @param messageProfileLongClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageProfileLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
            this.messageProfileLongClickListener = messageProfileLongClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the message is inserted.
         *
         * @param messageInsertedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageInsertedListener(@Nullable OnItemEventListener<BaseMessage> messageInsertedListener) {
            this.messageInsertedListener = messageInsertedListener;
            return this;
        }

        /**
         * Sets the UI configuration of edited text mark.
         *
         * @param configSentFromMe       the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers   the UI configuration of edited text mark in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
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
         * since 3.1.1
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
         * since 3.1.1
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
         * @param configSentFromMe       the UI configuration of the sender nickname text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the sender nickname text that was sent from others.
         * @param configSentFromOperator the UI configuration of the sender nickname text that was sent from operator.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setNicknameTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers, @Nullable TextUIConfig configSentFromOperator) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            if (configSentFromOperator != null)
                bundle.putParcelable(StringSet.KEY_OPERATOR_TEXT_UI_CONFIG, configSentFromOperator);
            return this;
        }

        /**
         * Sets the UI configuration of message input text.
         *
         * @param textUIConfig the UI configuration of the message input text.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.1
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
         * since 3.1.1
         */
        @NonNull
        public Builder setMessageBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of ogtag message background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the ogtag message background drawable that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the ogtag message background drawable that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
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
         * since 3.1.1
         */
        @NonNull
        public Builder setLinkedTextColor(@ColorRes int colorRes) {
            bundle.putInt(StringSet.KEY_LINKED_TEXT_COLOR, colorRes);
            return this;
        }

        /**
         * Sets open channel configuration for this fragment.
         * Use {@code UIKitConfig.openChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val fragment = OpenChannelFragment.Builder(CHANNEL_URL)
         *     .setOpenChannelConfig(
         *         UIKitConfig.openChannelConfig.clone().apply {
         *             this.enableOgTag = false
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param openChannelConfig The open channel config.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         */
        @NonNull
        public Builder setOpenChannelConfig(@NonNull OpenChannelConfig openChannelConfig) {
            this.bundle.putParcelable(StringSet.KEY_OPEN_CHANNEL_CONFIG, openChannelConfig);
            return this;
        }

        /**
         * Creates an {@link OpenChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public OpenChannelFragment build() {
            final OpenChannelFragment fragment = customFragment != null ? customFragment : new OpenChannelFragment();
            fragment.setArguments(bundle);
            fragment.adapter = adapter;
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.messageClickListener = messageClickListener;
            fragment.messageLongClickListener = messageLongClickListener;
            fragment.inputLeftButtonClickListener = inputLeftButtonClickListener;
            fragment.params = params;
            fragment.messageProfileClickListener = messageProfileClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.editModeTextChangedListener = editModeTextChangedListener;
            fragment.inputRightButtonClickListener = inputRightButtonClickListener;
            fragment.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
            fragment.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
            fragment.inputModeChangedListener = inputModeChangedListener;
            fragment.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
            fragment.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
            fragment.messageProfileLongClickListener = messageProfileLongClickListener;
            fragment.messageInsertedListener = messageInsertedListener;
            return fragment;
        }
    }
}
