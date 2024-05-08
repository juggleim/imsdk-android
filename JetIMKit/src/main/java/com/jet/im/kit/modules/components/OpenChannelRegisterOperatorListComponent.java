package com.jet.im.kit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.jet.im.kit.activities.adapter.OpenChannelRegisterOperatorListAdapter;
import com.jet.im.kit.providers.AdapterProviders;

/**
 * This class creates and performs a view corresponding the participant list area when registering operators in Sendbird UIKit.
 *
 * since 3.1.0
 */
public class OpenChannelRegisterOperatorListComponent extends SelectUserListComponent<User> {
    @NonNull
    private OpenChannelRegisterOperatorListAdapter adapter = AdapterProviders.getOpenChannelRegisterOperatorList().provide(null);

    /**
     * Returns the participant list adapter when registering operators.
     *
     * @return The adapter applied to this list component
     * since 3.1.0
     */
    @NonNull
    @Override
    protected OpenChannelRegisterOperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the participant list adapter when registering operators to provide child views on demand. The default is {@code new OpenChannelRegisterOperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.1.0
     */
    public <T extends OpenChannelRegisterOperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * since 3.9.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        setAdapter(AdapterProviders.getOpenChannelRegisterOperatorList().provide(channel));
    }
}
