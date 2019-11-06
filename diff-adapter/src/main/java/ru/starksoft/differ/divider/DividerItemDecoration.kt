package ru.starksoft.differ.divider

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import ru.starksoft.differ.R
import ru.starksoft.differ.adapter.DifferAdapter
import java.util.*

class DividerItemDecoration @JvmOverloads constructor(
	context: Context,
	private val adapter: DifferAdapter, @ColorRes separatorColor: Int = COLOR_RES_ID

) : DividerPaintItemDecoration(context, separatorColor, DIVIDER_HEIGHT) {
	private val cache = ExtraPaddingCache(context)

	override fun getExtraPadding(@IntRange(from = 0) childPosition: Int): ExtraPadding {
		return cache[getDividerType(childPosition)]
	}

	private fun getDividerType(@IntRange(from = 0) childPosition: Int): DividerType {
		return if (childPosition >= 0) adapter.getDividerType(childPosition) else DividerType.DISABLED
	}

	override fun disableDraw(@IntRange(from = 0) childPosition: Int): Boolean {
		return isLastElement(childPosition) || getDividerType(childPosition) === DividerType.DISABLED
	}

	private fun isLastElement(childPosition: Int) = adapter.itemCount - 1 == childPosition

	override fun isTopDivider(childPosition: Int): Boolean {
		return getDividerType(childPosition) === DividerType.TOP
	}

	private class ExtraPaddingCache(context: Context) {
		private val padding16 = context.resources.getDimension(R.dimen.padding_normal).toInt()
		private var padding48 = context.resources.getDimension(R.dimen.padding_extra_large).toInt()

		private val data = HashMap<DividerType, ExtraPadding>()

		operator fun get(key: DividerType): ExtraPadding {
			var value = data[key]
			if (value == null) {
				value = createExtraPadding(key)
				data[key] = value
			}
			return value
		}

		private fun createExtraPadding(type: DividerType): ExtraPadding {
			return when (type) {
				DividerType.LEFT_PADDING_16 -> ExtraPadding(padding16, 0, 0, 0)
				DividerType.PADDING_16 -> ExtraPadding(padding16, 0, padding16, 0)
				DividerType.LEFT_PADDING_48 -> ExtraPadding(padding48, 0, 0, 0)
				DividerType.WITHOUT_PADDING, DividerType.DISABLED -> ExtraPadding(0, 0, 0, 0)
				else -> ExtraPadding(0, 0, 0, 0)
			}
		}
	}

	companion object {
		private const val TAG = "DividerItemDecoration"
		@ColorRes
		private val COLOR_RES_ID = R.color.separatorDark
		@DimenRes
		private val DIVIDER_HEIGHT = R.dimen.separator_height
	}
}
