package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.framework.utils.function.value.orZero
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 侧滑菜单
 * 1) 第一个 = 左侧菜单 / 第二个 = 主内容
 * <?xml version="1.0" encoding="utf-8"?>
 * <com.example.mvvm.widget.SlidingLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@+id/slidingLayout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:orientation="horizontal">
 *
 *     <!-- 第一个：左侧菜单 -->
 *     <LinearLayout
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         android:background="#f44336"/>
 *
 *     <!-- 第二个：主内容 -->
 *     <LinearLayout
 *         android:id="@+id/contentLayout"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         android:background="#ffffff"/>
 *
 * </com.example.mvvm.widget.SlidingLayout>
 *
 * // 绑定（必须先调用）
 * slidingLayout.bind(this, contentView)
 * // 打开菜单
 * slidingLayout.scrollToLeftLayout()
 * // 关闭菜单
 * slidingLayout.scrollToRightLayout()
 * // 判断是否打开
 * val isOpen = slidingLayout.isLeftLayoutVisible()
 */
@SuppressLint("ClickableViewAccessibility")
class SlidingLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnTouchListener {
    // 左侧布局最多可以滑动到的左边缘。值由左侧布局的宽度来定，marginLeft到达此值之后，不能再减少
    private var leftEdge = 0
    // 左侧布局最多可以滑动到的右边缘。值恒为0，即marginLeft到达0之后，不能增加
    private val rightEdge = 0
    // 左侧布局完全显示时，留给右侧布局的宽度值
    private val leftLayoutPadding = 80
    // 记录手指按下时的横坐标
    private var xDown = 0f
    // 记录手指移动时的横坐标
    private var xMove = 0f
    // 记录手指抬起时的横坐标
    private var xUp = 0f
    // 左侧布局当前是显示还是隐藏。只有完全显示或隐藏时才会更改此值，滑动过程中此值无效
    private var isLeftLayoutVisible = false
    // 左侧布局对象
    private var leftLayout: View? = null
    // 右侧布局对象
    private var rightLayout: View? = null
    // 用于监听侧滑事件的View
    private var bindView: View? = null
    // 左侧布局的参数，通过此参数来重新确定左侧布局的宽度，以及更改leftMargin的值
    private var leftLayoutParams: MarginLayoutParams? = null
    // 右侧布局的参数，通过此参数来重新确定右侧布局的宽度
    private var rightLayoutParams: MarginLayoutParams? = null
    // 控件生命周期持有者
    private var lifecycleOwner: LifecycleOwner? = null
    // 滚动Job
    private var scrollJob: Job? = null
    // 用于计算手指滑动的速度。
    private var tracker = VelocityTracker.obtain()

    companion object {
        // 滚动显示和隐藏左侧布局时，手指滑动需要达到的速度
        private const val SNAP_VELOCITY = 200
    }

    /**
     * 在onLayout中重新设定左侧布局和右侧布局的参数
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            // 获取左侧布局对象
            leftLayout = getChildAt(0)
            leftLayoutParams = leftLayout?.layoutParams as? MarginLayoutParams
            // 重置左侧布局对象的宽度为屏幕宽度减去leftLayoutPadding
            leftLayoutParams?.width = screenWidth - leftLayoutPadding
            // 设置最左边距为负的左侧布局的宽度
            leftEdge = -leftLayoutParams?.width.orZero
            leftLayoutParams?.leftMargin = leftEdge
            leftLayout?.layoutParams = leftLayoutParams
            // 获取右侧布局对象
            rightLayout = getChildAt(1)
            rightLayoutParams = rightLayout?.layoutParams as? MarginLayoutParams
            rightLayoutParams?.width = screenWidth
            rightLayout?.layoutParams = rightLayoutParams
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        createVelocityTracker(event)
        when (event.action) {
            // 手指按下时，记录按下时的横坐标
            MotionEvent.ACTION_DOWN -> {
                xDown = event.rawX
            }
            MotionEvent.ACTION_MOVE -> {
                // 手指移动时，对比按下时的横坐标，计算出移动的距离，来调整左侧布局的leftMargin值，从而显示和隐藏左侧布局
                xMove = event.rawX
                val distanceX = (xMove - xDown).toInt()
                if (isLeftLayoutVisible) {
                    leftLayoutParams?.leftMargin = distanceX
                } else {
                    leftLayoutParams?.leftMargin = leftEdge + distanceX
                }
                if (leftLayoutParams?.leftMargin.orZero < leftEdge) {
                    leftLayoutParams?.leftMargin = leftEdge
                } else if (leftLayoutParams?.leftMargin.orZero > rightEdge) {
                    leftLayoutParams?.leftMargin = rightEdge
                }
                leftLayout?.layoutParams = leftLayoutParams
            }
            MotionEvent.ACTION_UP -> {
                // 手指抬起时，进行判断当前手势的意图，从而决定是滚动到左侧布局，还是滚动到右侧布局
                xUp = event.rawX
                if (wantToShowLeftLayout()) {
                    if (shouldScrollToLeftLayout()) {
                        scrollToLeftLayout()
                    } else {
                        scrollToRightLayout()
                    }
                } else if (wantToShowRightLayout()) {
                    if (shouldScrollToContent()) {
                        scrollToRightLayout()
                    } else {
                        scrollToLeftLayout()
                    }
                }
                recycleVelocityTracker()
            }
        }
        return isBindBasicLayout()
    }

    /**
     * 判断当前手势的意图是不是想显示右侧布局。如果手指移动的距离是负数，且当前左侧布局是可见的，则认为当前手势是想要显示右侧布局
     * @return 当前手势想显示右侧布局返回true，否则返回false
     */
    private fun wantToShowRightLayout(): Boolean {
        return xUp - xDown < 0 && isLeftLayoutVisible
    }

    /**
     * 判断当前手势的意图是不是想显示左侧布局。如果手指移动的距离是正数，且当前左侧布局是不可见的，则认为当前手势是想要显示左侧布局
     */
    private fun wantToShowLeftLayout(): Boolean {
        return xUp - xDown > 0 && !isLeftLayoutVisible
    }

    /**
     * 判断是否应该滚动将左侧布局展示出来。如果手指移动距离大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY，就认为应该滚动将左侧布局展示出来
     */
    private fun shouldScrollToLeftLayout(): Boolean {
        return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY
    }

    /**
     * 判断是否应该滚动将右侧布局展示出来。如果手指移动距离加上leftLayoutPadding大于屏幕的1/2，或者手指移动速度大于SNAP_VELOCITY， 就认为应该滚动将右侧布局展示出来
     * @return 如果应该滚动将右侧布局展示出来返回true，否则返回false
     */
    private fun shouldScrollToContent(): Boolean {
        return xDown - xUp + leftLayoutPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY
    }

    /**
     * 判断绑定滑动事件的View是不是一个基础layout，不支持自定义layout，只支持四种基本layout,AbsoluteLayout已被弃用
     * @return 如果绑定滑动事件的View是LinearLayout,RelativeLayout,FrameLayout,TableLayout之一就返回true，否则返回false
     */
    private fun isBindBasicLayout(): Boolean {
        if (bindView == null) {
            return false
        }
        val viewName = bindView?.javaClass?.name
        return viewName == LinearLayout::class.java.name || viewName == RelativeLayout::class.java.name || viewName == FrameLayout::class.java.name || viewName == TableLayout::class.java.name
    }

    /**
     * 创建VelocityTracker对象，并将触摸事件加入到VelocityTracker当中 ,右侧布局监听控件的滑动事件
     */
    private fun createVelocityTracker(event: MotionEvent) {
        tracker.addMovement(event)
    }

    /**
     * 获取手指在右侧布局的监听View上的滑动速度
     * @return 滑动速度，以每秒钟移动了多少像素值为单位
     */
    private fun getScrollVelocity(): Int {
        tracker.computeCurrentVelocity(1000)
        val velocity = tracker.xVelocity.toInt()
        return abs(velocity)
    }

    //回收VelocityTracker对象。
    private fun recycleVelocityTracker() {
        tracker.recycle()
        tracker = null
    }

    private fun scroll(speed: Int) {
        scrollJob?.cancel()
        scrollJob = lifecycleOwner?.lifecycleScope?.launch(Main.immediate) {
            var leftMargin = leftLayoutParams?.leftMargin.orZero
            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
            while (true) {
                leftMargin += speed
                if (leftMargin > rightEdge) {
                    leftMargin = rightEdge
                    break
                }
                if (leftMargin < leftEdge) {
                    leftMargin = leftEdge
                    break
                }
                leftLayoutParams?.leftMargin = leftMargin
                leftLayout?.layoutParams = leftLayoutParams
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
                delay(20)
            }
            isLeftLayoutVisible = speed > 0
            leftLayoutParams?.leftMargin = leftMargin
            leftLayout?.layoutParams = leftLayoutParams
        }
    }

    /**
     * 绑定监听侧滑事件的View，即在绑定的View进行滑动才可以显示和隐藏左侧布局
     */
    fun bind(owner: LifecycleOwner, view: View) {
        lifecycleOwner = owner
        bindView = view
        bindView?.setOnTouchListener(this)
    }

    /**
     * 将屏幕滚动到左侧布局界面，滚动速度设定为30
     */
    fun scrollToLeftLayout() {
        scroll(30)
    }

    /**
     * 将屏幕滚动到右侧布局界面，滚动速度设定为-30
     */
    fun scrollToRightLayout() {
        scroll(-30)
    }

    /**
     * 左侧布局是否完全显示出来，或完全隐藏，滑动过程中此值无效
     * @return 左侧布局完全显示返回true，完全隐藏返回false。
     */
    fun isLeftLayoutVisible(): Boolean {
        return isLeftLayoutVisible
    }

}