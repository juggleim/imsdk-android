package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.juggle.im.interfaces.IConversationManager;
import com.jet.im.kit.model.configurations.ChannelConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.juggle.im.model.Conversation;
import com.sendbird.android.params.MessageListParams;

import java.util.Objects;

public class ViewModelFactory implements ViewModelProvider.Factory {
    @Nullable
    private final Object[] params;

    public ViewModelFactory() {
        this.params = null;
    }

    public ViewModelFactory(@Nullable Object... params) {
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModel.class)) {
            return (T) new ChannelViewModel((Conversation) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageListParams) params[1] : null, params.length > 2 ? (ChannelConfig) params[2] : UIKitConfig.getGroupChannelConfig());
        } else if (modelClass.isAssignableFrom(ChannelListViewModel.class)) {
            return (T) new ChannelListViewModel(params != null && params.length > 0 ? (IConversationManager) params[0] : null);
        }else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
