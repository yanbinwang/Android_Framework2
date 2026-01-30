package com.example.common.widget.xrecyclerview.touch

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Interpolator
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ChildDrawingOrderCallback
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.common.R
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.logWTF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import androidx.core.graphics.withSave
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.toSafeLong
import kotlin.math.roundToInt

/**
 * 自定义 ItemTouchHelper（整合拖拽/滑动删除逻辑）
 * // 1. 创建 Callback（指定允许上下拖拽、左滑删除）
 * SimpleItemTouchCallBack callback = new SimpleItemTouchCallBack(new OnItemTouchListener() {
 * public void onItemMove(int fromPos, int toPos) {
 * // 拖拽换位：更新 Adapter 数据
 * Collections.swap(mDataList, fromPos, toPos);
 * mAdapter.notifyItemMoved(fromPos, toPos);
 * }
 * public void onItemDelete(int position) {
 * // 左滑删除：删除 Adapter 数据
 * mDataList.remove(position);
 * mAdapter.notifyItemRemoved(position);
 * }
 * });
 * // 2. 创建 XItemTouchHelper 并绑定到 RecyclerView
 * ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
 * itemTouchHelper.attachToRecyclerView(mRecyclerView);
 * // 3. 可选：开关左滑删除 --> 要实现侧滑删除条目，把 false 改成 true 就可以了
 * callback.setmSwipeEnable(true);
 * // 4. 可选：监听是否正在拖拽
 * itemTouchHelper.setOnMoveListener(isMoving -> {
 * // 拖拽时屏蔽列表点击
 * mRecyclerView.setEnabled(!isMoving);
 * });
 */
class ItemTouchHelper(private val mCallback: Callback) : ItemDecoration(),
    OnChildAttachStateChangeListener {
    private var mSlop = 0
    private var mSelectedFlags = 0
    private var mSwipeEscapeVelocity = 0f
    private var mMaxSwipeVelocity = 0f
    private var mDx = 0f
    private var mDy = 0f
    private var mInitialTouchX = 0f
    private var mInitialTouchY = 0f
    private var mSelectedStartX = 0f
    private var mSelectedStartY = 0f
    private var mDragScrollStartTimeInMs = 0L
    private var mActivePointerId = ACTIVE_POINTER_ID_NONE
    private var mActionState = ACTION_STATE_IDLE
    private var mRecoverAnimations = ArrayList<ItemTouchHelper.RecoverAnimation>()
    private var mDistances: MutableList<Int>? = null
    private var mRecyclerView: RecyclerView? = null
    private var mTmpRect: Rect? = null
    private var mSelected: RecyclerView.ViewHolder? = null
    private var mSwapTargets: MutableList<RecyclerView.ViewHolder?>? = null
    private var mItemTouchHelperGestureListener: ItemTouchHelperGestureListener? = null
    private val mTmpPosition = FloatArray(2)
    private val mPendingCleanup = ArrayList<View>()
    private val mChildDrawingOrderCallback: ChildDrawingOrderCallback? = null
    var mOverdrawChildPosition = -1
    var mOverdrawChild: View? = null
    var mVelocityTracker: VelocityTracker? = null
    var mGestureDetector: GestureDetectorCompat? = null

    private val mScrollRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mSelected != null && scrollIfNecessary()) {
                moveIfNecessary(mSelected)
                mRecyclerView?.removeCallbacks(mScrollRunnable)
                mRecyclerView?.postOnAnimation(this)
            }
        }
    }

    private val mOnItemTouchListener = object : OnItemTouchListener {
        override fun onInterceptTouchEvent(
            recyclerView: RecyclerView,
            event: MotionEvent
        ): Boolean {
            mGestureDetector?.onTouchEvent(event)
                "intercept: x:${event.x},y:${event.y}, ${event}".logWTF(TAG)
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
                "pointer index ${index}".logWTF(TAG)
                if (index >= 0) {
                    checkSelectForSwipe(action, event, index)
                }
            }
            mVelocityTracker?.addMovement(event)
            return mSelected != null
        }

        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
            mGestureDetector?.onTouchEvent(event)
            "on touch: x:${mInitialTouchX},y:${mInitialTouchY}, :${event}".logWTF(TAG)
                mVelocityTracker?.addMovement(event)
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
                    onMoveListener?.isMove(true)
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, mSelectedFlags, activePointerIndex)
                        moveIfNecessary(viewHolder)
                        mRecyclerView?.removeCallbacks(mScrollRunnable)
                        mScrollRunnable.run()
                        mRecyclerView?.invalidate()
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                        mVelocityTracker?.clear()
                    onMoveListener?.isMove(false)
                    select(null, ACTION_STATE_IDLE)
                    mActivePointerId = ACTIVE_POINTER_ID_NONE
                }

                MotionEvent.ACTION_UP -> {
                    onMoveListener?.isMove(false)
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
        const val DIRECTION_FLAG_COUNT = 8
        const val PIXELS_PER_SECOND = 1000
        const val ACTION_MODE_IDLE_MASK = (1 shl DIRECTION_FLAG_COUNT) - 1
        const val ACTION_MODE_SWIPE_MASK = ACTION_MODE_IDLE_MASK shl DIRECTION_FLAG_COUNT
        const val ACTION_MODE_DRAG_MASK = ACTION_MODE_SWIPE_MASK shl DIRECTION_FLAG_COUNT
        private const val TAG = "ItemTouchHelper"

        private fun hitTest(child: View?, x: Float, y: Float, left: Float, top: Float): Boolean {
            return x >= left && x <= left + child?.width.orZero && y >= top && y <= top + child?.height.orZero
        }

        class RecoverAnimation(
            val mViewHolder: RecyclerView.ViewHolder,
            val mAnimationType: Int,
            val mActionState: Int,
            val mStartDx: Float,
            val mStartDy: Float,
            val mTargetX: Float,
            val mTargetY: Float
        ) : Animator.AnimatorListener {
            var mX = 0f
            var mY = 0f
            var mEnded= false
            var mOverridden= false
            var mIsPendingCleanup= false
            private var mFraction = 0f
            private val mValueAnimator = ValueAnimator.ofFloat(0f, 1f)

            init {
                mValueAnimator.addUpdateListener {
                    setFraction(it.animatedFraction.orZero)
                }
                mValueAnimator.setTarget(mViewHolder.itemView)
                mValueAnimator.addListener(this)
                setFraction(0f)
            }

            fun setDuration(duration: Long) {
                mValueAnimator.setDuration(duration)
            }

            fun start() {
                mViewHolder.setIsRecyclable(false)
                mValueAnimator.start()
            }

            fun cancel() {
                mValueAnimator.cancel()
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
        }

        abstract class Callback {
            companion object {
                const val DEFAULT_DRAG_ANIMATION_DURATION = 200
                const val DEFAULT_SWIPE_ANIMATION_DURATION = 250
                const val RELATIVE_DIR_FLAGS =
                    START or END or ((START or END) shl DIRECTION_FLAG_COUNT) or ((START or END) shl (2 * DIRECTION_FLAG_COUNT))
                private const val ABS_HORIZONTAL_DIR_FLAGS =
                    LEFT or RIGHT or ((LEFT or RIGHT) shl DIRECTION_FLAG_COUNT) or ((LEFT or RIGHT) shl (2 * DIRECTION_FLAG_COUNT))
                private const val DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000L
                private val sDragScrollInterpolator = Interpolator {
                    var t = it
                    t * t * t * t * t
                }
                private val sDragViewScrollCapInterpolator = Interpolator {
                    var t = it
                    t -= 1.0f
                    t * t * t * t * t + 1.0f
                }
                private var mCachedMaxScrollSpeed = -1
            }

            fun getDefaultUIUtil(): ItemTouchUIImpl {
                return ItemTouchUIImpl.INSTANCE
            }

            fun convertToRelativeDirection(flags: Int, layoutDirection: Int): Int {
                var mFlags = flags
                val masked = mFlags and ABS_HORIZONTAL_DIR_FLAGS
                if (masked == 0) {
                    return mFlags
                }
                mFlags = mFlags and masked.inv()
                if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    mFlags = mFlags or (masked shl 2)
                    return mFlags
                } else {
                    mFlags =
                        mFlags or ((masked shl 1) and ABS_HORIZONTAL_DIR_FLAGS.inv())
                    mFlags =
                        mFlags or (((masked shl 1) and ABS_HORIZONTAL_DIR_FLAGS) shl 2)
                }
                return mFlags
            }

            fun makeMovementFlags(dragFlags: Int, swipeFlags: Int): Int {
                return makeFlag(ACTION_STATE_IDLE, swipeFlags or dragFlags) or makeFlag(
                    ACTION_STATE_SWIPE,
                    swipeFlags
                ) or makeFlag(ACTION_STATE_DRAG, dragFlags)
            }

            fun makeFlag(actionState: Int, directions: Int): Int {
                return directions shl (actionState * DIRECTION_FLAG_COUNT)
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
                    flags =
                        flags or ((masked shr 1) and RELATIVE_DIR_FLAGS.inv())
                    flags =
                        flags or (((masked shr 1) and RELATIVE_DIR_FLAGS) shr 2)
                }
                return flags
            }

            fun getAbsoluteMovementFlags(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val flags = getMovementFlags(recyclerView, viewHolder)
                return convertToAbsoluteDirection(
                    flags,
                    recyclerView?.let { ViewCompat.getLayoutDirection(it) }.orZero
                )
            }

            fun hasDragFlag(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder
            ): Boolean {
                val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
                return (flags and ACTION_MODE_DRAG_MASK) != 0
            }

            fun hasSwipeFlag(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder
            ): Boolean {
                val flags = getAbsoluteMovementFlags(recyclerView, viewHolder)
                return (flags and ACTION_MODE_SWIPE_MASK) != 0
            }

            fun canDropOver(
                recyclerView: RecyclerView?,
                current: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ): Boolean {
                return true
            }

            fun isLongPressDragEnabled(): Boolean {
                return true
            }

            fun isItemViewSwipeEnabled(): Boolean {
                return true
            }

            fun getBoundingBoxMargin(): Int {
                return 0
            }

            fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return .5f
            }

            fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
                return .5f
            }

            fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return defaultValue
            }

            fun getSwipeVelocityThreshold(defaultValue: Float): Float {
                return defaultValue
            }

            fun chooseDropTarget(
                selected: RecyclerView.ViewHolder?,
                dropTargets: MutableList<RecyclerView.ViewHolder?>?,
                curX: Int,
                curY: Int
            ): RecyclerView.ViewHolder? {
                val right = curX + selected?.itemView?.width.orZero
                val bottom = curY + selected?.itemView?.height.orZero
                var winner: RecyclerView.ViewHolder? = null
                var winnerScore = -1
                val dx = curX - selected?.itemView?.left.orZero
                val dy = curY - selected?.itemView?.top.orZero
                val targetsSize = dropTargets?.size.orZero
                for (i in 0..<targetsSize) {
                    val target = dropTargets.safeGet(i)
                    if (dx > 0) {
                        val diff = target?.itemView?.right.orZero - right
                        if (diff < 0 && target?.itemView?.right.orZero > selected?.itemView?.right.orZero) {
                            val score = abs(diff)
                            if (score > winnerScore) {
                                winnerScore = score
                                winner = target
                            }
                        }
                    }
                    if (dx < 0) {
                        val diff = target?.itemView?.left.orZero - curX
                        if (diff > 0 && target?.itemView?.left.orZero < selected?.itemView?.left.orZero) {
                            val score = abs(diff)
                            if (score > winnerScore) {
                                winnerScore = score
                                winner = target
                            }
                        }
                    }
                    if (dy < 0) {
                        val diff = target?.itemView?.top.orZero - curY
                        if (diff > 0 && target?.itemView?.top.orZero < selected?.itemView?.top.orZero) {
                            val score = abs(diff)
                            if (score > winnerScore) {
                                winnerScore = score
                                winner = target
                            }
                        }
                    }
                    if (dy > 0) {
                        val diff = target?.itemView?.bottom.orZero - bottom
                        if (diff < 0 && target?.itemView?.bottom.orZero > selected?.itemView?.bottom.orZero) {
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
                    ItemTouchUIImpl.INSTANCE.onSelected(viewHolder.itemView)
                }
            }

            private fun getMaxDragScroll(recyclerView: RecyclerView): Int {
                if (mCachedMaxScrollSpeed == -1) {
                    mCachedMaxScrollSpeed = recyclerView.resources
                        .getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame)
                }
                return mCachedMaxScrollSpeed
            }

            fun onMoved(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                fromPos: Int?,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int
            ) {
                val layoutManager: RecyclerView.LayoutManager? = recyclerView?.layoutManager
                if (layoutManager is ViewDropHandler) {
                    (layoutManager as? ViewDropHandler)?.prepareForDrop(
                        viewHolder?.itemView,
                        target.itemView,
                        x,
                        y
                    )
                    return
                }
                if (null != layoutManager) {
                    if (layoutManager.canScrollHorizontally()) {
                        val minLeft = layoutManager.getDecoratedLeft(target.itemView)
                        if (minLeft <= recyclerView.getPaddingLeft()) {
                            recyclerView.scrollToPosition(toPos)
                        }
                        val maxRight = layoutManager.getDecoratedRight(target.itemView)
                        if (maxRight >= recyclerView.width - recyclerView.getPaddingRight()) {
                            recyclerView.scrollToPosition(toPos)
                        }
                    }
                    if (layoutManager.canScrollVertically()) {
                        val minTop = layoutManager.getDecoratedTop(target.itemView)
                        if (minTop <= recyclerView.paddingTop) {
                            recyclerView.scrollToPosition(toPos)
                        }
                        val maxBottom = layoutManager.getDecoratedBottom(target.itemView)
                        if (maxBottom >= recyclerView.height - recyclerView.paddingBottom) {
                            recyclerView.scrollToPosition(toPos)
                        }
                    }
                }
            }

            fun onDraw(
                c: Canvas,
                parent: RecyclerView,
                selected: RecyclerView.ViewHolder?,
                recoverAnimationList: MutableList<ItemTouchHelper.RecoverAnimation>,
                actionState: Int,
                dX: Float,
                dY: Float
            ) {
                val recoverAnimSize = recoverAnimationList.size
                for (i in 0..<recoverAnimSize) {
                    val anim = recoverAnimationList[i]
                    anim.update()
                    c.withSave {
                        onChildDraw(
                            c,
                            parent,
                            anim.mViewHolder,
                            anim.mX,
                            anim.mY,
                            anim.mActionState,
                            false
                        )
                    }
                }
                if (selected != null) {
                    c.withSave {
                        onChildDraw(c, parent, selected, dX, dY, actionState, true)
                    }
                }
            }

            fun onDrawOver(
                c: Canvas,
                parent: RecyclerView,
                selected: RecyclerView.ViewHolder?,
                recoverAnimationList: MutableList<ItemTouchHelper.RecoverAnimation>,
                actionState: Int,
                dX: Float,
                dY: Float
            ) {
                val recoverAnimSize = recoverAnimationList.size
                for (i in 0..<recoverAnimSize) {
                    val anim = recoverAnimationList.get(i)
                    c.withSave {
                        onChildDrawOver(
                            c,
                            parent,
                            anim.mViewHolder,
                            anim.mX,
                            anim.mY,
                            anim.mActionState,
                            false
                        )
                    }
                }
                if (selected != null) {
                    c.withSave {
                        onChildDrawOver(c, parent, selected, dX, dY, actionState, true)
                    }
                }
                var hasRunningAnimation = false
                for (i in recoverAnimSize - 1 downTo 0) {
                    val anim = recoverAnimationList[i]
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

            fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
                viewHolder?.itemView?.let { ItemTouchUIImpl.INSTANCE.clearView(it) }
            }

            fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                ItemTouchUIImpl.INSTANCE.onDraw(
                    c,
                    recyclerView,
                    viewHolder.itemView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            fun onChildDrawOver(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                ItemTouchUIImpl.INSTANCE.onDrawOver(
                    c,
                    recyclerView,
                    viewHolder.itemView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            fun getAnimationDuration(
                recyclerView: RecyclerView?,
                animationType: Int,
                animateDx: Float,
                animateDy: Float
            ): Long {
                val itemAnimator = recyclerView?.itemAnimator
                return if (itemAnimator == null) {
                    (if (animationType == ANIMATION_TYPE_DRAG) DEFAULT_DRAG_ANIMATION_DURATION else DEFAULT_SWIPE_ANIMATION_DURATION).toSafeLong()
                } else {
                    if (animationType == ANIMATION_TYPE_DRAG) itemAnimator.moveDuration else itemAnimator.removeDuration
                }
            }

            fun interpolateOutOfBoundsScroll(
                recyclerView: RecyclerView,
                viewSize: Int,
                viewSizeOutOfBounds: Int,
                totalSize: Int,
                msSinceStartScroll: Long
            ): Int {
                val maxScroll = getMaxDragScroll(recyclerView)
                val absOutOfBounds = abs(viewSizeOutOfBounds)
                val direction = sign(viewSizeOutOfBounds.toSafeFloat()).toSafeInt()
                val outOfBoundsRatio = min(1f, 1f * absOutOfBounds / viewSize)
                val cappedScroll =
                    (direction * maxScroll * sDragViewScrollCapInterpolator.getInterpolation(
                        outOfBoundsRatio
                    )).toSafeInt()
                val timeRatio: Float
                if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
                    timeRatio = 1f
                } else {
                    timeRatio =
                        msSinceStartScroll.toSafeFloat() / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS
                }
                val value =
                    (cappedScroll * sDragScrollInterpolator.getInterpolation(
                        timeRatio
                    )).toSafeInt()
                if (value == 0) {
                    return if (viewSizeOutOfBounds > 0) 1 else -1
                }
                return value
            }

            abstract fun getMovementFlags(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder
            ): Int

            abstract fun onMove(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ): Boolean

            abstract fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

        }

        abstract class SimpleCallback(
            private var mDefaultDragDirs: Int,
            private var mDefaultSwipeDirs: Int
        ) : Callback() {
            fun setDefaultSwipeDirs(defaultSwipeDirs: Int) {
                mDefaultSwipeDirs = defaultSwipeDirs
            }

            fun setDefaultDragDirs(defaultDragDirs: Int) {
                mDefaultDragDirs = defaultDragDirs
            }

            fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder): Int {
                return mDefaultSwipeDirs
            }

            fun getDragDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder): Int {
                return mDefaultDragDirs
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(
                    getDragDirs(
                        recyclerView,
                        viewHolder
                    ), getSwipeDirs(recyclerView, viewHolder)
                )
            }
        }
    }

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
            mSwipeEscapeVelocity =
                resources.getDimension(R.dimen.item_touch_helper_swipe_escape_velocity)
            mMaxSwipeVelocity =
                resources.getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity)
            setupCallbacks()
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

    private fun destroyCallbacks() {
        mRecyclerView?.removeItemDecoration(this)
        mRecyclerView?.removeOnItemTouchListener(mOnItemTouchListener)
        mRecyclerView?.removeOnChildAttachStateChangeListener(this)
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val recoverAnimation = mRecoverAnimations.get(0)
            mCallback.clearView(mRecyclerView, recoverAnimation.mViewHolder)
        }
        mRecoverAnimations.clear()
        mOverdrawChild = null
        mOverdrawChildPosition = -1
        releaseVelocityTracker()
        stopGestureDetection()
    }

    private fun startGestureDetection() {
        mItemTouchHelperGestureListener = ItemTouchHelperGestureListener()
        mRecyclerView?.context?.let { context ->
            mItemTouchHelperGestureListener?.let {
                mGestureDetector = GestureDetectorCompat(context,it)
            }
        }
    }

    private fun stopGestureDetection() {
        if (mItemTouchHelperGestureListener != null) {
            mItemTouchHelperGestureListener?.doNotReactToLongPress()
            mItemTouchHelperGestureListener = null
        }
        if (mGestureDetector != null) {
            mGestureDetector = null
        }
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
            outPosition[1] = mSelected?.itemView?.getTranslationY().orZero
        }
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

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        mOverdrawChildPosition = -1
        var dx = 0f
        var dy = 0f
        if (mSelected != null) {
            getSelectedDxDy(mTmpPosition)
            dx = mTmpPosition[0]
            dy = mTmpPosition[1]
        }
        mCallback.onDraw(c, parent, mSelected, mRecoverAnimations, mActionState, dx, dy)
    }

    fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
        if (selected == mSelected && actionState == mActionState) {
            return
        }
        mDragScrollStartTimeInMs = Long.MIN_VALUE
        val prevActionState = mActionState
        endRecoverAnimation(selected, true)
        mActionState = actionState
        if (actionState == ACTION_STATE_DRAG) {
            mOverdrawChild = selected?.itemView
        }
        val actionStateMask =
            (1 shl (DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState)) - 1
        var preventLayout = false
        if (mSelected != null) {
            val prevSelected = mSelected
            if (prevSelected?.itemView?.parent != null) {
                val swipeDir =
                    if (prevActionState == ACTION_STATE_DRAG) 0 else swipeIfNecessary(prevSelected)
                releaseVelocityTracker()
                val targetTranslateX: Float
                val targetTranslateY: Float
                val animationType: Int
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
                if (prevActionState == ACTION_STATE_DRAG) {
                    animationType = ANIMATION_TYPE_DRAG
                } else if (swipeDir > 0) {
                    animationType = ANIMATION_TYPE_SWIPE_SUCCESS
                } else {
                    animationType = ANIMATION_TYPE_SWIPE_CANCEL
                }
                getSelectedDxDy(mTmpPosition)
                val currentTranslateX = mTmpPosition[0]
                val currentTranslateY = mTmpPosition[1]
                val rv = object :
                    ItemTouchHelper.RecoverAnimation(
                        prevSelected,
                        animationType,
                        prevActionState,
                        currentTranslateX,
                        currentTranslateY,
                        targetTranslateX,
                        targetTranslateY
                    ) {
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
                val duration = mCallback.getAnimationDuration(
                    mRecyclerView,
                    animationType,
                    targetTranslateX - currentTranslateX,
                    targetTranslateY - currentTranslateY
                )
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
            mRecyclerView?.let {
                mSelectedFlags = (mCallback.getAbsoluteMovementFlags(it, selected) and actionStateMask) shr (mActionState * DIRECTION_FLAG_COUNT)
            }
            mSelectedStartX = selected.itemView.left.toSafeFloat()
            mSelectedStartY = selected.itemView.top.toSafeFloat()
            mSelected = selected
            if (actionState == ACTION_STATE_DRAG) {
                mSelected?.itemView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
        val rvParent = mRecyclerView?.parent
        rvParent?.requestDisallowInterceptTouchEvent(mSelected != null)
        if (!preventLayout) {
            if (null != mRecyclerView?.layoutManager) {
                mRecyclerView?.layoutManager?.requestSimpleAnimationsInNextLayout()
            }
        }
        mCallback.onSelectedChanged(mSelected, mActionState)
        mRecyclerView?.invalidate()
    }

    private fun postDispatchSwipe(anim: ItemTouchHelper.RecoverAnimation, swipeDir: Int) {
        mRecyclerView?.post(object : Runnable {
            override fun run() {
                if (mRecyclerView != null && mRecyclerView?.isAttachedToWindow.orFalse && !anim.mOverridden && anim.mViewHolder.adapterPosition != RecyclerView.NO_POSITION) {
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

    fun hasRunningRecoverAnim(): Boolean {
        val size = mRecoverAnimations.size
        for (i in 0..<size) {
            if (!mRecoverAnimations[i].mEnded) {
                return true
            }
        }
        return false
    }

    fun scrollIfNecessary(): Boolean {
        if (mSelected == null) {
            mDragScrollStartTimeInMs = Long.MIN_VALUE
            return false
        }
        val now = System.currentTimeMillis()
        val scrollDuration =
            if (mDragScrollStartTimeInMs == Long.MIN_VALUE) 0 else now - mDragScrollStartTimeInMs
        val lm = mRecyclerView?.layoutManager
        if (mTmpRect == null) {
            mTmpRect = Rect()
        }
        var scrollX = 0
        var scrollY = 0
        if (null != lm) {
            mSelected?.itemView?.let {child->
                mTmpRect?.let {
                    lm.calculateItemDecorationsForChild(child, it)
                }
            }
            if (lm.canScrollHorizontally()) {
                val curX = (mSelectedStartX + mDx).toSafeInt()
                val leftDiff = curX - mTmpRect?.left.orZero - mRecyclerView?.getPaddingLeft().orZero
                if (mDx < 0 && leftDiff < 0) {
                    scrollX = leftDiff
                } else if (mDx > 0) {
                    val rightDiff =
                        curX + mSelected?.itemView?.width.orZero + mTmpRect?.right.orZero - (mRecyclerView?.width.orZero - mRecyclerView?.getPaddingRight().orZero)
                    if (rightDiff > 0) {
                        scrollX = rightDiff
                    }
                }
            }
            if (lm.canScrollVertically()) {
                val curY = (mSelectedStartY + mDy).toSafeInt()
                val topDiff = curY - mTmpRect?.top.orZero - mRecyclerView?.paddingTop.orZero
                if (mDy < 0 && topDiff < 0) {
                    scrollY = topDiff
                } else if (mDy > 0) {
                    val bottomDiff =
                        curY + mSelected?.itemView?.getHeight().orZero + mTmpRect?.bottom.orZero - (mRecyclerView?.height.orZero - mRecyclerView?.getPaddingBottom().orZero)
                    if (bottomDiff > 0) {
                        scrollY = bottomDiff
                    }
                }
            }
        }
        if (scrollX != 0) {
            mRecyclerView?.let {
                scrollX = mCallback.interpolateOutOfBoundsScroll(
                    it,
                    mSelected?.itemView?.width.orZero,
                    scrollX,
                    mRecyclerView?.width.orZero,
                    scrollDuration
                )
            }
        }
        if (scrollY != 0) {
            mRecyclerView?.let {
                scrollY = mCallback.interpolateOutOfBoundsScroll(
                    it,
                    mSelected?.itemView?.height.orZero,
                    scrollY,
                    mRecyclerView?.height.orZero,
                    scrollDuration
                )
            }
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

    private fun findSwapTargets(viewHolder: RecyclerView.ViewHolder?): MutableList<RecyclerView.ViewHolder?>? {
        if (mSwapTargets == null) {
            mSwapTargets = ArrayList<RecyclerView.ViewHolder?>()
            mDistances = ArrayList<Int>()
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
        if (null != lm) {
            val childCount = lm.childCount
            for (i in 0..<childCount) {
                val other = lm.getChildAt(i)
                if (null != other) {
                    if (other == viewHolder?.itemView) {
                        continue
                    }
                    if (other.bottom < top || other.top > bottom || other.right < left || other.left > right) {
                        continue
                    }
                    val otherVh = mRecyclerView?.getChildViewHolder(other)
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

    fun moveIfNecessary(viewHolder: RecyclerView.ViewHolder?) {
        if (mRecyclerView?.isLayoutRequested.orFalse) {
            return
        }
        if (mActionState != ACTION_STATE_DRAG) {
            return
        }
        val threshold = mCallback.getMoveThreshold(viewHolder)
        val x = (mSelectedStartX + mDx).toSafeInt()
        val y = (mSelectedStartY + mDy).toSafeInt()
        if (abs(y - viewHolder?.itemView?.top.orZero) < viewHolder?.itemView?.height.orZero * threshold && abs(
                x - viewHolder?.itemView?.left.orZero
            ) < viewHolder?.itemView?.width.orZero * threshold
        ) {
            return
        }
        val swapTargets = findSwapTargets(viewHolder)
        if (swapTargets?.isEmpty().orFalse) {
            return
        }
        val target = mCallback.chooseDropTarget(viewHolder, swapTargets, x, y)
        if (target == null) {
            mSwapTargets?.clear()
            mDistances?.clear()
            return
        }
        val toPosition = target.adapterPosition
        val fromPosition = viewHolder?.adapterPosition
        if (mCallback.onMove(mRecyclerView, viewHolder, target)) {
            mCallback.onMoved(mRecyclerView, viewHolder, fromPosition, target, toPosition, x, y)
        }
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

    fun endRecoverAnimation(viewHolder: RecyclerView.ViewHolder?, override: Boolean) {
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val anim = mRecoverAnimations.get(i)
            if (anim.mViewHolder == viewHolder) {
                anim.mOverridden = anim.mOverridden or override
                if (!anim.mEnded) {
                    anim.cancel()
                }
                mRecoverAnimations.removeAt(i)
                return
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.setEmpty()
    }

    fun obtainVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
        }
        mVelocityTracker = VelocityTracker.obtain()
    }

    private fun releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
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
        if (null != lm) {
            if (absDx > absDy && lm.canScrollHorizontally()) {
                return null
            } else if (absDy > absDx && lm.canScrollVertically()) {
                return null
            }
        }
        val child = findChildView(motionEvent) ?: return null
        return mRecyclerView?.getChildViewHolder(child)
    }

    fun checkSelectForSwipe(action: Int, motionEvent: MotionEvent, pointerIndex: Int) {
        if (mSelected != null || action != MotionEvent.ACTION_MOVE || mActionState == ACTION_STATE_DRAG || !mCallback.isItemViewSwipeEnabled()) {
            return
        }
        if (mRecyclerView?.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
            return
        }
        val vh = findSwipedView(motionEvent) ?: return
        val movementFlags = mCallback.getAbsoluteMovementFlags(mRecyclerView, vh)
        val swipeFlags =
            (movementFlags and ACTION_MODE_SWIPE_MASK) shr (DIRECTION_FLAG_COUNT * ACTION_STATE_SWIPE)
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

    fun findChildView(event: MotionEvent): View? {
        val x = event.getX()
        val y = event.getY()
        if (mSelected != null) {
            val selectedView = mSelected?.itemView
            if (hitTest(selectedView, x, y, mSelectedStartX + mDx, mSelectedStartY + mDy)) {
                return selectedView
            }
        }
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations.get(i)
            val view = anim.mViewHolder.itemView
            if (hitTest(view, x, y, anim.mX, anim.mY)) {
                return view
            }
        }
        return mRecyclerView?.findChildViewUnder(x, y)
    }

    fun startDrag(viewHolder: RecyclerView.ViewHolder) {
        if (!mCallback.hasDragFlag(mRecyclerView, viewHolder)) {
            "Start drag has been called but dragging is not enabled".logWTF(TAG)
            return
        }
        if (viewHolder.itemView.parent !== mRecyclerView) {
            "Start drag has been called with a view holder which is not a child of " + "the RecyclerView which is controlled by this ItemTouchHelper.".logWTF(TAG)
            return
        }
        obtainVelocityTracker()
        mDy = 0f
        mDx = mDy
        select(viewHolder, ACTION_STATE_DRAG)
    }

    fun startSwipe(viewHolder: RecyclerView.ViewHolder) {
        if (!mCallback.hasSwipeFlag(mRecyclerView, viewHolder)) {
            "Start swipe has been called but swiping is not enabled".logWTF(TAG)
            return
        }
        if (viewHolder.itemView.parent !== mRecyclerView) {
            "Start swipe has been called with a view holder which is not a child of " + "the RecyclerView controlled by this ItemTouchHelper.".logWTF(TAG)
            return
        }
        obtainVelocityTracker()
        mDy = 0f
        mDx = mDy
        select(viewHolder, ACTION_STATE_SWIPE)
    }

    private fun findAnimation(event: MotionEvent): ItemTouchHelper.RecoverAnimation? {
        if (mRecoverAnimations.isEmpty()) {
            return null
        }
        val target = findChildView(event)
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations.get(i)
            if (anim.mViewHolder.itemView == target) {
                return anim
            }
        }
        return null
    }

    fun updateDxDy(ev: MotionEvent, directionFlags: Int, pointerIndex: Int) {
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

    private fun swipeIfNecessary(viewHolder: RecyclerView.ViewHolder): Int {
        if (mActionState == ACTION_STATE_DRAG) {
            return 0
        }
        val originalMovementFlags = mCallback.getMovementFlags(mRecyclerView, viewHolder)
        val absoluteMovementFlags = mCallback.convertToAbsoluteDirection(
            originalMovementFlags, mRecyclerView?.let { ViewCompat.getLayoutDirection(it) }.orZero)
        val flags =
            (absoluteMovementFlags and ACTION_MODE_SWIPE_MASK) shr (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT)
        if (flags == 0) {
            return 0
        }
        val originalFlags =
            (originalMovementFlags and ACTION_MODE_SWIPE_MASK) shr (ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT)
        var swipeDir: Int
        if (abs(mDx) > abs(mDy)) {
            if ((checkHorizontalSwipe(viewHolder, flags).also { swipeDir = it }) > 0) {
                if ((originalFlags and swipeDir) == 0) {
                    return convertToRelativeDirection(
                        swipeDir,
                        mRecyclerView?.let { ViewCompat.getLayoutDirection(it) }.orZero
                    )
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
                    return convertToRelativeDirection(
                        swipeDir,
                        ViewCompat.getLayoutDirection(mRecyclerView?
                    )
                    )
                }
                return swipeDir
            }
        }
        return 0
    }

    private fun checkHorizontalSwipe(viewHolder: RecyclerView.ViewHolder, flags: Int): Int {
        if ((flags and (LEFT or RIGHT)) != 0) {
            val dirFlag = if (mDx > 0) RIGHT else LEFT
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker?.computeCurrentVelocity(
                    PIXELS_PER_SECOND,
                    mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity)
                )
                val xVelocity = mVelocityTracker?.getXVelocity(mActivePointerId)
                val yVelocity = mVelocityTracker?.getYVelocity(mActivePointerId)
                val velDirFlag = if (xVelocity > 0f) RIGHT else LEFT
                val absXVelocity = abs(xVelocity)
                if ((velDirFlag and flags) != 0 && dirFlag == velDirFlag && absXVelocity >= mCallback.getSwipeEscapeVelocity(
                        mSwipeEscapeVelocity
                    ) && absXVelocity > abs(yVelocity)
                ) {
                    return velDirFlag
                }
            }
            val threshold = mRecyclerView?.getWidth() * mCallback.getSwipeThreshold(viewHolder)
            if ((flags and dirFlag) != 0 && abs(mDx) > threshold) {
                return dirFlag
            }
        }
        return 0
    }

    private fun checkVerticalSwipe(viewHolder: RecyclerView.ViewHolder, flags: Int): Int {
        if ((flags and (UP or DOWN)) != 0) {
            val dirFlag = if (mDy > 0) DOWN else UP
            if (mVelocityTracker != null && mActivePointerId > -1) {
                mVelocityTracker?.computeCurrentVelocity(
                    PIXELS_PER_SECOND,
                    mCallback.getSwipeVelocityThreshold(mMaxSwipeVelocity)
                )
                val xVelocity = mVelocityTracker?.getXVelocity(mActivePointerId)
                val yVelocity = mVelocityTracker?.getYVelocity(mActivePointerId)
                val velDirFlag = if (yVelocity > 0f) DOWN else UP
                val absYVelocity = abs(yVelocity)
                if ((velDirFlag and flags) != 0 && velDirFlag == dirFlag && absYVelocity >= mCallback.getSwipeEscapeVelocity(
                        mSwipeEscapeVelocity
                    ) && absYVelocity > abs(xVelocity)
                ) {
                    return velDirFlag
                }
            }
            val threshold = mRecyclerView?.getHeight() * mCallback.getSwipeThreshold(viewHolder)
            if ((flags and dirFlag) != 0 && abs(mDy) > threshold) {
                return dirFlag
            }
        }
        return 0
    }

    fun removeChildDrawingOrderCallbackIfNecessary(view: View?) {
        if (view === mOverdrawChild) {
            mOverdrawChild = null
            if (mChildDrawingOrderCallback != null) {
                mRecyclerView?.setChildDrawingOrderCallback(null)
            }
        }
    }

    interface ViewDropHandler {
        fun prepareForDrop(view: View?, target: View, x: Int, y: Int)
    }

    interface OnMoveListener {
        fun isMove(move: Boolean)
    }

    private var onMoveListener: OnMoveListener? = null

    fun setOnMoveListener(onMoveListener: OnMoveListener) {
        this.onMoveListener = onMoveListener
    }

    private inner class ItemTouchHelperGestureListener() : SimpleOnGestureListener() {
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
            val child: View? = findChildView(e)
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
                        "onlong press: x:${mInitialTouchX},y:${mInitialTouchY}".logWTF(TAG)
                        if (mCallback.isLongPressDragEnabled()) {
                            select(vh, ACTION_STATE_DRAG)
                        }
                    }
                }
            }
        }
    }

}