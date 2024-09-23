package com.jet.im.kit.internal.testmodel

import androidx.lifecycle.MutableLiveData
import com.juggle.im.interfaces.IConnectionManager
import com.juggle.im.interfaces.IConversationManager
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.jet.im.kit.internal.contracts.GroupChannelCollectionContract
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract
import com.jet.im.kit.internal.contracts.TaskQueueContract
import com.juggle.im.model.ConversationInfo

internal interface ViewModelDataContract

internal interface ChannelListViewModelDataContract : ViewModelDataContract {
    val sendbirdUIKit: SendbirdUIKitContract
    var collection: GroupChannelCollectionContract?
    val query: IConversationManager
    val channelList: MutableLiveData<List<ConversationInfo>>
    val collectionHandler: IConversationManager.IConversationListener
    val taskQueue: TaskQueueContract
}
