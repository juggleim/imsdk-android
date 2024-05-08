package com.jet.im.kit.model.configurations

import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UIKitConfigurations internal constructor(
    internal val common: Common = Common(),
    @SerialName(KeySet.group_channel)
    internal val group: Group = Group(),
    @SerialName(KeySet.open_channel)
    internal val open: Open = Open()
) {
    /**
     * It retains the existing instance and overwrites the new value.
     */
    @JvmSynthetic
    internal fun merge(config: UIKitConfigurations): UIKitConfigurations {
        this.common.merge(config.common)
        this.group.merge(config.group)
        this.open.merge(config.open)
        return this
    }
}
