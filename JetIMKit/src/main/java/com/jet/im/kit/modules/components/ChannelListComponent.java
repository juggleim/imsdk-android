package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.im.model.ConversationInfo;
import com.sendbird.android.channel.GroupChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.ChannelListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.fragments.ItemAnimator;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.ChannelListUIParams;
import com.jet.im.kit.model.configurations.ChannelListConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.providers.AdapterProviders;

import java.util.List;

/**
 * This class creates and performs a view corresponding the channel list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ChannelListComponent {
    @Nullable
    private ChannelListAdapter adapter;

    @NonNull
    private final Params params;
    @Nullable
    private PagerRecyclerView pagerRecyclerView;

    @Nullable
    private OnItemClickListener<ConversationInfo> itemClickListener;
    @Nullable
    private OnItemLongClickListener<ConversationInfo> itemLongClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public ChannelListComponent() {
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
        return this.pagerRecyclerView;
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
     * Sets the channel list  adapter to provide child views on demand. The default is {@code new ChannelListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends ChannelListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;

        if (this.adapter.getOnItemClickListener() == null) {
            this.adapter.setOnItemClickListener(this::onItemClicked);
        }
        if (this.adapter.getOnItemLongClickListener() == null) {
            this.adapter.setOnItemLongClickListener(this::onItemLongClicked);
        }
        this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (fromPosition == 0 || toPosition == 0) {
                    if (pagerRecyclerView == null) return;
                    if (pagerRecyclerView.findFirstVisibleItemPosition() == 0) {
                        pagerRecyclerView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    if (pagerRecyclerView == null) return;
                    if (pagerRecyclerView.findFirstVisibleItemPosition() == 0) {
                        pagerRecyclerView.scrollToPosition(0);
                    }
                }
            }
        });
        if (pagerRecyclerView == null) return;
        this.pagerRecyclerView.setAdapter(this.adapter);
    }

    /**
     * Returns the channel list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @Nullable
    public ChannelListAdapter getAdapter() {
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
        this.pagerRecyclerView = createPagerRecyclerView(context);
        this.pagerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.pagerRecyclerView.setHasFixedSize(true);
        this.pagerRecyclerView.setItemAnimator(new ItemAnimator());
        this.pagerRecyclerView.setThreshold(5);
        this.adapter = AdapterProviders.getChannelList().provide(new ChannelListUIParams(
            params.channelListConfig.getEnableTypingIndicator(),
            params.channelListConfig.getEnableMessageReceiptStatus()
        ));
        setAdapter(adapter);
        return pagerRecyclerView;
    }

    /**
     * Register a callback to be invoked when the item of the channel is clicked.
     *
     * @param listener The callback that will run
     * since 3.0.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<ConversationInfo> listener) {
        this.itemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the item of the channel is long-clicked.
     *
     * @param listener The callback that will run
     * since 3.0.0
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<ConversationInfo> listener) {
        this.itemLongClickListener = listener;
    }

    /**
     * Sets the paged data loader for channel list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.0.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<ConversationInfo>> pagedDataLoader) {
        if (pagerRecyclerView != null) pagerRecyclerView.setPager(pagedDataLoader);
    }

    /**
     * Called when the channel list is changed.
     *
     * @param channelList The list of channels to be displayed on this component
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<ConversationInfo> channelList) {
        Logger.d("++ ChannelListComponent::notifyDataSetChanged()");
        adapter.setItems(channelList);
    }

    /**
     * Called when the item of the channel list is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param channel  The channel that the clicked item displays
     * since 3.0.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull ConversationInfo channel) {
        if (itemClickListener != null) itemClickListener.onItemClick(view, position, channel);
    }

    /**
     * Called when the item of the channel list is long-clicked.
     *
     * @param view     The View long-clicked.
     * @param position The position long-clicked.
     * @param channel  The channel that the long-clicked item displays
     * since 3.0.0
     */
    protected void onItemLongClicked(@NonNull View view, int position, @NonNull ConversationInfo channel) {
        if (itemLongClickListener != null)
            itemLongClickListener.onItemLongClick(view, position, channel);
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
        @NonNull
        private ChannelListConfig channelListConfig = UIKitConfig.getGroupChannelListConfig();

        protected Params() {
        }

        /**
         * Returns the channel list config.
         * Use {@code UIKitConfig.groupChannelListConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val channelListComponent = ChannelListComponent()
         * channelListComponent.params.channelListConfig = UIKitConfig.groupChannelListConfig.clone().apply {
         *     this.enableTypingIndicator = true
         * }
         * </pre>
         *
         * @param channelListConfig The channel list config to be applied to this list component
         * since 3.6.0
         */
        public void setChannelListConfig(@NonNull ChannelListConfig channelListConfig) {
            this.channelListConfig = channelListConfig;
        }

        /**
         * Returns the channel list config.
         *
         * @return The channel list config applied to this list component
         * since 3.6.0
         */
        @NonNull
        public ChannelListConfig getChannelListConfig() {
            return channelListConfig;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_CHANNEL_LIST_CONFIG} is mapped to {@link #setChannelListConfig(ChannelListConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_CHANNEL_LIST_CONFIG)) {
                setChannelListConfig(args.getParcelable(StringSet.KEY_CHANNEL_LIST_CONFIG));
            }
            return this;
        }
    }

    @VisibleForTesting
    @NonNull
    PagerRecyclerView createPagerRecyclerView(@NonNull Context context) {
        return new PagerRecyclerView(context, null, R.attr.sb_component_list);
    }
}
