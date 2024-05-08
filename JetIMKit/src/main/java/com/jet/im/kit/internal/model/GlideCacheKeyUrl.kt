package com.jet.im.kit.internal.model

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.model.GlideUrl
import com.jet.im.kit.consts.StringSet

/**
 * This class apply disk cache key for only http url.
 *
 * since 3.3.2
 */
private class GlideCacheKeyUrl(url: String, private val cacheKey: String) :
    GlideUrl(url.ifEmpty { StringSet.INVALID_URL }) {
    override fun getCacheKey(): String? = cacheKey.ifEmpty { super.getCacheKey() }
}

internal object GlideCachedUrlLoader {
    @JvmStatic
    fun load(
        requestManager: RequestManager,
        url: String,
        cacheKey: String
    ): RequestBuilder<Drawable> {
        if (url.startsWith("http", true)) {
            return requestManager.load(GlideCacheKeyUrl(url, cacheKey))
        }
        return requestManager.load(url)
    }

    @JvmStatic
    fun <ResourceType> load(
        requestBuilder: RequestBuilder<ResourceType>,
        url: String,
        cacheKey: String
    ): RequestBuilder<ResourceType> {
        if (url.startsWith("http", true)) {
            return requestBuilder.load(GlideCacheKeyUrl(url, cacheKey))
        }
        return requestBuilder.load(url)
    }
}
