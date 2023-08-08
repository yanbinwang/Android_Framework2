package com.github.fujianlian.klinechart

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
 * Created by tian on 2016/5/3.
 */
abstract class ScrollAndScaleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener, OnScaleGestureListener {
    protected var isLongPress = false//是否长按
    protected var touch = false
    protected var mScaleX = 1f
    protected var mScaleXMax = 2f
    protected var mScaleXMin = 0.5f
    protected var mScrollX = 0
    protected var mDetector: GestureDetectorCompat? = null
    protected var mScaleDetector: ScaleGestureDetector? = null
    private var mMultipleTouch = false
    private var mScrollEnable = true
    private var mScaleEnable = true
    private var x = 0f
    private var mScroller: OverScroller? = null

    init {
        setWillNotDraw(false)
        mDetector = GestureDetectorCompat(getContext(), this)
        mScaleDetector = ScaleGestureDetector(getContext(), this)
        mScroller = OverScroller(getContext())
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (!isLongPress && !isMultipleTouch()) {
            scrollBy(distanceX.roundToInt(), 0)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        isLongPress = true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!isTouch() && isScrollEnable()) {
            mScroller?.fling(mScrollX, 0, (velocityX / mScaleX).roundToInt(), 0, Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
        }
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

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!isScaleEnable()) {
            return false
        }
        val oldScale = mScaleX
        mScaleX *= detector.scaleFactor
        if (mScaleX < mScaleXMin) {
            mScaleX = mScaleXMin
        } else if (mScaleX > mScaleXMax) {
            mScaleX = mScaleXMax
        } else {
            onScaleChanged(mScaleX, oldScale)
        }
        return true
    }

    open fun onScaleChanged(scale: Float, oldScale: Float) {
        invalidate()
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 按压手指超过1个
        if (event.pointerCount > 1) {
            isLongPress = false
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                touch = true
                x = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                //长按之后移动
                if (isLongPress) {
                    onLongPress(event)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> invalidate()
            MotionEvent.ACTION_UP -> {
                if (x == event.x) {
                    if (isLongPress) {
                        isLongPress = false
                    }
                }
                touch = false
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                isLongPress = false
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

    /**
     * 滑到了最左边
     */
    abstract fun onLeftSide()

    /**
     * 滑到了最右边
     */
    abstract fun onRightSide()

    /**
     * 是否在触摸中
     *
     * @return
     */
    fun isTouch(): Boolean {
        return touch
    }

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

    /**
     * 设置ScrollX
     *
     * @param scrollX
     */
    override fun setScrollX(scrollX: Int) {
        mScrollX = scrollX
        scrollTo(scrollX, 0)
    }

    /**
     * 是否是多指触控
     *
     * @return
     */
    fun isMultipleTouch(): Boolean {
        return mMultipleTouch
    }

    protected fun checkAndFixScrollX() {
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            mScroller?.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            mScroller?.forceFinished(true)
        }
    }

    fun getScaleXMax(): Float {
        return mScaleXMax
    }

    fun getScaleXMin(): Float {
        return mScaleXMin
    }

    fun isScrollEnable(): Boolean {
        return mScrollEnable
    }

    fun isScaleEnable(): Boolean {
        return mScaleEnable
    }

    /**
     * 设置缩放的最大值
     */
    fun setScaleXMax(scaleXMax: Float) {
        mScaleXMax = scaleXMax
    }

    /**
     * 设置缩放的最小值
     */
    fun setScaleXMin(scaleXMin: Float) {
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

    override fun getScaleX(): Float {
        return mScaleX
    }
}