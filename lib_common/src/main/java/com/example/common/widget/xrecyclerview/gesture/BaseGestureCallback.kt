package com.example.common.widget.xrecyclerview.gesture

import android.graphics.Canvas
import android.view.View
import android.view.animation.Interpolator
import androidx.core.graphics.withSave
import androidx.core.view.ViewCompat
import androidx.core.view.size
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

abstract class BaseGestureCallback {

    companion object {
        private var mCachedMaxScrollSpeed = -1
        private val sDragScrollInterpolator = Interpolator { t ->
            t * t * t * t * t
        }
        private val sDragViewScrollCapInterpolator = Interpolator { t ->
            t - 1.0f
            t * t * t * t * t + 1.0f
        }
        private const val RELATIVE_DIR_FLAGS = ItemTouchHelper.START or ItemTouchHelper.END or ((ItemTouchHelper.START or ItemTouchHelper.END) shl ItemTouchHelper.DIRECTION_FLAG_COUNT) or ((ItemTouchHelper.START or ItemTouchHelper.END) shl (2 * ItemTouchHelper.DIRECTION_FLAG_COUNT))
        private const val ABS_HORIZONTAL_DIR_FLAGS = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ((ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) shl ItemTouchHelper.DIRECTION_FLAG_COUNT) or ((ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) shl (2 * ItemTouchHelper.DIRECTION_FLAG_COUNT))
        private const val DEFAULT_DRAG_ANIMATION_DURATION = 200
        private const val DEFAULT_SWIPE_ANIMATION_DURATION = 250
        private const val DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000L

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

        @JvmStatic
        fun makeMovementFlags(dragFlags: Int, swipeFlags: Int): Int {
            return makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, swipeFlags or dragFlags) or makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, swipeFlags) or makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, dragFlags)
        }

        @JvmStatic
        fun makeFlag(actionState: Int, directions: Int): Int {
            return directions shl (actionState * ItemTouchHelper.DIRECTION_FLAG_COUNT)
        }

        // <editor-fold defaultstate="collapsed" desc="监听参数处理">
        /**
         * onChildDraw 调取
         */
        private fun onDraw(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
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

        /**
         * onChildDrawOver 调取
         */
        private fun onDrawOver(c: Canvas, recyclerView: RecyclerView, view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        }

        /**
         * clearView 调取
         */
        private fun clearView(view: View) {
            val tag = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (tag is Float) {
                ViewCompat.setElevation(view, tag)
            }
            view.setTag(R.id.item_touch_helper_previous_elevation, null)
            view.translationX = 0f
            view.translationY = 0f
        }

        /**
         * onSelectedChanged 调取
         */
        private fun onSelected(view: View) {
        }

        /**
         * interpolateOutOfBoundsScroll 调取
         */
        private fun getMaxDragScroll(recyclerView: RecyclerView): Int {
            if (mCachedMaxScrollSpeed == -1) {
                mCachedMaxScrollSpeed = recyclerView.resources.getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame)
            }
            return mCachedMaxScrollSpeed
        }
        // </editor-fold>
    }

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

    fun getAbsoluteMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val flags = getMovementFlags(recyclerView, viewHolder)
        return convertToAbsoluteDirection(flags, ViewCompat.getLayoutDirection(recyclerView))
    }

    fun hasDragFlag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Boolean {
        val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
        return (flags and ItemTouchHelper.ACTION_MODE_DRAG_MASK) != 0
    }

    fun hasSwipeFlag(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Boolean {
        val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
        return (flags and ItemTouchHelper.ACTION_MODE_SWIPE_MASK) != 0
    }

    fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    fun getBoundingBoxMargin(): Int {
        return 0
    }

    fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue
    }

    fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return defaultValue
    }

    fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .5f
    }

    fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .5f
    }

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

    fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            onSelected(viewHolder.itemView)
        }
    }

    fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is ItemTouchHelper.ViewDropHandler) {
            (layoutManager as? ItemTouchHelper.ViewDropHandler)?.prepareForDrop(viewHolder.itemView, target.itemView, x, y)
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

    fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        clearView(viewHolder.itemView)
    }

    fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        onDraw(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive)
    }

    fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        onDrawOver(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive)
    }

    fun getAnimationDuration(recyclerView: RecyclerView, animationType: Int, animateDx: Float, animateDy: Float): Long {
        val itemAnimator = recyclerView.itemAnimator
        return if (itemAnimator == null) {
            (if (animationType == ItemTouchHelper.ANIMATION_TYPE_DRAG) DEFAULT_DRAG_ANIMATION_DURATION else DEFAULT_SWIPE_ANIMATION_DURATION).toLong()
        } else {
            if (animationType == ItemTouchHelper.ANIMATION_TYPE_DRAG) itemAnimator.moveDuration else itemAnimator.removeDuration
        }
    }

    fun interpolateOutOfBoundsScroll(recyclerView: RecyclerView, viewSize: Int, viewSizeOutOfBounds: Int, totalSize: Int, msSinceStartScroll: Long): Int {
        val maxScroll = getMaxDragScroll(recyclerView)
        val absOutOfBounds = abs(viewSizeOutOfBounds)
        val direction = sign(viewSizeOutOfBounds.toFloat()).toInt()
        val outOfBoundsRatio = min(1f, 1f * absOutOfBounds / viewSize)
        val cappedScroll = (direction * maxScroll * sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio)).toInt()
        val timeRatio = if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
            1f
        } else {
            msSinceStartScroll.toFloat() / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS
        }
        val value = (cappedScroll * sDragScrollInterpolator.getInterpolation(timeRatio)).toInt()
        if (value == 0) {
            return if (viewSizeOutOfBounds > 0) 1 else -1
        }
        return value
    }

    open fun isLongPressDragEnabled(): Boolean {
        return true
    }

    open fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    abstract fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int

    abstract fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean

    abstract fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

}