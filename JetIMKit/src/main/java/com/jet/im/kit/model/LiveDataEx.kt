package com.jet.im.kit.model

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.jet.im.kit.log.Logger

/**
 * Extension class for live data.
 *
 * @param <T> The type of data held by this instance
 * @since 3.0.0
 */
open class LiveDataEx<T> : LiveData<T> {
    constructor(value: T) : super(value)
    constructor() : super()

    /**
     * Observes forever, however, remove the observer when ON_DESTROY is invoked.
     *
     * @param owner The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     * @since 3.0.0
     */
    fun observeAlways(owner: LifecycleOwner, observer: Observer<T>) {
        Logger.d(">> LiveDataEx::observeAlways()")
        observeForever(observer)
        owner.lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner?, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                removeObserver(observer)
            }
        })
    }
}
