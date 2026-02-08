package com.example.common.widget.xrecyclerview.gesture

import android.graphics.Canvas
import android.view.View
import android.view.animation.Interpolator
import androidx.core.graphics.withSave
import androidx.core.view.ViewCompat
import androidx.core.view.size
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ACTION_MODE_DRAG_MASK
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ACTION_MODE_SWIPE_MASK
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ACTION_STATE_DRAG
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ACTION_STATE_IDLE
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ACTION_STATE_SWIPE
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.ANIMATION_TYPE_DRAG
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.DIRECTION_FLAG_COUNT
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.END
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.LEFT
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.RIGHT
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationHelper.START
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.toSafeLong
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

abstract class BaseGestureCallback {
    // 拖拽越界最大滚动速度，避免重复获取资源 (缓存)
    private var mCachedMaxScrollSpeed = -1

    companion object {
        // 相对 / 绝对方向的标记位掩码，用于位运算筛选方向
        private const val RELATIVE_DIR_FLAGS = START or END or ((START or END) shl DIRECTION_FLAG_COUNT) or ((START or END) shl (2 * DIRECTION_FLAG_COUNT))
        private const val ABS_HORIZONTAL_DIR_FLAGS = LEFT or RIGHT or ((LEFT or RIGHT) shl DIRECTION_FLAG_COUNT) or ((LEFT or RIGHT) shl (2 * DIRECTION_FLAG_COUNT))
        // 拖拽 / 侧滑的默认动画时长（200ms/250ms）
        private const val DEFAULT_DRAG_ANIMATION_DURATION = 200
        private const val DEFAULT_SWIPE_ANIMATION_DURATION = 250
        // 拖拽越界滚动的加速上限时间（2000ms）
        private const val DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000L
        // 拖拽滚动的插值器（五次方插值），实现先慢后快的滚动加速度
        private val sDragScrollInterpolator = Interpolator { t ->
            t * t * t * t * t
        }
        // 拖拽越界时的视图偏移插值器，实现越界越慢的阻尼效果
        private val sDragViewScrollCapInterpolator = Interpolator { t ->
            t - 1.0f
            t * t * t * t * t + 1.0f
        }

        /**
         * 碰撞检测工具方法
         * 判断「一个触摸坐标 (x,y) 是否落在某个 View 的可视范围内」
         * @child -> 要检测的目标 View（比如 RecyclerView 的某个 ItemView）
         * @x -> 触摸事件的横坐标（MotionEvent.getX () 获取的原始坐标）
         * @y -> 触摸事件的纵坐标（MotionEvent.getY () 获取的原始坐标）
         * @left -> 目标 View 左上角的基准横坐标（不是 View.getLeft ()，而是动态计算的位置）
         * @top -> 目标 View 左上角的基准纵坐标（同理，不是 View.getTop ()）
         */
        @JvmStatic
        fun hitTest(child: View, x: Float, y: Float, left: Float, top: Float): Boolean {
            return x >= left // 触摸点x坐标 ≥ View左上角x坐标（不超出左边界）
                    && x <= left + child.width // 触摸点x坐标 ≤ View右下角x坐标（不超出右边界）
                    && y >= top // 触摸点y坐标 ≥ View左上角y坐标（不超出上边界）
                    && y <= top + child.height // 触摸点y坐标 ≤ View右下角y坐标（不超出下边界）
        }

        /**
         * 构建手势方向标记位
         * 用于定义「哪个动作状态（拖拽 / 侧滑）支持哪些方向」，最终返回一个封装了所有规则的 int 型标记位
         */
        @JvmStatic
        fun makeMovementFlags(dragFlags: Int, swipeFlags: Int): Int {
            return makeFlag(ACTION_STATE_IDLE, swipeFlags or dragFlags) or makeFlag(ACTION_STATE_SWIPE, swipeFlags) or makeFlag(ACTION_STATE_DRAG, dragFlags)
        }

        /**
         * 为单个动作状态绑定方向
         * 基础的标记位构建方法，将「动作状态 + 方向」通过左移位运算封装成 int 型（不同动作状态占不同的位段，避免冲突）
         */
        @JvmStatic
        fun makeFlag(actionState: Int, directions: Int): Int {
            return directions shl (actionState * DIRECTION_FLAG_COUNT)
        }

        /**
         * 绝对方向转相对方向
         * 将LEFT/RIGHT这类固定绝对方向，转换成START/END这类随布局方向（LTR/RTL）变化的相对方向，适配多语言布局（比如阿拉伯语 RTL，左滑实际是 END 方向）
         */
        @JvmStatic
        fun convertToRelativeDirection(flags: Int, layoutDirection: Int): Int {
            var flags = flags
            val masked = flags and ABS_HORIZONTAL_DIR_FLAGS
            if (masked == 0) {
                return flags
            }
            flags = flags and masked.inv()
            if (layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                flags = flags or (masked shl 2)
                return flags
            } else {
                flags = flags or ((masked shl 1) and ABS_HORIZONTAL_DIR_FLAGS.inv())
                flags = flags or (((masked shl 1) and ABS_HORIZONTAL_DIR_FLAGS) shl 2)
            }
            return flags
        }

        /**
         * 相对方向转绝对方向
         * 将START/END转换成当前布局下的LEFT/RIGHT，方便在业务中判断实际的滑动方向（比如 RTL 下 START 是右，转换后可直接用 LEFT/RIGHT 判断）
         */
        @JvmStatic
        fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
            var flags = flags
            val masked = flags and RELATIVE_DIR_FLAGS
            if (masked == 0) {
                return flags
            }
            flags = flags and masked.inv()
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
                flags = flags or (masked shr 2)
                return flags
            } else {
                flags = flags or ((masked shr 1) and RELATIVE_DIR_FLAGS.inv())
                flags = flags or (((masked shr 1) and RELATIVE_DIR_FLAGS) shr 2)
            }
            return flags
        }
    }

    // <editor-fold defaultstate="collapsed" desc="标记位判断">
    /**
     * 判断 Item 是否支持拖拽 / 侧滑
     */
    fun hasDragFlag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Boolean {
        val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
        return (flags and ACTION_MODE_DRAG_MASK) != 0
    }

    fun hasSwipeFlag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Boolean {
        val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
        return (flags and ACTION_MODE_SWIPE_MASK) != 0
    }

    /**
     * 获取适配布局的绝对方向标记位
     * 先调用getMovementFlags获取子类定义的规则，再通过convertToAbsoluteDirection转换成绝对方向标记位，是后续所有手势判断的最终规则来源
     */
    fun getAbsoluteMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val flags = getMovementFlags(recyclerView, viewHolder)
        return convertToAbsoluteDirection(flags, ViewCompat.getLayoutDirection(recyclerView))
    }

    /**
     * 选择拖拽的目标 Item
     * 拖拽过程中，从候选的 DropTarget 中选择最优的目标 Item，默认实现基于位置距离
     */
    fun chooseDropTarget(selected: RecyclerView.ViewHolder, dropTargets: MutableList<RecyclerView.ViewHolder>, curX: Int, curY: Int): RecyclerView.ViewHolder? {
        val right = curX + selected.itemView.width
        val bottom = curY + selected.itemView.height
        var winner: RecyclerView.ViewHolder? = null
        var winnerScore = -1
        val dx = curX - selected.itemView.left
        val dy = curY - selected.itemView.top
        val targetsSize = dropTargets.size
        for (i in 0..<targetsSize) {
            val target = dropTargets[i]
            if (dx > 0) {
                val diff = target.itemView.right - right
                if (diff < 0 && target.itemView.right > selected.itemView.right) {
                    val score = abs(diff)
                    if (score > winnerScore) {
                        winnerScore = score
                        winner = target
                    }
                }
            }
            if (dx < 0) {
                val diff = target.itemView.left - curX
                if (diff > 0 && target.itemView.left < selected.itemView.left) {
                    val score = abs(diff)
                    if (score > winnerScore) {
                        winnerScore = score
                        winner = target
                    }
                }
            }
            if (dy < 0) {
                val diff = target.itemView.top - curY
                if (diff > 0 && target.itemView.top < selected.itemView.top) {
                    val score = abs(diff)
                    if (score > winnerScore) {
                        winnerScore = score
                        winner = target
                    }
                }
            }
            if (dy > 0) {
                val diff = target.itemView.bottom - bottom
                if (diff < 0 && target.itemView.bottom > selected.itemView.bottom) {
                    val score = abs(diff)
                    if (score > winnerScore) {
                        winnerScore = score
                        winner = target
                    }
                }
            }
        }
        return winner
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="手势状态回调 / 绘制 / 装饰方法">
    /**
     * Item 被选中（开始手势）的回调
     */
    fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    }

    /**
     * 拖拽过程中 Item 位置变化的回调
     */
    fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is ItemDecorationHelper.ViewDropHandler) {
            (layoutManager as? ItemDecorationHelper.ViewDropHandler)?.prepareForDrop(viewHolder.itemView, target.itemView, x, y)
            return
        }
        if (layoutManager?.canScrollHorizontally().orFalse) {
            val minLeft = layoutManager?.getDecoratedLeft(target.itemView).orZero
            if (minLeft <= recyclerView.getPaddingLeft()) {
                recyclerView.scrollToPosition(toPos)
            }
            val maxRight = layoutManager?.getDecoratedRight(target.itemView).orZero
            if (maxRight >= recyclerView.width - recyclerView.getPaddingRight()) {
                recyclerView.scrollToPosition(toPos)
            }
        }
        if (layoutManager?.canScrollVertically().orFalse) {
            val minTop = layoutManager?.getDecoratedTop(target.itemView).orZero
            if (minTop <= recyclerView.paddingTop) {
                recyclerView.scrollToPosition(toPos)
            }
            val maxBottom = layoutManager?.getDecoratedBottom(target.itemView).orZero
            if (maxBottom >= recyclerView.height - recyclerView.paddingBottom) {
                recyclerView.scrollToPosition(toPos)
            }
        }
    }

    /**
     * 手势结束（释放 Item）的回调
     */
    fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val view = viewHolder.itemView
        val tag = view.getTag(R.id.item_touch_helper_previous_elevation)
        if (tag is Float) {
            ViewCompat.setElevation(view, tag)
        }
        view.setTag(R.id.item_touch_helper_previous_elevation, null)
        view.translationX = 0f
        view.translationY = 0f
    }

    /**
     * 遍历所有恢复动画的 Item，调用update()更新动画进度，然后分别调用onChildDraw/onChildDrawOver绘制每个 Item；同时绘制当前正在被操作的选中 Item
     */
    fun onDraw(c: Canvas, parent: RecyclerView, selected: RecyclerView.ViewHolder?, recoverAnimationList: MutableList<RecoverAnimation>, actionState: Int, dX: Float, dY: Float) {
        val recoverAnimSize = recoverAnimationList.size
        for (i in 0..<recoverAnimSize) {
            val anim = recoverAnimationList[i]
            anim.update()
            c.withSave {
                onChildDraw(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false)
            }
        }
        if (selected != null) {
            c.withSave {
                onChildDraw(c, parent, selected, dX, dY, actionState, true)
            }
        }
    }

    fun onDrawOver(c: Canvas, parent: RecyclerView, selected: RecyclerView.ViewHolder?, recoverAnimationList: MutableList<RecoverAnimation>, actionState: Int, dX: Float, dY: Float) {
        val recoverAnimSize = recoverAnimationList.size
        for (i in 0..<recoverAnimSize) {
            val anim = recoverAnimationList[i]
            c.withSave {
                onChildDrawOver(c, parent, anim.mViewHolder, anim.mX, anim.mY, anim.mActionState, false)
            }
        }
        if (selected != null) {
            c.withSave {
                onChildDrawOver(c, parent, selected, dX, dY, actionState, true)
            }
        }
        var hasRunningAnimation = false
        for (i in recoverAnimSize - 1 downTo 0) {
            val anim = recoverAnimationList.get(i)
            if (anim.mEnded && !anim.mIsPendingCleanup) {
                recoverAnimationList.removeAt(i)
            } else if (!anim.mEnded) {
                hasRunningAnimation = true
            }
        }
        if (hasRunningAnimation) {
            parent.invalidate()
        }
    }

    /**
     * 默认空实现，子类可重写做上层装饰绘制
     */
    fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val view = viewHolder.itemView
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

    fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="动画 / 插值 / 滚动：手势的动画时长、越界插值、自动滚动">
    fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
        val itemAnimator = recyclerView.itemAnimator
        return if (itemAnimator == null) {
            (if (animationType == ANIMATION_TYPE_DRAG) DEFAULT_DRAG_ANIMATION_DURATION else DEFAULT_SWIPE_ANIMATION_DURATION).toSafeLong()
        } else {
            if (animationType == ANIMATION_TYPE_DRAG) itemAnimator.moveDuration else itemAnimator.removeDuration
        }
    }

    fun interpolateOutOfBoundsScroll(recyclerView: RecyclerView, viewSize: Int, viewSizeOutOfBounds: Int, totalSize: Int, msSinceStartScroll: Long): Int {
        val maxScroll = getMaxDragScroll(recyclerView)
        val absOutOfBounds = abs(viewSizeOutOfBounds)
        val direction = sign(viewSizeOutOfBounds.toSafeFloat()).toSafeInt()
        val outOfBoundsRatio = min(1f, 1f * absOutOfBounds / viewSize)
        val cappedScroll = (direction * maxScroll * sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio)).toSafeInt()
        val timeRatio = if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
            1f
        } else {
            msSinceStartScroll.toSafeFloat() / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS
        }
        val value = (cappedScroll * sDragScrollInterpolator.getInterpolation(timeRatio)).toSafeInt()
        if (value == 0) {
            return if (viewSizeOutOfBounds > 0) 1 else -1
        }
        return value
    }

    private fun getMaxDragScroll(recyclerView: RecyclerView): Int {
        if (mCachedMaxScrollSpeed == -1) {
            mCachedMaxScrollSpeed = recyclerView.resources.getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame)
        }
        return mCachedMaxScrollSpeed
    }
    // </editor-fold>

    /**
     * 设置 Item 的碰撞检测边距
     * 默认返回 0，即 Item 的原始边界为碰撞检测区域
     */
    open fun getBoundingBoxMargin(): Int {
        return 0
    }

    /**
     * 侧滑的速度阈值 (侧滑的逃逸速度/侧滑的速度阈值)
     */
    open fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue
    }

    open fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return defaultValue
    }

    /**
     * 手势的距离阈值 (侧滑的距离阈值/拖拽的距离阈值)
     */
    open fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .5f
    }

    open fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .5f
    }

    /**
     * 判断是否允许将 Item 拖拽到目标 Item 的位置
     */
    open fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    /**
     * 关闭长按拖拽，手动触发
     */
    open fun isLongPressDragEnabled(): Boolean {
        return true
    }

    /**
     * 全局禁止侧滑
     */
    open fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    /**
     * 定义方向规则
     */
    abstract fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int

    /**
     * 拖拽交换数据
     */
    abstract fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean

    /**
     * 侧滑业务逻辑
     */
    abstract fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

}