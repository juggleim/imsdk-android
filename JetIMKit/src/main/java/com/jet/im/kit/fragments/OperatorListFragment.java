package com.jet.im.kit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.RegisterOperatorActivity;
import com.jet.im.kit.activities.adapter.OperatorListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.modules.OperatorListModule;
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.OperatorListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.vm.OperatorListViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * Fragment displaying the operators of the channel.
 *
 * since 1.2.0
 */
public class OperatorListFragment extends BaseModuleFragment<OperatorListModule, OperatorListViewModel> {

    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OperatorListAdapter adapter;
    @Nullable
    private OnItemClickListener<User> itemClickListener;
    @Nullable
    private OnItemLongClickListener<User> itemLongClickListener;
    @Nullable
    private OnItemClickListener<User> actionItemClickListener;
    @Nullable
    private OnItemClickListener<User> profileClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    @NonNull
    @Override
    protected OperatorListModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getOperatorList().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull OperatorListModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected OperatorListViewModel onCreateViewModel() {
        return ViewModelProviders.getOperatorList().provide(this, getChannelUrl(), null, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OperatorListModule module, @NonNull OperatorListViewModel viewModel) {
        Logger.d(">> OperatorListFragment::onBeforeReady()");
        module.getOperatorListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getOperatorListComponent().setAdapter(adapter);
        }
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindOperatorListComponent(module.getOperatorListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull OperatorListModule module, @NonNull OperatorListViewModel viewModel) {
        Logger.d(">> OperatorListFragment::onReady(ReadyStatus=%s)", status);
        final GroupChannel channel = viewModel.getChannel();
        if (status != ReadyStatus.READY || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.getOperatorUnregistered().observe(getViewLifecycleOwner(), isDismissed -> {
            if (isDismissed) shouldActivityFinish();
        });
        viewModel.getChannelDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) shouldActivityFinish();
        });

        if (channel.getMyRole() != Role.OPERATOR) shouldActivityFinish();
        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, OperatorListModule, OperatorListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull OperatorListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> OperatorListFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            if (isFragmentAlive() && getContext() != null && channel != null) {
                startActivity(RegisterOperatorActivity.newIntent(getContext(), channel.getUrl()));
            }
        });
    }

    /**
     * Called to bind events to the OperatorListComponent. This is called from {@link #onBeforeReady(ReadyStatus, OperatorListModule, OperatorListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindOperatorListComponent(@NonNull OperatorListComponent listComponent, @NonNull OperatorListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> OperatorListFragment::onBindInviteUserListComponent()");

        listComponent.setOnItemClickListener(itemClickListener);
        listComponent.setOnItemLongClickListener(itemLongClickListener);
        listComponent.setOnActionItemClickListener(actionItemClickListener != null ? actionItemClickListener : this::onActionItemClicked);
        listComponent.setOnProfileClickListener(profileClickListener != null ? profileClickListener : this::onProfileClicked);

        viewModel.getUserList().observe(getViewLifecycleOwner(), users -> {
            Logger.dev("++ observing result members size : %s", users.size());
            if (channel != null) {
                listComponent.notifyDataSetChanged(users, channel.getMyRole());
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, OperatorListModule, OperatorListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull OperatorListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> OperatorListFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });

        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    /**
     * Called when the action has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The member data that was clicked.
     */
    protected void onActionItemClicked(@NonNull View view, int position, @NonNull User user) {
        if (getContext() == null) return;
        Logger.d(">> OperatorListFragment::onActionItemClicked()");
        DialogListItem[] items;
        DialogListItem removeOperator = new DialogListItem(R.string.sb_text_unregister_operator);
        items = new DialogListItem[]{removeOperator};
        DialogUtils.showListDialog(getContext(), user.getNickname(),
            items,
            (v, p, key) -> {
                shouldShowLoadingDialog();
                getViewModel().removeOperator(user.getUserId(), e -> {
                    shouldDismissLoadingDialog();
                    if (e != null) {
                        toastError(R.string.sb_text_error_unregister_operator);
                    }
                });
            }
        );
    }

    /**
     * Called when the user profile has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The member data that was clicked.
     * since 1.2.2
     */
    protected void onProfileClicked(@NonNull View view, int position, @NonNull User user) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, UIKitConfig.getCommon().getEnableUsingDefaultUserProfile());
        if (getContext() == null || SendbirdUIKit.getAdapter() == null || !useUserProfile) return;
        boolean useChannelCreateButton = !user.getUserId().equals(SendbirdUIKit.getAdapter().getUserInfo().getUserId());
        DialogUtils.showUserProfileDialog(getContext(), user, useChannelCreateButton, null, getModule().getLoadingDialogHandler());
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

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OperatorListAdapter adapter;
        @Nullable
        private OnItemClickListener<User> itemClickListener;
        @Nullable
        private OnItemLongClickListener<User> itemLongClickListener;
        @Nullable
        private OnItemClickListener<User> actionItemClickListener;
        @Nullable
        private OnItemClickListener<User> profileClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private OperatorListFragment customFragment;

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
         * Sets the custom fragment. It must inherit {@link OperatorListFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OperatorListFragment> Builder setCustomFragment(T fragment) {
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
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         * Sets the channel user list adapter.
         *
         * @param adapter the adapter for the channel user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public <T extends OperatorListAdapter> Builder setOperatorListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of channel user list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemClickListener(@NonNull OnItemClickListener<User> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of channel user list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemLongClickListener(@NonNull OnItemLongClickListener<User> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the action item click listener on the item of channel user list.
         *
         * @param actionItemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnActionItemClickListener(@NonNull OnItemClickListener<User> actionItemClickListener) {
            this.actionItemClickListener = actionItemClickListener;
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.2
         */
        @NonNull
        public Builder setOnProfileClickListener(@NonNull OnItemClickListener<User> profileClickListener) {
            this.profileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.2
         */
        @NonNull
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @see LoadingDialogHandler
         * since 1.2.5
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Creates an {@link OperatorListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OperatorListFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public OperatorListFragment build() {
            final OperatorListFragment fragment = customFragment != null ? customFragment : new OperatorListFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.adapter = adapter;
            fragment.itemClickListener = itemClickListener;
            fragment.itemLongClickListener = itemLongClickListener;
            fragment.actionItemClickListener = actionItemClickListener;
            fragment.profileClickListener = profileClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            return fragment;
        }
    }
}
