package com.jet.im.kit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.OpenChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.OpenChannelBannedUserListActivity;
import com.jet.im.kit.activities.OpenChannelMutedParticipantListActivity;
import com.jet.im.kit.activities.OpenChannelOperatorListActivity;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.OnMenuItemClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.OpenChannelModerationModule;
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.OpenChannelModerationListComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.vm.OpenChannelModerationViewModel;

/**
 * Fragment displaying the menu list to control the open channel.
 * It will be displayed if the participant is an operator.
 *
 * since 3.1.0
 */
public class OpenChannelModerationFragment extends BaseModuleFragment<OpenChannelModerationModule, OpenChannelModerationViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnMenuItemClickListener<OpenChannelModerationListComponent.ModerationMenu, BaseChannel> menuItemClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    @NonNull
    @Override
    protected OpenChannelModerationModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getOpenChannelModeration().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull OpenChannelModerationModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected OpenChannelModerationViewModel onCreateViewModel() {
        return ViewModelProviders.getOpenChannelModeration().provide(this, getChannelUrl());
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelModerationModule module, @NonNull OpenChannelModerationViewModel viewModel) {
        Logger.d(">> OpenChannelModerationFragment::onBeforeReady status=%s", status);
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, viewModel.getChannel());
        onBindModerationListComponent(module.getModerationListComponent(), viewModel, viewModel.getChannel());
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull OpenChannelModerationModule module, @NonNull OpenChannelModerationViewModel viewModel) {
        Logger.d(">> OpenChannelModerationFragment::onReady status=%s", status);
        final OpenChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }

        viewModel.getChannelDeleted().observe(getViewLifecycleOwner(), url -> shouldActivityFinish());
        viewModel.getCurrentUserBanned().observe(getViewLifecycleOwner(), isBanned -> {
            if (isBanned) shouldActivityFinish();
        });
        viewModel.getCurrentUserRegisteredOperator().observe(getViewLifecycleOwner(), isOperator -> {
            if (!isOperator) shouldActivityFinish();
        });
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModerationModule, OpenChannelModerationViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param openChannel The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull OpenChannelModerationViewModel viewModel, @Nullable OpenChannel openChannel) {
        Logger.d(">> OpenChannelModerationFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener);
    }

    /**
     * Called to bind events to the ModerationListComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelModerationModule, OpenChannelModerationViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param moderationListComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param openChannel The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onBindModerationListComponent(@NonNull OpenChannelModerationListComponent moderationListComponent, @NonNull OpenChannelModerationViewModel viewModel, @Nullable OpenChannel openChannel) {
        Logger.d(">> OpenChannelModerationFragment::onBindModerationListComponent()");
        if (openChannel == null) return;
        moderationListComponent.setOnMenuItemClickListener((view, menu, data) -> {
            Logger.dev("++ %s item clicked", menu.name());
            if (menuItemClickListener != null) {
                return menuItemClickListener.onMenuItemClicked(view, menu, openChannel);
            }
            if (getContext() == null) return false;
            switch (menu) {
                case OPERATORS:
                    startActivity(OpenChannelOperatorListActivity.newIntent(getContext(), openChannel.getUrl()));
                    break;
                case MUTED_PARTICIPANTS:
                    startActivity(OpenChannelMutedParticipantListActivity.newIntent(getContext(), openChannel.getUrl()));
                    break;
                case BANNED_PARTICIPANTS:
                    startActivity(OpenChannelBannedUserListActivity.newIntent(getContext(), openChannel.getUrl()));
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.1.0
     */
    protected boolean shouldShowLoadingDialog() {
        if (getContext() != null) {
            return getModule().shouldShowLoadingDialog(requireContext());
        }
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 3.1.0
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * since 3.1.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnMenuItemClickListener<OpenChannelModerationListComponent.ModerationMenu, BaseChannel> menuItemClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private OpenChannelModerationFragment customFragment;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * since 3.1.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom fragment. It must inherit {@link OpenChannelModerationFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OpenChannelModerationFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * since 3.1.0
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
         * since 3.1.0
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
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
         * since 3.1.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setUseHeaderRightButton(boolean useHeaderRightButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useHeaderRightButton);
            return this;
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the menu item is clicked.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.0
         */
        @NonNull
        public Builder setOnMenuItemClickListener(@NonNull OnMenuItemClickListener<OpenChannelModerationListComponent.ModerationMenu, BaseChannel> listener) {
            this.menuItemClickListener = listener;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 3.1.0
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Creates an {@link OpenChannelModerationFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelModerationFragment} applied to the {@link Bundle}.
         * since 3.1.0
         */
        @NonNull
        public OpenChannelModerationFragment build() {
            final OpenChannelModerationFragment fragment = customFragment != null ? customFragment : new OpenChannelModerationFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.menuItemClickListener = menuItemClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            return fragment;
        }
    }
}
