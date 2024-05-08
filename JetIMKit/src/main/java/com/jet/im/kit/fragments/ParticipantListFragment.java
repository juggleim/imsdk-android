package com.jet.im.kit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.adapter.ParticipantListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnCompleteHandler;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.modules.ParticipantListModule;
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.ParticipantListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.vm.ParticipantViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * Fragment displaying the operators of the channel.
 *
 * since 1.2.0
 */
public class ParticipantListFragment extends BaseModuleFragment<ParticipantListModule, ParticipantViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private ParticipantListAdapter adapter;
    @Nullable
    private OnItemClickListener<User> itemClickListener;
    @Nullable
    private OnItemLongClickListener<User> itemLongClickListener;
    @Nullable
    private OnItemClickListener<User> profileClickListener;
    @Nullable
    private OnItemClickListener<User> actionItemClickListener;

    @NonNull
    @Override
    protected ParticipantListModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getParticipantList().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull ParticipantListModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected ParticipantViewModel onCreateViewModel() {
        return ViewModelProviders.getParticipantList().provide(this, getChannelUrl(), null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ParticipantListModule module, @NonNull ParticipantViewModel viewModel) {
        Logger.d(">> ParticipantListFragment::onBeforeReady()");
        module.getParticipantListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getParticipantListComponent().setAdapter(adapter);
        }
        final OpenChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindParticipantsListComponent(module.getParticipantListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ParticipantListModule module, @NonNull ParticipantViewModel viewModel) {
        Logger.d(">> ParticipantListFragment::onReady(ReadyStatus=%s)", status);
        final OpenChannel channel = viewModel.getChannel();
        if (status != ReadyStatus.READY || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }

        viewModel.loadInitial();

        viewModel.getChannelDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) shouldActivityFinish();
        });
        viewModel.getUserBanned().observe(getViewLifecycleOwner(), restrictedUser -> {
            if (SendbirdUIKit.getAdapter() == null) return;
            if (restrictedUser.getUserId().equals(SendbirdUIKit.getAdapter().getUserInfo().getUserId())) {
                shouldActivityFinish();
            }
        });
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ParticipantListModule, ParticipantViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull ParticipantViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> ParticipantListFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener);
    }

    /**
     * Called to bind events to the ParticipantsListComponent. This is called from {@link #onBeforeReady(ReadyStatus, ParticipantListModule, ParticipantViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindParticipantsListComponent(@NonNull ParticipantListComponent listComponent, @NonNull ParticipantViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> ParticipantListFragment::onBindParticipantsListComponent()");

        listComponent.setOnItemClickListener(itemClickListener);
        listComponent.setOnItemLongClickListener(itemLongClickListener);
        listComponent.setOnProfileClickListener(profileClickListener != null ? profileClickListener : this::onProfileClicked);
        listComponent.setOnActionItemClickListener(actionItemClickListener != null ? actionItemClickListener : (view, position, participant) -> onActionItemClicked(view, position, participant, channel));
        viewModel.getUserList().observe(getViewLifecycleOwner(), users -> {
            Logger.dev("++ observing result participants size : %s", users.size());
            if (channel != null) {
                listComponent.notifyDataSetChanged(users, channel);
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, ParticipantListModule, ParticipantViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull ParticipantViewModel viewModel, @Nullable OpenChannel channel) {
        Logger.d(">> ParticipantListFragment::onBindStatusComponent()");
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
     * @param user     The member data that was clicked.
     * since 1.2.2
     */
    protected void onProfileClicked(@NonNull View view, int position, @NonNull User user) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, UIKitConfig.getCommon().getEnableUsingDefaultUserProfile());
        if (getContext() == null || !useUserProfile) return;
        DialogUtils.showUserProfileDialog(getContext(), user, false, null, null);
    }

    /**
     * Called when the action has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param participant   The participant data that was clicked.
     * @param channel  The {@code OpenChannel} that contains the data needed for this fragment
     * since 3.1.0
     */
    protected void onActionItemClicked(@NonNull View view, int position, @NonNull User participant, @Nullable OpenChannel channel) {
        if (getContext() == null || channel == null) return;
        boolean isOperator = channel.isOperator(participant);
        DialogListItem registerOperator = new DialogListItem(isOperator ? R.string.sb_text_unregister_operator : R.string.sb_text_register_operator);
        // mute menu is always shown as 'Mute' because there is no way to know user's muted info
        DialogListItem muteParticipant = new DialogListItem(R.string.sb_text_mute_participant);
        DialogListItem banParticipant = new DialogListItem(R.string.sb_text_ban_participant, 0, true);
        DialogListItem[] items = new DialogListItem[]{registerOperator, muteParticipant, banParticipant};

        final ParticipantListModule module = getModule();
        final ParticipantViewModel viewModel = getViewModel();
        DialogUtils.showListDialog(getContext(), participant.getNickname(),
            items, (v, p, item) -> {
                final int key = item.getKey();
                final OnCompleteHandler handler = e -> {
                    module.shouldDismissLoadingDialog();
                    if (e != null) {
                        int errorTextResId = R.string.sb_text_error_register_operator;
                        if (key == R.string.sb_text_unregister_operator) {
                            errorTextResId = R.string.sb_text_error_unregister_operator;
                        } else if (key == R.string.sb_text_mute_participant) {
                            errorTextResId = R.string.sb_text_error_mute_participant;
                        } else if (key == R.string.sb_text_ban_participant) {
                            errorTextResId = R.string.sb_text_error_ban_participant;
                        }
                        toastError(errorTextResId);
                    } else {
                        viewModel.loadInitial();
                    }
                };
                if (getContext() == null) return;
                module.shouldShowLoadingDialog(getContext());
                if (key == R.string.sb_text_register_operator) {
                    viewModel.addOperator(participant.getUserId(), handler);
                } else if (key == R.string.sb_text_unregister_operator) {
                    viewModel.removeOperator(participant.getUserId(), handler);
                } else if (key == R.string.sb_text_mute_participant) {
                    viewModel.muteUser(participant.getUserId(), handler);
                } else if (key == R.string.sb_text_ban_participant) {
                    viewModel.banUser(participant.getUserId(), handler);
                }
            });
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
     * This is a Builder that is able to create the fragment of participants list.
     * The builder provides options how the channel is showing and working. Also you can set the event handler what you want to override.
     *
     * since 2.0.0
     */
    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private ParticipantListAdapter adapter;
        @Nullable
        private OnItemClickListener<User> itemClickListener;
        @Nullable
        private OnItemLongClickListener<User> itemLongClickListener;
        @Nullable
        private OnItemClickListener<User> profileClickListener;
        @Nullable
        private OnItemClickListener<User> actionItemClickListener;
        @Nullable
        private ParticipantListFragment customFragment;

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
         * Sets the custom fragment. It must inherit {@link ParticipantListFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends ParticipantListFragment> Builder setCustomFragment(T fragment) {
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
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         * Sets the participants list adapter.
         *
         * @param adapter the adapter for the participant list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public <T extends ParticipantListAdapter> Builder setParticipantListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of participants list.
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
         * Sets the long click listener on the item of participants list.
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
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         */
        @NonNull
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * Register a callback to be invoked when the action button of the item is clicked.
         *
         * @param actionItemClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnActionItemClickListener(@Nullable OnItemClickListener<User> actionItemClickListener) {
            this.actionItemClickListener = actionItemClickListener;
            return this;
        }

        /**
         * Creates an {@link ParticipantListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ParticipantListFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public ParticipantListFragment build() {
            final ParticipantListFragment fragment = customFragment != null ? customFragment : new ParticipantListFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.adapter = adapter;
            fragment.itemClickListener = itemClickListener;
            fragment.itemLongClickListener = itemLongClickListener;
            fragment.profileClickListener = profileClickListener;
            fragment.actionItemClickListener = actionItemClickListener;
            return fragment;
        }
    }
}
