package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.R

internal open class ThemeableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {
    private val dividerDecoration: DividerItemDecoration
    private val dividerMarginRight: Float
    private val dividerMarginLeft: Float
    private var dividerColor = 0
    private var dividerHeight = 0f
    fun setUseDivider(useDividerLine: Boolean) {
        if (useDividerLine) {
            addItemDecoration(dividerDecoration)
        } else {
            removeItemDecoration(dividerDecoration)
        }
    }

    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        val divider = createDividerDrawable(
            dividerHeight.toInt(),
            dividerColor,
            dividerMarginLeft.toInt(),
            dividerMarginRight.toInt()
        )
        dividerDecoration.setDrawable(divider)
    }

    fun setDividerHeight(dividerHeight: Float) {
        this.dividerHeight = dividerHeight
        val divider = createDividerDrawable(
            dividerHeight.toInt(),
            dividerColor,
            dividerMarginLeft.toInt(),
            dividerMarginRight.toInt()
        )
        dividerDecoration.setDrawable(divider)
    }

    companion object {
        private fun createDividerDrawable(height: Int, color: Int, marginLeft: Int, marginRight: Int): Drawable {
            val divider = GradientDrawable()
            divider.shape = GradientDrawable.RECTANGLE
            divider.setSize(0, height)
            divider.setColor(color)
            return InsetDrawable(divider, marginLeft, 0, marginRight, 0)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ListComponent, defStyle, 0)
        try {
            val backgroundResId =
                a.getResourceId(R.styleable.ListComponent_sb_recycler_view_background, R.color.background_50)
            dividerColor = a.getColor(
                R.styleable.ListComponent_sb_recycler_view_divide_line_color,
                ContextCompat.getColor(context, R.color.onlight_04)
            )
            dividerHeight = a.getDimension(
                R.styleable.ListComponent_sb_recycler_view_divide_line_height,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_1).toFloat()
            )
            dividerMarginLeft = a.getDimension(R.styleable.ListComponent_sb_recycler_view_divide_margin_left, 0f)
            dividerMarginRight = a.getDimension(R.styleable.ListComponent_sb_recycler_view_divide_margin_right, 0f)
            this.setBackgroundResource(backgroundResId)
            val divider = createDividerDrawable(
                dividerHeight.toInt(),
                dividerColor,
                dividerMarginLeft.toInt(),
                dividerMarginRight.toInt()
            )
            dividerDecoration = object : DividerItemDecoration(context, LinearLayout.VERTICAL) {
                override fun onDraw(canvas: Canvas, parent: RecyclerView, state: State) {
                    val dividerLeft = parent.paddingLeft
                    val dividerRight = parent.width - parent.paddingRight
                    val childCount = parent.childCount
                    for (i in 0..childCount - 2) {
                        val child = parent.getChildAt(i)
                        val params = child.layoutParams as LayoutParams
                        val dividerTop = child.bottom + params.bottomMargin
                        val dividerBottom = dividerTop + divider.intrinsicHeight
                        divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                        divider.draw(canvas)
                    }
                }
            }
            dividerDecoration.setDrawable(divider)
            setUseDivider(true)
        } finally {
            a.recycle()
        }
    }
}
