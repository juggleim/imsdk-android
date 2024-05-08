package com.jet.im.kit.model.configurations

import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Open internal constructor(
    @SerialName(KeySet.channel)
    internal val channel: OpenChannelConfig = OpenChannelConfig()
) {

    @JvmSynthetic
    internal fun merge(config: Open): Open {
        this.channel.merge(config.channel)
        return this
    }
}
