package com.example.klinechart.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.OverScroller
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import kotlin.math.roundToInt

/**
 * 支持滑动和缩放功能的抽象自定义控件ScrollAndScaleView
 */
abstract class ScrollAndScaleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, OnScaleGestureListener {
    private var x = 0f
    private var mTouch = false
    private var mMultipleTouch = false
    private var mScrollEnable = true
    private var mScaleEnable = true
    private var mScroller: OverScroller? = null
    protected var mScrollX = 0
    protected var mScaleX = 1f
    protected var mScaleXMax = 2f
    protected var mScaleXMin = 0.5f
    protected var mIsLongPress = false
    protected var mDetector: GestureDetectorCompat? = null
    protected var mScaleDetector: ScaleGestureDetector? = null

    init {
        // 允许控件重绘
        setWillNotDraw(false)
        // 初始化手势检测器,缩放检测器,滚动器
        mDetector = GestureDetectorCompat(getContext(), this)
        mScaleDetector = ScaleGestureDetector(getContext(), this)
        mScroller = OverScroller(getContext())
    }

    /**
     * 监听按下事件（手指首次接触屏幕）
     */
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    /**
     * 监听快速滑动（甩动）事件，触发惯性滚动
     * 若未触摸且允许滑动，通过OverScroller的fling方法启动惯性滚动，速度会根据当前缩放比例mScaleX调整（缩放后实际速度需要适配）
     */
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!isTouch() && isScrollEnable()) {
            mScroller?.fling(mScrollX, 0, (velocityX / mScaleX).roundToInt(), 0, Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
        }
        return true
    }

    /**
     * 监听长按事件
     * 设置mIsLongPress = true标记长按状态
     */
    override fun onLongPress(e: MotionEvent) {
        mIsLongPress = true
    }

    /**
     * 监听滑动事件（手指在屏幕上移动）
     * 若未长按且非多指触摸，调用scrollBy处理滑动
     */
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (!mIsLongPress && !isMultipleTouch()) {
            scrollBy(distanceX.roundToInt(), 0)
            return true
        }
        return false
    }

    /**
     * 监听按下但未滑动 / 抬起的状态（如长按前的短暂停留）
     */
    override fun onShowPress(e: MotionEvent) {
    }

    /**
     * 监听单击抬起事件
     */
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    /**
     * 监听缩放过程（双指捏合 / 张开）
     * 若不允许缩放，返回false
     * 计算新缩放比例（mScaleX *= 缩放因子），并限制在mScaleXMin（最小）和mScaleXMax（最大）之间。
     * 若缩放比例在有效范围内，调用onScaleChanged通知缩放变化
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!isScaleEnable()) {
            return false
        }
        val oldScale = mScaleX
        mScaleX *= detector.getScaleFactor()
        if (mScaleX < mScaleXMin) {
            mScaleX = mScaleXMin
        } else if (mScaleX > mScaleXMax) {
            mScaleX = mScaleXMax
        } else {
            onScaleChanged(mScaleX, oldScale)
        }
        return true
    }

    /**
     * 监听缩放开始事件
     * true表示允许缩放（必须返回true才能触发后续onScale）
     */
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    /**
     * 监听缩放结束事件
     */
    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    /**
     * 统一处理触摸事件，分发到手势和缩放检测器
     * 处理多指触摸：若手指数量 > 1，重置长按状态（mIsLongPress = false）。
     * 按动作类型（ACTION_DOWN/MOVE/UP等）处理：
     * ACTION_DOWN：标记触摸开始，记录初始 X 坐标。
     * ACTION_MOVE：若处于长按状态，触发onLongPress。
     * ACTION_UP/CANCEL：标记触摸结束，重置状态并刷新界面。
     * 更新多指触摸标记（mMultipleTouch），并将事件分发给mDetector（手势）和mScaleDetector（缩放）。
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        // 按压手指超过1个
        if (event.pointerCount.orZero > 1) {
            mIsLongPress = false
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouch = true
                x = event.x
            }
            // 长按之后移动
            MotionEvent.ACTION_MOVE -> {
                if (mIsLongPress) {
                    onLongPress(event)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (x == event.x) {
                    if (mIsLongPress) {
                        mIsLongPress = false
                    }
                }
                mTouch = false
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsLongPress = false
                mTouch = false
                invalidate()
            }
            else -> {}
        }
        mMultipleTouch = event.pointerCount > 1
        mDetector?.onTouchEvent(event)
        mScaleDetector?.onTouchEvent(event)
        return true
    }

    /**
     * 配合OverScroller处理惯性滚动的帧更新
     * 若滚动器正在滚动（computeScrollOffset()返回true）：
     * 未触摸时，滚动到当前计算的位置（scrollTo）。
     * 触摸时，强制结束滚动（forceFinished(true)）。
     */
    override fun computeScroll() {
        if (mScroller?.computeScrollOffset().orFalse) {
            if (!isTouch()) {
                scrollTo(mScroller?.currX.orZero, mScroller?.currY.orZero)
            } else {
                mScroller?.forceFinished(true)
            }
        }
    }

    /**
     * 相对当前位置滚动指定距离（重写父类方法）
     * 将滚动距离根据当前缩放比例mScaleX调整后，调用scrollTo实现实际滚动（仅处理 X 方向）
     */
    override fun scrollBy(x: Int, y: Int) {
        scrollTo(mScrollX - (x / mScaleX).roundToInt(), 0)
    }

    /**
     * 滚动到绝对位置（重写父类方法）
     * 若不允许滑动，直接结束滚动。
     * 限制滚动范围在getMinScrollX()（最右）和getMaxScrollX()（最左）之间，超出则触发边界回调（onRightSide/onLeftSide）。
     * 调用onScrollChanged通知位置变化，并刷新界面（invalidate）
     */
    override fun scrollTo(x: Int, y: Int) {
        if (!isScrollEnable()) {
            mScroller?.forceFinished(true)
            return
        }
        val oldX = mScrollX
        mScrollX = x
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            onRightSide()
            mScroller?.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            onLeftSide()
            mScroller?.forceFinished(true)
        }
        onScrollChanged(mScrollX, 0, oldX, 0)
        invalidate()
    }

    /**
     * 获取当前 X 方向的缩放比例（重写父类方法）
     */
    override fun getScaleX(): Float {
        return mScaleX
    }

    /**
     * 直接设置 X 方向的滚动位置
     */
    override fun setScrollX(scrollX: Int) {
        this.mScrollX = scrollX
        scrollTo(scrollX, 0)
    }

    /**
     * 缩放比例变化时的回调（保护方法，可重写）
     */
    protected open fun onScaleChanged(scale: Float, oldScale: Float) {
        invalidate()
    }

    /**
     * 检查并修正滚动位置（确保在合法范围内）
     * 若mScrollX超出getMinScrollX()或getMaxScrollX()，强制修正并结束滚动
     */
    protected open fun checkAndFixScrollX() {
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            mScroller?.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            mScroller?.forceFinished(true)
        }
    }

    /**
     * 返回是否处于触摸状态（touch变量）
     */
    fun isTouch(): Boolean {
        return mTouch
    }

    /**
     * 返回是否是多指触摸（mMultipleTouch变量）
     */
    fun isMultipleTouch(): Boolean {
        return mMultipleTouch
    }

    /**
     * 返回是否允许滑动 / 缩放（mScrollEnable/mScaleEnable变量）
     */
    fun isScrollEnable(): Boolean {
        return mScrollEnable
    }

    fun isScaleEnable(): Boolean {
        return mScaleEnable
    }

    /**
     * 返回最大 / 最小缩放比例
     */
    fun getScaleXMax(): Float {
        return mScaleXMax
    }

    fun getScaleXMin(): Float {
        return mScaleXMin
    }

    /**
     * 设置缩放的最大值
     */
    open fun setScaleXMax(scaleXMax: Float) {
        mScaleXMax = scaleXMax
    }

    /**
     * 用于动态设置缩放范围（setScaleXMax/setScaleXMin）和启用 / 禁用滑动 / 缩放（setScrollEnable/setScaleEnable）
     */
    open fun setScaleXMin(scaleXMin: Float) {
        mScaleXMin = scaleXMin
    }

    open fun setScrollEnable(scrollEnable: Boolean) {
        mScrollEnable = scrollEnable
    }

    open fun setScaleEnable(scaleEnable: Boolean) {
        mScaleEnable = scaleEnable
    }

    /**
     * 滑到了最左边
     */
    abstract fun onLeftSide()

    /**
     * 滑到了最右边
     */
    abstract fun onRightSide()

    /**
     * 获取位移的最小值
     */
    abstract fun getMinScrollX(): Int

    /**
     * 获取位移的最大值
     */
    abstract fun getMaxScrollX(): Int

}