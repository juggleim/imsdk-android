package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

/**
 * Callback interface for loading list-type data.
 *
 * @param <T> Data type to be loaded.
 */
public interface OnPagedDataLoader<T> {
    /**
     * Loads data to be used when the page goes to the previous.
     * Synchronized function call must be used.
     *
     * @return Data to be used on the previous page
     * @throws Exception Occurred when the job was failed.
     * since 3.0.0
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    T loadPrevious() throws Exception;

    /**
     * Loads data to be used when the page goes to the next.
     * Synchronized function call must be used.
     *
     * @return Data to be used on the next page
     * @throws Exception Occurred when the job was failed.
     * since 3.0.0
     */
    @NonNull
    T loadNext() throws Exception;

    /**
     * Determine whether the data on the next page exists.
     *
     * @return {@code true} if there is any data on the next page, {@code false} otherwise
     * since 3.0.0
     */
    boolean hasNext();

    /**
     * Determine whether the data on the previous page exists.
     *
     * @return {@code true} if there is any data on the previous page, {@code false} otherwise
     * since 3.0.0
     */
    boolean hasPrevious();
}
