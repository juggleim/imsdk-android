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
import com.sendbird.android.user.Member;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.InviteUserActivity;
import com.jet.im.kit.activities.adapter.MemberListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.modules.MemberListModule;
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.MemberListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.vm.MemberListViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * Fragment displaying the member list in the channel.
 */
public class MemberListFragment extends BaseModuleFragment<MemberListModule, MemberListViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private MemberListAdapter adapter;
    @Nullable
    private OnItemClickListener<Member> itemClickListener;
    @Nullable
    private OnItemLongClickListener<Member> itemLongClickListener;
    @Nullable
    private OnItemClickListener<Member> actionItemClickListener;
    @Nullable
    private OnItemClickListener<Member> profileClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    @NonNull
    @Override
    protected MemberListModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getMemberList().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull MemberListModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @NonNull
    @Override
    protected MemberListViewModel onCreateViewModel() {
        return ViewModelProviders.getMemberList().provide(this, getChannelUrl());
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MemberListModule module, @NonNull MemberListViewModel viewModel) {
        Logger.d(">> MemberListFragment::onBeforeReady()");
        module.getMemberListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getMemberListComponent().setAdapter(adapter);
        }
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindMemberListComponent(module.getMemberListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull MemberListModule module, @NonNull MemberListViewModel viewModel) {
        Logger.d(">> MemberListFragment::onReady(ReadyStatus=%s)", status);
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.getChannelDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) shouldActivityFinish();
        });

        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, MemberListModule, MemberListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull MemberListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MemberListFragment::onBindHeaderComponent()");

        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            if (isFragmentAlive() && getContext() != null && channel != null) {
                startActivity(InviteUserActivity.newIntent(getContext(), channel.getUrl()));
            }
        });
    }

    /**
     * Called to bind events to the MemberListComponent. This is called from {@link #onBeforeReady(ReadyStatus, MemberListModule, MemberListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMemberListComponent(@NonNull MemberListComponent listComponent, @NonNull MemberListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MemberListFragment::onBindMemberListComponent()");
        listComponent.setOnItemClickListener(itemClickListener);
        listComponent.setOnItemLongClickListener(itemLongClickListener);
        listComponent.setOnActionItemClickListener(actionItemClickListener != null ? actionItemClickListener : (view, position, member) -> onActionItemClicked(view, position, member, channel));
        listComponent.setOnProfileClickListener(profileClickListener != null ? profileClickListener : this::onProfileClicked);
        viewModel.getUserList().observe(getViewLifecycleOwner(), members -> {
            Logger.dev("++ observing result members size : %s", members.size());
            if (channel != null) {
                listComponent.notifyDataSetChanged(members, channel.getMyRole());
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, MemberListModule, MemberListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull MemberListViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MemberListFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });

        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    /**
     * Called when the user profile has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param member   The member data that was clicked.
     * since 1.2.2
     */
    protected void onProfileClicked(@NonNull View view, int position, @NonNull Member member) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, UIKitConfig.getCommon().getEnableUsingDefaultUserProfile());
        if (getContext() == null || SendbirdUIKit.getAdapter() == null || !useUserProfile) return;
        boolean useChannelCreateButton = !member.getUserId().equals(SendbirdUIKit.getAdapter().getUserInfo().getUserId());
        DialogUtils.showUserProfileDialog(getContext(), member, useChannelCreateButton, null, getModule().getLoadingDialogHandler());
    }

    /**
     * Called when the action has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param member   The member data that was clicked.
     * @param channel  The {@code GroupChannel} that contains the data needed for this fragment
     */
    protected void onActionItemClicked(@NonNull View view, int position, @NonNull Member member, @Nullable GroupChannel channel) {
        if (getContext() == null || channel == null) return;
        boolean isMuted = member.isMuted();
        boolean isOperator = member.getRole() == Role.OPERATOR;
        DialogListItem registerOperator = new DialogListItem(isOperator ? R.string.sb_text_unregister_operator : R.string.sb_text_register_operator);
        DialogListItem muteMember = new DialogListItem(isMuted ? R.string.sb_text_unmute_member : R.string.sb_text_mute_member);
        DialogListItem banMember = new DialogListItem(R.string.sb_text_ban_member, 0, true);
        DialogListItem[] items = !channel.isBroadcast() ?
            new DialogListItem[]{registerOperator, muteMember, banMember} :
            new DialogListItem[]{registerOperator, banMember};

        final MemberListModule module = getModule();
        final MemberListViewModel viewModel = getViewModel();
        DialogUtils.showListDialog(getContext(), member.getNickname(),
            items, (v, p, item) -> {
                final int key = item.getKey();
                final OnCompleteHandler handler = e -> {
                    module.shouldDismissLoadingDialog();
                    if (e != null) {
                        int errorTextResId = R.string.sb_text_error_register_operator;
                        if (key == R.string.sb_text_unregister_operator) {
                            errorTextResId = R.string.sb_text_error_unregister_operator;
                        } else if (key == R.string.sb_text_mute_member) {
                            errorTextResId = R.string.sb_text_error_mute_member;
                        } else if (key == R.string.sb_text_unmute_member) {
                            errorTextResId = R.string.sb_text_error_unmute_member;
                        } else if (key == R.string.sb_text_ban_member) {
                            errorTextResId = R.string.sb_text_error_ban_member;
                        }
                        toastError(errorTextResId);
                    }
                };
                if (getContext() == null) return;
                module.shouldShowLoadingDialog(getContext());
                if (key == R.string.sb_text_register_operator) {
                    viewModel.addOperator(member.getUserId(), handler);
                } else if (key == R.string.sb_text_unregister_operator) {
                    viewModel.removeOperator(member.getUserId(), handler);
                } else if (key == R.string.sb_text_mute_member) {
                    viewModel.muteUser(member.getUserId(), handler);
                } else if (key == R.string.sb_text_unmute_member) {
                    viewModel.unmuteUser(member.getUserId(), handler);
                } else if (key == R.string.sb_text_ban_member) {
                    viewModel.banUser(member.getUserId(), handler);
                }
            });
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    protected boolean shouldShowLoadingDialog() {
        if (getContext() != null) {
            return getModule().shouldShowLoadingDialog(getContext());
        }
        return false;
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
        private MemberListAdapter adapter;
        @Nullable
        private OnItemClickListener<Member> itemClickListener;
        @Nullable
        private OnItemLongClickListener<Member> itemLongClickListener;
        @Nullable
        private OnItemClickListener<Member> actionItemClickListener;
        @Nullable
        private OnItemClickListener<Member> profileClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private MemberListFragment customFragment;

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
         * Sets the custom fragment. It must inherit {@link MemberListFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends MemberListFragment> Builder setCustomFragment(T fragment) {
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
         * Sets the member list adapter.
         *
         * @param adapter the adapter for the member list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public <T extends MemberListAdapter> Builder setMemberListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of member list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemClickListener(@NonNull OnItemClickListener<Member> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of member list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemLongClickListener(@NonNull OnItemLongClickListener<Member> itemLongClickListener) {
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
        public Builder setOnActionItemClickListener(@NonNull OnItemClickListener<Member> actionItemClickListener) {
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
        public Builder setOnProfileClickListener(@NonNull OnItemClickListener<Member> profileClickListener) {
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
         * @see LoadingDialogHandler
         * since 1.2.5
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Creates an {@link MemberListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link MemberListFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public MemberListFragment build() {
            final MemberListFragment fragment = customFragment != null ? customFragment : new MemberListFragment();
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
