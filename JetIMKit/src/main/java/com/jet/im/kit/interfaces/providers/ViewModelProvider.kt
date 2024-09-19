package com.jet.im.kit.interfaces.providers

import androidx.lifecycle.ViewModelStoreOwner
import com.juggle.im.interfaces.IConversationManager
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.providers.ModuleProviders
import com.jet.im.kit.vm.ChannelListViewModel
import com.jet.im.kit.vm.ChannelViewModel
import com.juggle.im.model.Conversation
import com.juggle.im.model.ConversationInfo
import com.sendbird.android.params.MessageListParams

/**
 * Interface definition to be invoked when ChannelListViewModel is created.
 * @see [ModuleProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListViewModelProvider {
    /**
     * Returns the ChannelListViewModel.
     *
     * @return The [ChannelListViewModel].
     * @since 3.9.0
     */
    fun provide(owner: ViewModelStoreOwner, query: IConversationManager?): ChannelListViewModel
}

/**
 * Interface definition to be invoked when ChannelViewModel is created.
 * @see [ModuleProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelViewModelProvider {
    /**
     * Returns the ChannelViewModel.
     *
     * @return The [ChannelViewModel].
     * @since 3.9.0
     */
    fun provide(
        owner: ViewModelStoreOwner,
        conversationInfo: Conversation,
        params: MessageListParams?,
        config: ChannelConfig
    ): ChannelViewModel
}
