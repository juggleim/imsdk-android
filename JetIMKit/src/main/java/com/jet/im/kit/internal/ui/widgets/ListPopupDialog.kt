package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.MutableBaseAdapter
import com.jet.im.kit.databinding.SbViewListPopupBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.utils.ContextUtils
import java.lang.ref.WeakReference

internal class ListPopupDialog<T : Any>(context: Context) {
    private val popupWindow: PopupWindow
    private val binding: SbViewListPopupBinding
    private val mOnScrollChangedListener = OnScrollChangedListener { alignToAnchor() }
    private val mOnLayoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> alignToAnchor() }

    private var anchor: WeakReference<View>? = null
    private var anchorRoot: WeakReference<View>? = null
    var onItemClickListener: OnItemClickListener<T>? = null
    private val onAnchorDetachedListener: View.OnAttachStateChangeListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                // Anchor might have been reattached in a different position.
                alignToAnchor()
            }

            override fun onViewDetachedFromWindow(v: View) {
                // Leave the popup in its current position.
                // The anchor might become attached again.
            }
        }
    val isShowing: Boolean
        get() = popupWindow.isShowing
    var adapter: MutableBaseAdapter<T>? = null
        set(value) {
            field = value
            field?.setOnItemClickListener { view: View, position: Int, data: T ->
                dismiss()
                onItemClickListener?.onItemClick(view, position, data)
            }
            binding.recyclerView.adapter = field
        }

    fun setContentView(contentView: View) {
        popupWindow.contentView = contentView
    }

    fun setScrollPosition(position: Int) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager?
        layoutManager?.scrollToPosition(position)
    }

    fun setCanceledOnTouchOutside(outsideTouchable: Boolean) {
        binding.root.setOnClickListener {
            if (outsideTouchable) {
                dismiss()
            }
        }
    }

    fun update(anchorView: View, items: List<T>) {
        adapter?.let {
            it.items = items
            if (items.isEmpty()) {
                dismiss()
            } else {
                if (!isShowing) {
                    showAsDropUp(anchorView)
                }
            }
        }
    }

    fun dismiss() {
        detachFromAnchor()
        popupWindow.dismiss()
    }

    private fun showAsDropUp(anchorView: View) {
        attachToAnchor(anchorView)
        val p = LayoutParams()
        findDropUpPosition(anchorView, p)
        popupWindow.height = getHeightAbove(anchorView)
        popupWindow.showAtLocation(anchorView, Gravity.BOTTOM or Gravity.END, p.x, p.y)
    }

    fun setUseDivider(useDivider: Boolean) {
        binding.recyclerView.setUseDivider(useDivider)
    }

    private fun alignToAnchor() {
        getAnchor()?.let {
            val p = LayoutParams()
            findDropUpPosition(it, p)
            popupWindow.update(p.x, p.y, -1, getHeightAbove(it), true)
        }
    }

    private fun getHeightAbove(anchor: View): Int {
        val screenLocation = IntArray(2)
        anchor.getLocationOnScreen(screenLocation)
        val lengthAboveAnchor = screenLocation[1]
        var statusBarHeight = 0

        // calculated Android top status bar.
        ContextUtils.getWindow(anchor.context)?.let {
            val rectangle = Rect()
            it.decorView.getWindowVisibleDisplayFrame(rectangle)
            statusBarHeight = rectangle.top
        }
        return lengthAboveAnchor - statusBarHeight
    }

    private fun findDropUpPosition(anchor: View, outParams: LayoutParams) {
        val appScreenLocation = IntArray(2)
        val appRootView = anchor.rootView
        appRootView.getLocationOnScreen(appScreenLocation)
        val screenLocation = IntArray(2)
        anchor.getLocationOnScreen(screenLocation)
        val drawingLocation = IntArray(2)
        drawingLocation[0] = screenLocation[0] - appScreenLocation[0]
        drawingLocation[1] = screenLocation[1] - appScreenLocation[1]
        outParams.x = drawingLocation[0]
        // calculated the y value assuming Gravity.BOTTOM.
        outParams.y = appRootView.height - drawingLocation[1]
    }

    private fun attachToAnchor(anchor: View) {
        detachFromAnchor()
        val vto = anchor.viewTreeObserver
        vto?.addOnScrollChangedListener(mOnScrollChangedListener)
        anchor.addOnAttachStateChangeListener(onAnchorDetachedListener)
        val anchorRoot = anchor.rootView
        anchorRoot.addOnLayoutChangeListener(mOnLayoutChangeListener)
        this.anchor = WeakReference(anchor)
        this.anchorRoot = WeakReference(anchorRoot)
    }

    private fun detachFromAnchor() {
        val anchor = getAnchor()
        if (anchor != null) {
            val vto = anchor.viewTreeObserver
            vto.removeOnScrollChangedListener(mOnScrollChangedListener)
            anchor.removeOnAttachStateChangeListener(onAnchorDetachedListener)
        }
        val anchorRoot = anchorRoot?.get()
        anchorRoot?.removeOnLayoutChangeListener(mOnLayoutChangeListener)
    }

    private fun getAnchor(): View? = anchor?.get()
    private class LayoutParams {
        var x = 0
        var y = 0
    }

    init {
        binding = SbViewListPopupBinding.inflate(LayoutInflater.from(context))
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.itemAnimator = null
        popupWindow =
            PopupWindow(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupWindow.animationStyle = R.style.Animation_Sendbird_Popup
        setCanceledOnTouchOutside(true)
    }
}
