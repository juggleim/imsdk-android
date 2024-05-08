package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.params.MessageListParams;

public class NotificationViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    @NonNull
    private final String channelUrl;

    @Nullable
    private MessageListParams params = null;

    public NotificationViewModelFactory(@NonNull String channelUrl) {
        this(channelUrl, null);
    }

    public NotificationViewModelFactory(@NonNull String channelUrl, @Nullable MessageListParams params) {
        this.channelUrl = channelUrl;
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FeedNotificationChannelViewModel.class)) {
            return (T) new FeedNotificationChannelViewModel(channelUrl, params);
        } else if (modelClass.isAssignableFrom(ChatNotificationChannelViewModel.class)) {
            return (T) new ChatNotificationChannelViewModel(channelUrl, params);
        } else {
            return super.create(modelClass);
        }
    }
}
