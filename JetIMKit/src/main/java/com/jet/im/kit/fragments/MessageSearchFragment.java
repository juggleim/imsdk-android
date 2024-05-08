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
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.query.MessageSearchQuery;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.activities.adapter.MessageSearchAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.interfaces.MessageDisplayDataProvider;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnSearchEventListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.modules.MessageSearchModule;
import com.jet.im.kit.modules.components.MessageSearchHeaderComponent;
import com.jet.im.kit.modules.components.MessageSearchListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.providers.ModuleProviders;
import com.jet.im.kit.providers.ViewModelProviders;
import com.jet.im.kit.utils.SoftInputUtils;
import com.jet.im.kit.vm.MessageSearchViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

import java.util.List;

/**
 * Fragment that provides message search
 */
public class MessageSearchFragment extends BaseModuleFragment<MessageSearchModule, MessageSearchViewModel> {
    @Nullable
    private OnSearchEventListener onSearchEventListener;
    @Nullable
    private MessageSearchAdapter adapter;
    @Nullable
    private OnItemClickListener<BaseMessage> itemClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;
    @Nullable
    private MessageSearchQuery query;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private View.OnClickListener clearButtonClickListener;

    @NonNull
    @Override
    protected MessageSearchModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getMessageSearch().provide(requireContext(), args);
    }

    @Override
    protected void onConfigureParams(@NonNull MessageSearchModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    public MessageSearchViewModel onCreateViewModel() {
        return ViewModelProviders.getMessageSearch().provide(this, getChannelUrl(), query);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MessageSearchModule module, @NonNull MessageSearchViewModel viewModel) {
        Logger.d(">> MessageSearchFragment::onBeforeReady()");
        module.getMessageListComponent().setPagedDataLoader(viewModel);
        if (adapter != null) {
            module.getMessageListComponent().setAdapter(adapter);
        }

        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindMessageSearchListComponent(module.getMessageListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull MessageSearchModule module, @NonNull MessageSearchViewModel viewModel) {
        Logger.d(">> MessageSearchFragment::onReady(ReadyStatus=%s)", status);
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            final StatusComponent statusComponent = module.getStatusComponent();
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            return;
        }
    }

    /**
     * Called to bind events to the MessageSearchHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageSearchModule, MessageSearchViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull MessageSearchHeaderComponent headerComponent, @NonNull MessageSearchViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageSearchFragment::onBindHeaderComponent()");

        headerComponent.setOnSearchEventListener(onSearchEventListener != null ? onSearchEventListener : keyword -> {
            Logger.d("++ request search keyword : %s", keyword);
            hideKeyboard();
            search(keyword);
        });
        headerComponent.setOnInputTextChangedListener(inputTextChangedListener);
        headerComponent.setOnClearButtonClickListener(clearButtonClickListener);
    }

    /**
     * Called to bind events to the MessageSearchListComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageSearchModule, MessageSearchViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel     A view model that provides the data needed for the fragment
     * @param channel       The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMessageSearchListComponent(@NonNull MessageSearchListComponent listComponent, @NonNull MessageSearchViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageSearchFragment::onBindMessageSearchListComponent()");
        listComponent.setOnItemClickListener(itemClickListener != null ? itemClickListener : this::onItemClicked);
        viewModel.getSearchResultList().observe(getViewLifecycleOwner(), searchResults -> {
            Logger.dev("++ search result size : %s", searchResults.size());
            onSearchResultReceived(searchResults);
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, MessageSearchModule, MessageSearchViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull MessageSearchViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> MessageSearchFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 2.1.0
     */
    public boolean shouldShowLoadingDialog() {
        if (getContext() != null) {
            getModule().shouldShowLoadingDialog(getContext());
        }
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 2.1.0
     */
    public void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * It called when the search request occurs with the written keyword.
     *
     * @param keyword Keyword to search for messages
     * since 2.1.0
     */
    protected void search(@NonNull String keyword) {
        shouldShowLoadingDialog();
        getViewModel().search(keyword, (result, e) -> {
            shouldDismissLoadingDialog();
            if (e != null) {
                getModule().getStatusComponent().notifyStatusChanged(StatusFrameView.Status.ERROR);
            }
        });
    }

    /**
     * It called when the search results exists.
     *
     * @param searchResults The search results.
     * since 2.1.0
     */
    protected void onSearchResultReceived(@NonNull List<BaseMessage> searchResults) {
        final MessageSearchModule module = getModule();
        shouldDismissLoadingDialog();
        module.getStatusComponent().notifyStatusChanged(StatusFrameView.Status.NONE);
        module.getMessageListComponent().notifyDataSetChanged(searchResults);

        if (searchResults.isEmpty()) {
            module.getStatusComponent().notifyStatusChanged(StatusFrameView.Status.EMPTY);
        }
    }

    /**
     * Called when a message item has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param message  The user data that was clicked.
     * since 2.1.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        Logger.d(">> MessageSearchFragment::onItemClicked(position=%s)", position);
        if (getContext() != null) {
            final String channelUrl = getViewModel().getChannel() == null ? "" : getViewModel().getChannel().getUrl();
            Intent intent = new ChannelActivity.IntentBuilder(getContext(), channelUrl)
                .setStartingPoint(message.getCreatedAt())
                .build();
            intent.putExtra(StringSet.KEY_FROM_SEARCH_RESULT, true);
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
        private OnSearchEventListener onSearchEventListener;
        @Nullable
        private MessageSearchAdapter adapter;
        @Nullable
        private OnItemClickListener<BaseMessage> itemClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private MessageSearchQuery query;
        @Nullable
        private OnInputTextChangedListener inputTextChangedListener;
        @Nullable
        private View.OnClickListener clearButtonClickListener;
        @Nullable
        private MessageSearchFragment customFragment;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * since 2.1.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * since 2.1.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * since 2.1.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom fragment. It must inherit {@link MessageSearchFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends MessageSearchFragment> Builder setCustomFragment(T fragment) {
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
         * @param useSearchBar <code>true</code> if the search bar is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setUseSearchBar(boolean useSearchBar) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useSearchBar);
            return this;
        }

        /**
         * Sets the text on the search bar's right side.
         *
         * @param text the text which is shown on the search bar's right side.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setSearchBarButtonText(@NonNull String text) {
            bundle.putString(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT, text);
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
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
         * since 2.1.0
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
         * Sets the search action event listener on the right button of the search bar.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setOnSearchEventListener(@NonNull OnSearchEventListener listener) {
            this.onSearchEventListener = listener;
            return this;
        }

        /**
         * Sets the message search adapter.
         *
         * @param adapter The adapter for displaying the searched message list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public <T extends MessageSearchAdapter> Builder setMessageSearchAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the message search adapter and the message display data provider.
         * The message display data provider is used to generate the data to display the message.
         *
         * @param adapter he adapter for displaying the searched message list.
         * @param provider the provider for the message display data.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.7
         */
        @NonNull
        public <T extends MessageSearchAdapter> Builder setMessageSearchAdapter(T adapter, @Nullable MessageDisplayDataProvider provider) {
            this.adapter = adapter;
            if (this.adapter != null) this.adapter.setMessageDisplayDataProvider(provider);
            return this;
        }

        /**
         * Sets the click listener on the item of the message search result list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnItemClickListener(@NonNull OnItemClickListener<BaseMessage> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 2.1.0
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the query of custom message search.
         * The channelUrl, the keyword, and the order properties in the MessageSearchQuery are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param query The {@link MessageSearchQuery} instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setMessageSearchQuery(@NonNull MessageSearchQuery query) {
            this.query = query;
            return this;
        }

        /**
         * Register a callback to be invoked when the input text is changed.
         *
         * @param textChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@Nullable OnInputTextChangedListener textChangedListener) {
            this.inputTextChangedListener = textChangedListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the clear button related to the input is clicked.
         *
         * @param clearButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnClearButtonClickListener(@Nullable View.OnClickListener clearButtonClickListener) {
            this.clearButtonClickListener = clearButtonClickListener;
            return this;
        }

        /**
         * Creates an {@link MessageSearchFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link MessageSearchFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public MessageSearchFragment build() {
            final MessageSearchFragment fragment = customFragment != null ? customFragment : new MessageSearchFragment();
            fragment.setArguments(bundle);
            fragment.onSearchEventListener = onSearchEventListener;
            fragment.adapter = adapter;
            fragment.itemClickListener = itemClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            fragment.query = query;
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.clearButtonClickListener = clearButtonClickListener;
            return fragment;
        }
    }
}
