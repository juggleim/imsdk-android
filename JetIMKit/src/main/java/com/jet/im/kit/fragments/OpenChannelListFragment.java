package com.jet.im.kit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelListQueryParams;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.CreateOpenChannelActivity;
import com.jet.im.kit.activities.OpenChannelActivity;
import com.jet.im.kit.activities.adapter.OpenChannelListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.OpenChannelListModule;
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.OpenChannelListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.vm.OpenChannelListViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * Fragment displaying the list of open channels.
 * since 3.2.0
 */
public class OpenChannelListFragment extends BaseModuleFragment<OpenChannelListModule, OpenChannelListViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OpenChannelListAdapter adapter;
    @Nullable
    private OnItemClickListener<OpenChannel> itemClickListener;
    @Nullable
    private OnItemLongClickListener<OpenChannel> itemLongClickListener;
    @Nullable
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    @NonNull
    private final ActivityResultLauncher<Intent> createChannelLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Logger.d("++ create channel result=%s", result.getResultCode());
        if (result.getResultCode() == Activity.RESULT_OK) {
            onRefresh();
        }
    });

    // In the case of generating fragments directly through inheritance, the custom type that is in the channel query has to be able to change.
    @Nullable
    protected OpenChannelListQueryParams params;

    @NonNull
    @Override
    protected OpenChannelListModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getOpenChannelList().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull OpenChannelListModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected OpenChannelListViewModel onCreateViewModel() {
        return ViewModelProviders.getOpenChannelList().provide(this, params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull OpenChannelListModule module, @NonNull OpenChannelListViewModel viewModel) {
        Logger.d(">> OpenChannelListFragment::onBeforeReady status=%s", status);
        module.getChannelListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getChannelListComponent().setAdapter(adapter);
        }
        onBindHeaderComponent(module.getHeaderComponent(), viewModel);
        onBindOpenChannelListComponent(module.getChannelListComponent(), viewModel);
        onBindStatusComponent(module.getStatusComponent(), viewModel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull OpenChannelListModule module, @NonNull OpenChannelListViewModel viewModel) {
        Logger.d(">> OpenChannelListFragment::onReady status=%s", status);
        if (status != ReadyStatus.READY) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
        viewModel.loadInitial();
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelListModule, OpenChannelListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * since 3.2.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull OpenChannelListViewModel viewModel) {
        Logger.d(">> OpenChannelListFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> createChannelLauncher.launch(new Intent(requireContext(), CreateOpenChannelActivity.class)));
    }

    /**
     * Called to bind events to the OpenChannelListComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelListModule, OpenChannelListViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param openChannelListComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * since 3.2.0
     */
    protected void onBindOpenChannelListComponent(@NonNull OpenChannelListComponent openChannelListComponent, @NonNull OpenChannelListViewModel viewModel) {
        Logger.d(">> OpenChannelListFragment::onBindOpenChannelListComponent()");
        openChannelListComponent.setOnItemClickListener(this::onItemClicked);
        openChannelListComponent.setOnItemLongClickListener(this::onItemLongClicked);
        openChannelListComponent.setOnRefreshListener(this::onRefresh);
        viewModel.getInitialLoaded().observe(getViewLifecycleOwner(), initialLoaded -> {
            if (initialLoaded) openChannelListComponent.notifyRefreshingFinished();
        });
        viewModel.getChannelList().observe(getViewLifecycleOwner(), openChannelListComponent::notifyDataSetChanged);
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, OpenChannelListModule, OpenChannelListViewModel)}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * since 3.2.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull OpenChannelListViewModel viewModel) {
        Logger.d(">> OpenChannelListFragment::setupStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });

        viewModel.getChannelList().observe(getViewLifecycleOwner(), channels -> statusComponent.notifyStatusChanged(channels.isEmpty() ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE));
    }

    /**
     * Called when the item of the channel list is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param channel  The channel that the clicked item displays
     * since 3.2.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull OpenChannel channel) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(view, position, channel);
            return;
        }
        startOpenChannelActivity(channel.getUrl());
    }

    /**
     * Called when the item of the channel list is long-clicked.
     *
     * @param view     The View long-clicked.
     * @param position The position long-clicked.
     * @param channel  The channel that the long-clicked item displays
     * since 3.2.0
     */
    protected void onItemLongClicked(@NonNull View view, int position, @NonNull OpenChannel channel) {
        if (itemLongClickListener != null) {
            itemLongClickListener.onItemLongClick(view, position, channel);
        }
    }

    /**
     * Refresh the open channel list.
     *
     * since 3.2.0
     */
    protected void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
            return;
        }
        getViewModel().loadInitial();
    }

    private void startOpenChannelActivity(@NonNull String channelUrl) {
        if (isFragmentAlive()) {
            startActivity(OpenChannelActivity.newIntent(requireContext(), OpenChannelActivity.class, channelUrl));
        }
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OpenChannelListAdapter adapter;
        @Nullable
        private OnItemClickListener<OpenChannel> itemClickListener;
        @Nullable
        private OnItemLongClickListener<OpenChannel> itemLongClickListener;
        @Nullable
        private SwipeRefreshLayout.OnRefreshListener refreshListener;
        @Nullable
        private OpenChannelListQueryParams params;
        @Nullable
        private OpenChannelListFragment customFragment;

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
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
        }

        /**
         * Sets the custom fragment. It must inherit {@link OpenChannelListFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OpenChannelListFragment> Builder setCustomFragment(@NonNull T fragment) {
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
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
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
         * Sets the open channel list adapter.
         *
         * @param adapter the adapter for the open channel list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends OpenChannelListAdapter> Builder setOpenChannelListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of open channel list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnItemClickListener(@NonNull OnItemClickListener<OpenChannel> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of open channel list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnItemLongClickListener(@NonNull OnItemLongClickListener<OpenChannel> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Set a callback to be invoked when a refresh is triggered via the swipe gesture.
         *
         * @param refreshListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setOnRefreshListener(@NonNull SwipeRefreshLayout.OnRefreshListener refreshListener) {
            this.refreshListener = refreshListener;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
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
         * since 3.2.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets whether the refresh layout is used.
         *
         * @param useRefreshLayout <code>true</code> if the refresh layout is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setUseRefreshLayout(boolean useRefreshLayout) {
            bundle.putBoolean(StringSet.KEY_USE_REFRESH_LAYOUT, useRefreshLayout);
            return this;
        }

        /**
         * Sets the params instance to get <code>OpenChannel</code>s the current <code>User</code> has joined.
         *
         * @param params The OpenChannelListQueryParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public Builder setCustomQueryParams(@NonNull OpenChannelListQueryParams params) {
            this.params = params;
            return this;
        }

        /**
         * Creates an {@link OpenChannelListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelListFragment} applied to the {@link Bundle}.
         * since 3.2.0
         */
        @NonNull
        public OpenChannelListFragment build() {
            final OpenChannelListFragment fragment = customFragment != null ? customFragment : new OpenChannelListFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.refreshListener = refreshListener;
            fragment.adapter = adapter;
            fragment.itemClickListener = itemClickListener;
            fragment.itemLongClickListener = itemLongClickListener;
            fragment.params = params;
            return fragment;
        }
    }
}
