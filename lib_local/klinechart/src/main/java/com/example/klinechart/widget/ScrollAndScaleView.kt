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
 * 可以滑动和放大的view
 */
abstract class ScrollAndScaleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, OnScaleGestureListener {
    private var x = 0f
    private var mMultipleTouch = false
    private var mScrollEnable = true
    private var mScaleEnable = true
    private var mScroller: OverScroller? = null
    protected var mScrollX = 0
    protected var mScaleX = 1f
    protected var mScaleXMax = 2f
    protected var mScaleXMin = 0.5f
    protected var mIsLongPress = false
    protected var touch = false
    protected var mDetector: GestureDetectorCompat? = null
    protected var mScaleDetector: ScaleGestureDetector? = null

    init {
        setWillNotDraw(false)
        mDetector = GestureDetectorCompat(getContext(), this)
        mScaleDetector = ScaleGestureDetector(getContext(), this)
        mScroller = OverScroller(getContext())
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!isTouch() && isScrollEnable()) {
            mScroller?.fling(mScrollX, 0, (velocityX / mScaleX).roundToInt(), 0, Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        mIsLongPress = true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (!mIsLongPress && !isMultipleTouch()) {
            scrollBy(distanceX.roundToInt(), 0)
            return true
        }
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

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

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        // 按压手指超过1个
        if (event.pointerCount.orZero > 1) {
            mIsLongPress = false
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                touch = true
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
                touch = false
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsLongPress = false
                touch = false
                invalidate()
            }
            else -> {}
        }
        mMultipleTouch = event.pointerCount > 1
        mDetector?.onTouchEvent(event)
        mScaleDetector?.onTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (mScroller?.computeScrollOffset().orFalse) {
            if (!isTouch()) {
                scrollTo(mScroller?.currX.orZero, mScroller?.currY.orZero)
            } else {
                mScroller?.forceFinished(true)
            }
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        scrollTo(mScrollX - (x / mScaleX).roundToInt(), 0)
    }

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

    override fun getScaleX(): Float {
        return mScaleX
    }

    /**
     * 设置ScrollX
     *
     * @param scrollX
     */
    override fun setScrollX(scrollX: Int) {
        this.mScrollX = scrollX
        scrollTo(scrollX, 0)
    }

    protected open fun onScaleChanged(scale: Float, oldScale: Float) {
        invalidate()
    }

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
     * 是否在触摸中
     *
     * @return
     */
    fun isTouch(): Boolean {
        return touch
    }

    /**
     * 是否是多指触控
     *
     * @return
     */
    fun isMultipleTouch(): Boolean {
        return mMultipleTouch
    }

    fun isScrollEnable(): Boolean {
        return mScrollEnable
    }

    fun isScaleEnable(): Boolean {
        return mScaleEnable
    }

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
     * 设置缩放的最小值
     */
    open fun setScaleXMin(scaleXMin: Float) {
        mScaleXMin = scaleXMin
    }

    /**
     * 设置是否可以滑动
     */
    open fun setScrollEnable(scrollEnable: Boolean) {
        mScrollEnable = scrollEnable
    }

    /**
     * 设置是否可以缩放
     */
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
     *
     * @return
     */
    abstract fun getMinScrollX(): Int

    /**
     * 获取位移的最大值
     *
     * @return
     */
    abstract fun getMaxScrollX(): Int

}