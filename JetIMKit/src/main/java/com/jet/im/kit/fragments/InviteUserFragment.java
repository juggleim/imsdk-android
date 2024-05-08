package com.jet.im.kit.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.activities.adapter.InviteUserListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnUserSelectChangedListener;
import com.jet.im.kit.interfaces.OnUserSelectionCompleteListener;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.InviteUserModule;
import com.jet.im.kit.modules.components.InviteUserListComponent;
import com.jet.im.kit.modules.components.SelectUserHeaderComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.vm.InviteUserViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the user list to invite to the channel.
 */
public class InviteUserFragment extends BaseModuleFragment<InviteUserModule, InviteUserViewModel> {

    @Nullable
    private PagedQueryHandler<UserInfo> pagedQueryHandler;
    @Nullable
    private InviteUserListAdapter adapter;
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
    protected InviteUserModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getInviteUser().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull InviteUserModule module, @NonNull Bundle args) {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @NonNull
    @Override
    protected InviteUserViewModel onCreateViewModel() {
        return ViewModelProviders.getInviteUser().provide(this, getChannelUrl(), pagedQueryHandler);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull InviteUserModule module, @NonNull InviteUserViewModel viewModel) {
        Logger.d(">> InviteUserFragment::onBeforeReady()");
        module.getInviteUserListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getInviteUserListComponent().setAdapter(adapter);
        }
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindInviteUserListComponent(module.getInviteUserListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull InviteUserModule module, @NonNull InviteUserViewModel viewModel) {
        Logger.d(">> InviteUserFragment::onReady(ReadyStatus=%s)", status);
        final GroupChannel channel = viewModel.getChannel();
        if (status != ReadyStatus.READY || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }

        module.getInviteUserListComponent().notifyDisabledUserIds(getDisabledUserIds(channel));
        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the SelectUserHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, InviteUserModule, InviteUserViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull SelectUserHeaderComponent headerComponent, @NonNull InviteUserViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> InviteUserFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> getModule().getInviteUserListComponent().notifySelectionComplete());
    }

    /**
     * Called to bind events to the InviteUserListComponent. This is called from {@link #onBeforeReady(ReadyStatus, InviteUserModule, InviteUserViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindInviteUserListComponent(@NonNull InviteUserListComponent listComponent, @NonNull InviteUserViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> InviteUserFragment::onBindInviteUserListComponent()");

        listComponent.setOnUserSelectChangedListener(userSelectChangedListener != null ? userSelectChangedListener : (selectedUserIds, isSelected) -> getModule().getHeaderComponent().notifySelectedUserChanged(selectedUserIds.size()));
        listComponent.setOnUserSelectionCompleteListener(userSelectionCompleteListener != null ? userSelectionCompleteListener : InviteUserFragment.this::onUserSelectionCompleted);
        viewModel.getUserList().observe(getViewLifecycleOwner(), listComponent::notifyDataSetChanged);
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, InviteUserModule, InviteUserViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull InviteUserViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> InviteUserFragment::onBindStatusComponent()");
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
     */
    @NonNull
    protected List<String> getDisabledUserIds(@NonNull GroupChannel channel) {
        Logger.d(">> InviteUserFragment::getDisabledUserIds()");
        List<String> invitedUserIds = new ArrayList<>();
        if (!channel.isSuper() && !channel.isBroadcast()) {
            for (User user : channel.getMembers()) {
                invitedUserIds.add(user.getUserId());
            }
        }

        return invitedUserIds;
    }

    /**
     * Called when the user selection completed.
     *
     * @param selectedUsers selected user's ids.
     * since 3.0.0
     */
    protected void onUserSelectionCompleted(@NonNull List<String> selectedUsers) {
        Logger.d(">> InviteUserFragment::onUserSelectComplete()");
        inviteUser(selectedUsers);
    }

    /**
     * It will be called before inviting users.
     * If you want add more data, you can override this and set the data.
     *
     * @param userIds The user list who will be invited.
     * since 1.1.1
     */
    protected void onBeforeInviteUsers(@NonNull List<String> userIds) {
    }

    /**
     * Invites users.
     *
     * @param userIds The user list who will be invited.
     * since 1.1.1
     */
    protected void inviteUser(@NonNull List<String> userIds) {
        Logger.d(">> InviteUserFragment::inviteUser()");
        final GroupChannel channel = getViewModel().getChannel();
        onBeforeInviteUsers(userIds);
        getViewModel().inviteUser(userIds, e -> {
            if (e != null) {
                toastError(R.string.sb_text_error_invite_member);
                Logger.e(e);
                return;
            }
            if (channel != null) {
                onNewUserInvited(channel);
            }
        });
    }

    /**
     * It will be called when the new users have been invited.
     *
     * @param channel the channel where new users are invited.
     * since 1.1.1
     */
    protected void onNewUserInvited(@NonNull GroupChannel channel) {
        Logger.d(">> InviteUserFragment::onNewUserInvited()");
        if (isFragmentAlive()) {
            Intent intent = ChannelActivity.newIntent(requireContext(), channel.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private PagedQueryHandler<UserInfo> customUserListQueryHandler;
        @Nullable
        private InviteUserListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnUserSelectChangedListener userSelectChangedListener;
        @Nullable
        private OnUserSelectionCompleteListener userSelectionCompleteListener;
        @Nullable
        private InviteUserFragment customFragment;

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
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
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
         * Sets the custom fragment. It must inherit {@link InviteUserFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends InviteUserFragment> Builder setCustomFragment(T fragment) {
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
         * Sets the invite button text of the header.
         *
         * @param inviteButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.0
         */
        @NonNull
        public Builder setInviteButtonText(@NonNull String inviteButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, inviteButtonText);
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
         * Sets the handler that loads the list of user.
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setCustomPagedQueryHandler(@NonNull PagedQueryHandler<UserInfo> handler) {
            this.customUserListQueryHandler = handler;
            return this;
        }

        /**
         * Sets the user list adapter.
         *
         * @param adapter the adapter for the user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setInviteUserListAdapter(@NonNull InviteUserListAdapter adapter) {
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
         * Creates an {@link InviteUserFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link InviteUserFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public InviteUserFragment build() {
            final InviteUserFragment fragment = customFragment != null ? customFragment : new InviteUserFragment();
            fragment.setArguments(bundle);
            fragment.pagedQueryHandler = customUserListQueryHandler;
            fragment.adapter = adapter;
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.userSelectChangedListener = userSelectChangedListener;
            fragment.userSelectionCompleteListener = userSelectionCompleteListener;
            return fragment;
        }
    }
}
