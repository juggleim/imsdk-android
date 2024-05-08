package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.MessageSearchAdapter;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.providers.AdapterProviders;

import java.util.List;

/**
 * This class creates and performs a view corresponding the message search result list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class MessageSearchListComponent {
    @NonNull
    private final Params params;
    @Nullable
    private PagerRecyclerView pagerRecyclerView;

    @NonNull
    private MessageSearchAdapter adapter = AdapterProviders.getMessageSearch().provide();

    @Nullable
    private OnItemClickListener<BaseMessage> itemClickListener;

    /**
     * Constructor that is called when a module is created, supplying parameters
     * that can customize a default View.
     *
     * since 3.0.0
     */
    public MessageSearchListComponent() {
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
     * Gets the parameter object on this component
     *
     * @return The data sets of this component.
     * @see MessageListComponent.Params
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Sets the message search result list  adapter to provide child views on demand. The default is {@code new MessageSearchAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends MessageSearchAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        if (this.adapter.getOnItemClickListener() == null) {
            this.adapter.setOnItemClickListener(this::onItemClicked);
        }
        if (!(getRootView() instanceof PagerRecyclerView)) return;
        final PagerRecyclerView listView = (PagerRecyclerView) getRootView();
        this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (fromPosition == 0 || toPosition == 0) {
                    if (listView.findFirstVisibleItemPosition() == 0) {
                        listView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0) {
                    if (listView.findFirstVisibleItemPosition() == 0) {
                        listView.scrollToPosition(0);
                    }
                }
            }
        });
        listView.setAdapter(adapter);
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
        this.pagerRecyclerView = new PagerRecyclerView(context, null, R.attr.sb_component_list);
        this.pagerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.pagerRecyclerView.setHasFixedSize(true);
        this.pagerRecyclerView.setThreshold(5);
        this.pagerRecyclerView.setUseDivider(false);
        setAdapter(adapter);
        return this.pagerRecyclerView;
    }

    /**
     * Register a callback to be invoked when the item of the message search result is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.itemClickListener = listener;
    }

    /**
     * Sets the paged data loader for message search result list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.0.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<BaseMessage>> pagedDataLoader) {
        if (pagerRecyclerView != null) pagerRecyclerView.setPager(pagedDataLoader);
    }

    /**
     * Notifies this component that the message search result is changed.
     *
     * @param messageList The list of message search result to be displayed on this component
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<BaseMessage> messageList) {
        Logger.d("++ ChannelListComponent::notifyDataSetChanged()");
        if (pagerRecyclerView == null) return;
        adapter.setItems(messageList);
    }

    /**
     * Called when the item of the message search result is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (itemClickListener != null) itemClickListener.onItemClick(view, position, message);
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
        protected Params() {
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            return this;
        }
    }
}
