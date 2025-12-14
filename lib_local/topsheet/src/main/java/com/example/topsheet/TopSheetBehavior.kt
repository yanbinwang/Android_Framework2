package com.example.topsheet

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import androidx.core.os.ParcelableCompat
import androidx.core.os.ParcelableCompatCreatorCallbacks
import androidx.core.view.MotionEventCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.VelocityTrackerCompat
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
import androidx.core.content.withStyledAttributes

@SuppressLint("PrivateResource")
class TopSheetBehavior<V : View>(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<V>(context, attrs) {
    private val HIDE_THRESHOLD = 0.5f
    private val HIDE_FRICTION = 0.1f
    private var mMaximumVelocity = 0f
    private var mPeekHeight = 0
    private var mMinOffset = 0
    private var mMaxOffset = 0
    private var mLastNestedScrollDy = 0
    private var mParentHeight = 0
    private var mActivePointerId = 0
    private var mInitialY = 0
    private var mHideable = false
    private var mSkipCollapsed = true
    private var mIgnoreEvents = false
    private var mTouchingScrollingChild = false
    private var mNestedScrolled = false
    private var mNestedScrollingChildRef: WeakReference<View>? = null
    private var mVelocityTracker: VelocityTracker? = null

    companion object {
        /**
         * The bottom sheet is dragging.
         */
        const val STATE_DRAGGING = 1

        /**
         * The bottom sheet is settling.
         */
        const val STATE_SETTLING = 2

        /**
         * The bottom sheet is expanded.
         */
        const val STATE_EXPANDED = 3

        /**
         * The bottom sheet is collapsed.
         */
        const val STATE_COLLAPSED = 4

        /**
         * The bottom sheet is hidden.
         */
        const val STATE_HIDDEN = 5

        /**
         * @hide
         */
        @IntDef(STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class State

        /**
         * A utility function to get the [TopSheetBehavior] associated with the `view`.
         *
         * @param view The [View] with [TopSheetBehavior].
         * @return The [TopSheetBehavior] associated with the `view`.
         */
        @JvmStatic
        fun <V : View> from(view: V): TopSheetBehavior<V>? {
            val params = view.layoutParams
            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
            val behavior = params.behavior
            require(behavior is TopSheetBehavior<*>) { "The view is not associated with TopSheetBehavior" }
            return behavior as? TopSheetBehavior<V>?
        }

        @JvmStatic
        fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        @JvmStatic
        fun constrain(amount: Float, low: Float, high: Float): Float {
            return if (amount < low) low else if (amount > high) high else amount
        }

        @State
        private var mState = STATE_EXPANDED
        private var mViewRef: WeakReference<View>? = null
        private var mViewDragHelper: ViewDragHelper? = null
        private var mCallback: TopSheetCallback? = null

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
    }

    /**
     * Default constructor for inflating TopSheetBehaviors from layout.
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

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable? {
        return super.onSaveInstanceState(parent, child)?.let { SavedState(it, mState) }
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as? SavedState
        ss?.superState?.let { super.onRestoreInstanceState(parent, child, it) }
        // Intermediate states are restored as collapsed state
        mState = if (ss?.state == STATE_DRAGGING || ss?.state == STATE_SETTLING) {
            STATE_COLLAPSED
        } else {
            ss?.state.orZero
        }
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            ViewCompat.setFitsSystemWindows(child, true)
        }
        val savedTop = child.top
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)
        // Offset the bottom sheet
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

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown()) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(event)
        // Record the velocity
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
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX: Int = event.x.toSafeInt()
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
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = mNestedScrollingChildRef?.get()
        return action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, event.x.toSafeInt(), event.y.toSafeInt()) && abs((mInitialY - event.y).toSafeDouble()) > mViewDragHelper?.touchSlop.orZero
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown()) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(event)
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (mViewDragHelper != null) {
            mViewDragHelper?.processTouchEvent(event)
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (abs((mInitialY - event.y).toSafeDouble()) > mViewDragHelper?.touchSlop.orZero) {
                mViewDragHelper?.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !mIgnoreEvents
    }

    @Deprecated("Deprecated in Java")
    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        mLastNestedScrollDy = 0
        mNestedScrolled = false
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray) {
        val scrollingChild = mNestedScrollingChildRef?.get()
        if (target !== scrollingChild) {
            return
        }
        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) { // Upward
            if (!ViewCompat.canScrollVertically(target, 1)) {
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
        } else if (dy < 0) { // Downward
            // Negative to check scrolling up, positive to check scrolling down
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
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        return target == mNestedScrollingChildRef?.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY))
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels.
     * @attr ref R.styleable#TopSheetBehavior_Params_behavior_peekHeight
     */
    fun setPeekHeight(peekHeight: Int?) {
        mPeekHeight = max(0.0, peekHeight?.toSafeDouble().orZero).toSafeInt()
        //        mMaxOffset = mParentHeight - peekHeight;
        if (mViewRef?.get() != null) {
            mMinOffset = max(-mViewRef?.get()?.height.orZero.toSafeDouble(), -(mViewRef?.get()?.height.orZero - mPeekHeight).toSafeDouble()).toSafeInt()
        }
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet.
     * @attr ref R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    fun getPeekHeight(): Int {
        return mPeekHeight
    }

    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable `true` to make this bottom sheet hideable.
     * @attr ref R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    fun setHideable(hideable: Boolean) {
        mHideable = hideable
    }

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return `true` if this bottom sheet can hide.
     * @attr ref R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    fun isHideable(): Boolean {
        return mHideable
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
     * @attr ref R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    fun setSkipCollapsed(skipCollapsed: Boolean) {
        mSkipCollapsed = skipCollapsed
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    fun getSkipCollapsed(): Boolean {
        return mSkipCollapsed
    }

    /**
     * Sets a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    fun setTopSheetCallback(callback: TopSheetCallback) {
        mCallback = callback
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of [.STATE_COLLAPSED], [.STATE_EXPANDED], or
     * [.STATE_HIDDEN].
     */
    fun setState(@State state: Int) {
        if (state == mState) {
            return
        }
        if (mViewRef == null) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
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
            ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of [.STATE_EXPANDED], [.STATE_COLLAPSED], [.STATE_DRAGGING],
     * and [.STATE_SETTLING].
     */
    @State
    fun getState(): Int {
        return mState
    }

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

    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

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

    private fun getYVelocity(): Float {
        mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
        return VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId)
    }

    private val mDragCallback by lazy { object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (mState == STATE_DRAGGING) {
                return false
            }
            if (mTouchingScrollingChild) {
                return false
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                val scroll = mNestedScrollingChildRef?.get()
                if (scroll != null && ViewCompat.canScrollVertically(scroll, -1)) {
                    // Let the content scroll up
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
            @State val targetState: Int
            if (yvel > 0) { // Moving up
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
                ViewCompat.postOnAnimation(releasedChild, SettleRunnable(releasedChild, targetState))
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
    }}

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

    private fun shouldHide(child: View, yvel: Float): Boolean {
        if (child.top > mMinOffset) {
            // It should not hide, but collapse.
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return abs((newTop - mMinOffset).toSafeDouble()) / mPeekHeight.toSafeFloat() > HIDE_THRESHOLD
    }

    private class SettleRunnable(private val mView: View, @State private val mTargetState: Int) : Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper?.continueSettling(true).orFalse) {
                ViewCompat.postOnAnimation(mView, this)
            } else {
                setStateInternal(mTargetState)
            }
        }
    }

    private class SavedState : AbsSavedState {
        @State
        var state: Int? = null

        companion object {
            @JvmField
            val CREATOR = ParcelableCompat.newCreator(object : ParcelableCompatCreatorCallbacks<SavedState?> {
                override fun createFromParcel(parcel: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(parcel, loader)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            })
        }

        constructor(source: Parcel) : super(source, null)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            //noinspection ResourceType
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
     * Callback for monitoring events about bottom sheets.
     */
    abstract class TopSheetCallback {
        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState    The new state. This will be one of [.STATE_DRAGGING],
         * [.STATE_SETTLING], [.STATE_EXPANDED],
         * [.STATE_COLLAPSED], or [.STATE_HIDDEN].
         */
        abstract fun onStateChanged(bottomSheet: View, @State newState: Int)

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within its range, from 0 to 1
         * when it is moving upward, and from 0 to -1 when it moving downward.
         */
        abstract fun onSlide(bottomSheet: View, slideOffset: Float)
    }

}