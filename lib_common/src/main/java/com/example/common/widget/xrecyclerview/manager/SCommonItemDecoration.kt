package com.example.common.widget.xrecyclerview.manager

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero

/**
 * Set dividers' properties(horizontal and vertical space...) of item with type.
 * 通过item type 设置边框属性
 * Created by bosong on 2017/3/10.
 */
@SuppressLint("WrongConstant")
class SCommonItemDecoration(private val mPropMap: SparseArray<ItemDecorationProps>?) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter
        val itemType = adapter?.getItemViewType(position).orZero
        val props: ItemDecorationProps?
        if (mPropMap != null) {
            props = mPropMap[itemType]
        } else {
            return
        }
        if (props == null) {
            return
        }
        var spanIndex = 0
        var spanSize = 1
        var spanCount = 1
        var orientation = OrientationHelper.VERTICAL
        if (parent.layoutManager is GridLayoutManager) {
            val lp = view.layoutParams as? GridLayoutManager.LayoutParams
            spanIndex = lp?.spanIndex.orZero
            spanSize = lp?.spanSize.orZero
            val layoutManager = parent.layoutManager as? GridLayoutManager
            spanCount = layoutManager?.spanCount.orZero // Assume that there're spanCount items in this row/column.
            orientation = layoutManager?.orientation.orZero
        } else if (parent.layoutManager is StaggeredGridLayoutManager) {
            val lp = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
            spanIndex = lp?.spanIndex.orZero
            val layoutManager = parent.layoutManager as? StaggeredGridLayoutManager
            spanCount = layoutManager?.spanCount.orZero // Assume that there're spanCount items in this row/column.
            spanSize = if (lp?.isFullSpan.orFalse) spanCount else 1
            orientation = layoutManager?.orientation.orZero
        }
        val isFirstRowOrColumn: Boolean
        val isLastRowOrColumn: Boolean
        val prePos = if (position > 0) position - 1 else -1
        val nextPos = if (position < adapter?.itemCount.orZero - 1) position + 1 else -1
        // Last position on the last row 上一行的最后一个位置
        val preRowPos = if (position > spanIndex) position - (1 + spanIndex) else -1
        // First position on the next row 下一行的第一个位置
        val nextRowPos = if (position < adapter?.itemCount.orZero - (spanCount - spanIndex)) position + (spanCount - spanIndex) else -1
        isFirstRowOrColumn = position == 0 || prePos == -1 || itemType != adapter?.getItemViewType(prePos) || preRowPos == -1 || itemType != adapter.getItemViewType(preRowPos)
        isLastRowOrColumn = position == adapter?.itemCount.orZero - 1 || nextPos == -1 || itemType != adapter?.getItemViewType(nextPos) || nextRowPos == -1 || itemType != adapter.getItemViewType(nextRowPos)
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        if (orientation == GridLayoutManager.VERTICAL) {
            if (props.hasVerticalEdge) {
                left = props.verticalSpace * (spanCount - spanIndex) / spanCount
                right = props.verticalSpace * (spanIndex + (spanSize - 1) + 1) / spanCount
            } else {
                left = props.verticalSpace * spanIndex / spanCount
                right = props.verticalSpace * (spanCount - (spanIndex + spanSize - 1) - 1) / spanCount
            }
            if (isFirstRowOrColumn) { // First row
                if (props.hasHorizontalEdge) top = props.horizontalSpace
            }
            if (isLastRowOrColumn) { // Last row
                if (props.hasHorizontalEdge) bottom = props.horizontalSpace
            } else {
                bottom = props.horizontalSpace
            }
        } else {
            if (props.hasHorizontalEdge) {
                top = props.horizontalSpace * (spanCount - spanIndex) / spanCount
                bottom = props.horizontalSpace * (spanIndex + (spanSize - 1) + 1) / spanCount
            } else {
                top = props.horizontalSpace * spanIndex / spanCount
                bottom = props.horizontalSpace * (spanCount - (spanIndex + spanSize - 1) - 1) / spanCount
            }
            if (isFirstRowOrColumn) { // First column
                if (props.hasVerticalEdge) left = props.verticalSpace
            }
            if (isLastRowOrColumn) { // Last column
                if (props.hasVerticalEdge) right = props.verticalSpace
            } else {
                right = props.verticalSpace
            }
        }
        outRect[left, top, right] = bottom
    }

    data class ItemDecorationProps(var horizontalSpace: Int, var verticalSpace: Int, var hasHorizontalEdge: Boolean, var hasVerticalEdge: Boolean)

}