package com.example.common.widget.xrecyclerview.touch

import android.graphics.Canvas
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.example.common.R

/**
 * RecyclerView Item 拖拽/滑动时的 UI 视觉处理接口
 * 负责控制 Item 在「拖拽/滑动过程中」「交互结束后」的视觉状态（位移、层级、阴影等）
 */
interface ItemTouchUIImpl {

    companion object {
        @JvmStatic
        val INSTANCE = object : ItemTouchUIImpl {}

        private fun findMaxElevation(recyclerView: RecyclerView, itemView: View): Float {
            var maxElevation = 0f
            for (i in 0..<recyclerView.size) {
                val child = recyclerView.getChildAt(i)
                if (child != itemView) {
                    val elevation = ViewCompat.getElevation(child)
                    if (elevation > maxElevation) {
                        maxElevation = elevation
                    }
                }
            }
            return maxElevation
        }
    }

    /**
     * 【核心绘制】拖拽/滑动过程中，每帧绘制时调用
     * 主要用于更新 Item 的位移、层级等视觉效果，让 Item 跟随手指移动/滑动
     *
     * @param c 绘制画布（RecyclerView 的画布）
     * @param recyclerView 当前绑定的 RecyclerView
     * @param view 正在拖拽/滑动的目标 Item 视图
     * @param dX Item 在 X 轴的偏移量（手指拖动的水平距离，左滑为负、右滑为正）
     * @param dY Item 在 Y 轴的偏移量（手指拖动的垂直距离，上拖为负、下拖为正）
     * @param actionState 当前交互状态：ACTION_STATE_IDLE(闲置)/ACTION_STATE_SWIPE(滑动)/ACTION_STATE_DRAG(拖拽)
     * @param isCurrentlyActive 是否是当前正在交互的 Item（true=手指还按着 Item，false=动画恢复阶段）
     */
    fun onDraw(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (isCurrentlyActive) {
            var originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view)
                val newElevation = 1f + findMaxElevation(recyclerView, view)
                ViewCompat.setElevation(view, newElevation)
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        }
        // 更新 Item 位移，跟随手指移动
        view.translationX = dX
        view.translationY = dY
    }

    /**
     * 【顶层绘制】拖拽/滑动过程中，在所有 Item 绘制完成后调用（覆盖层绘制）
     * 常用于绘制「超出 Item 范围」的视觉效果（比如滑动删除时的删除图标/背景）
     * 参数含义同 onDraw
     */
    fun onDrawOver(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {}

    /**
     * 【选中触发】Item 被选中（开始拖拽/滑动）时调用
     * 常用于给 Item 加「选中态」（比如抬升层级、加阴影、改背景色）
     * @param view 被选中的目标 Item 视图
     */
    fun onSelected(view: View) {}

    /**
     * 【状态恢复】拖拽/滑动交互完全结束后调用（动画也已完成）
     * 核心作用：把 Item 的视觉状态恢复到初始值（位移、层级、透明度等）
     * @param view 完成交互的目标 Item 视图
     */
    fun clearView(view: View) {
        val originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation) as? Float
        originalElevation?.let { ViewCompat.setElevation(view, it) }
        // 清空 Tag + 恢复位移
        view.setTag(R.id.item_touch_helper_previous_elevation, null)
        view.translationX = 0f
        view.translationY = 0f
    }

}