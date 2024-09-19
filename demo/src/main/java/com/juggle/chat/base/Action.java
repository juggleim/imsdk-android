package com.juggle.chat.base;

public interface Action<T> {
    void call(T t);
}
