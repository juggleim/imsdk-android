package com.jet.im.kit.internal.ui.widgets

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.R
import com.jet.im.kit.internal.ui.messages.ImageFileView

internal class MultipleFilesMessageItemDecoration(
    private val spanCount: Int,
    @DimenRes private val spacing: Int,
    private val includeEdge: Boolean = true,
    private val isMine: Boolean = false,
    private val parentRadius: Int = 0,
    private val childRadius: Int = 0,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val size = parent.adapter?.itemCount ?: 0
        val position = parent.getChildAdapterPosition(view) // get position of the item
        val column = position % spanCount // calculate the column of the item

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }

        if (size % spanCount != 0 && position == size - 1) {
            if (isMine) {
                outRect.left = view.resources.getDimensionPixelSize(R.dimen.sb_message_half_width) + spacing / 2
                outRect.right = spacing
            } else {
                outRect.right = view.resources.getDimensionPixelSize(R.dimen.sb_message_half_width) + spacing / 2
            }
        }

        val cornerRadii: FloatArray =
            if (position == 0) {
                if (size == spanCount) {
                    // topLeft bottomLeft parentRadius
                    floatArrayOf(
                        parentRadius.toFloat(), parentRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat(),
                        parentRadius.toFloat(), parentRadius.toFloat()
                    )
                } else {
                    // topLeft parentRadius
                    floatArrayOf(
                        parentRadius.toFloat(), parentRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat()
                    )
                }
            } else if (position == spanCount - 1) {
                if (size == spanCount) {
                    // topRight bottomRight parentRadius
                    floatArrayOf(
                        childRadius.toFloat(), childRadius.toFloat(),
                        parentRadius.toFloat(), parentRadius.toFloat(),
                        parentRadius.toFloat(), parentRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat()
                    )
                } else {
                    // topRight parentRadius
                    floatArrayOf(
                        childRadius.toFloat(), childRadius.toFloat(),
                        parentRadius.toFloat(), parentRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat(),
                        childRadius.toFloat(), childRadius.toFloat()
                    )
                }
            } else if ((isMine && size % spanCount != 0 && position == size - 1) ||
                (!isMine && size % spanCount == 0 && position == size - 1)
            ) {
                // bottomRight parentRadius
                floatArrayOf(
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat(),
                    parentRadius.toFloat(), parentRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat()
                )
            } else if ((!isMine && (size % spanCount == 0 && position == size - spanCount || size % spanCount != 0 && position == size - 1)) ||
                (isMine && size % spanCount == 0 && position == size - 1)
            ) {
                // bottomLeft parentRadius
                floatArrayOf(
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat(),
                    parentRadius.toFloat(), parentRadius.toFloat()
                )
            } else {
                floatArrayOf(
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat(),
                    childRadius.toFloat(), childRadius.toFloat()
                )
            }

        if (view is ImageFileView) {
            view.cornerRadii = cornerRadii
        }
    }
}
