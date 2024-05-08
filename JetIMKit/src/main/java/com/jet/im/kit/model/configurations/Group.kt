package com.jet.im.kit.model.configurations

import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Group internal constructor(
    @SerialName(KeySet.channel)
    internal val channel: ChannelConfig = ChannelConfig(),
    @SerialName(KeySet.channel_list)
    internal val channelList: ChannelListConfig = ChannelListConfig(),
    @SerialName(KeySet.setting)
    internal val setting: ChannelSettingConfig = ChannelSettingConfig()
) {

    @JvmSynthetic
    internal fun merge(config: Group): Group {
        this.channel.merge(config.channel)
        this.channelList.merge(config.channelList)
        this.setting.merge(config.setting)
        return this
    }
}
