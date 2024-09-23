package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.interfaces.OnPagedDataLoader
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal class InnerLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    var listener: OnLayoutCompleteListener? = null

    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        listener?.onLayoutComplete(state)
    }
}

internal interface OnLayoutCompleteListener {
    fun onLayoutComplete(state: RecyclerView.State)
}

internal class PagerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ThemeableRecyclerView(context, attrs, defStyle) {
    private var layoutManager: LinearLayoutManager? = null
    private val onScrollListener: OnScrollListener by lazy { OnScrollListener(layoutManager) }
    var onLayoutCompleteListener: OnLayoutCompleteListener? = null
        set(value) {
            field = value
            if (layoutManager is InnerLinearLayoutManager) (layoutManager as InnerLinearLayoutManager).listener = value
        }

    override fun setLayoutManager(layoutManager: LayoutManager?) {
        require(layoutManager is LinearLayoutManager) { "LinearLayoutManager supports only." }
        this.layoutManager = layoutManager
        super.setLayoutManager(layoutManager)
    }

    override fun getLayoutManager(): LinearLayoutManager? = layoutManager

    fun setPager(pager: OnPagedDataLoader<*>) {
        onScrollListener.pager = pager
        onScrollListener.layoutManager = layoutManager
        addOnScrollListener(onScrollListener)
    }

    @TestOnly
    fun getPager() = onScrollListener.pager

    fun setThreshold(threshold: Int) {
        onScrollListener.threshold = threshold
    }

    @TestOnly
    fun getThreshold(): Int = onScrollListener.threshold

    fun findFirstVisibleItemPosition(): Int {
        return layoutManager?.findFirstVisibleItemPosition() ?: 0
    }

    fun findLastVisibleItemPosition(): Int {
        return layoutManager?.findLastVisibleItemPosition() ?: 0
    }

    fun isReverseLayout(): Boolean {
        return layoutManager?.reverseLayout ?: false
    }

    fun setOnScrollEndDetectListener(scrollEndDetectListener: OnScrollEndDetectListener?) {
        onScrollListener.scrollEndDetectListener = scrollEndDetectListener
    }

    fun useReverseData() {
        onScrollListener.useReverseData = true
    }

    fun setStackFromEnd(stackFromEnd: Boolean) {
        post { layoutManager?.stackFromEnd = stackFromEnd }
    }

    fun isScrollable(): Boolean {
        return canScrollVertically(1) || canScrollVertically(-1)
    }

    fun getStackFromEnd(): Boolean = layoutManager?.stackFromEnd ?: false

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private class OnScrollListener constructor(var layoutManager: LinearLayoutManager?) :
        RecyclerView.OnScrollListener() {
        var threshold = 1
            set(value) {
                require(value > 0) { "illegal threshold: $value" }
                field = value
            }
        var pager: OnPagedDataLoader<*>? = null
        var scrollEndDetectListener: OnScrollEndDetectListener? = null
        private val prevLoadingWorker = Executors.newSingleThreadExecutor()
        private val nextLoadingWorker = Executors.newSingleThreadExecutor()
        private val prevLoading = AtomicBoolean(false)
        private val nextLoading = AtomicBoolean(false)
        var useReverseData: Boolean = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            layoutManager?.run {
                val lastVisibleItemPosition = findLastVisibleItemPosition()
                val firstVisibleItemPosition = findFirstVisibleItemPosition()
                if (!recyclerView.canScrollVertically(ScrollDirection.Bottom.direction)) {
                    scrollEndDetectListener?.onScrollEnd(ScrollDirection.Bottom)
                }
                if (!recyclerView.canScrollVertically(ScrollDirection.Top.direction)) {
                    scrollEndDetectListener?.onScrollEnd(ScrollDirection.Top)
                }
                pager?.run {
                    val prevLoadMore =
                        hasPrevious() && if (useReverseData) itemCount - lastVisibleItemPosition <= threshold else firstVisibleItemPosition <= threshold
                    if (!prevLoading.get() && prevLoadMore) {
                        prevLoading.set(true)
                        prevLoadingWorker.submit {
                            try {
                                loadPrevious()
                            } catch (ignore: Exception) {
                            } finally {
                                prevLoading.set(false)
                            }
                        }
                    }

                    val nextLoadMore =
                        hasNext() && if (useReverseData) firstVisibleItemPosition <= threshold else itemCount - lastVisibleItemPosition <= threshold
                    if (!nextLoading.get() && nextLoadMore) {
                        nextLoading.set(true)
                        nextLoadingWorker.submit {
                            try {
                                loadNext()
                            } catch (ignore: Exception) {
                            } finally {
                                nextLoading.set(false)
                            }
                        }
                    }
                }
            }
        }

        fun dispose() {
            prevLoadingWorker.shutdown()
            nextLoadingWorker.shutdown()
        }
    }

    interface OnScrollEndDetectListener {
        fun onScrollEnd(direction: ScrollDirection)
    }

    enum class ScrollDirection(val direction: Int) {
        Top(-1), Bottom(1);
    }
}
