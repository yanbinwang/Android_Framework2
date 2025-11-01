package com.example.klinechart.widget

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.dimen
import com.example.klinechart.R
import com.example.klinechart.adapter.IAdapter
import com.example.klinechart.draw.IChartDraw
import com.example.klinechart.draw.MainDraw
import com.example.klinechart.draw.Status
import com.example.klinechart.entity.IKLine
import com.example.klinechart.formatter.IDateTimeFormatter
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.formatter.TimeFormatter
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.utils.ViewUtil
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * k线图
 */
abstract class BaseKLineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollAndScaleView(context, attrs, defStyleAttr) {
    private var displayHeight = 0
    private var mItemCount = 0 // 当前点的个数
    private var mChildDrawPosition = -1
    private var mWidth = 0
    private var mTopPadding = 0
    private var mChildPadding = 0
    private var mBottomPadding = 0
    private var mMainMaxIndex = 0
    private var mMainMinIndex = 0
    private var mStartIndex = 0
    private var mStopIndex = 0
    private var mGridRows = 4
    private var mGridColumns = 4
    private var mSelectedIndex = 0
    private var mMainScaleY = 1f
    private var mVolScaleY = 1f
    private var mChildScaleY = 1f
    private var mDataLen = 0f
    private var mMainHighMaxValue = 0f
    private var mMainLowMinValue = 0f
    private var mPointWidth = 6f
    private var mOverScrollRange = 0f
    private var mLineWidth = 0f
    private var mTranslateX = Float.MIN_VALUE
    private var mMainMaxValue = Float.MAX_VALUE
    private var mMainMinValue = Float.MIN_VALUE
    private var mVolMaxValue = Float.MAX_VALUE
    private var mVolMinValue = Float.MIN_VALUE
    private var mChildMaxValue = Float.MAX_VALUE
    private var mChildMinValue = Float.MIN_VALUE
    private val mAnimationDuration = 500L
    private var isWR = false
    private var isShowChild = false
    private val mGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMaxMinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectedXLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectedYLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectPointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mMainRect: Rect? = null
    private var mVolRect: Rect? = null
    private var mChildRect: Rect? = null
    private var mMainDraw: IChartDraw<Any>? = null
    private var mVolDraw: IChartDraw<Any>? = null
    private var mainDraw: MainDraw? = null
    private var mAdapter: IAdapter? = null
    private var mChildDraw: IChartDraw<Any>? = null
    private var mValueFormatter: IValueFormatter? = null
    private var mDateTimeFormatter: IDateTimeFormatter? = null
    private val mChildDraws = ArrayList<IChartDraw<Any>>()
    private var mAnimator: ValueAnimator? = null
    private var mOnSelectedChangedListener: OnSelectedChangedListener? = null
    private val mDataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            mItemCount = getAdapter()?.getCount().orZero
            notifyChanged()
        }

        override fun onInvalidated() {
            super.onInvalidated()
            mItemCount = getAdapter()?.getCount().orZero
            notifyChanged()
        }
    }

    init {
        setWillNotDraw(false)
        mDetector = GestureDetectorCompat(getContext(), this)
        mScaleDetector = ScaleGestureDetector(getContext(), this)
        mTopPadding = dimen(R.dimen.chart_top_padding).toSafeInt()
        mChildPadding = dimen(R.dimen.child_top_padding).toSafeInt()
        mBottomPadding = dimen(R.dimen.chart_bottom_padding).toSafeInt()
        mAnimator = ValueAnimator.ofFloat(0f, 1f)
        mAnimator?.setDuration(mAnimationDuration)
        mAnimator?.addUpdateListener { invalidate() }
        mSelectorFramePaint.strokeWidth = ViewUtil.Dp2Px(getContext(), 0.6f).toSafeFloat()
        mSelectorFramePaint.style = Paint.Style.STROKE
        mSelectorFramePaint.setColor(Color.WHITE)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.mWidth = w
        displayHeight = h - mTopPadding - mBottomPadding
        initRect()
        setTranslateXFromScrollX(mScrollX)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(mBackgroundPaint.color)
        if (mWidth == 0 || mMainRect?.height() == 0 || mItemCount == 0) {
            return
        }
        calculateValue()
        canvas.withScale(1f, 1f) {
            drawGird(this)
            drawK(this)
            drawText(this)
            drawMaxAndMin(this)
            drawValue(this, if (isLongPress) mSelectedIndex else mStopIndex)
        }
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        val lastIndex = mSelectedIndex
        calculateSelectedX(e.x)
        if (lastIndex != mSelectedIndex) {
            onSelectedChanged(this, getItem(mSelectedIndex), mSelectedIndex)
        }
        invalidate()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        setTranslateXFromScrollX(mScrollX)
    }

    override fun onScaleChanged(scale: Float, oldScale: Float) {
        checkAndFixScrollX()
        setTranslateXFromScrollX(mScrollX)
        super.onScaleChanged(scale, oldScale)
    }

    override fun getMinScrollX(): Int {
        return -(mOverScrollRange / mScaleX).toSafeInt()
    }

    override fun getMaxScrollX(): Int {
        return (getMaxTranslateX() - getMinTranslateX()).roundToInt()
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        mBackgroundPaint.setColor(color)
    }

    private fun initRect() {
        if (isShowChild) {
            val mMainHeight = (displayHeight * 0.6f).toSafeInt()
            val mVolHeight = (displayHeight * 0.2f).toSafeInt()
            val mChildHeight = (displayHeight * 0.2f).toSafeInt()
            mMainRect = Rect(0, mTopPadding, mWidth, mTopPadding + mMainHeight)
            mVolRect = Rect(0, mMainRect?.bottom.orZero + mChildPadding, mWidth, mMainRect?.bottom.orZero + mVolHeight)
            mChildRect = Rect(0, mVolRect?.bottom.orZero + mChildPadding, mWidth, mVolRect?.bottom.orZero + mChildHeight)
        } else {
            val mMainHeight = (displayHeight * 0.75f).toSafeInt()
            val mVolHeight = (displayHeight * 0.25f).toSafeInt()
            mMainRect = Rect(0, mTopPadding, mWidth, mTopPadding + mMainHeight)
            mVolRect = Rect(0, mMainRect?.bottom.orZero + mChildPadding, mWidth, mMainRect?.bottom.orZero + mVolHeight)
        }
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    private fun drawGird(canvas: Canvas) {
        //-----------------------上方k线图------------------------
        // 横向的grid
        val rowSpace = (mMainRect?.height().orZero / mGridRows).toSafeFloat()
        for (i in 0..mGridRows) {
            canvas.drawLine(0f, rowSpace * i + mMainRect?.top.orZero, mWidth.toSafeFloat(), rowSpace * i + mMainRect?.top.orZero, mGridPaint)
        }
        //-----------------------下方子图------------------------
        if (mChildDraw != null) {
            canvas.drawLine(0f, mVolRect?.bottom.toSafeFloat(), mWidth.toSafeFloat(), mVolRect?.bottom.toSafeFloat(), mGridPaint)
            canvas.drawLine(0f, mChildRect?.bottom.toSafeFloat(), mWidth.toSafeFloat(), mChildRect?.bottom.toSafeFloat(), mGridPaint)
        } else {
            canvas.drawLine(0f, mVolRect?.bottom.toSafeFloat(), mWidth.toSafeFloat(), mVolRect?.bottom.toSafeFloat(), mGridPaint)
        }
        // 纵向的grid
        val columnSpace = (mWidth / mGridColumns).toSafeFloat()
        for (i in 1..<mGridColumns) {
            canvas.drawLine(columnSpace * i, 0f, columnSpace * i, mMainRect?.bottom.toSafeFloat(), mGridPaint)
            canvas.drawLine(columnSpace * i, mMainRect?.bottom.toSafeFloat(), columnSpace * i, mVolRect?.bottom.toSafeFloat(), mGridPaint)
            if (mChildDraw != null) {
                canvas.drawLine(columnSpace * i, mVolRect?.bottom.toSafeFloat(), columnSpace * i, mChildRect?.bottom.toSafeFloat(), mGridPaint)
            }
        }
    }

    /**
     * 画k线图
     *
     * @param canvas
     */
    private fun drawK(canvas: Canvas) {
        // 保存之前的平移，缩放
        canvas.withTranslation(mTranslateX * mScaleX, 0f) {
            scale(mScaleX, 1f)
            for (i in mStartIndex..mStopIndex) {
                val currentPoint = getItem(i)
                val currentPointX = getX(i)
                val lastPoint = if (i == 0) currentPoint else getItem(i - 1)
                val lastX = if (i == 0) currentPointX else getX(i - 1)
                if (mMainDraw != null) {
                    mMainDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, this, this@BaseKLineChartView, i)
                }
                if (mVolDraw != null) {
                    mVolDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, this, this@BaseKLineChartView, i)
                }
                if (mChildDraw != null) {
                    mChildDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, this, this@BaseKLineChartView, i)
                }
            }
            // 画选择线
            if (isLongPress) {
                val point = getItem(mSelectedIndex) as? IKLine
                val x = getX(mSelectedIndex)
                val y = getMainY(point?.getClosePrice().orZero)
                // k线图竖线
                drawLine(x, mMainRect?.top.toSafeFloat(), x, mMainRect?.bottom.toSafeFloat(), mSelectedYLinePaint)
                // k线图横线
                drawLine(-mTranslateX, y, -mTranslateX + mWidth / mScaleX, y, mSelectedXLinePaint)
                // 柱状图竖线
                drawLine(x, mMainRect?.bottom.toSafeFloat(), x, mVolRect?.bottom.toSafeFloat(),mSelectedYLinePaint)
                if (mChildDraw != null) {
                    // 子线图竖线
                    drawLine(x, mVolRect?.bottom.toSafeFloat(), x, mChildRect?.bottom.toSafeFloat(), mSelectedYLinePaint)
                }
            }
        }
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private fun calculateWidth(text: String): Int {
        val rect = Rect()
        mTextPaint.getTextBounds(text, 0, text.length, rect)
        return rect.width() + 5
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private fun calculateMaxMin(text: String): Rect {
        val rect = Rect()
        mMaxMinPaint.getTextBounds(text, 0, text.length, rect)
        return rect
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private fun drawText(canvas: Canvas) {
        val fm = mTextPaint.getFontMetrics()
        val textHeight = fm.descent - fm.ascent
        val baseLine = (textHeight - fm.bottom - fm.top) / 2
        //--------------画上方k线图的值-------------
        if (mMainDraw != null) {
            canvas.drawText(formatValue(mMainMaxValue), (mWidth - calculateWidth(formatValue(mMainMaxValue))).toSafeFloat(), baseLine + mMainRect?.top.orZero, mTextPaint)
            canvas.drawText(formatValue(mMainMinValue), (mWidth - calculateWidth(formatValue(mMainMinValue))).toSafeFloat(), mMainRect?.bottom.orZero - textHeight + baseLine, mTextPaint)
            val rowValue = (mMainMaxValue - mMainMinValue) / mGridRows
            val rowSpace = (mMainRect?.height().orZero / mGridRows).toSafeFloat()
            for (i in 1..<mGridRows) {
                val text = formatValue(rowValue * (mGridRows - i) + mMainMinValue)
                canvas.drawText(text, (mWidth - calculateWidth(text)).toSafeFloat(), fixTextY(rowSpace * i + mMainRect?.top.orZero), mTextPaint)
            }
        }
        //--------------画中间子图的值-------------
        if (mVolDraw != null) {
            canvas.drawText(mVolDraw?.getValueFormatter()?.format(mVolMaxValue).orEmpty(), (mWidth - calculateWidth(formatValue(mVolMaxValue))).toSafeFloat(), mMainRect?.bottom.orZero + baseLine, mTextPaint)
            /*canvas.drawText(mVolDraw.getValueFormatter().format(mVolMinValue),
                    mWidth - calculateWidth(formatValue(mVolMinValue)), mVolRect.bottom, mTextPaint);*/
        }
        //--------------画下方子图的值-------------
        if (mChildDraw != null) {
            canvas.drawText(mChildDraw?.getValueFormatter()?.format(mChildMaxValue).orEmpty(), (mWidth - calculateWidth(formatValue(mChildMaxValue))).toSafeFloat(), mVolRect?.bottom.orZero + baseLine, mTextPaint)
            /*canvas.drawText(mChildDraw.getValueFormatter().format(mChildMinValue),
                    mWidth - calculateWidth(formatValue(mChildMinValue)), mChildRect.bottom, mTextPaint);*/
        }
        //--------------画时间---------------------
        val columnSpace = (mWidth / mGridColumns).toSafeFloat()
        var y = if (isShowChild) {
            mChildRect?.bottom.orZero + baseLine + 5
        } else {
            mVolRect?.bottom.orZero + baseLine + 5
        }
        val startX = getX(mStartIndex) - mPointWidth / 2
        val stopX = getX(mStopIndex) + mPointWidth / 2
        for (i in 1..<mGridColumns) {
            val translateX = xToTranslateX(columnSpace * i)
            if (translateX in startX..stopX) {
                val index = indexOfTranslateX(translateX)
                val text = mAdapter?.getDate(index)
                canvas.drawText(text.orEmpty(), columnSpace * i - mTextPaint.measureText(text) / 2, y, mTextPaint)
            }
        }
        var translateX = xToTranslateX(0f)
        if (translateX in startX..stopX) {
            canvas.drawText(getAdapter()?.getDate(mStartIndex).orEmpty(), 0f, y, mTextPaint)
        }
        translateX = xToTranslateX(mWidth.toSafeFloat())
        if (translateX in startX..stopX) {
            val text = getAdapter()?.getDate(mStopIndex).orEmpty()
            canvas.drawText(text, mWidth - mTextPaint.measureText(text), y, mTextPaint)
        }
        if (isLongPress) {
            // 画Y值
            val point = getItem(mSelectedIndex) as? IKLine
            val w1 = ViewUtil.Dp2Px(context, 5f)
            val w2 = ViewUtil.Dp2Px(context, 3f)
            var r = textHeight / 2 + w2
            y = getMainY(point?.getClosePrice().orZero)
            var x: Float
            val text = formatValue(point?.getClosePrice().orZero)
            var textWidth = mTextPaint.measureText(text)
            if (translateXtoX(getX(mSelectedIndex)) < getChartWidth() / 2) {
                x = 1f
                val path = Path()
                path.moveTo(x, y - r)
                path.lineTo(x, y + r)
                path.lineTo(textWidth + 2 * w1, y + r)
                path.lineTo(textWidth + 2 * w1 + w2, y)
                path.lineTo(textWidth + 2 * w1, y - r)
                path.close()
                canvas.drawPath(path, mSelectPointPaint)
                canvas.drawPath(path, mSelectorFramePaint)
                canvas.drawText(text, x + w1, fixTextY1(y), mTextPaint)
            } else {
                x = mWidth - textWidth - 1 - 2 * w1 - w2
                val path = Path()
                path.moveTo(x, y)
                path.lineTo(x + w2, y + r)
                path.lineTo((mWidth - 2).toSafeFloat(), y + r)
                path.lineTo((mWidth - 2).toSafeFloat(), y - r)
                path.lineTo(x + w2, y - r)
                path.close()
                canvas.drawPath(path, mSelectPointPaint)
                canvas.drawPath(path, mSelectorFramePaint)
                canvas.drawText(text, x + w1 + w2, fixTextY1(y), mTextPaint)
            }
            // 画X值
            val date = mAdapter?.getDate(mSelectedIndex).orEmpty()
            textWidth = mTextPaint.measureText(date)
            r = textHeight / 2
            x = translateXtoX(getX(mSelectedIndex))
            y = if (isShowChild) {
                mChildRect?.bottom.toSafeFloat()
            } else {
                mVolRect?.bottom.toSafeFloat()
            }
            if (x < textWidth + 2 * w1) {
                x = 1 + textWidth / 2 + w1
            } else if (mWidth - x < textWidth + 2 * w1) {
                x = mWidth - 1 - textWidth / 2 - w1
            }
            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, mSelectPointPaint)
            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, mSelectorFramePaint)
            canvas.drawText(date, x - textWidth / 2, y + baseLine + 5, mTextPaint)
        }
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private fun drawMaxAndMin(canvas: Canvas) {
        if (!mainDraw?.isLine().orFalse) {
            //绘制最大值和最小值
            var x: Float = translateXtoX(getX(mMainMinIndex))
            var y = getMainY(mMainLowMinValue)
            var lowString = "── $mMainLowMinValue"
            //计算文本宽度
            val lowStringWidth = calculateMaxMin(lowString).width()
            val lowStringHeight = calculateMaxMin(lowString).height()
            if (x < width / 2) {
                // 画右边
                canvas.drawText(lowString, x, y + lowStringHeight / 2, mMaxMinPaint)
            } else {
                // 画左边
                lowString = "$mMainLowMinValue ──"
                canvas.drawText(lowString, x - lowStringWidth, y + lowStringHeight / 2, mMaxMinPaint)
            }
            x = translateXtoX(getX(mMainMaxIndex))
            y = getMainY(mMainHighMaxValue)
            var highString = "── $mMainHighMaxValue"
            val highStringWidth = calculateMaxMin(highString).width()
            val highStringHeight = calculateMaxMin(highString).height()
            if (x < width / 2) {
                // 画右边
                canvas.drawText(highString, x, y + highStringHeight / 2, mMaxMinPaint)
            } else {
                // 画左边
                highString = "$mMainHighMaxValue ──"
                canvas.drawText(highString, x - highStringWidth, y + highStringHeight / 2, mMaxMinPaint)
            }
        }
    }

    /**
     * 画值
     *
     * @param canvas
     * @param position 显示某个点的值
     */
    private fun drawValue(canvas: Canvas?, position: Int) {
        val fm = mTextPaint.getFontMetrics()
        val textHeight = fm.descent - fm.ascent
        val baseLine = (textHeight - fm.bottom - fm.top) / 2
        if (position in 0..<mItemCount) {
            if (mMainDraw != null) {
                val y = mMainRect?.top.orZero + baseLine - textHeight
                mMainDraw?.drawText(canvas, this, position, 0f, y)
            }
            if (mVolDraw != null) {
                val y = mMainRect?.bottom.orZero + baseLine
                mVolDraw?.drawText(canvas, this, position, 0f, y)
            }
            if (mChildDraw != null) {
                val y = mVolRect?.bottom.orZero + baseLine
                mChildDraw?.drawText(canvas, this, position, 0f, y)
            }
        }
    }

    private fun calculateSelectedX(x: Float) {
        mSelectedIndex = indexOfTranslateX(xToTranslateX(x))
        if (mSelectedIndex < mStartIndex) {
            mSelectedIndex = mStartIndex
        }
        if (mSelectedIndex > mStopIndex) {
            mSelectedIndex = mStopIndex
        }
    }

    /**
     * 计算当前的显示区域
     */
    private fun calculateValue() {
        if (!isLongPress()) {
            mSelectedIndex = -1
        }
        mMainMaxValue = Float.MIN_VALUE
        mMainMinValue = Float.MAX_VALUE
        mVolMaxValue = Float.MIN_VALUE
        mVolMinValue = Float.MAX_VALUE
        mChildMaxValue = Float.MIN_VALUE
        mChildMinValue = Float.MAX_VALUE
        mStartIndex = indexOfTranslateX(xToTranslateX(0f))
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth.toSafeFloat()))
        mMainMaxIndex = mStartIndex
        mMainMinIndex = mStartIndex
        mMainHighMaxValue = Float.MIN_VALUE
        mMainLowMinValue = Float.MAX_VALUE
        for (i in mStartIndex..mStopIndex) {
            val point = getItem(i) as? IKLine
            if (mMainDraw != null) {
                mMainMaxValue = mMainMaxValue.coerceAtLeast(mMainDraw?.getMaxValue(point).orZero)
                mMainMinValue = mMainMinValue.coerceAtMost(mMainDraw?.getMinValue(point).orZero)
                if (mMainHighMaxValue != mMainHighMaxValue.coerceAtLeast(point?.getHighPrice().orZero)) {
                    mMainHighMaxValue = point?.getHighPrice().orZero
                    mMainMaxIndex = i
                }
                if (mMainLowMinValue != mMainLowMinValue.coerceAtMost(point?.getLowPrice().orZero)) {
                    mMainLowMinValue = point?.getLowPrice().orZero
                    mMainMinIndex = i
                }
            }
            if (mVolDraw != null) {
                mVolMaxValue = mVolMaxValue.coerceAtLeast(mVolDraw?.getMaxValue(point).orZero)
                mVolMinValue = mVolMinValue.coerceAtMost(mVolDraw?.getMinValue(point).orZero)
            }
            if (mChildDraw != null) {
                mChildMaxValue = mChildMaxValue.coerceAtLeast(mChildDraw?.getMaxValue(point).orZero)
                mChildMinValue = mChildMinValue.coerceAtMost(mChildDraw?.getMinValue(point).orZero)
            }
        }
        if (mMainMaxValue != mMainMinValue) {
            val padding = (mMainMaxValue - mMainMinValue) * 0.05f
            mMainMaxValue += padding
            mMainMinValue -= padding
        } else {
            // 当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += abs(mMainMaxValue * 0.05f)
            mMainMinValue -= abs(mMainMinValue * 0.05f)
            if (mMainMaxValue == 0f) {
                mMainMaxValue = 1f
            }
        }
        if (abs(mVolMaxValue) < 0.01) {
            mVolMaxValue = 15.00f
        }
        if (abs(mChildMaxValue) < 0.01 && abs(mChildMinValue) < 0.01) {
            mChildMaxValue = 1f
        } else if (mChildMaxValue.equals(mChildMinValue)) {
            // 当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mChildMaxValue += abs(mChildMaxValue * 0.05f)
            mChildMinValue -= abs(mChildMinValue * 0.05f)
            if (mChildMaxValue == 0f) {
                mChildMaxValue = 1f
            }
        }
        if (isWR) {
            mChildMaxValue = 0f
            if (abs(mChildMinValue) < 0.01) mChildMinValue = -10.00f
        }
        mMainScaleY = mMainRect?.height().orZero * 1f / (mMainMaxValue - mMainMinValue)
        mVolScaleY = mVolRect?.height().orZero * 1f / (mVolMaxValue - mVolMinValue)
        if (mChildRect != null) {
            mChildScaleY = mChildRect?.height().orZero * 1f / (mChildMaxValue - mChildMinValue)
        }
        if (mAnimator?.isRunning.orFalse) {
            val value = mAnimator?.getAnimatedValue() as? Float
            mStopIndex = mStartIndex + (value.orZero * (mStopIndex - mStartIndex)).roundToInt()
        }
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private fun getMinTranslateX(): Float {
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2
    }

    /**
     * 获取平移的最大值
     *
     * @return
     */
    private fun getMaxTranslateX(): Float {
        if (!isFullScreen()) {
            return getMinTranslateX()
        }
        return mPointWidth / 2
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private fun setTranslateXFromScrollX(scrollX: Int) {
        mTranslateX = scrollX + getMinTranslateX()
    }

    fun getMainY(value: Float): Float {
        return (mMainMaxValue - value) * mMainScaleY + mMainRect?.top.orZero
    }

    fun getMainBottom(): Float {
        return mMainRect?.bottom.toSafeFloat()
    }

    fun getVolY(value: Float): Float {
        return (mVolMaxValue - value) * mVolScaleY + mVolRect?.top.orZero
    }

    fun getChildY(value: Float): Float {
        return (mChildMaxValue - value) * mChildScaleY + mChildRect?.top.orZero
    }

    /**
     * 解决text居中的问题
     */
    fun fixTextY(y: Float): Float {
        val fontMetrics = mTextPaint.getFontMetrics()
        return y + fontMetrics.descent - fontMetrics.ascent
    }

    /**
     * 解决text居中的问题
     */
    fun fixTextY1(y: Float): Float {
        val fontMetrics = mTextPaint.getFontMetrics()
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent)
    }

    fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toSafeInt()
    }

    fun sp2px(spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toSafeInt()
    }

    /**
     * 格式化值
     */
    fun formatValue(value: Float): String {
        if (getValueFormatter() == null) {
            setValueFormatter(ValueFormatter())
        }
        return getValueFormatter()?.format(value).orEmpty()
    }

    /**
     * 重新计算并刷新线条
     */
    fun notifyChanged() {
        if (isShowChild && mChildDrawPosition == -1) {
            mChildDraw = mChildDraws[0]
            mChildDrawPosition = 0
        }
        if (mItemCount != 0) {
            mDataLen = (mItemCount - 1) * mPointWidth
            checkAndFixScrollX()
            setTranslateXFromScrollX(mScrollX)
        } else {
            setScrollX(0)
        }
        invalidate()
    }

    /**
     * MA/BOLL切换及隐藏
     *
     * @param status MA/BOLL/NONE
     */
    fun changeMainDrawType(status: Status) {
        if (mainDraw != null && mainDraw?.getStatus() != status) {
            mainDraw?.setStatus(status)
            invalidate()
        }
    }

    fun indexOfTranslateX(translateX: Float): Int {
        return indexOfTranslateX(translateX, 0, mItemCount - 1)
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    fun drawMainLine(canvas: Canvas, paint: Paint, startX: Float, startValue: Float, stopX: Float, stopValue: Float) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint)
    }

    /**
     * 在主区域画分时线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    fun drawMainMinuteLine(canvas: Canvas, paint: Paint, startX: Float, startValue: Float, stopX: Float, stopValue: Float) {
        val path5 = Path()
        path5.moveTo(startX, (displayHeight + mTopPadding + mBottomPadding).toSafeFloat())
        path5.lineTo(startX, getMainY(startValue))
        path5.lineTo(stopX, getMainY(stopValue))
        path5.lineTo(stopX, (displayHeight + mTopPadding + mBottomPadding).toSafeFloat())
        path5.close()
        canvas.drawPath(path5, paint)
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    fun drawChildLine(canvas: Canvas, paint: Paint, startX: Float, startValue: Float, stopX: Float, stopValue: Float) {
        canvas.drawLine(startX, getChildY(startValue), stopX, getChildY(stopValue), paint)
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    fun drawVolLine(canvas: Canvas, paint: Paint, startX: Float, startValue: Float, stopX: Float, stopValue: Float) {
        canvas.drawLine(startX, getVolY(startValue), stopX, getVolY(stopValue), paint)
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    fun getItem(position: Int): Any? {
        return if (mAdapter != null) {
            mAdapter?.getItem(position)
        } else {
            null
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
     */
    fun getX(position: Int): Float {
        return position * mPointWidth
    }

    /**
     * 获取适配器
     *
     * @return
     */
    fun getAdapter(): IAdapter? {
        return mAdapter
    }

    /**
     * 设置当前子图
     *
     * @param position
     */
    fun setChildDraw(position: Int) {
        if (mChildDrawPosition != position) {
            if (!isShowChild) {
                isShowChild = true
                initRect()
            }
            mChildDraw = mChildDraws[position]
            mChildDrawPosition = position
            isWR = position == 5
            invalidate()
        }
    }

    /**
     * 隐藏子图
     */
    fun hideChildDraw() {
        mChildDrawPosition = -1
        isShowChild = false
        mChildDraw = null
        initRect()
        invalidate()
    }

    /**
     * 给子区域添加画图方法
     *
     * @param childDraw IChartDraw
     */
    fun addChildDraw(childDraw: IChartDraw<Any>?) {
        childDraw ?: return
        mChildDraws.add(childDraw)
    }

    /**
     * 获取ValueFormatter
     *
     * @return
     */
    fun getValueFormatter(): IValueFormatter? {
        return mValueFormatter
    }

    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    fun setValueFormatter(valueFormatter: IValueFormatter) {
        this.mValueFormatter = valueFormatter
    }

    /**
     * 获取DatetimeFormatter
     *
     * @return 时间格式化器
     */
    fun getDateTimeFormatter(): IDateTimeFormatter? {
        return mDateTimeFormatter
    }

    /**
     * 设置dateTimeFormatter
     *
     * @param dateTimeFormatter 时间格式化器
     */
    fun setDateTimeFormatter(dateTimeFormatter: IDateTimeFormatter) {
        mDateTimeFormatter = dateTimeFormatter
    }

    /**
     * 格式化时间
     *
     * @param date
     */
    fun formatDateTime(date: Date?): String {
        if (getDateTimeFormatter() == null) {
            setDateTimeFormatter(TimeFormatter())
        }
        return getDateTimeFormatter()?.format(date).orEmpty()
    }

    /**
     * 获取主区域的 IChartDraw
     *
     * @return IChartDraw
     */
    fun getMainDraw(): IChartDraw<Any>? {
        return mMainDraw
    }

    /**
     * 设置主区域的 IChartDraw
     *
     * @param mainDraw IChartDraw
     */
    fun setMainDraw(mainDraw: IChartDraw<Any>?) {
        mainDraw ?: return
        mMainDraw = mainDraw
        this.mainDraw = mMainDraw as? MainDraw
    }

    fun getVolDraw(): IChartDraw<Any>? {
        return mVolDraw
    }

    fun setVolDraw(mVolDraw: IChartDraw<Any>?) {
        mVolDraw ?: return
        this.mVolDraw = mVolDraw
    }

    /**
     * 二分查找当前值的index
     *
     * @return
     */
    fun indexOfTranslateX(translateX: Float, start: Int, end: Int): Int {
        if (end == start) {
            return start
        }
        if (end - start == 1) {
            val startValue = getX(start)
            val endValue = getX(end)
            return if (abs(translateX - startValue) < abs(translateX - endValue)) start else end
        }
        val mid = start + (end - start) / 2
        val midValue = getX(mid)
        return if (translateX < midValue) {
            indexOfTranslateX(translateX, start, mid)
        } else if (translateX > midValue) {
            indexOfTranslateX(translateX, mid, end)
        } else {
            mid
        }
    }

    /**
     * 设置数据适配器
     */
    fun setAdapter(adapter: IAdapter?) {
        if (mAdapter != null) {
            mAdapter?.unregisterDataSetObserver(mDataSetObserver)
        }
        mAdapter = adapter
        if (mAdapter != null) {
            mAdapter?.registerDataSetObserver(mDataSetObserver)
            mItemCount = mAdapter?.getCount().orZero
        } else {
            mItemCount = 0
        }
        notifyChanged()
    }

    /**
     * 开始动画
     */
    fun startAnimation() {
        if (mAnimator != null) {
            mAnimator?.start()
        }
    }

    /**
     * 设置动画时间
     */
    fun setAnimationDuration(duration: Long) {
        if (mAnimator != null) {
            mAnimator?.setDuration(duration)
        }
    }

    /**
     * 设置表格行数
     */
    fun setGridRows(gridRows: Int) {
        var gridRows = gridRows
        if (gridRows < 1) {
            gridRows = 1
        }
        mGridRows = gridRows
    }

    /**
     * 设置表格列数
     */
    fun setGridColumns(gridColumns: Int) {
        var gridColumns = gridColumns
        if (gridColumns < 1) {
            gridColumns = 1
        }
        mGridColumns = gridColumns
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    fun xToTranslateX(x: Float): Float {
        return -mTranslateX + x / mScaleX
    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    fun translateXtoX(translateX: Float): Float {
        return (translateX + mTranslateX) * mScaleX
    }

    /**
     * 获取上方padding
     */
    fun getTopPadding(): Float {
        return mTopPadding.toSafeFloat()
    }

    /**
     * 获取上方padding
     */
    fun getChildPadding(): Float {
        return mChildPadding.toSafeFloat()
    }

    /**
     * 获取子试图上方padding
     */
    fun getmChildScaleYPadding(): Float {
        return mChildPadding.toSafeFloat()
    }

    /**
     * 获取图的宽度
     *
     * @return
     */
    fun getChartWidth(): Int {
        return mWidth
    }

    /**
     * 是否长按
     */
    fun isLongPress(): Boolean {
        return isLongPress
    }

    /**
     * 获取选择索引
     */
    fun getSelectedIndex(): Int {
        return mSelectedIndex
    }

    fun getChildRect(): Rect? {
        return mChildRect
    }

    fun getVolRect(): Rect? {
        return mVolRect
    }

    /**
     * 设置选择监听
     */
    fun setOnSelectedChangedListener(l: OnSelectedChangedListener) {
        this.mOnSelectedChangedListener = l
    }

    fun onSelectedChanged(view: BaseKLineChartView?, point: Any?, index: Int) {
        mOnSelectedChangedListener?.onSelectedChanged(view, point, index)
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    fun isFullScreen(): Boolean {
        return mDataLen >= mWidth / mScaleX
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    fun setOverScrollRange(overScrollRange: Float) {
        var overScrollRange = overScrollRange
        if (overScrollRange < 0) {
            overScrollRange = 0f
        }
        mOverScrollRange = overScrollRange
    }

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    fun setTopPadding(topPadding: Int) {
        mTopPadding = topPadding
    }

    /**
     * 设置下方padding
     *
     * @param bottomPadding
     */
    fun setBottomPadding(bottomPadding: Int) {
        mBottomPadding = bottomPadding
    }

    /**
     * 设置表格线宽度
     */
    fun setGridLineWidth(width: Float) {
        mGridPaint.strokeWidth = width
    }

    /**
     * 设置表格线颜色
     */
    fun setGridLineColor(color: Int) {
        mGridPaint.setColor(color)
    }

    /**
     * 设置选择器横线宽度
     */
    fun setSelectedXLineWidth(width: Float) {
        mSelectedXLinePaint.strokeWidth = width
    }

    /**
     * 设置选择器横线颜色
     */
    fun setSelectedXLineColor(color: Int) {
        mSelectedXLinePaint.setColor(color)
    }

    /**
     * 设置选择器竖线宽度
     */
    fun setSelectedYLineWidth(width: Float) {
        mSelectedYLinePaint.strokeWidth = width
    }

    /**
     * 设置选择器竖线颜色
     */
    fun setSelectedYLineColor(color: Int) {
        mSelectedYLinePaint.setColor(color)
    }

    /**
     * 设置文字颜色
     */
    open fun setTextColor(color: Int) {
        mTextPaint.setColor(color)
    }

    /**
     * 设置文字大小
     */
    open fun setTextSize(textSize: Float) {
        mTextPaint.textSize = textSize
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    fun setMTextColor(color: Int) {
        mMaxMinPaint.setColor(color)
    }

    /**
     * 设置最大值/最小值文字大小
     */
    fun setMTextSize(textSize: Float) {
        mMaxMinPaint.textSize = textSize
    }

    /**
     * 设置选中point 值显示背景
     */
    fun setSelectPointColor(color: Int) {
        mSelectPointPaint.setColor(color)
    }

    /**
     * 获取文字大小
     */
    fun getTextSize(): Float {
        return mTextPaint.textSize
    }

    /**
     * 获取曲线宽度
     */
    fun getLineWidth(): Float {
        return mLineWidth
    }

    /**
     * 设置曲线的宽度
     */
    open fun setLineWidth(lineWidth: Float) {
        mLineWidth = lineWidth
    }

    /**
     * 设置每个点的宽度
     */
    fun setPointWidth(pointWidth: Float) {
        mPointWidth = pointWidth
    }

    fun getGridPaint(): Paint {
        return mGridPaint
    }

    fun getTextPaint(): Paint {
        return mTextPaint
    }

    fun getBackgroundPaint(): Paint {
        return mBackgroundPaint
    }

    fun getDisplayHeight(): Int {
        return displayHeight + mTopPadding + mBottomPadding
    }

    /**
     * 选中点变化时的监听
     */
    interface OnSelectedChangedListener {

        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        fun onSelectedChanged(view: BaseKLineChartView?, point: Any?, index: Int)

    }

}