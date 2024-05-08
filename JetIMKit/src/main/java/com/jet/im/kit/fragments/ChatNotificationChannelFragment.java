package com.jet.im.kit.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.params.MessageListParams;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler;
import com.jet.im.kit.internal.extensions.NotificationExtensionsKt;
import com.jet.im.kit.internal.ui.notifications.ChatNotificationChannelModule;
import com.jet.im.kit.internal.ui.notifications.ChatNotificationHeaderComponent;
import com.jet.im.kit.internal.ui.notifications.ChatNotificationListComponent;
import com.jet.im.kit.internal.ui.notifications.NotificationStatusComponent;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.Action;
import com.jet.im.kit.model.ReadyStatus;
import com.jet.im.kit.utils.IntentUtils;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.vm.ChatNotificationChannelViewModel;
import com.jet.im.kit.widgets.StatusFrameView;

public class ChatNotificationChannelFragment extends BaseModuleFragment<ChatNotificationChannelModule, ChatNotificationChannelViewModel> {

    @Nullable
    private OnNotificationTemplateActionHandler actionHandler;
    @Nullable
    private MessageListParams params;

    @NonNull
    @Override
    protected ChatNotificationChannelModule onCreateModule(@NonNull Bundle args) {
        return NotificationExtensionsKt.createChatNotificationChannelModule(this, args);
    }

    @Override
    protected void onConfigureParams(@NonNull ChatNotificationChannelModule module, @NonNull Bundle args) {
    }

    @NonNull
    @Override
    protected ChatNotificationChannelViewModel onCreateViewModel() {
        final ChatNotificationChannelViewModel viewModel = NotificationExtensionsKt.createChatNotificationChannelViewModel(this, getChannelUrl(), params);
        getLifecycle().addObserver(viewModel);
        return viewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shouldDismissLoadingDialog();
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChatNotificationChannelModule module, @NonNull ChatNotificationChannelViewModel viewModel) {
        Logger.d(">> ChatNotificationChannelFragment::onBeforeReady status=%s", status);
        module.getNotificationListComponent().setPagedDataLoader(viewModel);
        final GroupChannel channel = viewModel.getChannel();
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindNotificationListComponent(module.getNotificationListComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ChatNotificationChannelModule module, @NonNull ChatNotificationChannelViewModel viewModel) {
        Logger.d(">> ChatNotificationChannelFragment::onReady status=%s", status);
        shouldDismissLoadingDialog();
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }

        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getNotificationListComponent().notifyChannelChanged(channel);
        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), channelUrl -> shouldActivityFinish());
        loadInitial();
    }

    /**
     * Called to bind events to the ChatNotificationHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChatNotificationChannelModule, ChatNotificationChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param channel The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.5.0
     */
    protected void onBindHeaderComponent(@NonNull ChatNotificationHeaderComponent headerComponent, @NonNull ChatNotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChatNotificationChannelFragment::onChatNotificationHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(v -> shouldActivityFinish());
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), headerComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the ChatNotificationListComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChatNotificationChannelModule, ChatNotificationChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param listComponent The component to which the event will be bound
     * @param viewModel A view model that provides the data needed for the fragment
     * @param channel The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.5.0
     */
    protected void onBindNotificationListComponent(@NonNull ChatNotificationListComponent listComponent, @NonNull ChatNotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChatNotificationChannelFragment::onBindChatNotificationListComponent()");
        listComponent.setOnMessageTemplateActionHandler(actionHandler != null ? actionHandler : this::handleAction);
        listComponent.setOnTooltipClickListener(v -> listComponent.scrollToFirst());

        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), listComponent::notifyChannelChanged);
        viewModel.getNotificationList().observeAlways(getViewLifecycleOwner(), notificationData -> {
            Logger.d("++ notification data = %s", notificationData);
            if (!isFragmentAlive() || channel == null) return;

            final String eventSource = notificationData.getTraceName();
            listComponent.notifyDataSetChanged(notificationData.getMessages(), channel, notifications -> {
                if (!isFragmentAlive() || eventSource == null) return;

                switch (eventSource) {
                    case StringSet.EVENT_MESSAGE_RECEIVED:
                        listComponent.notifyNewNotificationReceived();
                        break;
                    case StringSet.ACTION_INIT_FROM_REMOTE:
                    case StringSet.MESSAGE_CHANGELOG:
                    case StringSet.MESSAGE_FILL:
                        listComponent.notifyMessagesFilled();
                        break;
                    default:
                        break;
                }
            });
        });
    }

    /**
     * Called to bind events to the NotificationStatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChatNotificationChannelModule, ChatNotificationChannelViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.5.0
     */
    protected void onBindStatusComponent(@NonNull NotificationStatusComponent statusComponent, @NonNull ChatNotificationChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChatNotificationChannelFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    private void handleAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        switch (action.type) {
            case StringSet.web:
                handleWebAction(view, action, message);
                break;
            case StringSet.custom:
                handleCustomAction(view, action, message);
                break;
            default:
                break;
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * since 3.5.0
     */
    protected void handleWebAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> ChatNotificationChannelFragment::handleWebAction() action=%s", action);
        final Intent intent = IntentUtils.getWebViewerIntent(action.data);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Logger.e(e);
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * since 3.5.0
     */
    protected void handleCustomAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> ChatNotificationChannelFragment::handleCustomAction() action=%s", action);
        try {
            final String data = action.data;
            if (TextUtils.isNotEmpty(data)) {
                final Uri uri = Uri.parse(data);
                Logger.d("++ uri = %s", uri);
                final String scheme = uri.getScheme();
                Logger.d("++ scheme=%s", scheme);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                boolean hasIntent = IntentUtils.hasIntent(requireContext(), intent);
                if (!hasIntent) {
                    final String alterData = action.alterData;
                    if (alterData != null) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alterData));
                    }
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            Logger.w(e);
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.5.0
     */
    protected boolean shouldShowLoadingDialog() {
        return getModule().shouldShowLoadingDialog();
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 3.5.0
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    private synchronized void loadInitial() {
        getViewModel().loadInitial(Long.MAX_VALUE);
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * since 3.5.0
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
        private ChatNotificationChannelFragment customFragment;
        @Nullable
        private OnNotificationTemplateActionHandler actionHandler;
        @Nullable
        private MessageListParams params;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * since 3.5.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * since 3.5.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * since 3.5.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            if (customThemeResId != 0) {
                bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets the click listener on the message template view clicked.
         * Sets the click listener when the view component that has {@link Action} is clicked
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.0
         */
        @NonNull
        public Builder setOnMessageTemplateActionHandler(@NonNull OnNotificationTemplateActionHandler handler) {
            this.actionHandler = handler;
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.0
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
         * since 3.5.2
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the custom fragment. It must inherit {@link ChatNotificationChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.0
         */
        @NonNull
        public <T extends ChatNotificationChannelFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the notification list params for this channel.
         * The reverse property in the MessageListParams are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param params The MessageListParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.0
         */
        @NonNull
        public Builder setNotificationListParams(@NonNull MessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.14.0
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
         * since 3.14.0
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
         * since 3.14.0
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
         * since 3.14.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Creates an {@link ChatNotificationChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChatNotificationChannelFragment} applied to the {@link Bundle}.
         * since 3.5.0
         */
        @NonNull
        public ChatNotificationChannelFragment build() {
            ChatNotificationChannelFragment fragment = customFragment != null ? customFragment : new ChatNotificationChannelFragment();
            fragment.setArguments(bundle);
            fragment.params = params;
            fragment.actionHandler = actionHandler;
            return fragment;
        }
    }
}
