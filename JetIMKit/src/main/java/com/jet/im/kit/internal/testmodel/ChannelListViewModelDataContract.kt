package com.jet.im.kit.internal.testmodel

import androidx.lifecycle.MutableLiveData
import com.juggle.im.interfaces.IConversationManager
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract
import com.jet.im.kit.internal.contracts.TaskQueueContract
import com.juggle.im.model.ConversationInfo

internal interface ViewModelDataContract

internal interface ChannelListViewModelDataContract : ViewModelDataContract {
    val sendbirdUIKit: SendbirdUIKitContract
    val query: IConversationManager
    val channelList: MutableLiveData<List<ConversationInfo>>
    val collectionHandler: IConversationManager.IConversationListener
    val taskQueue: TaskQueueContract
}
