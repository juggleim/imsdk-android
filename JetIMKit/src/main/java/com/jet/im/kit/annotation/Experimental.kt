package com.jet.im.kit.annotation

@RequiresOptIn(message = "This ViewHolder is experimental. It may be changed in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class MessageViewHolderExperimental
