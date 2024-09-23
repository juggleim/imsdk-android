package com.jet.im.kit.model

/**
 * LiveData which publicly exposes setValue(T) and postValue(T) method.
 *
 * @param <T> The type of data hold by this instance
 * @since 3.0.0
 */
class MutableLiveDataEx<T> : LiveDataEx<T> {
    constructor(value: T) : super(value)
    constructor() : super()

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }
}
