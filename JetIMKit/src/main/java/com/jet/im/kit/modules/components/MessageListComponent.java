package com.jet.im.kit.modules.components;

import android.view.View;

import androidx.annotation.NonNull;

import com.jet.im.kit.activities.adapter.MessageListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.providers.AdapterProviders;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;

/**
 * This class creates and performs a view corresponding the message list area in Sendbird UIKit.
 * <p>
 * since 3.0.0
 */
public class MessageListComponent extends BaseMessageListComponent<MessageListAdapter> {

    /**
     * Constructor
     * <p>
     * since 3.0.0
     */
    public MessageListComponent() {
        super(new Params(), true, true);
    }

    @Override
    public void setAdapter(@NonNull MessageListAdapter adapter) {
        super.setAdapter(adapter);
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return (Params) super.getParams();
    }

    @Override
    public void notifyChannelChanged(@NonNull ConversationInfo channel) {
        if (getAdapter() == null) {
            setAdapter(
                    AdapterProviders.getMessageList().provide(channel, new MessageListUIParams.Builder()
                            .setUseMessageGroupUI(getParams().shouldUseGroupUI())
                            .setChannelConfig(getParams().getChannelConfig())
                            .build())
            );
        }
        super.notifyChannelChanged(channel);
    }

    @Override
    protected void onListItemClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull Message message) {
        final Message.MessageState status = message.getState();
        if (status == Message.MessageState.SENDING) return;

        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                onMessageClicked(view, position, message);
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                onMessageProfileClicked(view, position, message);
                break;
        }
    }

    @Override
    protected void onListItemLongClicked(@NonNull View view, @NonNull String identifier, int position, @NonNull Message message) {
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

    public static class Params extends BaseMessageListComponent.Params {
    }
}
