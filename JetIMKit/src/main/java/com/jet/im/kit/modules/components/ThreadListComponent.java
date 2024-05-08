package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.SendingStatus;
import com.jet.im.kit.activities.adapter.ThreadListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.widgets.MessageRecyclerView;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.providers.AdapterProviders;

/**
 * This class creates and performs a view corresponding the thread list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ThreadListComponent extends BaseMessageListComponent<ThreadListAdapter> {
    @Nullable
    private OnItemClickListener<BaseMessage> parentMessageMenuClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public ThreadListComponent() {
        super(new Params(), false, false);
        getParams().setInitialStartingPoint(0L);
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.3.0
     */
    @NonNull
    public Params getParams() {
        return (Params) super.getParams();
    }

    private boolean isScrollable() {
        if (messageRecyclerView == null) return false;
        return messageRecyclerView.getRecyclerView().isScrollable();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        final View view = super.onCreateView(context, inflater, parent, args);
        if (view instanceof MessageRecyclerView) {
            final MessageRecyclerView messageRecyclerView = (MessageRecyclerView) view;
            final PagerRecyclerView recyclerView = messageRecyclerView.getRecyclerView();
            recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (oldBottom == 0 || oldBottom == bottom) return;
                // if the layout size is changed it should measure again. (e.g: when the keyboard is shown or hidden)
                messageRecyclerView.getRecyclerView().setStackFromEnd(!isScrollable());
                Logger.i("++ get stack from end=%s, oldBottom=%s, bottom=%s", messageRecyclerView.getRecyclerView().getStackFromEnd(), oldBottom, bottom);
                if (messageRecyclerView.getRecyclerView().getStackFromEnd()) {
                    recyclerView.scrollBy(0, oldBottom - bottom);
                }
            });

            recyclerView.setOnLayoutCompleteListener(state -> {
                if (state.getItemCount() <= 0) return;
                Logger.d("++ onLayoutComplete isScrollable=%s, state=%s", isScrollable(), state);
                messageRecyclerView.getRecyclerView().setStackFromEnd(!isScrollable());
            });
        }
        if (getAdapter() == null) {
            final MessageListUIParams messageListUIParams = new MessageListUIParams.Builder()
                    .setUseMessageGroupUI(getParams().shouldUseGroupUI())
                    .setUseMessageReceipt(false)
                    .build();
            setAdapter(
                AdapterProviders.getThreadList().provide(messageListUIParams)
            );
        }
        return view;
    }

    @Override
    void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
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
            case StringSet.ParentMessageMenu:
                // ClickableViewType.ParentMessageMenu
                onParentMessageMenuClicked(view, position, message);
        }
    }

    @Override
    void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull BaseMessage message) {
        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageLongClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileLongClicked(view, position, message);
                break;
        }
    }

    /**
     * Register a callback to be invoked when the parent message menu is clicked.
     *
     * @param parentMessageMenuClickListener The callback that will run
     * since 3.3.0
     */
    public void setOnParentMessageMenuClickListener(@Nullable OnItemClickListener<BaseMessage> parentMessageMenuClickListener) {
        this.parentMessageMenuClickListener = parentMessageMenuClickListener;
    }

    /**
     * Called when the parent message menu is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The parent message clicked
     * since 3.3.0
     */
    protected void onParentMessageMenuClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (parentMessageMenuClickListener != null)
            parentMessageMenuClickListener.onItemClick(view, position, message);
    }

    /**
     * Returns the current createdAt of the view item which is shown in the middle of list
     *
     * @return 3.3.0
     */
    public long getCurrentViewPoint() {
        if (messageRecyclerView == null || getAdapter() == null || getAdapter().getItemCount() <= 0) return 0L;
        int first = messageRecyclerView.getRecyclerView().findFirstVisibleItemPosition();
        int last = messageRecyclerView.getRecyclerView().findLastVisibleItemPosition();
        int currentPosition = (first + last) / 2;
        final BaseMessage item = getAdapter().getItem(currentPosition);
        Logger.d("++ getCurrentViewPoint position : %s, message=%s", currentPosition, item.getMessage());
        return item.getCreatedAt();
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.3.0
     */
    public static class Params extends BaseMessageListComponent.Params {}
}
