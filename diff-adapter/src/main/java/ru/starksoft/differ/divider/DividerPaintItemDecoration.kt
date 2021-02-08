package ru.starksoft.differ.divider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

abstract class DividerPaintItemDecoration(
	context: Context,
	@ColorRes
	colorResId: Int,
	@DimenRes
	dividerWidthDimenResId: Int
) :
    RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val color = ContextCompat.getColor(context, colorResId)

    init {
        paint.color = color
        paint.strokeWidth = context.resources.getDimension(dividerWidthDimenResId).toInt().toFloat()
    }

    protected abstract fun getExtraPadding(
		@IntRange(from = 0)
		childPosition: Int
	): ExtraPadding

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        paint.color = color
        val alpha = paint.alpha

        val childCount = parent.childCount

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)

            val childPosition = parent.getChildAdapterPosition(child)
            if (childPosition >= 0 && disableDraw(childPosition) || childPosition < 0) {
                continue
            }

            paint.alpha = (child.alpha * alpha).toInt()

			val (left, _, right) = getExtraPadding(childPosition)
            val startX = parent.paddingLeft + left + child.translationX.toInt()
            val stopX = parent.width - parent.paddingRight - right + child.translationX.toInt()

            val params = child.layoutParams as RecyclerView.LayoutParams
            val y = if (isTopDivider(childPosition)) {
                child.top + params.topMargin + child.translationY.toInt()
            } else {
                child.bottom + params.bottomMargin + child.translationY.toInt()
            }
            c.drawLine(startX.toFloat(), y.toFloat(), stopX.toFloat(), y.toFloat(), paint)
        }
    }

    open fun disableDraw(
		@IntRange(from = 0)
		childPosition: Int
	): Boolean {
        return false
    }

    open fun isTopDivider(
		@IntRange(from = 0)
		childPosition: Int
	): Boolean {
        return false
    }
}
