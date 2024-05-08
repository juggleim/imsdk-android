package com.jet.im.kit.internal.model

import com.jet.im.kit.model.MessageDisplayData

internal data class MessageDisplayDataWrapper(
    val messageDisplayData: MessageDisplayData,
    val updatedAt: Long
)
