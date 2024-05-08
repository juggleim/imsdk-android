package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sendbird.android.channel.OpenChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.adapter.OpenChannelListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.fragments.ItemAnimator;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.providers.AdapterProviders;

import java.util.List;

/**
 * This class creates and performs a view corresponding the open channel list area in Sendbird UIKit.
 *
 * since 3.2.0
 */
public class OpenChannelListComponent {
    @NonNull
    private OpenChannelListAdapter adapter = AdapterProviders.getOpenChannelList().provide();
    @Nullable
    private PagerRecyclerView recyclerview;
    @Nullable
    private SwipeRefreshLayout refreshLayout;
    @Nullable
    private OnItemClickListener<OpenChannel> itemClickListener;
    @Nullable
    private OnItemLongClickListener<OpenChannel> itemLongClickListener;
    @Nullable
    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    @NonNull
    private final Params params;

    public OpenChannelListComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.2.0
     */
    @Nullable
    public View getRootView() {
        return recyclerview;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.2.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Sets the open channel list adapter to provide child views on demand. The default is {@code new OpenChannelListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.2.0
     */
    public <T extends OpenChannelListAdapter> void setAdapter(@NonNull T adapter) {
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
                    if (recyclerview == null) return;
                    if (recyclerview.findFirstVisibleItemPosition() == 0) {
                        recyclerview.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    if (recyclerview == null) return;
                    if (recyclerview.findFirstVisibleItemPosition() == 0) {
                        recyclerview.scrollToPosition(0);
                    }
                }
            }
        });
        if (recyclerview == null) return;
        this.recyclerview.setAdapter(this.adapter);
    }

    /**
     * Returns the channel list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.2.0
     */
    @NonNull
    public OpenChannelListAdapter getAdapter() {
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
     * since 3.2.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);

        this.recyclerview = new PagerRecyclerView(context, null, R.attr.sb_component_list);
        this.recyclerview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
        this.recyclerview.setHasFixedSize(true);
        this.recyclerview.setItemAnimator(new ItemAnimator());
        this.recyclerview.setThreshold(5);
        if (params.useRefreshLayout) {
            this.refreshLayout = new SwipeRefreshLayout(context);
            this.refreshLayout.setColorSchemeResources(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId());
            this.refreshLayout.setOnRefreshListener(this::onRefresh);
            this.refreshLayout.addView(this.recyclerview);
        }
        setAdapter(adapter);
        return this.refreshLayout != null ? this.refreshLayout : this.recyclerview;
    }

    /**
     * Register a callback to be invoked when the item of the channel is clicked.
     *
     * @param listener The callback that will run
     * since 3.2.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<OpenChannel> listener) {
        this.itemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when the item of the channel is long-clicked.
     *
     * @param listener The callback that will run
     * since 3.2.0
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<OpenChannel> listener) {
        this.itemLongClickListener = listener;
    }

    /**
     * Register a callback to be invoked when a refresh is triggered via the swipe gesture.
     *
     * @param listener The callback that will run
     * since 3.2.0
     */
    public void setOnRefreshListener(@Nullable SwipeRefreshLayout.OnRefreshListener listener) {
        this.refreshListener = listener;
    }

    /**
     * Sets the paged data loader for open channel list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.2.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<OpenChannel>> pagedDataLoader) {
        if (recyclerview != null) recyclerview.setPager(pagedDataLoader);
    }

    /**
     * Called when the channel list is changed.
     *
     * @param channelList The list of channels to be displayed on this component
     * since 3.2.0
     */
    public void notifyDataSetChanged(@NonNull List<OpenChannel> channelList) {
        Logger.d("++ OpenChannelListComponent::notifyDataSetChanged()");
        adapter.setItems(channelList);
    }

    /**
     * For stopping the refreshing progressbar, this has to be called when the refresh is finished.
     *
     * since 3.2.0
     */
    public void notifyRefreshingFinished() {
        Logger.d("++ OpenChannelListComponent::notifyRefreshingFinished()");
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
        if (recyclerview != null) {
            recyclerview.scrollToPosition(0);
        }
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
        if (itemClickListener != null) itemClickListener.onItemClick(view, position, channel);
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
        if (itemLongClickListener != null) itemLongClickListener.onItemLongClick(view, position, channel);
    }

    /**
     * Called when the channel list is pulled to refresh.
     *
     * since 3.2.0
     */
    protected void onRefresh() {
        if (refreshListener != null) refreshListener.onRefresh();
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.2.0
     */
    public static class Params {
        private boolean useRefreshLayout = true;

        /**
         * Constructor
         *
         * since 3.2.0
         */
        protected Params() {
        }

        /**
         * Sets whether the refresh layout is used.
         *
         * @param useRefreshLayout <code>true</code> if the refresh layout is used, <code>false</code> otherwise
         * since 3.2.0
         */
        public void setUseRefreshLayout(boolean useRefreshLayout) {
            this.useRefreshLayout = useRefreshLayout;
        }

        /**
         * Returns whether the right button of the header is used.
         *
         * @return <code>true</code> if the right button of the header is used, <code>false</code> otherwise
         * since 3.2.0
         */
        public boolean shouldUseRefreshLayout() {
            return useRefreshLayout;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_REFRESH_LAYOUT} is mapped to {@link #setUseRefreshLayout(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.2.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_USE_REFRESH_LAYOUT)) {
                setUseRefreshLayout(args.getBoolean(StringSet.KEY_USE_REFRESH_LAYOUT));
            }
            return this;
        }
    }
}
