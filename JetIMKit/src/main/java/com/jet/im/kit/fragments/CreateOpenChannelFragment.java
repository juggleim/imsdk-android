package com.jet.im.kit.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.OpenChannelActivity;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.CustomParamsHandler;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.internal.ui.components.ChannelProfileInputView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.CreateOpenChannelModule;
import com.jet.im.kit.modules.components.ChannelProfileInputComponent;
import com.jet.im.kit.modules.components.StateHeaderComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.IntentUtils;
import com.jet.im.kit.utils.PermissionUtils;
import com.jet.im.kit.utils.SoftInputUtils;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.vm.CreateOpenChannelViewModel;

import java.io.File;
import java.util.Collections;

/**
 * Fragment displaying the user list to create a open channel.
 *
 * since 3.2.0
 */
public class CreateOpenChannelFragment extends BaseModuleFragment<CreateOpenChannelModule, CreateOpenChannelViewModel> {
    @Nullable
    private Uri mediaUri;
    @Nullable
    private File mediaFile;
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private View.OnClickListener onMediaSelectButtonClickListener;
    @Nullable
    private View.OnClickListener clearButtonClickListener;

    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null) return;
        this.mediaUri = intent.getData();
        if (mediaUri != null && isFragmentAlive()) {
            mediaFile = FileUtils.uriToFile(requireContext(), mediaUri);
            updateChannelCover(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        final Uri mediaUri = this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            mediaFile = FileUtils.uriToFile(requireContext(), mediaUri);
            updateChannelCover(mediaUri);
        }
    });

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);
    }

    @NonNull
    @Override
    protected CreateOpenChannelModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getCreateOpenChannel().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull CreateOpenChannelModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected CreateOpenChannelViewModel onCreateViewModel() {
        return ViewModelProviders.getCreateOpenChannel().provide(this);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull CreateOpenChannelModule module, @NonNull CreateOpenChannelViewModel viewModel) {
        Logger.d(">> CreateOpenChannelFragment::onBeforeReady status=%s", status);

        onBindHeaderComponentComponent(module.getHeaderComponent(), viewModel);
        onBindChannelProfileInputComponent(module.getChannelProfileInputComponent(), viewModel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull CreateOpenChannelModule module, @NonNull CreateOpenChannelViewModel viewModel) {
        Logger.d(">> CreateOpenChannelFragment::onReady status=%s", status);
        if (status == ReadyStatus.ERROR) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_retry_request);
                shouldActivityFinish();
            }
        }
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, CreateOpenChannelModule, CreateOpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.2.0
     */
    protected void onBindHeaderComponentComponent(@NonNull StateHeaderComponent headerComponent, @NonNull CreateOpenChannelViewModel viewModel) {
        Logger.d(">> CreateOpenChannelFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            OpenChannelCreateParams params = new OpenChannelCreateParams();
            final View rootView = getModule().getChannelProfileInputComponent().getRootView();
            if (rootView instanceof ChannelProfileInputView) {
                CharSequence text = ((ChannelProfileInputView) rootView).getText();
                if (TextUtils.isNotEmpty(text)) {
                    params.setName(text.toString().trim());
                }
            }
            if (mediaFile != null) {
                params.setCoverImage(mediaFile);
            }
            params.setOperators(Collections.singletonList(SendbirdChat.getCurrentUser()));
            createOpenChannel(params);
        });
    }

    /**
     * Called to bind events to the ChannelProfileInputComponent. This is called from {@link #onBeforeReady(ReadyStatus, CreateOpenChannelModule, CreateOpenChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param channelProfileInputComponent The component to which the event will be bound
     * @param viewModel                    A view model that provides the data needed for the fragment
     * since 3.2.0
     */
    protected void onBindChannelProfileInputComponent(@NonNull ChannelProfileInputComponent channelProfileInputComponent, @NonNull CreateOpenChannelViewModel viewModel) {
        Logger.d(">> CreateOpenChannelFragment::onBindChannelProfileInputComponent()");
        final StateHeaderComponent headerComponent = getModule().getHeaderComponent();
        headerComponent.notifyRightButtonEnableStateChanged(false);
        channelProfileInputComponent.setOnInputTextChangedListener(inputTextChangedListener != null ? inputTextChangedListener : (s, start, before, count) -> headerComponent.notifyRightButtonEnableStateChanged(TextUtils.isNotEmpty(s.toString().trim())));
        channelProfileInputComponent.setOnMediaSelectButtonClickListener(onMediaSelectButtonClickListener != null ? onMediaSelectButtonClickListener : view -> showMediaSelectDialog());
        channelProfileInputComponent.setOnClearButtonClickListener(clearButtonClickListener);
    }

    /**
     * It will be called before creating open channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link OpenChannelCreateParams}.
     * since 3.2.0
     */
    protected void onBeforeCreateOpenChannel(@NonNull OpenChannelCreateParams params) {
    }

    /**
     * Creates <code>OpenChannel</code> with OpenChannelCreateParams.
     *
     * @param params Params of channel. Refer to {@link OpenChannelCreateParams}.
     * since 3.2.0
     */
    protected void createOpenChannel(@NonNull OpenChannelCreateParams params) {
        Logger.dev(">> CreateOpenChannelFragment::createOpenChannel()");
        CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeCreateOpenChannel(params);
        }
        onBeforeCreateOpenChannel(params);

        Logger.dev("++ createOpenChannel params : " + params);
        getViewModel().createOpenChannel(params, (channel, e) -> {
            if (e != null) {
                toastError(R.string.sb_text_error_create_channel);
                Logger.e(e);
            } else {
                if (channel != null) {
                    onNewChannelCreated(channel);
                }
            }
        });
    }

    /**
     * It will be called when the new channel has been created.
     *
     * @param channel the new channel
     * since 3.2.0
     */
    protected void onNewChannelCreated(@NonNull OpenChannel channel) {
        if (isFragmentAlive() && getActivity() != null) {
            startActivity(OpenChannelActivity.newIntent(requireContext(), OpenChannelActivity.class, channel.getUrl()));
            getActivity().setResult(RESULT_OK);
            shouldActivityFinish();
        }
    }

    private void showMediaSelectDialog() {
        if (getContext() == null) return;

        DialogListItem delete = new DialogListItem(R.string.text_remove_photo, 0, true);
        DialogListItem camera = new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_camera);
        DialogListItem gallery = new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_gallery);
        DialogListItem[] items;
        if (mediaFile == null) {
            items = new DialogListItem[]{camera, gallery};
        } else {
            items = new DialogListItem[]{delete, camera, gallery};
        }

        hideKeyboard();
        DialogUtils.showListBottomDialog(requireContext(), items, (view, p, item) -> {
            try {
                final int key = item.getKey();
                SendbirdChat.setAutoBackgroundDetection(false);
                if (key == R.string.sb_text_channel_settings_change_channel_image_camera) {
                    takeCamera();
                } else if (key == R.string.sb_text_channel_settings_change_channel_image_gallery) {
                    takePhoto();
                } else {
                    removePhoto();
                }
            } catch (Exception e) {
                Logger.e(e);
                toastError(R.string.sb_text_error_open_camera);
            }
        });
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    private void takeCamera() {
        SendbirdChat.setAutoBackgroundDetection(false);
        requestPermission(PermissionUtils.CAMERA_PERMISSION, () -> {
            if (getContext() == null) return;
            this.mediaUri = FileUtils.createImageFileUri(getContext());
            if (mediaUri == null) return;
            Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
            if (IntentUtils.hasIntent(getContext(), intent)) {
                takeCameraLauncher.launch(intent);
            }
        });
    }

    private void takePhoto() {
        SendbirdChat.setAutoBackgroundDetection(false);
        Logger.d("++ build sdk int=%s", Build.VERSION.SDK_INT);
        final String[] permissions = PermissionUtils.GET_CONTENT_PERMISSION;
        if (permissions.length > 0) {
            requestPermission(permissions, () -> {
                Intent intent = IntentUtils.getImageGalleryIntent();
                getContentLauncher.launch(intent);
            });
        } else {
            Intent intent = IntentUtils.getImageGalleryIntent();
            getContentLauncher.launch(intent);
        }
    }

    private void removePhoto() {
        this.mediaFile = null;
        this.mediaUri = null;
        getModule().getChannelProfileInputComponent().notifyCoverImageChanged(null);
    }

    private void updateChannelCover(@NonNull Uri mediaUri) {
        getModule().getChannelProfileInputComponent().notifyCoverImageChanged(mediaUri);
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private CreateOpenChannelFragment customFragment;
        @Nullable
        private OnInputTextChangedListener inputTextChangedListener;
        @Nullable
        private View.OnClickListener onMediaSelectButtonClickListener;
        @Nullable
        private View.OnClickListener clearButtonClickListener;

        /**
         * Constructor
         *
         * since 3.2.0
         */
        public Builder() {
            this(0);
        }

        /**
         * Constructor
         *
         * @param themeMode {@link SendbirdUIKit.ThemeMode}
         * since 3.2.0
         */
        public Builder(@NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         * since 3.2.0
         */
        public Builder(@StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
        }

        /**
         * Sets the custom fragment. It must inherit {@link CreateOpenChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends CreateOpenChannelFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
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
         * since 3.2.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the create button text of the header.
         *
         * @param rightButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setRightButtonText(@NonNull String rightButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, rightButtonText);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
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
         * since 3.2.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Register a callback to be invoked when the input text is changed.
         *
         * @param textChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@NonNull OnInputTextChangedListener textChangedListener) {
            this.inputTextChangedListener = textChangedListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the clear button related to the input is clicked.
         *
         * @param clearButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnClearButtonClickListener(@Nullable View.OnClickListener clearButtonClickListener) {
            this.clearButtonClickListener = clearButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the media selector is clicked.
         *
         * @param onMediaSelectButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnMediaSelectButtonClickListener(@Nullable View.OnClickListener onMediaSelectButtonClickListener) {
            this.onMediaSelectButtonClickListener = onMediaSelectButtonClickListener;
            return this;
        }

        /**
         * Creates an {@link CreateOpenChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link CreateOpenChannelFragment} applied to the {@link Bundle}.
         * since 3.2.0
         */
        @NonNull
        public CreateOpenChannelFragment build() {
            final CreateOpenChannelFragment fragment = customFragment != null ? customFragment : new CreateOpenChannelFragment();
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.clearButtonClickListener = clearButtonClickListener;
            fragment.onMediaSelectButtonClickListener = onMediaSelectButtonClickListener;
            fragment.setArguments(bundle);
            return fragment;
        }
    }
}
