package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.*
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toSafeInt

/**
 * @description 屏蔽滑动手势，处理CoordinatorLayout和AppBarLayout嵌套使用滑动问题
 * @author yan
 */
class SupportNestedFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingChild, NestedScrollingParent {
    private var mLastY = 0
    private var mParentHelper: NestedScrollingParentHelper? = null
    private var mChildHelper: NestedScrollingChildHelper? = null
    private val mScrollOffset by lazy { IntArray(2) }
    private val mScrollConsumed by lazy { IntArray(2) }

    init {
        mParentHelper = NestedScrollingParentHelper(this)
        mChildHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper?.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper?.isNestedScrollingEnabled.orFalse
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper?.startNestedScroll(axes).orFalse
    }

    override fun stopNestedScroll() {
        mChildHelper?.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper?.hasNestedScrollingParent().orFalse
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return if (mChildHelper?.isNestedScrollingEnabled.orFalse) {
            mChildHelper?.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow).orFalse
        } else {
            false
        }
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return if (mChildHelper?.isNestedScrollingEnabled.orFalse) {
            mChildHelper?.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow).orFalse
        } else {
            false
        }
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return if (mChildHelper?.isNestedScrollingEnabled.orFalse) {
            mChildHelper?.dispatchNestedFling(velocityX, velocityY, consumed).orFalse
        } else {
            false
        }
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return if (mChildHelper?.isNestedScrollingEnabled.orFalse) {
            mChildHelper?.dispatchNestedPreFling(velocityX, velocityY).orFalse
        } else {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        val y = event?.y.toSafeInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY = y
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> {
                var dy = mLastY - y
                val oldY = scrollY
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1]
                }
                mLastY = y - mScrollOffset[1]
                if (dy < 0) {
                    val newScrollY = 0.coerceAtLeast(oldY + dy)
                    dy -= newScrollY - oldY
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        mLastY -= mScrollOffset[1]
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopNestedScroll()
        }
        return true
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        //通知协作
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        mParentHelper?.onNestedScrollAccepted(child, target, nestedScrollAxes)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL) //父布局处理
    }

    override fun onStopNestedScroll(child: View) {
        mParentHelper?.onStopNestedScroll(child)
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        //把子的消耗传上去
        dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, null)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        dispatchNestedPreScroll(dx, dy, consumed, null)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityX)
    }

    override fun getNestedScrollAxes(): Int {
        return 0
    }

}