package com.example.common.widget.xrecyclerview.gesture

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Rect
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import com.example.common.widget.xrecyclerview.gesture.BaseGestureCallback.Companion.convertToAbsoluteDirection
import com.example.common.widget.xrecyclerview.gesture.BaseGestureCallback.Companion.convertToRelativeDirection
import com.example.common.widget.xrecyclerview.gesture.BaseGestureCallback.Companion.hitTest
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * 为了兼容下拉刷新，上拉加载已经拖拽手势，进行自定义ItemDecorationHelper类
 * 1) 使用方法
 * // 拖拽移动和左滑删除
 * val callBack = ItemTouchCallBack(mBinding?.adapter)
 * // 要实现侧滑删除条目，把 false 改成 true 就可以了
 * callBack.setSwipeEnable(false)
 * val helper = ItemDecorationHelper(callBack)
 *  // 设置是否关闭刷新
 * helper.setOnMoveListener { move ->
 * if (move) {
 * mBinding?.xrvList?.refresh.disable()
 *  } else {
 * mBinding?.xrvList?.refresh.enable()
 * }
 * }
 * helper.attachToRecyclerView(mBinding?.xrvList?.recycler)
 * 2) 适配器需要继承ItemTouchHelperCallBack.OnItemTouchListener并重写
 * 3) 更改完数据后可不请求服务器,而是setResult丢回列表集合后请求
 */
class ItemDecorationHelper(private val mCallback: BaseGestureCallback) : ItemDecoration(), OnChildAttachStateChangeListener {
    private var mSlop = 0
    private var mSelectedFlags = 0
    private var mActivePointerId = ACTIVE_POINTER_ID_NONE
    private var mActionState = ACTION_STATE_IDLE
    private var mDx = 0f
    private var mDy = 0f
    private var mSelectedStartX = 0f
    private var mSelectedStartY = 0f
    private var mInitialTouchX = 0f
    private var mInitialTouchY = 0f
    private var mSwipeEscapeVelocity = 0f
    private var mMaxSwipeVelocity = 0f
    private var mDragScrollStartTimeInMs = 0L
    private var mOverdrawChild: View? = null
    private var mSelected: RecyclerView.ViewHolder? = null
    private var mRecyclerView: RecyclerView? = null
    private var mTmpRect: Rect? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mDistances: MutableList<Int>? = null
    private var mSwapTargets: MutableList<RecyclerView.ViewHolder>? = null
    private var mOnMoveListener: OnMoveListener? = null
    private var mGestureCallback: ItemGestureListener? = null
    private var mGestureDetector: GestureDetectorCompat? = null

    private val mTmpPosition = FloatArray(2)
    private val mPendingCleanup = ArrayList<View>()
    private val mRecoverAnimations = ArrayList<RecoverAnimation>()
    private val mScrollRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mSelected != null && scrollIfNecessary()) {
                moveIfNecessary(mSelected)
                mRecyclerView?.removeCallbacks(mScrollRunnable)
                mRecyclerView?.postOnAnimation(this)
            }
        }
    }
    private val mChildDrawingOrderCallback = object : RecyclerView.ChildDrawingOrderCallback {
        override fun onGetChildDrawingOrder(childCount: Int, i: Int): Int {
            if (mSelected == null) {
                // 无拖拽时，按默认顺序绘制
                return i
            }
            // 获取被拖拽 Item 的索引
            val selectedIndex = mRecyclerView?.indexOfChild(mSelected?.itemView).orZero
            if (selectedIndex == -1) {
                return i
            }
            // 让被拖拽的 Item 最后绘制（显示在最上层）
            if (i == childCount - 1) {
                return selectedIndex
            }
            if (i >= selectedIndex) {
                return i + 1
            }
            return i
        }
    }
    private val mOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
            mGestureDetector?.onTouchEvent(event)
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                mActivePointerId = event.getPointerId(0)
                mInitialTouchX = event.x
                mInitialTouchY = event.y
                obtainVelocityTracker()
                if (mSelected == null) {
                    val animation = findAnimation(event)
                    if (animation != null) {
                        mInitialTouchX -= animation.mX
                        mInitialTouchY -= animation.mY
                        endRecoverAnimation(animation.mViewHolder, true)
                        if (mPendingCleanup.remove(animation.mViewHolder.itemView)) {
                            mCallback.clearView(mRecyclerView, animation.mViewHolder)
                        }
                        select(animation.mViewHolder, animation.mActionState)
                        updateDxDy(event, mSelectedFlags, 0)
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mActivePointerId = ACTIVE_POINTER_ID_NONE
                select(null, ACTION_STATE_IDLE)
            } else if (mActivePointerId != ACTIVE_POINTER_ID_NONE) {
                val index = event.findPointerIndex(mActivePointerId)
                if (index >= 0) {
                    checkSelectForSwipe(action, event, index)
                }
            }
            if (mVelocityTracker != null) {
                mVelocityTracker?.addMovement(event)
            }
            return mSelected != null
        }

        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
            mGestureDetector?.onTouchEvent(event)
            if (mVelocityTracker != null) {
                mVelocityTracker?.addMovement(event)
            }
            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
                return
            }
            val action = event.actionMasked
            val activePointerIndex = event.findPointerIndex(mActivePointerId)
            if (activePointerIndex >= 0) {
                checkSelectForSwipe(action, event, activePointerIndex)
            }
            val viewHolder = mSelected ?: return
            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    mOnMoveListener?.isMove(true)
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, mSelectedFlags, activePointerIndex)
                        moveIfNecessary(viewHolder)
                        mRecyclerView?.removeCallbacks(mScrollRunnable)
                        mScrollRunnable.run()
                        mRecyclerView?.invalidate()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (mVelocityTracker != null) {
                        mVelocityTracker?.clear()
                    }
                    mOnMoveListener?.isMove(false)
                    select(null, ACTION_STATE_IDLE)
                    mActivePointerId = ACTIVE_POINTER_ID_NONE
                }
                MotionEvent.ACTION_UP -> {
                    mOnMoveListener?.isMove(false)
                    select(null, ACTION_STATE_IDLE)
                    mActivePointerId = ACTIVE_POINTER_ID_NONE
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mActivePointerId = event.getPointerId(newPointerIndex)
                        updateDxDy(event, mSelectedFlags, pointerIndex)
                    }
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            if (!disallowIntercept) {
                return
            }
            select(null, ACTION_STATE_IDLE)
        }
    }

    companion object {
        const val UP = 1
        const val DOWN = 1 shl 1
        const val LEFT = 1 shl 2
        const val RIGHT = 1 shl 3
        const val START = LEFT shl 2
        const val END = RIGHT shl 2
        const val ACTION_STATE_IDLE = 0
        const val ACTION_STATE_SWIPE = 1
        const val ACTION_STATE_DRAG = 2
        const val ANIMATION_TYPE_SWIPE_SUCCESS = 1 shl 1
        const val ANIMATION_TYPE_SWIPE_CANCEL = 1 shl 2
        const val ANIMATION_TYPE_DRAG = 1 shl 3
        const val ACTIVE_POINTER_ID_NONE = -1
        const val PIXELS_PER_SECOND = 1000
        const val DIRECTION_FLAG_COUNT = 8
        const val ACTION_MODE_IDLE_MASK = (1 shl DIRECTION_FLAG_COUNT) - 1
        const val ACTION_MODE_SWIPE_MASK = ACTION_MODE_IDLE_MASK shl DIRECTION_FLAG_COUNT
        const val ACTION_MODE_DRAG_MASK = ACTION_MODE_SWIPE_MASK shl DIRECTION_FLAG_COUNT
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var dx = 0f
        var dy = 0f
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition)
            dx = mTmpPosition[0]
            dy = mTmpPosition[1]
        }
        mCallback.onDraw(c, parent, mSelected, mRecoverAnimations, mActionState, dx, dy)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var dx = 0f
        var dy = 0f
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition)
            dx = mTmpPosition[0]
            dy = mTmpPosition[1]
        }
        mCallback.onDrawOver(c, parent, mSelected, mRecoverAnimations, mActionState, dx, dy)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.setEmpty()
    }

    override fun onChildViewAttachedToWindow(view: View) {
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        removeChildDrawingOrderCallbackIfNecessary(view)
        val holder = mRecyclerView?.getChildViewHolder(view) ?: return
        if (mSelected != null && holder == mSelected) {
            select(null, ACTION_STATE_IDLE)
        } else {
            endRecoverAnimation(holder, false)
            if (mPendingCleanup.remove(holder.itemView)) {
                mCallback.clearView(mRecyclerView, holder)
            }
        }
    }

    private fun scrollIfNecessary(): Boolean {
        if (mSelected == null) {
            mDragScrollStartTimeInMs = Long.MIN_VALUE
            return false
        }
        val now = System.currentTimeMillis()
        val scrollDuration = if (mDragScrollStartTimeInMs == Long.MIN_VALUE) 0 else now - mDragScrollStartTimeInMs
        val lm = mRecyclerView?.layoutManager
        if (mTmpRect == null) {
            mTmpRect = Rect()
        }
        var scrollX = 0
        var scrollY = 0
        mSelected?.itemView?.let { child ->
            mTmpRect?.let { outRect ->
                lm?.calculateItemDecorationsForChild(child, outRect)
            }
        }
        if (lm?.canScrollHorizontally().orFalse) {
            val curX = (mSelectedStartX + mDx).toSafeInt()
            val leftDiff = curX - mTmpRect?.left.orZero - mRecyclerView?.getPaddingLeft().orZero
            if (mDx < 0 && leftDiff < 0) {
                scrollX = leftDiff
            } else if (mDx > 0) {
                val rightDiff = curX + mSelected?.itemView?.width.orZero + mTmpRect?.right.orZero - (mRecyclerView?.width.orZero - mRecyclerView?.getPaddingRight().orZero)
                if (rightDiff > 0) {
                    scrollX = rightDiff
                }
            }
        }
        if (lm?.canScrollVertically().orFalse) {
            val curY = (mSelectedStartY + mDy).toSafeInt()
            val topDiff = curY - mTmpRect?.top.orZero - mRecyclerView?.paddingTop.orZero
            if (mDy < 0 && topDiff < 0) {
                scrollY = topDiff
            } else if (mDy > 0) {
                val bottomDiff = curY + mSelected?.itemView?.height.orZero + mTmpRect?.bottom.orZero - (mRecyclerView?.height.orZero - mRecyclerView?.paddingBottom.orZero)
                if (bottomDiff > 0) {
                    scrollY = bottomDiff
                }
            }
        }
        if (scrollX != 0) {
            scrollX = mCallback.interpolateOutOfBoundsScroll(mRecyclerView, mSelected?.itemView?.width.orZero, scrollX, mRecyclerView?.width.orZero, scrollDuration)
        }
        if (scrollY != 0) {
            scrollY = mCallback.interpolateOutOfBoundsScroll(mRecyclerView, mSelected?.itemView?.height.orZero, scrollY, mRecyclerView?.height.orZero, scrollDuration)
        }
        if (scrollX != 0 || scrollY != 0) {
            if (mDragScrollStartTimeInMs == Long.MIN_VALUE) {
                mDragScrollStartTimeInMs = now
            }
            mRecyclerView?.scrollBy(scrollX, scrollY)
            return true
        }
        mDragScrollStartTimeInMs = Long.MIN_VALUE
        return false
    }

    private fun getSelectedDxDy(outPosition: FloatArray) {
        if ((mSelectedFlags and (LEFT or RIGHT)) != 0) {
            outPosition[0] = mSelectedStartX + mDx - mSelected?.itemView?.left.orZero
        } else {
            outPosition[0] = mSelected?.itemView?.translationX.orZero
        }
        if ((mSelectedFlags and (UP or DOWN)) != 0) {
            outPosition[1] = mSelectedStartY + mDy - mSelected?.itemView?.top.orZero
        } else {
            outPosition[1] = mSelected?.itemView?.translationY.orZero
        }
    }

    private fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
        if (selected == mSelected && actionState == mActionState) {
            return
        }
        mDragScrollStartTimeInMs = Long.MIN_VALUE
        val prevActionState = mActionState
        endRecoverAnimation(selected, true)
        mActionState = actionState
        if (actionState == ACTION_STATE_DRAG) {
            mOverdrawChild = selected?.itemView
            // 设置绘制顺序回调，让拖拽 Item 显示在最上层
            mRecyclerView?.setChildDrawingOrderCallback(mChildDrawingOrderCallback)
        }
        val actionStateMask = (1 shl (DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState)) - 1
        var preventLayout = false
        if (mSelected != null) {
            val prevSelected = mSelected
            if (prevSelected?.itemView?.parent != null) {
                val swipeDir = if (prevActionState == ACTION_STATE_DRAG) 0 else swipeIfNecessary(prevSelected)
                releaseVelocityTracker()
                val targetTranslateX: Float
                val targetTranslateY: Float
                when (swipeDir) {
                    LEFT, RIGHT, START, END -> {
                        targetTranslateY = 0f
                        targetTranslateX = sign(mDx) * mRecyclerView?.width.orZero
                    }
                    UP, DOWN -> {
                        targetTranslateX = 0f
                        targetTranslateY = sign(mDy) * mRecyclerView?.height.orZero
                    }
                    else -> {
                        targetTranslateX = 0f
                        targetTranslateY = 0f
                    }
                }
                val animationType = if (prevActionState == ACTION_STATE_DRAG) {
                    ANIMATION_TYPE_DRAG
                } else if (swipeDir > 0) {
                    ANIMATION_TYPE_SWIPE_SUCCESS
                } else {
                    ANIMATION_TYPE_SWIPE_CANCEL
                }
                getSelectedDxDy(mTmpPosition)
                val currentTranslateX = mTmpPosition[0]
                val currentTranslateY = mTmpPosition[1]
                val rv = object : RecoverAnimation(prevSelected, animationType, prevActionState, currentTranslateX, currentTranslateY, targetTranslateX, targetTranslateY) {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (this.mOverridden) {
                            return
                        }
                        if (swipeDir <= 0) {
                            mCallback.clearView(mRecyclerView, prevSelected)
                        } else {
                            mPendingCleanup.add(prevSelected.itemView)
                            mIsPendingCleanup = true
                            postDispatchSwipe(this, swipeDir)
                        }
                        if (mOverdrawChild == prevSelected.itemView) {
                            removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView)
                        }
                    }
                }
                val duration = mCallback.getAnimationDuration(mRecyclerView, animationType, targetTranslateX - currentTranslateX, targetTranslateY - currentTranslateY)
                rv.setDuration(duration)
                mRecoverAnimations.add(rv)
                rv.start()
                preventLayout = true
            } else {
                removeChildDrawingOrderCallbackIfNecessary(prevSelected?.itemView)
                mCallback.clearView(mRecyclerView, prevSelected)
            }
            mSelected = null
        }
        if (selected != null) {
            mSelectedFlags = (mCallback.getAbsoluteMovementFlags(mRecyclerView, selected) and actionStateMask) shr (mActionState * DIRECTION_FLAG_COUNT)
            mSelectedStartX = selected.itemView.left.toFloat()
            mSelectedStartY = selected.itemView.top.toFloat()
            mSelected = selected
            if (actionState == ACTION_STATE_DRAG) {
                mSelected?.itemView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
        val rvParent = mRecyclerView?.parent
        rvParent?.requestDisallowInterceptTouchEvent(mSelected != null)
        if (!preventLayout) {
            mRecyclerView?.layoutManager?.requestSimpleAnimationsInNextLayout()
        }
        mCallback.onSelectedChanged(mSelected, mActionState)
        mRecyclerView?.invalidate()
    }

    private fun postDispatchSwipe(anim: RecoverAnimation, swipeDir: Int) {
        mRecyclerView?.post(object : Runnable {
            override fun run() {
                if (mRecyclerView != null && mRecyclerView?.isAttachedToWindow.orFalse && !anim.mOverridden && anim.mViewHolder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    val animator = mRecyclerView?.itemAnimator
                    if ((animator == null || !animator.isRunning(null)) && !hasRunningRecoverAnim()) {
                        mCallback.onSwiped(anim.mViewHolder, swipeDir)
                    } else {
                        mRecyclerView?.post(this)
                    }
                }
            }
        })
    }

    private fun hasRunningRecoverAnim(): Boolean {
        val size = mRecoverAnimations.size
        for (i in 0..<size) {
            if (!mRecoverAnimations[i].mEnded) {
                return true
            }
        }
        return false
    }

    private fun moveIfNecessary(viewHolder: RecyclerView.ViewHolder?) {
        if (mRecyclerView?.isLayoutRequested.orFalse) {
            return
        }
        if (mActionState != ACTION_STATE_DRAG) {
            return
        }
        val threshold = mCallback.getMoveThreshold(viewHolder)
        val x = (mSelectedStartX + mDx).toSafeInt()
        val y = (mSelectedStartY + mDy).toSafeInt()
        if (abs(y - viewHolder?.itemView?.top.orZero) < viewHolder?.itemView?.height.orZero * threshold && abs(x - viewHolder?.itemView?.left.orZero) < viewHolder?.itemView?.width.orZero * threshold) {
            return
        }
        val swapTargets = findSwapTargets(viewHolder)
        if (swapTargets.isNullOrEmpty()) {
            return
        }
        val target = mCallback.chooseDropTarget(viewHolder, swapTargets, x, y)
        if (target == null) {
            mSwapTargets?.clear()
            mDistances?.clear()
            return
        }
        val toPosition = target.getBindingAdapterPosition()
        val fromPosition = viewHolder?.getBindingAdapterPosition().orZero
        if (mCallback.onMove(mRecyclerView, viewHolder, target)) {
            mCallback.onMoved(mRecyclerView, viewHolder, fromPosition, target, toPosition, x, y)
        }
    }

    private fun findSwapTargets(viewHolder: RecyclerView.ViewHolder?): MutableList<RecyclerView.ViewHolder>? {
        if (mSwapTargets == null) {
            mSwapTargets = ArrayList()
            mDistances = ArrayList()
        } else {
            mSwapTargets?.clear()
            mDistances?.clear()
        }
        val margin = mCallback.getBoundingBoxMargin()
        val left = (mSelectedStartX + mDx).roundToInt() - margin
        val top = (mSelectedStartY + mDy).roundToInt() - margin
        val right = left + viewHolder?.itemView?.width.orZero + 2 * margin
        val bottom = top + viewHolder?.itemView?.height.orZero + 2 * margin
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2
        val lm = mRecyclerView?.layoutManager
        val childCount = lm?.childCount.orZero
        for (i in 0..<childCount) {
            val other = lm?.getChildAt(i)
            if (other == viewHolder?.itemView) {
                continue
            }
            if (other?.bottom.orZero < top || other?.top.orZero > bottom || other?.right.orZero < left || other?.left.orZero > right) {
                continue
            }
            other?.let { child ->
                mRecyclerView?.getChildViewHolder(child)?.let { otherVh ->
                    if (mCallback.canDropOver(mRecyclerView, mSelected, otherVh)) {
                        val dx = abs(centerX - (other.left + other.right) / 2)
                        val dy = abs(centerY - (other.top + other.bottom) / 2)
                        val dist = dx * dx + dy * dy
                        var pos = 0
                        val cnt = mSwapTargets?.size.orZero
                        for (j in 0..<cnt) {
                            if (dist > mDistances?.get(j).orZero) {
                                pos++
                            } else {
                                break
                            }
                        }
                        mSwapTargets?.add(pos, otherVh)
                        mDistances?.add(pos, dist)
                    }
                }
            }
        }
        return mSwapTargets
    }

    private fun endRecoverAnimation(viewHolder: RecyclerView.ViewHolder?, override: Boolean) {
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val anim = mRecoverAnimations[i]
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden = anim.mOverridden || override
                if (!anim.mEnded) {
                    anim.cancel()
                }
                mRecoverAnimations.removeAt(i)
                return
            }
        }
    }

    private fun obtainVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = VelocityTracker.obtain()
    }

    private fun releaseVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun findSwipedView(motionEvent: MotionEvent): RecyclerView.ViewHolder? {
        val lm = mRecyclerView?.layoutManager
        if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
            return null
        }
        val pointerIndex = motionEvent.findPointerIndex(mActivePointerId)
        val dx = motionEvent.getX(pointerIndex) - mInitialTouchX
        val dy = motionEvent.getY(pointerIndex) - mInitialTouchY
        val absDx = abs(dx)
        val absDy = abs(dy)
        if (absDx < mSlop && absDy < mSlop) {
            return null
        }
        if (absDx > absDy && lm?.canScrollHorizontally().orFalse) {
            return null
        } else if (absDy > absDx && lm?.canScrollVertically().orFalse) {
            return null
        }
        val child = findChildView(motionEvent) ?: return null
        return mRecyclerView?.getChildViewHolder(child)
    }

    private fun checkSelectForSwipe(action: Int, motionEvent: MotionEvent, pointerIndex: Int) {
        if (mSelected != null || action != MotionEvent.ACTION_MOVE || mActionState == ACTION_STATE_DRAG || !mCallback.isItemViewSwipeEnabled()) {
            return
        }
        if (mRecyclerView?.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
            return
        }
        val vh = findSwipedView(motionEvent) ?: return
        val movementFlags = mCallback.getAbsoluteMovementFlags(mRecyclerView, vh)
        val swipeFlags = (movementFlags and ACTION_MODE_SWIPE_MASK) shr (DIRECTION_FLAG_COUNT * ACTION_STATE_SWIPE)
        if (swipeFlags == 0) {
            return
        }
        val x = motionEvent.getX(pointerIndex)
        val y = motionEvent.getY(pointerIndex)
        val dx = x - mInitialTouchX
        val dy = y - mInitialTouchY
        val absDx = abs(dx)
        val absDy = abs(dy)
        if (absDx < mSlop && absDy < mSlop) {
            return
        }
        if (absDx > absDy) {
            if (dx < 0 && (swipeFlags and LEFT) == 0) {
                return
            }
            if (dx > 0 && (swipeFlags and RIGHT) == 0) {
                return
            }
        } else {
            if (dy < 0 && (swipeFlags and UP) == 0) {
                return
            }
            if (dy > 0 && (swipeFlags and DOWN) == 0) {
                return
            }
        }
        mDy = 0f
        mDx = mDy
        mActivePointerId = motionEvent.getPointerId(0)
        select(vh, ACTION_STATE_SWIPE)
    }

    private fun findChildView(event: MotionEvent): View? {
        val x = event.x
        val y = event.y
        if (mSelected != null) {
            val selectedView = mSelected?.itemView
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView
            }
        }
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations[i]
            val view = anim.mViewHolder.itemView
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view
            }
        }
        return mRecyclerView?.findChildViewUnder(x, y)
    }

    private fun findAnimation(event: MotionEvent): RecoverAnimation? {
        if (mRecoverAnimations.isEmpty()) {
            return null
        }
        val target = findChildView(event)
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations[i]
            if (anim.mViewHolder.itemView == target) {
                return anim
            }
        }
        return null
    }

    private fun updateDxDy(ev: MotionEvent, directionFlags: Int, pointerIndex: Int) {
        val x = ev.getX(pointerIndex)
        val y = ev.getY(pointerIndex)
        mDx = x - mInitialTouchX
        mDy = y - mInitialTouchY
        if ((directionFlags and LEFT) == 0) {
            mDx = max(0f, mDx)
        }
        if ((directionFlags and RIGHT) == 0) {
            mDx = min(0f, mDx)
        }
        if ((directionFlags and UP) == 0) {
            mDy = max(0f, mDy)
        }
        if ((directionFlags and DOWN) == 0) {
            mDy = min(0f, mDy)
        }
    }

    private fun swipeIfNecessary(viewHolder: RecyclerView.ViewHolder?): Int {
        if (mActionState == ACTION_STATE_DRAG) {
            return 0
        }
        val originalMovementFlags = mCallback.getMovementFlags(mRecyclerView, viewHolder)
        val absoluteMovementFlags = convertToAbsoluteDirection(originalMovementFlags, mRecyclerView?.layoutDirection.orZero)
        val flags = (absoluteMovementFlags and ACTION_MODE_SWIPE_MASK) shr (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT)
        if (flags == 0) {
            return 0
        }
        val originalFlags = (originalMovementFlags and ACTION_MODE_SWIPE_MASK) shr (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT)
        var swipeDir: Int
        if (abs(mDx) > abs(mDy)) {
            if ((checkHorizontalSwipe(viewHolder, flags).also { swipeDir = it }) > 0) {
                if ((originalFlags and swipeDir) == 0) {
                    return convertToRelativeDirection(swipeDir, mRecyclerView?.layoutDirection.orZero)
                }
                return swipeDir
            }
            if ((checkVerticalSwipe(viewHolder, flags).also { swipeDir = it }) > 0) {
                return swipeDir
            }
        } else {
            if ((checkVerticalSwipe(viewHolder, flags).also { swipeDir = it }) > 0) {
                return swipeDir
            }
            if ((checkHorizontalSwipe(viewHolder, flags).also { swipeDir = it }) > 0) {
                if ((originalFlags and swipeDir) == 0) {
                    return convertToRelativeDirection(swipeDir, mRecyclerView?.layoutDirection.orZero)
                }
                return swipeDir
            }
        }
        return 0
    }

    private fun checkHorizontalSwipe(viewHolder: RecyclerView.ViewHolder?, flags: Int): Int {
        if ((flags and (LEFT or RIGHT)) != 0) {
            val dirFlag = if (mDx > 0) RIGHT else LEFT
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker?.computeCurrentVelocity(PIXELS_PER_SECOND, mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity))
                val xVelocity = mVelocityTracker?.getXVelocity(mActivePointerId).orZero
                val yVelocity = mVelocityTracker?.getYVelocity(mActivePointerId).orZero
                val velDirFlag = if (xVelocity > 0f) RIGHT else LEFT
                val absXVelocity = abs(xVelocity)
                if ((velDirFlag and flags) != 0 && dirFlag == velDirFlag && absXVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity) && absXVelocity > abs(yVelocity)) {
                    return velDirFlag
                }
            }
            val threshold = mRecyclerView?.width.orZero * mCallback.getSwipeThreshold(viewHolder)
            if ((flags and dirFlag) != 0 && abs(mDx) > threshold) {
                return dirFlag
            }
        }
        return 0
    }

    private fun checkVerticalSwipe(viewHolder: RecyclerView.ViewHolder?, flags: Int): Int {
        if ((flags and (UP or DOWN)) != 0) {
            val dirFlag = if (mDy > 0) DOWN else UP
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker?.computeCurrentVelocity(PIXELS_PER_SECOND, mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity))
                val xVelocity = mVelocityTracker?.getXVelocity(mActivePointerId).orZero
                val yVelocity = mVelocityTracker?.getYVelocity(mActivePointerId).orZero
                val velDirFlag = if (yVelocity > 0f) DOWN else UP
                val absYVelocity = abs(yVelocity)
                if ((velDirFlag and flags) != 0 && velDirFlag == dirFlag && absYVelocity >= mCallback.getSwipeEscapeVelocity(mSwipeEscapeVelocity) && absYVelocity > abs(xVelocity)) {
                    return velDirFlag
                }
            }
            val threshold = mRecyclerView?.height.orZero * mCallback.getSwipeThreshold(viewHolder)
            if ((flags and dirFlag) != 0 && abs(mDy) > threshold) {
                return dirFlag
            }
        }
        return 0
    }

    private fun removeChildDrawingOrderCallbackIfNecessary(view: View?) {
        if (view == mOverdrawChild) {
            mOverdrawChild = null
            mRecyclerView?.setChildDrawingOrderCallback(null)
        }
    }

    private fun setupCallbacks() {
        val vc = mRecyclerView?.context?.let { ViewConfiguration.get(it) }
        mSlop = vc?.scaledTouchSlop.orZero
        mRecyclerView?.addItemDecoration(this)
        mRecyclerView?.addOnItemTouchListener(mOnItemTouchListener)
        mRecyclerView?.addOnChildAttachStateChangeListener(this)
        startGestureDetection()
    }

    private fun startGestureDetection() {
        mGestureCallback = ItemGestureListener()
        mRecyclerView?.context?.let { context ->
            mGestureCallback?.let { listener ->
                mGestureDetector = GestureDetectorCompat(context, listener)
            }
        }
    }

    private fun stopGestureDetection() {
        if (mGestureCallback != null) {
            mGestureCallback?.doNotReactToLongPress()
            mGestureCallback = null
        }
        if (mGestureDetector != null) {
            mGestureDetector = null
        }
    }

    private fun destroyCallbacks() {
        mRecyclerView?.removeItemDecoration(this)
        mRecyclerView?.removeOnItemTouchListener(mOnItemTouchListener)
        mRecyclerView?.removeOnChildAttachStateChangeListener(this)
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val recoverAnimation = mRecoverAnimations[0]
            mCallback.clearView(mRecyclerView, recoverAnimation.mViewHolder)
        }
        mRecoverAnimations.clear()
        mOverdrawChild = null
        releaseVelocityTracker()
        stopGestureDetection()
    }

    private inner class ItemGestureListener() : SimpleOnGestureListener() {
        private var mShouldReactToLongPress = true

        fun doNotReactToLongPress() {
            mShouldReactToLongPress = false
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (!mShouldReactToLongPress) {
                return
            }
            val child = findChildView(e)
            if (child != null) {
                val vh = mRecyclerView?.getChildViewHolder(child)
                if (vh != null) {
                    if (!mCallback.hasDragFlag(mRecyclerView, vh)) {
                        return
                    }
                    val pointerId = e.getPointerId(0)
                    if (pointerId == mActivePointerId) {
                        val index = e.findPointerIndex(mActivePointerId)
                        val x = e.getX(index)
                        val y = e.getY(index)
                        mInitialTouchX = x
                        mInitialTouchY = y
                        mDy = 0f
                        mDx = mDy
                        if (mCallback.isLongPressDragEnabled()) {
                            select(vh, ACTION_STATE_DRAG)
                        }
                    }
                }
            }
        }
    }

    /**
     * 绑定RecyclerView
     */
    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (mRecyclerView == recyclerView) {
            return
        }
        if (mRecyclerView != null) {
            destroyCallbacks()
        }
        mRecyclerView = recyclerView
        if (recyclerView != null) {
            val resources = recyclerView.resources
            mSwipeEscapeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_velocity)
            mMaxSwipeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity)
            setupCallbacks()
        }
    }

    /**
     * 手动触发拖拽排序手势
     * 主动让传入的 viewHolder 对应的 Item 进入拖拽状态，效果等同于用户长按该 Item 后触发的拖拽，后续可以拖动该 Item 进行上下 / 左右排序
     */
    fun startDrag(viewHolder: RecyclerView.ViewHolder?) {
        // 该Item是否开启了拖拽功能
        if (!mCallback.hasDragFlag(mRecyclerView, viewHolder)) {
            return
        }
        // 该Item是否属于当前RecyclerView
        if (viewHolder?.itemView?.parent != mRecyclerView) {
            return
        }
        // 获取速度追踪器，准备跟踪手势
        obtainVelocityTracker()
        // 重置偏移量，避免残留上次手势的偏移
        mDy = 0f
        mDx = mDy
        // 调用select方法，进入拖拽状态（ACTION_STATE_DRAG）
        select(viewHolder, ACTION_STATE_DRAG)
    }

    /**
     * 手动触发侧滑手势
     * 主动让传入的 viewHolder 对应的 Item 进入侧滑状态，效果等同于用户触摸并滑动该 Item 后触发的侧滑，后续该 Item 会处于可侧滑状态，继续滑动可触发侧滑删除 / 回调。
     */
    fun startSwipe(viewHolder: RecyclerView.ViewHolder?) {
        if (!mCallback.hasSwipeFlag(mRecyclerView, viewHolder)) {
            return
        }
        if (viewHolder?.itemView?.parent != mRecyclerView) {
            return
        }
        obtainVelocityTracker()
        mDy = 0f
        mDx = mDy
        select(viewHolder, ACTION_STATE_SWIPE)
    }

    /**
     * 设置是否正在拖拽移动的监听
     */
    fun setOnMoveListener(listener: OnMoveListener) {
        this.mOnMoveListener = listener
    }

    interface OnMoveListener {
        fun isMove(move: Boolean)
    }

    /**
     * 让 LayoutManager 在「拖拽 Item 即将落到目标 Item 位置时」，提前做布局相关的准备工作，
     * public class CustomLinearLayoutManager extends LinearLayoutManager implements ItemTouchHelper.ViewDropHandler {
     * public CustomLinearLayoutManager(Context context) {
     * super(context);
     * }
     * // 实现布局预处理方法
     * public void prepareForDrop(@NonNull View view, @NonNull View target, int x, int y) {
     * // 1. view → 被拖拽的ItemView（可通过view.getTag()获取对应的ViewHolder）
     * // 2. target → 目标ItemView（同理可获取目标ViewHolder）
     * ItemTouchHelperCallBack.VH holder = (ItemTouchHelperCallBack.VH) view.getTag();
     * ItemTouchHelperCallBack.VH targetHolder = (ItemTouchHelperCallBack.VH) target.getTag();
     * // 示例1：布局预处理 → 让目标Item轻微缩放，提示即将交换位置
     * ViewCompat.animate(target).setDuration(100).scaleX(1.05f).scaleY(1.05f).start();
     * // 示例2：调整被拖拽Item的布局参数，让放下时更贴合
     * ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
     * lp.topMargin = 10;
     * view.setLayoutParams(lp);
     * // 注：这里只做「预处理」，不更新数据——数据更新交给适配器的onMove回调
     * }
     * }
     */
    interface ViewDropHandler {
        fun prepareForDrop(view: View?, target: View?, x: Int, y: Int)
    }

    /**
     * 动画类 (处理手势结束后 Item 的「恢复 / 滑出」动画)
     * @mViewHolder (手势结束前，正在被操作的ViewHolder)
     * @mAnimationType (动画类型标识)
     * ANIMATION_TYPE_SWIPE_SUCCESS -> 滑动（Swipe）手势成功触发后的结束动画
     * ANIMATION_TYPE_SWIPE_CANCEL -> 滑动（Swipe）手势取消后的复位动画
     * ANIMATION_TYPE_DRAG -> 拖拽（Drag）手势结束后的复位动画
     * @mActionState (手势结束前，当前的手势状态（拖拽/滑动/闲置）)
     * prevActionState=ACTION_STATE_DRAG（拖拽手势）→ 无论结果如何，animationType=ANIMATION_TYPE_DRAG（拖拽复位动画）；
     * prevActionState=ACTION_STATE_SWIPE（滑动手势）+ 手势结果 = 成功 → animationType=ANIMATION_TYPE_SWIPE_SUCCESS；
     * prevActionState=ACTION_STATE_SWIPE（滑动手势）+ 手势结果 = 取消 → animationType=ANIMATION_TYPE_SWIPE_CANCEL。
     * @mStartDx/mStartDy/mTargetX/mTargetY (动画从哪开始，到哪结束 单位像素（px）)
     */
    open class RecoverAnimation(val mViewHolder: RecyclerView.ViewHolder, val mAnimationType: Int, val mActionState: Int, private val mStartDx: Float, private val mStartDy: Float, private val mTargetX: Float, private val mTargetY: Float) : Animator.AnimatorListener {
        var mX = 0f
        var mY = 0f
        var mFraction = 0f
        var mEnded = false
        var mOverridden = false
        var mIsPendingCleanup = false
        private var mValueAnimator: ValueAnimator? = null

        init {
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
            mValueAnimator?.addUpdateListener { animation: ValueAnimator? ->
                setFraction(animation?.animatedFraction.orZero)
            }
            mValueAnimator?.setTarget(mViewHolder.itemView)
            mValueAnimator?.addListener(this)
            setFraction(0f)
        }

        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true)
            }
            mEnded = true
        }

        override fun onAnimationCancel(animation: Animator) {
            setFraction(1f)
        }

        override fun onAnimationRepeat(animation: Animator) {
        }

        fun setDuration(duration: Long) {
            mValueAnimator?.setDuration(duration)
        }

        fun start() {
            mViewHolder.setIsRecyclable(false)
            mValueAnimator?.start()
        }

        fun cancel() {
            mValueAnimator?.cancel()
        }

        fun setFraction(fraction: Float) {
            mFraction = fraction
        }

        fun update() {
            mX = if (mStartDx == mTargetX) {
                mViewHolder.itemView.translationX
            } else {
                mStartDx + mFraction * (mTargetX - mStartDx)
            }
            mY = if (mStartDy == mTargetY) {
                mViewHolder.itemView.translationY
            } else {
                mStartDy + mFraction * (mTargetY - mStartDy)
            }
        }

    }

}