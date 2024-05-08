package com.jet.im.kit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.activities.adapter.CreateChannelUserListAdapter;
import com.jet.im.kit.consts.CreatableChannelType;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.CustomParamsHandler;
import com.jet.im.kit.interfaces.OnUserSelectChangedListener;
import com.jet.im.kit.interfaces.OnUserSelectionCompleteListener;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.CreateChannelModule;
import com.jet.im.kit.modules.components.CreateChannelUserListComponent;
import com.jet.im.kit.modules.components.SelectUserHeaderComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.vm.CreateChannelViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;

/**
 * Fragment displaying the user list to create the channel.
 */
public class CreateChannelFragment extends BaseModuleFragment<CreateChannelModule, CreateChannelViewModel> {

    @Nullable
    private PagedQueryHandler<UserInfo> pagedQueryHandler;
    @Nullable
    private CreateChannelUserListAdapter adapter;
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnUserSelectChangedListener userSelectChangedListener;
    @Nullable
    private OnUserSelectionCompleteListener userSelectionCompleteListener;

    @NonNull
    @Override
    protected CreateChannelModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getCreateChannel().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull CreateChannelModule module, @NonNull Bundle args) {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @NonNull
    @Override
    protected CreateChannelViewModel onCreateViewModel() {
        return ViewModelProviders.getCreateChannel().provide(this, pagedQueryHandler);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull CreateChannelModule module, @NonNull CreateChannelViewModel viewModel) {
        Logger.d(">> CreateChannelFragment::onBeforeReady(ReadyStatus=%s)", status);
        module.getUserListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getUserListComponent().setAdapter(adapter);
        }
        onBindHeaderComponent(module.getHeaderComponent(), viewModel);
        onBindUserListComponent(module.getUserListComponent(), viewModel);
        onBindStatusComponent(module.getStatusComponent(), viewModel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull CreateChannelModule module, @NonNull CreateChannelViewModel viewModel) {
        Logger.d(">> CreateChannelFragment::onReady()");
        if (status != ReadyStatus.READY) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the SelectUserHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, CreateChannelModule, CreateChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull SelectUserHeaderComponent headerComponent, @NonNull CreateChannelViewModel viewModel) {
        Logger.d(">> CreateChannelFragment::onBindHeaderComponent()");

        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            final CreateChannelUserListComponent listComponent = getModule().getUserListComponent();
            listComponent.notifySelectionComplete();
        });
    }

    /**
     * Called to bind events to the CreateChannelUserListComponent. This is called from {@link #onBeforeReady(ReadyStatus, CreateChannelModule, CreateChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindUserListComponent(@NonNull CreateChannelUserListComponent listComponent, @NonNull CreateChannelViewModel viewModel) {
        Logger.d(">> CreateChannelFragment::onBindUserListComponent()");
        listComponent.setOnUserSelectChangedListener(userSelectChangedListener != null ? userSelectChangedListener : (selectedUserIds, isSelected) -> {
            final SelectUserHeaderComponent headerComponent = getModule().getHeaderComponent();
            headerComponent.notifySelectedUserChanged(selectedUserIds.size());
        });
        listComponent.setOnUserSelectionCompleteListener(userSelectionCompleteListener != null ? userSelectionCompleteListener : CreateChannelFragment.this::onUserSelectionCompleted);
        viewModel.getUserList().observe(getViewLifecycleOwner(), listComponent::notifyDataSetChanged);
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, CreateChannelModule, CreateChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull CreateChannelViewModel viewModel) {
        Logger.d(">> CreateChannelFragment::onBindStatusComponent()");

        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    /**
     * Returns the lists of user ids that you want to disable.
     *
     * @return The user id list.
     * since 1.2.0
     */
    @NonNull
    protected List<String> getDisabledUserIds() {
        return Collections.emptyList();
    }

    /**
     * Called when the user selection completed.
     *
     * @param selectedUsers selected user's ids.
     * since 3.0.0
     */
    protected void onUserSelectionCompleted(@NonNull List<String> selectedUsers) {
        GroupChannelCreateParams params = new GroupChannelCreateParams();
        params.setUserIds(selectedUsers);
        params.setName("");
        params.setCoverUrl("");
        params.setOperators(Collections.singletonList(SendbirdChat.getCurrentUser()));
        if (getArguments() != null && getArguments().containsKey(StringSet.KEY_DISTINCT)) {
            params.setDistinct(getArguments().getBoolean(StringSet.KEY_DISTINCT));
        }

        final CreatableChannelType selectedChannelType = getModule().getParams().getSelectedChannelType();
        Logger.d("=++ selected channel type : " + selectedChannelType);
        switch (selectedChannelType) {
            case Super:
                params.setSuper(true);
                break;
            case Broadcast:
                params.setBroadcast(true);
                break;
            default:
                break;
        }
        createGroupChannel(params);
    }

    /**
     * It will be called before creating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelCreateParams}.
     * since 1.0.4
     */
    protected void onBeforeCreateGroupChannel(@NonNull GroupChannelCreateParams params) {
    }

    /**
     * Creates <code>GroupChannel</code> with GroupChannelParams.
     *
     * @param params Params of channel. Refer to {@link GroupChannelCreateParams}.
     * since 1.0.4
     */
    protected void createGroupChannel(@NonNull GroupChannelCreateParams params) {
        Logger.dev(">> CreateChannelFragment::createGroupChannel()");
        CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeCreateGroupChannel(params);
        }
        onBeforeCreateGroupChannel(params);

        Logger.dev("++ createGroupChannel params : " + params);
        getViewModel().createChannel(params, (channel, e) -> {
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
     * since 1.0.4
     */
    protected void onNewChannelCreated(@NonNull GroupChannel channel) {
        if (isFragmentAlive()) {
            startActivity(ChannelActivity.newIntent(requireContext(), channel.getUrl()));
            shouldActivityFinish();
        }
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private PagedQueryHandler<UserInfo> pagedQueryHandler;
        @Nullable
        private CreateChannelUserListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnUserSelectChangedListener userSelectChangedListener;
        @Nullable
        private OnUserSelectionCompleteListener userSelectionCompleteListener;
        @Nullable
        private CreateChannelFragment customFragment;

        /**
         * Constructor
         */
        public Builder() {
            this(0);
        }

        /**
         * Constructor
         *
         * @param themeMode {@link SendbirdUIKit.ThemeMode}
         */
        public Builder(@NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param type A type of channel. Default is a {@link CreatableChannelType#Normal}
         * since 3.0.0
         */
        public Builder(@NonNull CreatableChannelType type) {
            this(SendbirdUIKit.getDefaultThemeMode().getResId(), type);
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@StyleRes int customThemeResId) {
            this(customThemeResId, CreatableChannelType.Normal);
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         * @param type             A type of channel. Default is a {@link CreatableChannelType#Normal}
         * since 3.0.0
         */
        public Builder(@StyleRes int customThemeResId, @NonNull CreatableChannelType type) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putSerializable(StringSet.KEY_SELECTED_CHANNEL_TYPE, type);
        }

        /**
         * Sets the custom fragment. It must inherit {@link CreateChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends CreateChannelFragment> Builder setCustomFragment(T fragment) {
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
         * Sets the create button text of the header.
         *
         * @param createButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.0
         */
        @NonNull
        public Builder setCreateButtonText(@NonNull String createButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, createButtonText);
            return this;
        }

        /**
         * Sets the handler that loads the list of user.
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setCustomPagedQueryHandler(@NonNull PagedQueryHandler<UserInfo> handler) {
            this.pagedQueryHandler = handler;
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
         * Sets distinct mode. Distinct mode must be false, if super mode is true.
         *
         * @param isDistinct true if distinct mode channel.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setIsDistinct(boolean isDistinct) {
            bundle.putBoolean(StringSet.KEY_DISTINCT, isDistinct);
            return this;
        }

        /**
         * Sets the user list adapter.
         *
         * @param adapter the adapter for the user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setCreateChannelUserListAdapter(@NonNull CreateChannelUserListAdapter adapter) {
            this.adapter = adapter;
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
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.6
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
         * since 2.1.6
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
         * Register a callback to be invoked when the user is selected.
         *
         * @param userSelectChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnUserSelectChangedListener(@Nullable OnUserSelectChangedListener userSelectChangedListener) {
            this.userSelectChangedListener = userSelectChangedListener;
            return this;
        }

        /**
         * Register a callback to be invoked when selecting users is completed.
         *
         * @param userSelectionCompleteListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnUserSelectionCompleteListener(@Nullable OnUserSelectionCompleteListener userSelectionCompleteListener) {
            this.userSelectionCompleteListener = userSelectionCompleteListener;
            return this;
        }

        /**
         * Creates an {@link CreateChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link CreateChannelFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public CreateChannelFragment build() {
            final CreateChannelFragment fragment = customFragment != null ? customFragment : new CreateChannelFragment();
            fragment.setArguments(bundle);
            fragment.pagedQueryHandler = pagedQueryHandler;
            fragment.adapter = adapter;
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.userSelectChangedListener = userSelectChangedListener;
            fragment.userSelectionCompleteListener = userSelectionCompleteListener;
            return fragment;
        }
    }
}
