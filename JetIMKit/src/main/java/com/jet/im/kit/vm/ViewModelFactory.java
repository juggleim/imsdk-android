package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.jet.im.interfaces.IConversationManager;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.model.configurations.ChannelConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.query.MessageSearchQuery;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.ThreadMessageListParams;

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
            return (T) new ChannelViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageListParams) params[1] : null, params.length > 2 ? (ChannelConfig) params[2] : UIKitConfig.getGroupChannelConfig());
        } else if (modelClass.isAssignableFrom(ChannelListViewModel.class)) {
            return (T) new ChannelListViewModel(params != null && params.length > 0 ? (IConversationManager) params[0] : null);
        } else if (modelClass.isAssignableFrom(MessageSearchViewModel.class)) {
            return (T) new MessageSearchViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageSearchQuery) params[1] : null);
        } else if (modelClass.isAssignableFrom(ChannelSettingsViewModel.class)) {
            return (T) new ChannelSettingsViewModel((String) Objects.requireNonNull(params)[0]);
        }  else if (modelClass.isAssignableFrom(CreateChannelViewModel.class)) {
            return (T) new CreateChannelViewModel(params != null && params.length > 0 ? (PagedQueryHandler<UserInfo>) params[0] : null);
        } else if (modelClass.isAssignableFrom(InviteUserViewModel.class)) {
            return (T) new InviteUserViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (PagedQueryHandler<UserInfo>) params[1] : null);
        }  else if (modelClass.isAssignableFrom(MessageThreadViewModel.class)) {
            return (T) new MessageThreadViewModel((String) Objects.requireNonNull(params)[0], (BaseMessage) Objects.requireNonNull(params)[1], params.length > 2 ? (ThreadMessageListParams) params[2] : null);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
