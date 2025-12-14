package com.example.framework.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.MotionEventCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.example.framework.utils.function.value.toSafeInt

/**
 * @description 屏蔽滑动手势，处理CoordinatorLayout和AppBarLayout嵌套使用滑动问题
 * @author yan
 * <!-- 父布局：CoordinatorLayout -->
 * <androidx.coordinatorlayout.widget.CoordinatorLayout>
 * <!-- 顶部可折叠的AppBarLayout -->
 * <com.google.android.material.appbar.AppBarLayout>
 * <!-- 标题栏等 -->
 * </com.google.android.material.appbar.AppBarLayout>
 *
 * <!-- 中间容器：用SupportNestedFrameLayout替代普通FrameLayout，解决滑动冲突 -->
 * <com.example.framework.utils.SupportNestedFrameLayout
 * app:layout_behavior="@string/appbar_scrolling_view_behavior">
 *
 * <!-- 内部可滚动子View：如ViewPager、RecyclerView -->
 * <androidx.viewpager2.widget.ViewPager2>
 * <!-- 子页面包含RecyclerView -->
 * </androidx.viewpager2.widget.ViewPager2>
 * </com.example.framework.utils.SupportNestedFrameLayout>
 * </androidx.coordinatorlayout.widget.CoordinatorLayout>
 *
 * 仅支持垂直滚动：代码中 startNestedScroll 固定传 SCROLL_AXIS_VERTICAL，不处理水平滚动（符合 AppBarLayout 垂直折叠的场景，如需水平滚动需修改 axes 参数）；
 * 依赖嵌套滚动机制：必须配合支持嵌套滚动的父布局（如 CoordinatorLayout）和子 View（如 RecyclerView，默认支持嵌套滚动），如果是普通 ScrollView ,其本身需手动开启 setNestedScrollingEnabled(true)；
 * 触摸事件拦截：onTouchEvent 直接返回 true，表示 “消费所有触摸事件”
 * 这是为了避免事件被其他 View 拦截，但需注意：如果容器内有非滚动 View（如按钮），需确保点击事件能正常传递（可通过重写 onInterceptTouchEvent 优化，当前代码未处理，可能存在点击冲突风险）。
 */
@SuppressLint("ClickableViewAccessibility")
class SupportNestedFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingChild, NestedScrollingParent {
    private var mLastY = 0
    private val mScrollOffset by lazy { IntArray(2) }
    private val mScrollConsumed by lazy { IntArray(2) }
    private val mParentHelper by lazy { NestedScrollingParentHelper(this) }
    private val mChildHelper by lazy { NestedScrollingChildHelper(this) }

    init {
        isNestedScrollingEnabled = true
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return if (mChildHelper.isNestedScrollingEnabled) {
            mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
        } else {
            false
        }
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return if (mChildHelper.isNestedScrollingEnabled) {
            mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
        } else {
            false
        }
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return if (mChildHelper.isNestedScrollingEnabled) {
            mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
        } else {
            false
        }
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return if (mChildHelper.isNestedScrollingEnabled) {
            mChildHelper.dispatchNestedPreFling(velocityX, velocityY)
        } else {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        val y = event?.y.toSafeInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录初始触摸位置
                mLastY = y
                // 开启垂直嵌套滚动
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> {
                // 开启垂直嵌套滚动
                var dy = mLastY - y
                // 开启垂直嵌套滚动
                val oldY = scrollY
                // 先让父布局消耗滚动（比如CoordinatorLayout滑AppBarLayout）
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    // 父布局消耗多少，就从总dy中减去多少
                    dy -= mScrollConsumed[1]
                }
                // 更新上一次Y坐标（处理偏移，避免位置偏差）
                mLastY = y - mScrollOffset[1]
                // 处理剩余的dy（父布局没消耗完的部分）
                // 向下滑（dy为负，说明当前Y > 上一次Y，想把AppBarLayout滑展开）
                if (dy < 0) {
                    // 确保滚动位置不小于0（避免滑出屏幕外）
                    val newScrollY = 0.coerceAtLeast(oldY + dy)
                    // 计算容器自身消耗的dy
                    dy -= newScrollY - oldY
                    // 通知父布局同步剩余滚动（比如容器滑到顶了，剩余dy让父布局处理）
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        // 同步偏移
                        mLastY -= mScrollOffset[1]
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopNestedScroll()
        }
        return true
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        // 通知协作
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes)
        // 父布局处理
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
    }

    override fun onStopNestedScroll(child: View) {
        mParentHelper.onStopNestedScroll(child)
        stopNestedScroll()
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        // 把子的消耗传上去
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