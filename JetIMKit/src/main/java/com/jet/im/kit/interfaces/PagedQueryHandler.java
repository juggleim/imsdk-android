package com.jet.im.kit.interfaces;


import androidx.annotation.NonNull;

import com.jet.im.kit.interfaces.OnListResultHandler;

/**
 * Interface definition for a callback to be invoked when a list of data is loaded.
 *
 * @param <T> A class of data's type.
 * since 3.0.0
 */
public interface PagedQueryHandler<T> {

    /**
     * Called when the user list is first loaded.
     *
     * @param handler The callback of result that was loaded.
     * since 3.0.0
     */
    void loadInitial(@NonNull OnListResultHandler<T> handler);

    /**
     * Called when a list of user has been loaded.
     *
     * @param handler The callback of result that was loaded.
     * since 3.0.0
     */
    void loadMore(@NonNull OnListResultHandler<T> handler);

    /**
     * Called whether a list of user is loaded.
     *
     * @return <code>true</code> if a list of user will be loaded, <code>false</code> otherwise.
     * since 3.0.0
     */
    boolean hasMore();
}
