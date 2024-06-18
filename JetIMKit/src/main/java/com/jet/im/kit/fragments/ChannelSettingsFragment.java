package com.jet.im.kit.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.params.GroupChannelUpdateParams;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.MessageSearchActivity;
import com.jet.im.kit.consts.DialogEditTextParams;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.CustomParamsHandler;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.OnEditTextResultListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.tasks.JobResultTask;
import com.jet.im.kit.internal.tasks.TaskQueue;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.configurations.ChannelSettingConfig;
import com.jet.im.kit.modules.ChannelSettingsModule;
import com.jet.im.kit.modules.components.ChannelSettingsHeaderComponent;
import com.jet.im.kit.modules.components.ChannelSettingsInfoComponent;
import com.jet.im.kit.modules.components.ChannelSettingsMenuComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.IntentUtils;
import com.jet.im.kit.utils.PermissionUtils;
import com.jet.im.kit.vm.ChannelSettingsViewModel;

import java.io.File;

/**
 * Fragment displaying the information of {@code GroupChannel}.
 */
public class ChannelSettingsFragment extends BaseModuleFragment<ChannelSettingsModule, ChannelSettingsViewModel> {

    @Nullable
    private Uri mediaUri;
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnItemClickListener<ChannelSettingsMenuComponent.Menu> menuItemClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null) return;
        final Uri mediaUri = intent.getData();
        if (mediaUri != null && isFragmentAlive()) {
            processPickedImage(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        final Uri mediaUri = ChannelSettingsFragment.this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            processPickedImage(mediaUri);
        }
    });

    @NonNull
    @Override
    protected ChannelSettingsModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getChannelSettings().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull ChannelSettingsModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected ChannelSettingsViewModel onCreateViewModel() {
        return ViewModelProviders.getChannelSettings().provide(this, getChannelUrl());
    }

    private void processPickedImage(@NonNull Uri uri) {
        TaskQueue.addTask(new JobResultTask<File>() {
            @Override
            @Nullable
            public File call() {
                if (!isFragmentAlive()) return null;
                return FileUtils.uriToFile(requireContext(), uri);
            }

            @Override
            public void onResultForUiThread(@Nullable File file, @Nullable SendbirdException e) {
                if (e != null) {
                    Logger.w(e);
                    return;
                }
                if (isFragmentAlive()) {
                    GroupChannelUpdateParams params = new GroupChannelUpdateParams();
                    params.setCoverImage(file);
                    toastSuccess(R.string.sb_text_toast_success_start_upload_file);
                    updateGroupChannel(params);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelSettingsModule module, @NonNull ChannelSettingsViewModel viewModel) {
        Logger.d(">> ChannelSettingsFragment::onBeforeReady status=%s", status);
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindSettingsInfoComponent(module.getChannelSettingsInfoComponent(), viewModel, channel);
        onBindSettingsMenuComponent(module.getChannelSettingsMenuComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ChannelSettingsModule module, @NonNull ChannelSettingsViewModel viewModel) {
        Logger.d(">> ChannelSettingsFragment::onReady status=%s", status);
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }
        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getChannelSettingsInfoComponent().notifyChannelChanged(channel);
        module.getChannelSettingsMenuComponent().notifyChannelChanged(channel);
        viewModel.shouldFinish().observe(getViewLifecycleOwner(), finished -> shouldActivityFinish());
    }

    /**
     * Called to bind events to the ChannelSettingsHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelSettingsModule, ChannelSettingsViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull ChannelSettingsHeaderComponent headerComponent, @NonNull ChannelSettingsViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelSettingsFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> showChannelInfoEditDialog());
    }

    /**
     * Called to bind events to the ChannelSettingsInfoComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelSettingsModule, ChannelSettingsViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param infoComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindSettingsInfoComponent(@NonNull ChannelSettingsInfoComponent infoComponent, @NonNull ChannelSettingsViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelSettingsFragment::onBindSettingsInfoComponent()");
        viewModel.getChannelUpdated().observe(getViewLifecycleOwner(), infoComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the ChannelSettingsMenuComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelSettingsModule, ChannelSettingsViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param menuComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindSettingsMenuComponent(@NonNull ChannelSettingsMenuComponent menuComponent, @NonNull ChannelSettingsViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelSettingsFragment::onBindSettingsMenuComponent()");

        menuComponent.setOnMenuClickListener(menuItemClickListener != null ? menuItemClickListener : (view, position, menu) -> {
            if (menu == ChannelSettingsMenuComponent.Menu.LEAVE_CHANNEL) {
                leaveChannel();
            } else {
                startMessageSearchActivity();
            }
        });
        viewModel.getChannelUpdated().observe(getViewLifecycleOwner(), menuComponent::notifyChannelChanged);
    }

    private void startMessageSearchActivity() {
        if (isFragmentAlive()) {
            startActivity(MessageSearchActivity.newIntent(requireContext(), getViewModel().getChannelUrl()));
        }
    }

    private void showChannelInfoEditDialog() {
        DialogListItem[] items = {
            new DialogListItem(R.string.sb_text_channel_settings_change_channel_name),
            new DialogListItem(R.string.sb_text_channel_settings_change_channel_image)
        };

        if (getContext() == null) return;
        DialogUtils.showListBottomDialog(requireContext(), items, (view, p, item) -> {
            final int key = item.getKey();
            if (key == R.string.sb_text_channel_settings_change_channel_name) {
                if (getContext() == null) return;

                Logger.dev("change channel name");
                OnEditTextResultListener listener = res -> {
                    GroupChannelUpdateParams params = new GroupChannelUpdateParams();
                    params.setName(res);
                    updateGroupChannel(params);
                };

                DialogEditTextParams params = new DialogEditTextParams(getString(R.string.sb_text_channel_settings_change_channel_name_hint));
                params.setEnableSingleLine(true);
                DialogUtils.showInputDialog(
                    requireContext(),
                    getString(R.string.sb_text_channel_settings_change_channel_name),
                    params, listener,
                    getString(R.string.sb_text_button_save), null,
                    getString(R.string.sb_text_button_cancel), null);
            } else if (key == R.string.sb_text_channel_settings_change_channel_image) {
                Logger.dev("change channel image");
                requestPermission(PermissionUtils.CAMERA_PERMISSION, this::showMediaSelectDialog);
            }
        });
    }

    /**
     * Leaves this channel.
     *
     * since 1.0.4
     */
    protected void leaveChannel() {
        shouldShowLoadingDialog();
        getViewModel().leaveChannel(e -> {
            shouldDismissLoadingDialog();
            if (e != null) toastError(R.string.sb_text_error_leave_channel);
        });
    }

    /**
     * It will be called before updating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelUpdateParams}.
     * since 1.0.4
     */
    protected void onBeforeUpdateGroupChannel(@NonNull GroupChannelUpdateParams params) {
    }

    /**
     * Update this channel with GroupChannelParams.
     *
     * @param params Params of channel. Refer to {@link GroupChannelUpdateParams}.
     * since 1.0.4
     */
    protected void updateGroupChannel(@NonNull GroupChannelUpdateParams params) {
        CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeUpdateGroupChannel(params);
        }
        onBeforeUpdateGroupChannel(params);
        getViewModel().updateChannel(params, e -> {
            if (e != null) {
                Logger.e(e);
                toastError(R.string.sb_text_error_update_channel);
            }
        });
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    public boolean shouldShowLoadingDialog() {
        if (!isFragmentAlive()) return false;
        return getModule().shouldShowLoadingDialog(requireContext());
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 1.2.5
     */
    public void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
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

    private void showMediaSelectDialog() {
        if (getContext() == null) return;

        DialogListItem[] items = {
            new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_camera),
            new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_gallery)};

        DialogUtils.showListDialog(getContext(), getString(R.string.sb_text_channel_settings_change_channel_image),
            items, (v, p, item) -> {
                try {
                    final int key = item.getKey();
                    SendbirdChat.setAutoBackgroundDetection(false);
                    if (key == R.string.sb_text_channel_settings_change_channel_image_camera) {
                        takeCamera();
                    } else if (key == R.string.sb_text_channel_settings_change_channel_image_gallery) {
                        takePhoto();
                    }
                } catch (Exception e) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_open_camera);
                }
            });
    }

    private void takeCamera() {
        if (!isFragmentAlive()) return;
        mediaUri = FileUtils.createImageFileUri(requireContext());
        if (mediaUri == null) return;
        Intent intent = IntentUtils.getCameraIntent(requireActivity(), mediaUri);
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent);
        }
    }

    private void takePhoto() {
        Intent intent = IntentUtils.getImageGalleryIntent();
        getContentLauncher.launch(intent);
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnItemClickListener<ChannelSettingsMenuComponent.Menu> menuItemClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private ChannelSettingsFragment customFragment;

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
         * Sets the custom fragment. It must inherit {@link ChannelSettingsFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends ChannelSettingsFragment> Builder setCustomFragment(T fragment) {
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
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
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
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.3
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
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the text on the right button of the header.
         *
         * @param rightButtonText The String to be displayed on the right button
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setRightButtonText(@Nullable String rightButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, rightButtonText);
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the channel setting menu click listener.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMenuClickListener(@NonNull OnItemClickListener<ChannelSettingsMenuComponent.Menu> listener) {
            this.menuItemClickListener = listener;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 1.2.5
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the channel setting configuration for this fragment.
         * Use {@code UIKitConfig.groupChannelSettingConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val fragment = ChannelSettingsFragment.Builder()
         *     .setChannelSettingConfig(
         *         UIKitConfig.groupChannelSettingConfig.clone().apply {
         *             this.enableMessageSearch = true
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param channelSettingConfig The channel setting config.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         */
        @NonNull
        public Builder setChannelSettingConfig(@NonNull ChannelSettingConfig channelSettingConfig) {
            this.bundle.putParcelable(StringSet.KEY_CHANNEL_SETTING_CONFIG, channelSettingConfig);
            return this;
        }

        /**
         * Creates an {@link ChannelSettingsFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelSettingsFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public ChannelSettingsFragment build() {
            final ChannelSettingsFragment fragment = customFragment != null ? customFragment : new ChannelSettingsFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.menuItemClickListener = menuItemClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            return fragment;
        }
    }
}
