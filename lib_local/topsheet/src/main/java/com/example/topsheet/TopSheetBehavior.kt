package com.example.topsheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.MotionEventCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.customview.view.AbsSavedState
import androidx.customview.widget.ViewDragHelper
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max

/**
 * 继承自 CoordinatorLayout.Behavior。它实现了所有的物理交互逻辑：拖拽、嵌套滚动、惯性滑动、状态管理、边界计算等。它是 BottomSheetBehavior 的“垂直镜像版”
 */
@SuppressLint("PrivateResource")
class TopSheetBehavior<V : View>(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<V>(context, attrs) {
    @State
    private var mState = STATE_EXPANDED
    private var mPeekHeight = 0
    private var mMinOffset = 0
    private var mMaxOffset = 0
    private var mLastNestedScrollDy = 0
    private var mParentHeight = 0
    private var mActivePointerId = 0
    private var mInitialY = 0
    private var mMaximumVelocity = 0f
    private var mHideable = false
    private var mSkipCollapsed = true
    private var mIgnoreEvents = false
    private var mTouchingScrollingChild = false
    private var mNestedScrolled = false
    private var mNestedScrollingChildRef: WeakReference<View>? = null
    private var mViewRef: WeakReference<View>? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mViewDragHelper: ViewDragHelper? = null
    private var mCallback: TopSheetCallback? = null

    companion object {
        // 隐藏触发阈值与摩擦系数
        private const val HIDE_THRESHOLD = 0.5f
        private const val HIDE_FRICTION = 0.1f

        // 拖拽中
        const val STATE_DRAGGING = 1
        // settling 动画中
        const val STATE_SETTLING = 2
        // 展开
        const val STATE_EXPANDED = 3
        // 折叠
        const val STATE_COLLAPSED = 4
        // 隐藏
        const val STATE_HIDDEN = 5
        /**
         * 状态值约束注解
         */
        @IntDef(STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class State

        /**
         * 获取指定视图关联的TopSheetBehavior实例
         */
        fun <V : View> from(view: V): TopSheetBehavior<V> {
            val params = view.layoutParams
            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
            val behavior = params.behavior
            require(behavior is TopSheetBehavior<*>) { "The view is not associated with TopSheetBehavior" }
            return behavior as TopSheetBehavior<V>
        }

        /**
         * 数值范围约束工具方法
         */
        fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        fun constrain(amount: Float, low: Float, high: Float): Float {
            return if (amount < low) low else if (amount > high) high else amount
        }
    }

    /**
     * 初始化行为参数：读取XML属性及系统滑动速度配置
     */
    init {
        context.withStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout) {
            setPeekHeight(getDimensionPixelSize(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0))
            setHideable(getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false))
            setSkipCollapsed(getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, true))
        }
        val configuration = ViewConfiguration.get(context)
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toSafeFloat()
    }

    /**
     * 保存面板状态
     */
    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable? {
        return super.onSaveInstanceState(parent, child)?.let { SavedState(it, mState) }
    }

    /**
     * 恢复面板状态，中间态强制转为折叠态
     */
    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as? SavedState
        ss?.superState?.let { super.onRestoreInstanceState(parent, child, it) }
        mState = if (ss?.state == STATE_DRAGGING || ss?.state == STATE_SETTLING) {
            STATE_COLLAPSED
        } else {
            ss?.state.orZero
        }
    }

    /**
     * 布局子视图：计算滑动边界并根据当前状态定位视图
     */
    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (parent.fitsSystemWindows && !child.fitsSystemWindows) {
            child.fitsSystemWindows = true
        }
        val savedTop = child.top
        parent.onLayoutChild(child, layoutDirection)
        mParentHeight = parent.height
        mMinOffset = max(-child.height.toSafeDouble(), -(child.height - mPeekHeight).toSafeDouble()).toSafeInt()
        mMaxOffset = 0
        if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset)
        } else if (mHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, -child.height)
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset)
        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }
        mViewRef = WeakReference<View>(child)
        mNestedScrollingChildRef = WeakReference<View>(findScrollingChild(child))
        return true
    }

    /**
     * 拦截触摸事件：判断是否应接管手势拖拽
     */
    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mTouchingScrollingChild = false
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX = event.x.toSafeInt()
                mInitialY = event.y.toSafeInt()
                val scroll = mNestedScrollingChildRef?.get()
                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                    mActivePointerId = event.getPointerId(event.actionIndex)
                    mTouchingScrollingChild = true
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, initialX, mInitialY)
            }
        }
        if (!mIgnoreEvents && mViewDragHelper?.shouldInterceptTouchEvent(event).orFalse) {
            return true
        }
        val scroll = mNestedScrollingChildRef?.get()
        return action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, event.x.toSafeInt(), event.y.toSafeInt()) && abs((mInitialY - event.y).toSafeDouble()) > mViewDragHelper?.touchSlop.orZero
    }

    /**
     * 处理触摸事件：委托给ViewDragHelper执行拖拽逻辑
     */
    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(event)
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        mViewDragHelper?.processTouchEvent(event)
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (abs((mInitialY - event.y).toSafeDouble()) > mViewDragHelper?.touchSlop.orZero) {
                mViewDragHelper?.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !mIgnoreEvents
    }

    /**
     * 嵌套滚动开始：重置滚动状态标记
     */
    @Deprecated("Deprecated in Java")
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        mLastNestedScrollDy = 0
        mNestedScrolled = false
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    /**
     * 嵌套滚动预处理：在子视图滚动前消费偏移量以驱动面板滑动
     */
    @Deprecated("Deprecated in Java")
    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray) {
        val scrollingChild = mNestedScrollingChildRef?.get()
        if (target !== scrollingChild) {
            return
        }
        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) {
            if (!target.canScrollVertically(1)) {
                if (newTop >= mMinOffset || mHideable) {
                    consumed[1] = dy
                    ViewCompat.offsetTopAndBottom(child, -dy)
                    setStateInternal(STATE_DRAGGING)
                } else {
                    consumed[1] = currentTop - mMinOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    setStateInternal(STATE_COLLAPSED)
                }
            }
        } else if (dy < 0) {
            if (newTop < mMaxOffset) {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            } else {
                consumed[1] = currentTop - mMaxOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            }
        }
        dispatchOnSlide(child.top)
        mLastNestedScrollDy = dy
        mNestedScrolled = true
    }

    /**
     * 嵌套滚动结束：根据滚动方向和速度确定目标状态并启动动画
     */
    @Deprecated("Deprecated in Java")
    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View) {
        if (child.top == mMaxOffset) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (target !== mNestedScrollingChildRef?.get() || !mNestedScrolled) {
            return
        }
        val top: Int
        val targetState: Int
        if (mLastNestedScrollDy < 0) {
            top = mMaxOffset
            targetState = STATE_EXPANDED
        } else if (mHideable && shouldHide(child, getYVelocity())) {
            top = -child.height
            targetState = STATE_HIDDEN
        } else if (mLastNestedScrollDy == 0) {
            val currentTop = child.top
            if (abs((currentTop - mMinOffset).toSafeDouble()) > abs((currentTop - mMaxOffset).toSafeDouble())) {
                top = mMaxOffset
                targetState = STATE_EXPANDED
            } else {
                top = mMinOffset
                targetState = STATE_COLLAPSED
            }
        } else {
            top = mMinOffset
            targetState = STATE_COLLAPSED
        }
        if (mViewDragHelper?.smoothSlideViewTo(child, child.left, top).orFalse) {
            setStateInternal(STATE_SETTLING)
            child.postOnAnimation(SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false
    }

    /**
     * 嵌套Fling预处理：面板展开时允许子视图自行处理惯性滑动
     */
    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        return target == mNestedScrollingChildRef?.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY))
    }

    /**
     * 设置/获取折叠状态下可见高度
     */
    fun setPeekHeight(peekHeight: Int?) {
        mPeekHeight = max(0.0, peekHeight?.toSafeDouble().orZero).toSafeInt()
//        mMaxOffset = mParentHeight - peekHeight
        if (mViewRef?.get() != null) {
            mMinOffset = max(-mViewRef?.get()?.height.orZero.toSafeDouble(), -(mViewRef?.get()?.height.orZero - mPeekHeight).toSafeDouble()).toSafeInt()
        }
    }

    fun getPeekHeight(): Int {
        return mPeekHeight
    }

    /**
     * 设置/获取是否允许下滑隐藏
     */
    fun setHideable(hideable: Boolean) {
        mHideable = hideable
    }

    fun isHideable(): Boolean {
        return mHideable
    }

    /**
     * 设置/获取隐藏时是否跳过折叠状态
     */
    fun setSkipCollapsed(skipCollapsed: Boolean) {
        mSkipCollapsed = skipCollapsed
    }

    fun getSkipCollapsed(): Boolean {
        return mSkipCollapsed
    }

    /**
     * 设置面板状态与滑动回调
     */
    fun setTopSheetCallback(callback: TopSheetCallback) {
        mCallback = callback
    }

    /**
     * 设置面板状态（带动画过渡）
     */
    fun setState(@State state: Int) {
        if (state == mState) {
            return
        }
        if (mViewRef == null) {
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED || mHideable && state == STATE_HIDDEN) {
                mState = state
            }
            return
        }
        val child = mViewRef?.get() ?: return
        val top = if (state == STATE_COLLAPSED) {
            mMinOffset
        } else if (state == STATE_EXPANDED) {
            mMaxOffset
        } else if (mHideable && state == STATE_HIDDEN) {
            -child.height
        } else {
            throw IllegalArgumentException("Illegal state argument: $state")
        }
        setStateInternal(STATE_SETTLING)
        if (mViewDragHelper?.smoothSlideViewTo(child, child.left, top).orFalse) {
            child.postOnAnimation(SettleRunnable(child, state))
        }
    }

    /**
     * 获取当前面板状态
     */
    @State
    fun getState(): Int {
        return mState
    }

    /**
     * 内部状态更新并分发回调
     */
    private fun setStateInternal(@State state: Int) {
        if (mState == state) {
            return
        }
        mState = state
        val bottomSheet = mViewRef?.get()
        if (bottomSheet != null && mCallback != null) {
            mCallback?.onStateChanged(bottomSheet, state)
        }
    }

    /**
     * 重置手势追踪状态
     */
    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    /**
     * 递归查找可嵌套滚动的子视图
     */
    private fun findScrollingChild(view: View): View? {
        if (view is NestedScrollingChild) {
            return view
        }
        if (view is ViewGroup) {
            val group = view
            var i = 0
            val count = group.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(group.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    /**
     * 获取Y轴滑动速度
     */
    private fun getYVelocity(): Float {
        mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
        return mVelocityTracker?.getYVelocity(mActivePointerId).orZero
    }

    /**
     * ViewDragHelper回调：定义拖拽捕获、位置约束及释放后的状态结算逻辑
     */
    private val mDragCallback by lazy {
        object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                if (mState == STATE_DRAGGING) {
                    return false
                }
                if (mTouchingScrollingChild) {
                    return false
                }
                if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                    val scroll = mNestedScrollingChildRef?.get()
                    if (scroll != null && scroll.canScrollVertically(-1)) {
                        return false
                    }
                }
                return mViewRef != null && mViewRef?.get() == child
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                dispatchOnSlide(top)
            }

            override fun onViewDragStateChanged(state: Int) {
                if (state == ViewDragHelper.STATE_DRAGGING) {
                    setStateInternal(STATE_DRAGGING)
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                val top: Int
                @State
                val targetState: Int
                if (yvel > 0) {
                    top = mMaxOffset
                    targetState = STATE_EXPANDED
                } else if (mHideable && shouldHide(releasedChild, yvel)) {
                    top = -mViewRef?.get()?.height.orZero
                    targetState = STATE_HIDDEN
                } else if (yvel == 0f) {
                    val currentTop = releasedChild.top
                    if (abs((currentTop - mMinOffset).toSafeDouble()) > abs((currentTop - mMaxOffset).toSafeDouble())) {
                        top = mMaxOffset
                        targetState = STATE_EXPANDED
                    } else if (mSkipCollapsed) {
                        top = -mViewRef?.get()?.height.orZero
                        targetState = STATE_HIDDEN
                    } else {
                        top = mMinOffset
                        targetState = STATE_COLLAPSED
                    }
                } else if (mSkipCollapsed) {
                    top = -mViewRef?.get()?.height.orZero
                    targetState = STATE_HIDDEN
                } else {
                    top = mMinOffset
                    targetState = STATE_COLLAPSED
                }
                if (mViewDragHelper?.settleCapturedViewAt(releasedChild.left, top).orFalse) {
                    setStateInternal(STATE_SETTLING)
                    releasedChild.postOnAnimation(SettleRunnable(releasedChild, targetState))
                } else {
                    setStateInternal(targetState)
                }
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return constrain(top, if (mHideable) -child.height else mMinOffset, mMaxOffset)
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                return child.left
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return if (mHideable) {
                    child.height
                } else {
                    mMaxOffset - mMinOffset
                }
            }
        }
    }

    /**
     * 计算并分发滑动偏移比例回调
     */
    private fun dispatchOnSlide(top: Int) {
        val bottomSheet = mViewRef?.get()
        if (bottomSheet != null && mCallback != null) {
            if (top < mMinOffset) {
                mCallback?.onSlide(bottomSheet, (top - mMinOffset).toSafeFloat() / mPeekHeight)
            } else {
                mCallback?.onSlide(bottomSheet, (top - mMinOffset).toSafeFloat() / (mMaxOffset - mMinOffset))
            }
        }
    }

    /**
     * 判断当前滑动是否应触发隐藏
     */
    private fun shouldHide(child: View, yvel: Float): Boolean {
        if (child.top > mMinOffset) {
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return abs((newTop - mMinOffset).toSafeDouble()) / mPeekHeight.toSafeFloat() > HIDE_THRESHOLD
    }

    /**
     * 状态过渡动画执行器
     */
    private inner class SettleRunnable(private val mView: View, @field:State private val mTargetState: Int) : Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper?.continueSettling(true).orFalse) {
                mView.postOnAnimation(this)
            } else {
                setStateInternal(mTargetState)
            }
        }
    }

    /**
     * 面板状态持久化Parcelable实现
     */
    private class SavedState : AbsSavedState {
        @State
        var state: Int? = null

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

        constructor(source: Parcel) : super(source, null) {
            state = source.readInt()
        }

        constructor(superState: Parcelable, @State state: Int?) : super(superState) {
            this.state = state
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state.orZero)
        }
    }

    /**
     * 面板事件回调抽象类
     */
    abstract class TopSheetCallback {

        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        abstract fun onSlide(bottomSheet: View, slideOffset: Float)

    }

}