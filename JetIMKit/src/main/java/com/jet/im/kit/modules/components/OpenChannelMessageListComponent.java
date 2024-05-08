package com.jet.im.kit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.SendingStatus;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.OpenChannelMessageListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.fragments.ItemAnimator;
import com.jet.im.kit.interfaces.OnConsumableClickListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemEventListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.ui.widgets.MessageRecyclerView;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.MessageUIConfig;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.model.configurations.OpenChannelConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.providers.AdapterProviders;
import com.jet.im.kit.utils.MessageUtils;

import java.util.List;

/**
 * This class creates and performs a view corresponding the message list area for {@code OpenChannel} in Sendbird UIKit.
 *
 * since 3.0.0
 */
@SuppressWarnings("unused")
public class OpenChannelMessageListComponent {
    @NonNull
    private final Params params;
    @Nullable
    private MessageRecyclerView messageRecyclerView;
    @Nullable
    private OpenChannelMessageListAdapter adapter;
    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnPagedDataLoader<List<BaseMessage>> pagedDataLoader;
    @Nullable
    @Deprecated
    private View.OnClickListener scrollBottomButtonClickListener;
    @Nullable
    private OnConsumableClickListener scrollFirstButtonClickListener;
    @Nullable
    private OnItemEventListener<BaseMessage> messageInsertedListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public OpenChannelMessageListComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.messageRecyclerView;
    }

    /**
     * Returns the recycler view used in the list component by default.
     *
     * @return {@link RecyclerView} used in this component
     * since 3.0.0
     */
    @Nullable
    public RecyclerView getRecyclerView() {
        return messageRecyclerView != null ? messageRecyclerView.getRecyclerView() : null;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Sets the message list adapter for {@code OpenChannel}. The default is {@code new OpenChannelMessageListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends OpenChannelMessageListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        if (this.adapter.getMessageUIConfig() == null) {
            this.adapter.setMessageUIConfig(params.messageUIConfig);
        }
        if (this.adapter.getOnListItemClickListener() == null) {
            this.adapter.setOnListItemClickListener(this::onListItemClicked);
        }
        if (this.adapter.getOnListItemLongClickListener() == null) {
            this.adapter.setOnListItemLongClickListener(this::onListItemLongClicked);
        }

        if (messageRecyclerView == null) return;
        this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    BaseMessage message = adapter.getItem(positionStart);
                    LinearLayoutManager layoutManager = messageRecyclerView.getRecyclerView().getLayoutManager();
                    if ((MessageUtils.isMine(message) ||
                        (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0))) {
                        onMessageInserted(message);
                    }
                }
            }
        });
        messageRecyclerView.getRecyclerView().setAdapter(adapter);
    }

    /**
     * Returns the message list adapter for {@code OpenChannel}.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @Nullable
    public OpenChannelMessageListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        this.messageRecyclerView = new MessageRecyclerView(context, null, R.attr.sb_component_list);

        final PagerRecyclerView recyclerView = this.messageRecyclerView.getRecyclerView();
        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(false);
        recyclerView.setThreshold(5);
        recyclerView.useReverseData();
        recyclerView.setItemAnimator(new ItemAnimator());

        recyclerView.setOnScrollEndDetectListener(direction -> onScrollEndReaches(direction, this.messageRecyclerView));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!isScrollOnTheFirst(recyclerView)) {
                    messageRecyclerView.showScrollFirstButton();
                }
            }
        });
        this.messageRecyclerView.setOnScrollFirstButtonClickListener(this::onScrollFirstButtonClicked);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        if (adapter == null) {
            this.adapter = AdapterProviders.getOpenChannelMessageList().provide(
                new MessageListUIParams.Builder()
                    .setUseMessageGroupUI(params.useGroupUI)
                    .setOpenChannelConfig(params.getOpenChannelConfig())
                    .build()
            );
        }
        setAdapter(adapter);
        return this.messageRecyclerView;
    }

    /********************************************************************************************
     *                                      PRIVATE AREA
     *********************************************************************************************/
    private boolean hasNextMessages() {
        return pagedDataLoader != null && pagedDataLoader.hasNext();
    }

    private boolean isScrollOnTheFirst(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            return linearLayoutManager.findFirstVisibleItemPosition() <= 0;
        }
        return false;
    }

    private void onScrollEndReaches(@NonNull PagerRecyclerView.ScrollDirection direction, @NonNull MessageRecyclerView messageListView) {
        final PagerRecyclerView.ScrollDirection endDirection = PagerRecyclerView.ScrollDirection.Bottom;
        if (!hasNextMessages() && direction == endDirection) {
            messageListView.hideScrollFirstButton();
        }
    }

    /**
     * Register a callback to be invoked when the button to scroll to the bottom is clicked.
     *
     * @param scrollBottomButtonClickListener The callback that will run
     * since 3.0.0
     * @deprecated 3.2.2
     * This method is no longer acceptable to invoke event.
     * <p> Use {@link #setOnScrollFirstButtonClickListener(OnConsumableClickListener)} instead.
     */
    @Deprecated
    public void setOnScrollBottomButtonClickListener(@Nullable View.OnClickListener scrollBottomButtonClickListener) {
        this.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the button to scroll to the first position is clicked.
     *
     * @param scrollFirstButtonClickListener The callback that will run
     * since 3.2.2
     */
    public void setOnScrollFirstButtonClickListener(@Nullable OnConsumableClickListener scrollFirstButtonClickListener) {
        this.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
    }

    /**
     * Sets the paged data loader for open channel message list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.0.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<BaseMessage>> pagedDataLoader) {
        this.pagedDataLoader = pagedDataLoader;
        if (messageRecyclerView != null)
            messageRecyclerView.getRecyclerView().setPager(pagedDataLoader);
    }

    /**
     * Register a callback to be invoked when the message is clicked.
     *
     * @param messageClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageClickListener(@Nullable OnItemClickListener<BaseMessage> messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is clicked.
     *
     * @param messageProfileClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageProfileClickListener(@Nullable OnItemClickListener<BaseMessage> messageProfileClickListener) {
        this.messageProfileClickListener = messageProfileClickListener;
    }

    /**
     * Register a callback to be invoked when the message is long-clicked.
     *
     * @param messageLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the message is long-clicked.
     *
     * @param messageProfileLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageProfileLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
        this.messageProfileLongClickListener = messageProfileLongClickListener;
    }

    /**
     * Register a callback to be invoked when the message is inserted.
     *
     * @param messageInsertedListener The callback that will run
     * since 3.0.0
     */
    public void setOnMessageInsertedListener(@Nullable OnItemEventListener<BaseMessage> messageInsertedListener) {
        this.messageInsertedListener = messageInsertedListener;
    }

    private void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return;

        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                break;
        }
    }

    private void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageLongClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileLongClicked(view, position, message);
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                break;
        }
    }

    /**
     * Scrolls to the bottom of the message list.
     *
     * since 3.0.0
     * @deprecated 3.2.2
     * <p> Use {@link #scrollToFirst()} instead.
     */
    @Deprecated
    public void scrollToBottom() {
        scrollToFirst();
    }

    /**
     * Scrolls to the first position of the recycler view.
     *
     * since 3.2.2
     */
    public void scrollToFirst() {
        if (messageRecyclerView == null) return;
        messageRecyclerView.getRecyclerView().stopScroll();
        messageRecyclerView.getRecyclerView().scrollToPosition(0);
        onScrollEndReaches(PagerRecyclerView.ScrollDirection.Bottom, messageRecyclerView);
    }

    /**
     * Notifies this component that the data needed to draw the message list has changed.
     *
     * @param messageList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList, @NonNull OpenChannel channel, @Nullable OnMessageListUpdateHandler callback) {
        if (messageRecyclerView == null) return;
        final OpenChannelMessageListAdapter adapter = this.adapter;
        if (adapter != null) {
            adapter.setItems(channel, messageList, callback);
        }
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        if (messageRecyclerView == null) return;
        final boolean isFrozen = channel.isFrozen();
        messageRecyclerView.getBannerView().setVisibility(isFrozen ? View.VISIBLE : View.GONE);
        if (isFrozen) {
            messageRecyclerView.setBannerText(messageRecyclerView.getContext().getString(R.string.sb_text_information_channel_frozen));
        }
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageClickListener != null) messageClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the profile view of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (!params.useUserProfile) return;
        if (messageProfileClickListener != null)
            messageProfileClickListener.onItemClick(view, position, message);
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageLongClickListener != null)
            messageLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the profile view of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onMessageProfileLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileLongClickListener != null)
            messageProfileLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the button to scroll to the bottom is clicked.
     *
     * @param view The view that was clicked
     * since 3.0.0
     * @deprecated 3.2.2
     * This method is no longer acceptable to invoke event.
     * <p> Use {@link #onScrollFirstButtonClicked(View)} instead.
     */
    @Deprecated
    protected void onScrollBottomButtonClicked(@NonNull View view) {
        if (scrollBottomButtonClickListener != null) scrollBottomButtonClickListener.onClick(view);
    }

    /**
     * Called when the button to scroll to the first position is clicked.
     *
     * @param view The view that was clicked
     * since 3.2.2
     */
    protected boolean onScrollFirstButtonClicked(@NonNull View view) {
        boolean handled = scrollBottomButtonClickListener != null;

        onScrollBottomButtonClicked(view);
        if (scrollFirstButtonClickListener != null) {
            handled = scrollFirstButtonClickListener.onClick(view);
        }
        return handled;
    }

    /**
     * Called when the message is inserted.
     *
     * @param message The message that has been inserted
     * since 3.0.0
     */
    protected void onMessageInserted(@NonNull BaseMessage message) {
        if (messageInsertedListener != null) messageInsertedListener.onItemEvent(message);
    }


    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        private boolean useGroupUI = true;
        private boolean useUserProfile = UIKitConfig.getCommon().getEnableUsingDefaultUserProfile();
        @NonNull
        private final MessageUIConfig messageUIConfig;
        @NonNull
        private OpenChannelConfig openChannelConfig = UIKitConfig.getOpenChannelConfig();

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
            this.messageUIConfig = new MessageUIConfig();
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseMessageGroupUI(boolean useMessageGroupUI) {
            this.useGroupUI = useMessageGroupUI;
        }

        /**
         * Sets whether the user profile is shown when the profile of message is clicked.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseUserProfile(boolean useUserProfile) {
            this.useUserProfile = useUserProfile;
        }

        /**
         * Sets the UI configuration of searched text.
         *
         * @param configSentFromMe       the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers   the UI configuration of edited text mark in the message that was sent from others.
         * since 3.0.0
         */
        public void setEditedTextMarkUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyEditedTextMarkUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherEditedTextMarkUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of message text.
         *
         * @param configSentFromMe       the UI configuration of the message text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message text that was sent from others.
         * since 3.1.1
         */
        public void setMessageTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMyMessageTextUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherMessageTextUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of message sentAt text.
         *
         * @param configSentFromMe       the UI configuration of the message sentAt text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message sentAt text that was sent from others.
         * since 3.1.1
         */
        public void setSentAtTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null) this.messageUIConfig.getMySentAtTextUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherSentAtTextUIConfig().apply(configSentFromOthers);
        }

        /**
         * Sets the UI configuration of sender nickname text.
         *
         * @param configSentFromMe       the UI configuration of the sender nickname text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the sender nickname text that was sent from others.
         * @param configSentFromOperator the UI configuration of the sender nickname text that was sent from operator.
         * since 3.1.1
         */
        public void setNicknameTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers, @Nullable TextUIConfig configSentFromOperator) {
            if (configSentFromMe != null) this.messageUIConfig.getMyNicknameTextUIConfig().apply(configSentFromMe);
            if (configSentFromOthers != null)
                this.messageUIConfig.getOtherNicknameTextUIConfig().apply(configSentFromOthers);
            if (configSentFromOperator != null)
                this.messageUIConfig.getOperatorNicknameTextUIConfig().apply(configSentFromOperator);
        }

        /**
         * Sets the UI configuration of message background drawable.
         *
         * @param drawableSentFromMe     the UI configuration of the message background that was sent from me.
         * @param drawableSentFromOthers the UI configuration of the message background that was sent from others.
         * since 3.1.1
         */
        public void setMessageBackground(@Nullable Drawable drawableSentFromMe, @Nullable Drawable drawableSentFromOthers) {
            if (drawableSentFromMe != null) this.messageUIConfig.setMyMessageBackground(drawableSentFromMe);
            if (drawableSentFromOthers != null) this.messageUIConfig.setOtherMessageBackground(drawableSentFromOthers);
        }

        /**
         * Sets the UI configuration of ogtag message background drawable.
         *
         * @param drawableSentFromMe     the UI configuration of the ogtag message background drawable that was sent from me.
         * @param drawableSentFromOthers the UI configuration of the ogtag message background drawable that was sent from others.
         * since 3.1.1
         */
        public void setOgtagBackground(@Nullable Drawable drawableSentFromMe, @Nullable Drawable drawableSentFromOthers) {
            if (drawableSentFromMe != null) this.messageUIConfig.setMyOgtagBackground(drawableSentFromMe);
            if (drawableSentFromOthers != null) this.messageUIConfig.setOtherOgtagBackground(drawableSentFromOthers);
        }

        /**
         * Sets the UI configuration of the linked text color in the message text.
         *
         * @param color the UI configuration of the linked text color.
         * since 3.1.1
         */
        public void setLinkedTextColor(@NonNull ColorStateList color) {
            this.messageUIConfig.setLinkedTextColor(color);
        }

        /**
         * Sets {@link OpenChannelConfig} that will be used in this component.
         * Use {@code UIKitConfig.openChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val openChannelMessageListComponent = OpenChannelMessageListComponent()
         * openChannelMessageListComponent.params.openChannelConfig = UIKitConfig.openChannelConfig.clone().apply {
         *     this.enableOgTag = false
         * }
         * </pre>
         *
         * @param openChannelConfig Channel config to be used in this component.
         * since 3.6.0
         */
        public void setOpenChannelConfig(@NonNull OpenChannelConfig openChannelConfig) {
            this.openChannelConfig = openChannelConfig;
        }

        /**
         * Returns whether the user profile uses when the profile of message is clicked.
         *
         * @return <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseUserProfile() {
            return useUserProfile;
        }

        /**
         * Returns whether the message group UI is used.
         *
         * @return <code>true</code> if the message group UI is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseGroupUI() {
            return useGroupUI;
        }

        /**
         * Returns {@link OpenChannelConfig} that will be used in this component.
         *
         * @return OpenChannel config to be used in this component.
         * since 3.6.0
         */
        @NonNull
        public OpenChannelConfig getOpenChannelConfig() {
            return openChannelConfig;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_USER_PROFILE} is mapped to {@link #setUseUserProfile(boolean)}
         * {@code KEY_USE_MESSAGE_GROUP_UI} is mapped to {@link #setUseMessageGroupUI(boolean)}
         * {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME} and {@code KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME} and {@code KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setMessageTextUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME} and {@code KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS} are mapped to {@link #setSentAtTextUIConfig(TextUIConfig, TextUIConfig)}
         * {@code KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_ME}, {@code KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS} and {@code KEY_OPERATOR_TEXT_UI_CONFIG} are mapped to {@link #setNicknameTextUIConfig(TextUIConfig, TextUIConfig, TextUIConfig)}
         * {@code KEY_MESSAGE_BACKGROUND_SENT_FROM_ME} and {@code KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS} are mapped to {@link #setMessageBackground(Drawable, Drawable)}
         * {@code KEY_OGTAG_BACKGROUND_SENT_FROM_ME} and {@code KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS} are mapped to {@link #setOgtagBackground(Drawable, Drawable)}
         * {@code KEY_LINKED_TEXT_COLOR} is mapped to {@link #setLinkedTextColor(ColorStateList)}
         * {@code KEY_OPEN_CHANNEL_CONFIG} is mapped to {@link #setOpenChannelConfig(OpenChannelConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_USE_USER_PROFILE)) {
                setUseUserProfile(args.getBoolean(StringSet.KEY_USE_USER_PROFILE));
            }
            if (args.containsKey(StringSet.KEY_USE_MESSAGE_GROUP_UI)) {
                setUseMessageGroupUI(args.getBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI));
            }
            setEditedTextMarkUIConfig(args.getParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS));
            setMessageTextUIConfig(args.getParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS));
            setSentAtTextUIConfig(args.getParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME), args.getParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS));
            setNicknameTextUIConfig(args.getParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_ME),
                args.getParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS),
                args.getParcelable(StringSet.KEY_OPERATOR_TEXT_UI_CONFIG));

            Drawable messageBackgroundSentFromMe = null;
            Drawable messageBackgroundSentFromOthers = null;
            Drawable ogtagBackgroundSentFromMe = null;
            Drawable ogtagBackgroundSentFromOthers = null;
            if (args.containsKey(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME)) {
                messageBackgroundSentFromMe = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME));
            }
            if (args.containsKey(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS)) {
                messageBackgroundSentFromOthers = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS));
            }
            if (args.containsKey(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME)) {
                ogtagBackgroundSentFromMe = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME));
            }
            if (args.containsKey(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS)) {
                ogtagBackgroundSentFromOthers = AppCompatResources.getDrawable(context, args.getInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS));
            }
            setMessageBackground(messageBackgroundSentFromMe, messageBackgroundSentFromOthers);
            setOgtagBackground(ogtagBackgroundSentFromMe, ogtagBackgroundSentFromOthers);

            if (args.containsKey(StringSet.KEY_LINKED_TEXT_COLOR)) {
                final ColorStateList linkedTextColor = AppCompatResources.getColorStateList(context, args.getInt(StringSet.KEY_LINKED_TEXT_COLOR));
                if (linkedTextColor != null) setLinkedTextColor(linkedTextColor);
            }

            if (args.containsKey(StringSet.KEY_OPEN_CHANNEL_CONFIG)) {
                setOpenChannelConfig(args.getParcelable(StringSet.KEY_OPEN_CHANNEL_CONFIG));
            }
            return this;
        }
    }
}
