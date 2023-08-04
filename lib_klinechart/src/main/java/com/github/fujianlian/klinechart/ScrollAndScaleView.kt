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
abstract class ScrollAndScaleView : RelativeLayout, GestureDetector.OnGestureListener, OnScaleGestureListener {
    protected var mScrollX = 0
    protected var mDetector: GestureDetectorCompat? = null
    protected var mScaleDetector: ScaleGestureDetector? = null

    open var isLongPress = false
    private var mScroller: OverScroller? = null

    /**
     * 是否在触摸中
     *
     * @return
     */
    var isTouch = false
        protected set
    protected var mScaleX = 1f

    /**
     * 设置缩放的最大值
     */
    var scaleXMax = 2f

    /**
     * 设置缩放的最小值
     */
    var scaleXMin = 0.5f

    /**
     * 是否是多指触控
     *
     * @return
     */
    var isMultipleTouch = false
        private set

    /**
     * 设置是否可以滑动
     */
    open var isScrollEnable = true

    /**
     * 设置是否可以缩放
     */
    open var isScaleEnable = true

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        mDetector = GestureDetectorCompat(context, this)
        mScaleDetector = ScaleGestureDetector(context, this)
        mScroller = OverScroller(context)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (distanceX.isNaN()) return false
        if (isLongPress || isMultipleTouch) return false
        scrollBy(distanceX.roundToInt(), 0)
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        isLongPress = true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!isTouch && isScrollEnable) {
            //            mScroller?.fling(mScrollX, 0, (velocityX / mScaleX).toSafeInt(), 0, Int.MIN_VALUE, Int.MAX_VALUE,
            //                0, 0)
        }
        return true
    }

    override fun computeScroll() {
        if (mScroller?.computeScrollOffset().orFalse) {
            if (!isTouch) {
                scrollTo(mScroller?.currX.orZero, mScroller?.currY.orZero)
            } else {
                mScroller?.forceFinished(true)
            }
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        scrollTo(mScrollX - Math.round(x / mScaleX), 0)
    }

    override fun scrollTo(x: Int, y: Int) {
        if (!isScrollEnable) {
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
        if (!isScaleEnable) {
            return false
        }
        val oldScale = mScaleX
        mScaleX *= detector.scaleFactor
        if (mScaleX < scaleXMin) {
            mScaleX = scaleXMin
        } else if (mScaleX > scaleXMax) {
            mScaleX = scaleXMax
        } else {
            onScaleChanged(mScaleX, oldScale)
        }
        return true
    }

    protected open fun onScaleChanged(scale: Float, oldScale: Float) {
        invalidate()
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 按压手指超过1个
        if (event.pointerCount > 1) {
            isLongPress = false
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isTouch = true
            }
            MotionEvent.ACTION_MOVE ->                 //长按之后移动
                if (isLongPress) {
                    //长按时强制捕捉事件
                    parent.requestDisallowInterceptTouchEvent(true)
                    onLongPress(event)
                }
            MotionEvent.ACTION_POINTER_UP -> invalidate()
            MotionEvent.ACTION_UP -> {
                isLongPress = false
                isTouch = false
                //释放事件
                parent.requestDisallowInterceptTouchEvent(false)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                isLongPress = false
                isTouch = false
                //释放事件
                parent.requestDisallowInterceptTouchEvent(false)
                invalidate()
            }
            else -> {
            }
        }
        isMultipleTouch = event.pointerCount > 1
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

    protected fun checkAndFixScrollX() {
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX()
            mScroller?.forceFinished(true)
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX()
            mScroller?.forceFinished(true)
        }
    }

    override fun getScaleX(): Float {
        return mScaleX
    }

    override fun setScaleX(scale: Float) {
        mScaleX = scale
        invalidate()
    }
}