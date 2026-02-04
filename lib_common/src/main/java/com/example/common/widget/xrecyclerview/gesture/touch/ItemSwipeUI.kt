package com.example.common.widget.xrecyclerview.gesture.touch

import android.graphics.Canvas
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.size
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 侧滑（拖拽 / 滑动）过程中的 UI 绘制 / 状态回调
 */
class ItemSwipeUI private constructor() : OnItemSwipeUIListener {

    companion object {
        @JvmStatic
        val instance by lazy { ItemSwipeUI() }

        private fun findMaxElevation(recyclerView: RecyclerView, itemView: View): Float {
            val childCount = recyclerView.size
            var max = 0f
            for (i in 0..<childCount) {
                val child = recyclerView.getChildAt(i)
                if (child === itemView) {
                    continue
                }
                val elevation = ViewCompat.getElevation(child)
                if (elevation > max) {
                    max = elevation
                }
            }
            return max
        }
    }

    override fun onDraw(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (isCurrentlyActive) {
            var originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view)
                val newElevation = 1f + findMaxElevation(recyclerView, view)
                ViewCompat.setElevation(view, newElevation)
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        }
        view.translationX = dX
        view.translationY = dY
    }

    override fun onDrawOver(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
    }

    override fun clearView(view: View) {
        val tag = view.getTag(R.id.item_touch_helper_previous_elevation)
        if (tag is Float) {
            ViewCompat.setElevation(view, tag)
        }
        view.setTag(R.id.item_touch_helper_previous_elevation, null)
        view.translationX = 0f
        view.translationY = 0f
    }

    override fun onSelected(view: View) {
    }

}

interface OnItemSwipeUIListener {

    fun onDraw(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)

    fun onDrawOver(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)

    fun clearView(view: View)

    fun onSelected(view: View)

}