package com.github.fujianlian.klinechart

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.view.GestureDetectorCompat
import com.example.common.utils.function.dimen
import com.example.framework.utils.function.value.min
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.github.fujianlian.klinechart.KLineHelper.isRiseRed
import com.github.fujianlian.klinechart.base.IAdapter
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.draw.MainDraw
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.formatter.ValueFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import com.example.common.utils.function.pt as pt2
import com.example.common.utils.function.ptFloat as ptFloat2

/**
 * k线图
 * Created by tian on 2016/5/3.
 */
abstract class BaseKLineChartView : ScrollAndScaleView {
    private val pt1 = dimen(R.dimen.textSize10) / 10

    protected var mDisplayHeight = 0
    private var mChildDrawPosition = -1
    protected var mTranslateX = Float.MIN_VALUE
        private set

    /**
     * 获取图的宽度
     *
     * @return
     */
    var chartWidth = 0
        protected set
    var chartHeight = 0
        protected set
    protected var mTopPadding = 0
    protected var mChildPadding = 0
    protected var mBottomPadding = 0
    private var mMainScaleY = 1f
    private var mVolScaleY = 1f
    private var mChildScaleY = 1f
    private var mDataLen = 0f
    private var mMainMaxValue = Float.MAX_VALUE
    private var mMainMinValue = Float.MIN_VALUE
    private var mMainHighMaxValue = 0f
    private var mMainLowMinValue = 0f
    private var mMainMaxIndex = 0
    private var mMainMinIndex = 0
    private var mVolMaxValue = Float.MAX_VALUE
    private var mVolMinValue = Float.MIN_VALUE
    private var mChildMaxValue = Float.MAX_VALUE
    private var mChildMinValue = Float.MIN_VALUE
    private var mStartIndex = 0
    private var mStopIndex = 0
    protected var mPointWidth = 6f
        private set
    private var mGridRows = 4
    protected var mGridColumns = 4
        private set
    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(2.ptFloat, 2.ptFloat), 0f)
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMaxMinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 设置选中point的Paint
    private val mSelectPointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mSelectTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectedXLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectedYLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    // 设置开仓
    protected val mOpenXLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mOpenTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mOpenTextBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mOpenTextBgPaintBg = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val mOpenPointBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mOpenArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val mOpenCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    // 设置SLTP
    protected val mSlTpLine = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mSlTpBg = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mSlTpText = Paint(Paint.ANTI_ALIAS_FLAG)

    protected var colorRed = 0
        private set
    protected var colorGreen = 0
        private set

    // 最后一根动态线
    protected val mLastLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 最后一根动态线标点
    protected val mLastPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //最后一根动态线文字
    protected val mLastTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 获取选择索引
     */
    var selectedIndex = 0
        private set

    /* 开仓信息*/
    protected var openData: PointInfoBean? = null
        set(value) {
            field = value
            invalidate()
        }

    /* 止盈止损信息*/
    protected var sltpData: SlTpInfoBean? = null
        set(value) {
            field = value
            invalidate()
        }


    private var mMainDraw: IChartDraw<Any>? = null
    private var mainDraw: MainDraw? = null
    protected var volDraw: IChartDraw<Any>? = null
    private var mAdapter: IAdapter? = null
    private var isWR = false
    protected var isShowChild = false

    // 当前点的个数，即最后一条数据
    protected var mItemCount = 0
        private set
    private var mChildDraw: IChartDraw<Any>? = null
    private val mChildDraws: MutableList<IChartDraw<Any>> = ArrayList()
    private val mDataSetObserver: DataSetObserver? = object : DataSetObserver() {
        override fun onChanged() {
            mItemCount = adapter?.count.orZero
            notifyChanged()
        }

        override fun onInvalidated() {
            mItemCount = adapter?.count.orZero
            //            notifyChanged();
        }
    }
    /**
     * 获取ValueFormatter
     *
     * @return
     */
    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    var valueFormatter: IValueFormatter? = null
    /**
     * 获取DatetimeFormatter
     *
     * @return 时间格式化器
     */
    /**
     * 设置dateTimeFormatter
     *
     * @param dateTimeFormatter 时间格式化器
     */
    private var mAnimator: ValueAnimator? = null
    private val mAnimationDuration: Long = 500
    protected var mOverScrollRange = 0f
        private set
    private var mOnSelectedChangedListener: OnSelectedChangedListener? = null
    protected var mMainRect: Rect? = null
    var volRect: Rect? = null
    protected var childRect: Rect? = null
    /**
     * 获取曲线宽度
     */
    /**
     * 设置曲线的宽度
     */
    protected open var lineWidth = 0f
    protected var mColumnSpace = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    private fun init() {
        if (isInEditMode) return

        setWillNotDraw(false)
        mDetector = GestureDetectorCompat(context, this)
        mScaleDetector = ScaleGestureDetector(context, this)
        mTopPadding = resources.getDimension(R.dimen.chart_top_padding).toInt()
        mChildPadding = resources.getDimension(R.dimen.child_top_padding).toInt()
        mBottomPadding = resources.getDimension(R.dimen.chart_bottom_padding).toInt()
        mAnimator = ValueAnimator.ofFloat(0f, 1f)
        mAnimator?.duration = mAnimationDuration
        mAnimator?.addUpdateListener { invalidate() }

        // 初始化动态线
        mLastLinePaint.isAntiAlias = true
        mLastLinePaint.style = Paint.Style.STROKE
        mLastLinePaint.strokeWidth = 2f
        val effects: PathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
        mLastLinePaint.pathEffect = effects

        mOpenTextBgPaint.style = Paint.Style.FILL_AND_STROKE
        mOpenTextBgPaint.strokeWidth = 4f
        mOpenTextBgPaintBg.style = Paint.Style.FILL
        mOpenXLinePaint.strokeWidth = 1f

        mOpenPointBgPaint.style = Paint.Style.FILL_AND_STROKE
        mOpenPointBgPaint.strokeWidth = 4f
        mOpenArrowPaint.style = Paint.Style.FILL_AND_STROKE
        mOpenArrowPaint.strokeWidth = 4f
        mOpenArrowPaint.strokeCap = Paint.Cap.ROUND
        mOpenArrowPaint.strokeJoin = Paint.Join.ROUND

        mOpenCirclePaint.style = Paint.Style.FILL

        mSlTpLine.style = Paint.Style.STROKE
        mSlTpLine.strokeCap = Paint.Cap.SQUARE
        mSlTpLine.strokeJoin = Paint.Join.MITER
        mSlTpLine.strokeWidth = 2f
        mSlTpBg.style = Paint.Style.FILL


        // 初始化动态点
        mLastPaint.style = Paint.Style.FILL
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * 解决text居中的问题
     */
    fun fixTextY(y: Float): Float {
        val fontMetrics = textPaint.fontMetrics
        return y + fontMetrics.descent - fontMetrics.ascent
    }

    /**
     * 解决text居中的问题
     */
    fun fixTextY1(y: Float): Float {
        val fontMetrics = textPaint.fontMetrics
        return y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    private fun drawGird(canvas: Canvas) {
        //-----------------------上方k线图------------------------
        //横向的grid
        val rowSpace = mMainRect?.height().orZero / mGridRows.toFloat()
        for (i in 0..mGridRows) {
            canvas.drawLine(0f, rowSpace * i + mMainRect?.top.orZero, chartWidth.toSafeFloat(), rowSpace * i + mMainRect?.top.orZero, gridPaint)
        }
        //-----------------------边框------------------------
        //顶部
        canvas.drawLine(0f, borderPaint.strokeWidth / 2f, chartWidth.toSafeFloat(), borderPaint.strokeWidth / 2f, borderPaint)
        //主图下部
        if (mChildDraw != null) {
            canvas.drawLine(0f, volRect?.bottom.toSafeFloat(), chartWidth.toSafeFloat(), volRect?.bottom.toSafeFloat(), borderPaint)
            canvas.drawLine(0f, childRect?.bottom.toSafeFloat(), chartWidth.toSafeFloat(), childRect?.bottom.toSafeFloat(), borderPaint)
        } else {
            canvas.drawLine(0f, volRect?.bottom.toSafeFloat(), chartWidth.toSafeFloat(), volRect?.bottom.toSafeFloat(), borderPaint)
        }
        //纵向左
        canvas.drawLine(borderPaint.strokeWidth / 2f, 0f, borderPaint.strokeWidth / 2f, volRect?.bottom.toSafeFloat(), borderPaint)
        //纵向右
        canvas.drawLine(chartWidth - borderPaint.strokeWidth / 2f, 0f, chartWidth - borderPaint.strokeWidth / 2f, volRect?.bottom.toSafeFloat(), borderPaint)

        //---------------纵向的grid-------------------
        val columnSpace = chartWidth / mGridColumns.toFloat()
        for (i in 1 until mGridColumns) {
            canvas.drawLine(columnSpace * i, 0f, columnSpace * i, mMainRect?.bottom.toSafeFloat(), gridPaint)
            canvas.drawLine(columnSpace * i, mMainRect?.bottom.toSafeFloat(), columnSpace * i, volRect?.bottom.toSafeFloat(), gridPaint)
            if (mChildDraw != null) {
                canvas.drawLine(columnSpace * i, volRect?.bottom.toSafeFloat(), columnSpace * i, childRect?.bottom.toSafeFloat(), gridPaint)
            }
        }
    }

    /**
     * 画k线图
     *
     * @param canvas
     */
    private fun drawK(canvas: Canvas) {
        //保存之前的平移，缩放
        canvas.save()
        canvas.translate(mTranslateX * mScaleX, 0f)
        canvas.scale(mScaleX, 1f)
        for (i in mStartIndex..mStopIndex) {
            val currentPoint = getItem(i)
            val currentPointX = getX(i)
            val lastPoint = if (i == 0) currentPoint else getItem(i - 1)
            val lastX = if (i == 0) currentPointX else getX(i - 1)
            mMainDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i)
            volDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i)
            mChildDraw?.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i)
        }
        //还原 平移缩放
        canvas.restore()
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private fun calculateWidth(text: String): Int {
        val rect = Rect()
        textPaint.getTextBounds(text, 0, text.length, rect)
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
        // 画线
        val point = getItem(mItemCount - 1) as? IKLine ?: return
        // 位数
        val digits = point.digits
        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        val baseLine = (textHeight - fm.bottom - fm.top) / 2
        //--------------画上方k线图的值-------------
        if (mMainDraw != null) {
            canvas.drawText(formatValue(mMainMaxValue, digits), chartWidth - calculateWidth(formatValue(mMainMaxValue, digits)) - 3.ptFloat, baseLine + mMainRect?.top.orZero, textPaint)
            canvas.drawText(formatValue(mMainMinValue, digits), chartWidth - calculateWidth(formatValue(mMainMinValue, digits)) - 3.ptFloat, mMainRect?.bottom.orZero - textHeight + baseLine, textPaint)
            val rowValue = (mMainMaxValue - mMainMinValue) / mGridRows
            val rowSpace = mMainRect?.height().orZero / mGridRows.toFloat()
            for (i in 1 until mGridRows) {
                val text = formatValue(rowValue * (mGridRows - i) + mMainMinValue, digits)
                canvas.drawText(text, chartWidth - calculateWidth(text) - 3.ptFloat, fixTextY(rowSpace * i + mMainRect?.top.orZero), textPaint)
            }
        }
        //--------------画中间子图的值-------------
        if (volDraw != null) {
            canvas.drawText(volDraw?.valueFormatter?.format(mVolMaxValue, digits).orEmpty(),
                chartWidth - calculateWidth(formatValue(mVolMaxValue, digits)).toFloat(), mMainRect?.bottom.orZero + baseLine, textPaint)
            /*canvas.drawText(mVolDraw.getValueFormatter().format(mVolMinValue),
                    mWidth - calculateWidth(formatValue(mVolMinValue)), mVolRect.bottom, mTextPaint);*/
        }
        //--------------画下方子图的值-------------
        if (mChildDraw != null) {
            canvas.drawText(mChildDraw?.valueFormatter?.format(mChildMaxValue, digits).orEmpty(),
                chartWidth - calculateWidth(formatValue(mChildMaxValue, digits)).toFloat(), volRect?.bottom.orZero + baseLine, textPaint)
            /*canvas.drawText(mChildDraw.getValueFormatter().format(mChildMinValue),
                    mWidth - calculateWidth(formatValue(mChildMinValue)), mChildRect.bottom, mTextPaint);*/
        }
        //--------------画时间---------------------
        val columnSpace = chartWidth / mGridColumns.toFloat()
        var y: Float
        y = if (isShowChild) {
            childRect?.bottom.orZero + baseLine + 5
        } else {
            volRect?.bottom.orZero + baseLine + 5
        }
        val startX = getX(mStartIndex) - mPointWidth / 2
        val stopX = getX(mStopIndex) + mPointWidth / 2
        for (i in 1 until mGridColumns) {
            val translateX = xToTranslateX(columnSpace * i)
            if (translateX in startX..stopX) {
                val index = indexOfTranslateX(translateX)
                val text = mAdapter?.getDate(index)
                canvas.drawText(text.orEmpty(), columnSpace * i - textPaint.measureText(text.orEmpty()) / 2, y, textPaint)
            }
        }
        var translateX = xToTranslateX(0f)
        if (translateX in startX..stopX) {
            canvas.drawText(adapter?.getDate(mStartIndex).orEmpty(), 0f, y, textPaint)
        }
        translateX = xToTranslateX(chartWidth.toFloat())
        if (translateX in startX..stopX) {
            val text = adapter?.getDate(mStopIndex)
            canvas.drawText(text.orEmpty(), chartWidth - textPaint.measureText(text), y, textPaint)
        }
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private fun drawMaxAndMin(canvas: Canvas) {
        if (!mainDraw?.isLine().orTrue) {
            //绘制最大值和最小值
            var x = translateXtoX(getX(mMainMinIndex))
            var y = getMainY(mMainLowMinValue)
            var LowString = "── $mMainLowMinValue"
            //计算显示位置
            //计算文本宽度
            val lowStringWidth = calculateMaxMin(LowString).width()
            val lowStringHeight = calculateMaxMin(LowString).height()
            if (x < width / 2) {
                //画右边
                canvas.drawText(LowString, x, y + lowStringHeight / 2, mMaxMinPaint)
            } else {
                //画左边
                LowString = "$mMainLowMinValue ──"
                canvas.drawText(LowString, x - lowStringWidth, y + lowStringHeight / 2, mMaxMinPaint)
            }
            x = translateXtoX(getX(mMainMaxIndex))
            y = getMainY(mMainHighMaxValue)
            var highString = "── $mMainHighMaxValue"
            val highStringWidth = calculateMaxMin(highString).width()
            val highStringHeight = calculateMaxMin(highString).height()
            if (x < width / 2) {
                //画右边
                canvas.drawText(highString, x, y + highStringHeight / 2, mMaxMinPaint)
            } else {
                //画左边
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
    private fun drawValue(canvas: Canvas, position: Int) {
        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        val baseLine = (textHeight - fm.bottom - fm.top) / 2
        if (position in 0 until mItemCount) {
            if (mMainDraw != null) {
                val y = mMainRect?.top.orZero + baseLine - textHeight
                mMainDraw?.drawText(canvas, this, position, 3.ptFloat, y)
            }
            if (volDraw != null) {
                val y = mMainRect?.bottom.orZero + baseLine
                volDraw?.drawText(canvas, this, position, 3.ptFloat, y)
            }
            if (mChildDraw != null) {
                val y = volRect?.bottom.orZero + baseLine
                mChildDraw?.drawText(canvas, this, position, 3.ptFloat, y)
            }
        }
    }

    /**
     * 格式化值,有位数
     */
    fun formatValue(value: Float, digits: String?): String {
        if (valueFormatter == null) {
            valueFormatter = ValueFormatter()
        }
        return valueFormatter?.format(value, digits ?: "0") ?: ""
    }

    /**
     * MA/BOLL切换及隐藏
     *
     * @param status MA/BOLL/NONE
     */
    fun changeMainDrawType(status: Status) {
        if (mainDraw?.status != status) {
            mainDraw?.status = status
            invalidate()
        }
    }

    private fun calculateSelectedX(x: Float) {
        selectedIndex = indexOfTranslateX(xToTranslateX(x))
        if (selectedIndex < mStartIndex) {
            selectedIndex = mStartIndex
        }
        if (selectedIndex > mStopIndex) {
            selectedIndex = mStopIndex
        }
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        val lastIndex = selectedIndex
        calculateSelectedX(e.x)
        if (lastIndex != selectedIndex) {
            onSelectedChanged(this, getItem(selectedIndex), selectedIndex)
        }
        invalidate()
    }

    override fun onScaleChanged(scale: Float, oldScale: Float) {
        checkAndFixScrollX()
        setTranslateXFromScrollX(mScrollX)
        super.onScaleChanged(scale, oldScale)
    }

    override fun getMinScrollX(): Int {
        return (-(mOverScrollRange / mScaleX)).toSafeInt()
    }

    override fun getMaxScrollX(): Int {
        return (maxTranslateX - minTranslateX).toSafeInt()
    }

    /**
     * 获取平移的最大值
     *
     * @return
     */
    private val maxTranslateX: Float
        get() = if (!isFullScreen) {
            minTranslateX
        } else mPointWidth / 2

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    val isFullScreen: Boolean
        get() = mDataLen >= chartWidth / mScaleX

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    protected fun setTranslateXFromScrollX(scrollX: Int) {
        mTranslateX = scrollX + minTranslateX
    } // 如果数据很少无法布满整个屏幕，从左边显示
    //        if (isFullScreen()) {
    //            return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    //        } else {
    //            return 0;
    //        }
    /**
     * 获取平移的最小值
     *
     * @return
     */
    private val minTranslateX: Float
        get() = -mDataLen + chartWidth / mScaleX - mPointWidth / 2

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        setTranslateXFromScrollX(mScrollX)
    }

    //    @Override
    //    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    //        super.onSizeChanged(w, h, oldw, oldh);
    //        this.mWidth = w;
    //        displayHeight = h - mTopPadding - mBottomPadding;
    //        initRect();
    //        setTranslateXFromScrollX(mScrollX);
    //    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chartWidth = w
        chartHeight = h
        //列宽度
        mColumnSpace = chartWidth / mGridColumns
        //右边移动最大设置一个列宽度
        setOverScrollRange(mColumnSpace / 5 * 4.toFloat())
        //初始化默认滚动设置一个列宽度
        scrollX = (-mOverScrollRange).toInt()
        mDisplayHeight = h - mTopPadding - mBottomPadding
        initRect()
        setTranslateXFromScrollX(mScrollX)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundPaint.color)
        if (chartWidth == 0 || mMainRect?.height() == 0 || mItemCount == 0) {
            return
        }
        calculateValue()
        canvas.save()
        canvas.scale(1f, 1f)
        drawGird(canvas)
        drawK(canvas)
        drawText(canvas)
        drawMaxAndMin(canvas)
        //画开仓
        drawOpenPoint(canvas)
        //画止盈止损
        drawSlTpLine(canvas)
        //当前点
        drawNowPoint(canvas)
        //选中的值
        drawSelectPoint(canvas)
        //悬浮弹窗
        drawValue(canvas, if (isLongPress) selectedIndex else mStopIndex)
        canvas.restore()
    }

    open fun drawNowPoint(canvas: Canvas) {
        // 画动态线
        if (mItemCount != 0) {
            // 画线
            val point = getItem(mItemCount - 1) as? IKLine
            point?.apply {
                // 位数
                val digits = point.digits
                val y = getMainY(point.closePrice)
                canvas.drawLine(0f, y, canvas.width.toFloat(), y, mLastLinePaint)

                //保存之前的平移
                //                // 画倒锥方块
                val fm = textPaint.fontMetrics
                val textHeight = fm.descent - fm.ascent
                val w1 = 5.ptFloat
                val w2 = 2.ptFloat
                val r = textHeight / 2 + w2
                val text = formatValue(point.closePrice, digits)
                val textWidth = textPaint.measureText(text)
                val x = chartWidth.toFloat()
                val path = Path()
                path.moveTo(x, y - r)
                path.lineTo(x - w1 - textWidth, y - r)
                path.lineTo(x - w1 - textWidth - w1, y)
                path.lineTo(x - w1 - textWidth, y + r)
                path.lineTo(x, y + r)
                path.close()
                canvas.drawPath(path, mLastPaint)

                // 画文字
                canvas.drawText(text, x - w1 - textWidth, fixTextY1(y), mLastTextPaint)
            }
        }
        //还原平移
        canvas.restore()
    }

    //画选择线
    private fun drawSelectPoint(canvas: Canvas) {
        if (isLongPress) {
            val point = getItem(selectedIndex) as? IKLine
            val y = getMainY(point?.closePrice.orZero)
            val x = (getX(selectedIndex) + mTranslateX) * mScaleX
            // k线图竖线
            canvas.drawLine(x,
                mMainRect?.top.toSafeFloat(), x,
                mMainRect?.bottom.toSafeFloat(), mSelectedYLinePaint)
            // k线图横线
            canvas.drawLine(0f, y, chartWidth.toFloat(), y, mSelectedXLinePaint)
            // 柱状图竖线
            canvas.drawLine(x,
                mMainRect?.bottom.toSafeFloat(), x,
                volRect?.bottom.toSafeFloat(), mSelectedYLinePaint)
            if (mChildDraw != null) {
                // 子线图竖线
                canvas.drawLine(x,
                    volRect?.bottom.toSafeFloat(), x,
                    childRect?.bottom.toSafeFloat(), mSelectedYLinePaint)
            }
        }

        if (isLongPress) {
            val fm = textPaint.fontMetrics
            val textHeight = fm.descent - fm.ascent
            val baseLine = (textHeight - fm.bottom - fm.top) / 2
            // 画Y值
            val pointY = getItem(selectedIndex) as? IKLine
            // 位数
            val digitsY = pointY?.digits ?: "0"
            val w1 = 5.ptFloat
            val w2 = 2.ptFloat
            var r = textHeight / 2 + w2
            var y = getMainY(pointY?.closePrice.orZero)
            var x: Float
            val text = formatValue(pointY?.closePrice.orZero, digitsY)
            var textWidth = textPaint.measureText(text)
            val path = Path()
            val textX: Float
            val textY: Float
            if (translateXtoX(getX(selectedIndex)) < chartWidth / 2) {
                x = 0f
                path.moveTo(x, y - r)
                path.lineTo(x, y + r)
                path.lineTo(textWidth + w1, y + r)
                path.lineTo(textWidth + 2 * w1, y)
                path.lineTo(textWidth + w1, y - r)
                path.close()
                textX = x + w1
                textY = fixTextY1(y)
            } else {
                x = chartWidth - textWidth - 2 * w1
                path.moveTo(x, y)
                path.lineTo(x + w1, y + r)
                path.lineTo(chartWidth.toFloat(), y + r)
                path.lineTo(chartWidth.toFloat(), y - r)
                path.lineTo(x + w1, y - r)
                path.close()
                textX = x + w1
                textY = fixTextY1(y)
            }
            canvas.drawPath(path, mSelectPointPaint)
            canvas.drawText(text, textX, textY, mSelectTextPaint)

            // 画X值
            val date = mAdapter?.getDate(selectedIndex)
            textWidth = textPaint.measureText(date)
            r = textHeight / 2
            x = translateXtoX(getX(selectedIndex))
            y = if (isShowChild) {
                childRect?.bottom.toSafeFloat()
            } else {
                volRect?.bottom.toSafeFloat()
            }
            if (x < textWidth + 2 * w1) {
                x = 1 + textWidth / 2 + w1
            } else if (chartWidth - x < textWidth + 2 * w1) {
                x = chartWidth - 1 - textWidth / 2 - w1
            }
            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, mSelectPointPaint)
            canvas.drawText(date.orEmpty(), x - textWidth / 2, y + baseLine + 5, mSelectTextPaint)
        }
    }

    //画开仓线
    open fun drawOpenPoint(canvas: Canvas) {
        val openPrice = openData?.price ?: return
        val openTime = openData?.time ?: return
        val openIsBuy = openData?.isBuy ?: return

        if (openPrice <= 0 || openTime <= 0) return

        val realIndex = getPositionFromTime(openTime)
        val index = realIndex.orZero
        val x = (getX(index) + mTranslateX) * mScaleX
        val y = getMainY(openPrice)
        when {
            openIsBuy && isRiseRed() -> colorRed
            openIsBuy && !isRiseRed() -> colorGreen
            !openIsBuy && isRiseRed() -> colorGreen
            !openIsBuy && !isRiseRed() -> colorRed
            else -> colorGreen
        }.apply {
            mOpenXLinePaint.color = this
            mOpenTextBgPaint.color = this
            mOpenTextPaint.color = this
            mOpenCirclePaint.color = this
        }
        when {
            realIndex == null -> {
                canvas.drawLine(0f, y, chartWidth.toFloat(), y, mOpenXLinePaint)
            }
            x >= chartWidth - mPointWidth -> {
            }
            else -> if (x >= mPointWidth / 2) {
                canvas.drawCircle(x, y, 6.ptFloat, mOpenPointBgPaint)
                canvas.drawLine(x, y, chartWidth.toFloat(), y, mOpenXLinePaint)
                canvas.drawCircle(x, y, 6.ptFloat, mOpenCirclePaint)

                val path = Path()
                if (openIsBuy) {
                    path.moveTo(x - 2.3.ptFloat, y + 1.5.ptFloat)
                    path.lineTo(x + 2.3.ptFloat, y + 1.5.ptFloat)
                    path.lineTo(x, y - 2.1.ptFloat)
                } else {
                    path.moveTo(x - 2.3.ptFloat, y - 1.5.ptFloat)
                    path.lineTo(x + 2.3.ptFloat, y - 1.5.ptFloat)
                    path.lineTo(x, y + 2.1.ptFloat)
                }
                path.close()

                canvas.drawPath(path, mOpenArrowPaint)
            } else {
                canvas.drawLine(0f, y, chartWidth.toFloat(), y, mOpenXLinePaint)
            }
        }


        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        // 位数
        val digitsY = (getItem(0) as? IKLine)?.digits ?: "0"
        val w1 = 5.ptFloat
        val w2 = 2.ptFloat
        val r = textHeight / 2 + w2
        val text = formatValue(openPrice, digitsY)
        val textWidth = textPaint.measureText(text)
        val txtX = chartWidth - textWidth - 2 * w1

        val bgSquare = RectF(txtX + 2, y + r, chartWidth.toFloat() - 2, y - r)
        canvas.drawRoundRect(bgSquare, r, r, mOpenTextBgPaint)
        canvas.drawRoundRect(bgSquare, r, r, mOpenTextBgPaintBg)
        canvas.drawText(text, txtX + w1, fixTextY1(y), mOpenTextPaint)
    }


    private val slTpHorizontalPadding = 5.ptFloat
    private val slTpVerticalPadding = 2.ptFloat

    //画止损
    open fun drawSlTpLine(canvas: Canvas) {
        val isBuy = sltpData?.isBuy ?: return

        val colorSl: Int
        val colorTp: Int
        when {
            isBuy && isRiseRed() -> {
                colorTp = colorRed
                colorSl = colorGreen
            }
            isBuy && !isRiseRed() -> {
                colorTp = colorGreen
                colorSl = colorRed
            }
            !isBuy && isRiseRed() -> {
                colorSl = colorRed
                colorTp = colorGreen
            }
            !isBuy && !isRiseRed() -> {
                colorSl = colorGreen
                colorTp = colorRed
            }
            else -> {
                colorSl = colorGreen
                colorTp = colorRed
            }
        }

        val fm = mSlTpText.fontMetrics
        val textHeight = fm.descent - fm.ascent
        val originalTagHeight = textHeight + slTpVerticalPadding * 2

        val priceTp = sltpData?.tp?.price
        var ySl: Float? = null
        var isSlAbove: Boolean? = null
        var yTp: Float? = null
        var isTpAbove: Boolean? = null

        val priceSl = sltpData?.sl?.price
        if (priceSl != null && priceSl > 0) {
            val originalYSl = getMainY(priceSl)
            ySl = when {
                originalYSl <= 0 -> 1f
                originalYSl >= chartHeight -> chartHeight.toFloat() - 1f
                else -> originalYSl
            }
            isSlAbove = getIsTagAbove(!isBuy, chartHeight.toFloat(), originalTagHeight, ySl)
        }

        if (priceTp != null && priceTp > 0) {
            val originalYTp = getMainY(priceTp)
            yTp = when {
                originalYTp <= 0 -> 1f
                originalYTp >= chartHeight -> chartHeight.toFloat() - 1f
                else -> originalYTp
            }

            isTpAbove = getIsTagAbove(isBuy, chartHeight.toFloat(), originalTagHeight, yTp)
        }

        if (isBuy) {
            if (yTp != null && yTp <= originalTagHeight) { //买并且止盈在顶端，则先画止盈
                drawSlTp(canvas, priceTp, colorTp, isTpAbove, yTp, originalTagHeight)
                drawSlTp(canvas, priceSl, colorSl, isSlAbove, ySl, originalTagHeight)
            } else { //买并且止盈不在顶端，则先画止损
                drawSlTp(canvas, priceSl, colorSl, isSlAbove, ySl, originalTagHeight)
                drawSlTp(canvas, priceTp, colorTp, isTpAbove, yTp, originalTagHeight)
            }
        } else {
            if (ySl != null && ySl <= originalTagHeight) { //卖并且止损在顶端，则先画止损
                drawSlTp(canvas, priceSl, colorSl, isSlAbove, ySl, originalTagHeight)
                drawSlTp(canvas, priceTp, colorTp, isTpAbove, yTp, originalTagHeight)
            } else { //卖并且止损不在顶端，则先画止盈
                drawSlTp(canvas, priceTp, colorTp, isTpAbove, yTp, originalTagHeight)
                drawSlTp(canvas, priceSl, colorSl, isSlAbove, ySl, originalTagHeight)
            }
        }
    }

    private fun getIsTagAbove(isAbove: Boolean, chartHeight: Float, tagHeight: Float, y: Float): Boolean {
        return when {
            y < tagHeight -> false
            chartHeight - y < tagHeight -> true
            else -> isAbove
        }
    }

    private fun drawSlTp(canvas: Canvas, price: Float?, color: Int, isTagAbove: Boolean?, y: Float?, originalTagHeight: Float) {
        price ?: return
        isTagAbove ?: return
        y ?: return

        mSlTpLine.color = color
        mSlTpText.color = color

        val xLeft = 0f
        val xRight = chartWidth.toFloat() - 1f

        canvas.drawLine(xLeft, y, xRight, y, mSlTpLine)

        val point = getItem(mItemCount - 1) as? IKLine
        val digits = point?.digits

        val text = formatValue(price, digits)
        val textWidth = mSlTpText.measureText(text)

        val tagHeight = originalTagHeight * (if (isTagAbove) 1 else -1)

        val path = Path()
        path.moveTo(xRight, y)
        path.lineTo(xRight, y - tagHeight)
        path.lineTo(xRight - textWidth - slTpHorizontalPadding, y - tagHeight)
        path.lineTo(xRight - textWidth - slTpHorizontalPadding * 2, y)
        path.lineTo(xRight, y)
        path.close()
        canvas.drawPath(path, mSlTpBg)
        canvas.drawPath(path, mSlTpLine)

        // 画文字
        canvas.drawText(text, xRight - slTpHorizontalPadding - textWidth, fixTextY1(y - tagHeight / 2), mSlTpText)
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        backgroundPaint.color = color
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    fun setOverScrollRange(overScrollRange: Float) {
        mOverScrollRange = overScrollRange.min(0f)
    }

    /**
     * K线模块大小百分比分配
     */
    protected open fun initRect() {
        if (isShowChild) {
            val mMainHeight = (mDisplayHeight * 0.6f).toInt()
            val mVolHeight = (mDisplayHeight * 0.2f).toInt()
            val mChildHeight = (mDisplayHeight * 0.2f).toInt()
            mMainRect = Rect(0, mTopPadding, chartWidth, mTopPadding + mMainHeight)
            volRect =
                Rect(0, mMainRect?.bottom.orZero + mChildPadding, chartWidth, mMainRect?.bottom.orZero + mVolHeight)
            childRect =
                Rect(0, volRect?.bottom.orZero + mChildPadding, chartWidth, volRect?.bottom.orZero + mChildHeight)
        } else {
            // 精髓改动，将下面烛状图空白隐藏
            val mMainHeight = (mDisplayHeight * 0.95f).toInt()
            val mVolHeight = (mDisplayHeight * 0.05f).toInt()
            mMainRect = Rect(0, mTopPadding, chartWidth, mTopPadding + mMainHeight)
            volRect =
                Rect(0, mMainRect?.bottom.orZero + mChildPadding, chartWidth, mMainRect?.bottom.orZero + mVolHeight)
        }
    }

    /**
     * 计算当前的显示区域
     */
    private fun calculateValue() {
        if (!isLongPress) {
            selectedIndex = -1
        }
        mMainMaxValue = Float.MIN_VALUE
        mMainMinValue = Float.MAX_VALUE
        mVolMaxValue = Float.MIN_VALUE
        mVolMinValue = Float.MAX_VALUE
        mChildMaxValue = Float.MIN_VALUE
        mChildMinValue = Float.MAX_VALUE
        mStartIndex = indexOfTranslateX(xToTranslateX(0f))
        mStopIndex = indexOfTranslateX(xToTranslateX(chartWidth.toFloat()))
        mMainMaxIndex = mStartIndex
        mMainMinIndex = mStartIndex
        mMainHighMaxValue = Float.MIN_VALUE
        mMainLowMinValue = Float.MAX_VALUE
        for (i in mStartIndex..mStopIndex) {
            val point = getItem(i) as? IKLine ?: continue
            if (mMainDraw != null) {
                mMainMaxValue = max(mMainMaxValue, mMainDraw?.getMaxValue(point))
                mMainMinValue = min(mMainMinValue, mMainDraw?.getMinValue(point))
                if (mMainHighMaxValue != max(mMainHighMaxValue, point.highPrice)) {
                    mMainHighMaxValue = point.highPrice
                    mMainMaxIndex = i
                }
                if (mMainLowMinValue != min(mMainLowMinValue, point.lowPrice)) {
                    mMainLowMinValue = point.lowPrice
                    mMainMinIndex = i
                }
            }
            if (volDraw != null) {
                mVolMaxValue = max(mVolMaxValue, volDraw?.getMaxValue(point))
                mVolMinValue = min(mVolMinValue, volDraw?.getMinValue(point))
            }
            if (mChildDraw != null) {
                mChildMaxValue = max(mChildMaxValue, mChildDraw?.getMaxValue(point))
                mChildMinValue = min(mChildMinValue, mChildDraw?.getMinValue(point))
            }
        }
        if (mMainMaxValue != mMainMinValue) {
            val padding = (mMainMaxValue - mMainMinValue) * 0.05f
            mMainMaxValue += padding
            mMainMinValue -= padding
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += Math.abs(mMainMaxValue * 0.05f)
            mMainMinValue -= Math.abs(mMainMinValue * 0.05f)
            if (mMainMaxValue == 0f) {
                mMainMaxValue = 1f
            }
        }
        if (Math.abs(mVolMaxValue) < 0.01) {
            mVolMaxValue = 15.00f
        }
        // 指标显示异常,过小过大的开盘价会导致遮挡和不显示
        if (Math.abs(mChildMaxValue) < 0.01 && Math.abs(mChildMinValue) < 0.00001) {
            mChildMaxValue = 1f
        } else if (mChildMaxValue == mChildMinValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mChildMaxValue += Math.abs(mChildMaxValue * 0.05f)
            mChildMinValue -= Math.abs(mChildMinValue * 0.05f)
            if (mChildMaxValue == 0f) {
                mChildMaxValue = 1f
            }
        }
        if (isWR) {
            mChildMaxValue = 0f
            if (Math.abs(mChildMinValue) < 0.01) mChildMinValue = -10.00f
        }
        mMainScaleY = mMainRect?.height().orZero * 1f / (mMainMaxValue - mMainMinValue)
        mVolScaleY = volRect?.height().orZero * 1f / (mVolMaxValue - mVolMinValue)
        if (childRect != null) mChildScaleY =
            childRect?.height().orZero * 1f / (mChildMaxValue - mChildMinValue)
        if (mAnimator?.isRunning.orFalse) {
            val value = mAnimator?.animatedValue as? Float
            mStopIndex = mStartIndex + (value.orZero * (mStopIndex - mStartIndex)).roundToInt()
        }
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

    fun getMainY(value: Float): Float {
        return (mMainMaxValue - value) * mMainScaleY + mMainRect?.top.orZero
    }

    /**
     * 在主区域画分时线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    open fun drawMainMinuteLine(canvas: Canvas, paint: Paint, startX: Float, startValue: Float, stopX: Float, stopValue: Float) {
        val path5 = Path()
        path5.moveTo(startX, mDisplayHeight + mTopPadding + mBottomPadding.toFloat())
        path5.lineTo(startX, getMainY(startValue))
        path5.lineTo(stopX, getMainY(stopValue))
        path5.lineTo(stopX, mDisplayHeight + mTopPadding + mBottomPadding.toFloat())
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

    fun getChildY(value: Float): Float {
        return (mChildMaxValue - value) * mChildScaleY + childRect?.top.orZero
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

    fun getVolY(value: Float): Float {
        return (mVolMaxValue - value) * mVolScaleY + volRect?.top.orZero
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    fun getItem(position: Int): Any? {
        return mAdapter?.getItem(position)
    }

    /**
     * 根据索引获取实体
     *
     * @return
     */
    fun getLastItem(): Any? {
        return try {
            mAdapter?.count?.let { mAdapter?.getItem(it - 1) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    fun getPositionFromTime(time: Long): Int? {
        return mAdapter?.getPositionFromTime(time)
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
    /**
     * 设置数据适配器
     */
    var adapter: IAdapter?
        get() = mAdapter
        set(adapter) {
            if (mDataSetObserver != null) {
                mAdapter?.unregisterDataSetObserver(mDataSetObserver)
            }
            mAdapter = adapter
            mAdapter?.registerDataSetObserver(mDataSetObserver)
            mItemCount = mAdapter?.count.orZero
            notifyChanged()
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
            scrollX = 0
        }
        invalidate()
    }
    //    public void notifyChanged() {
    //        if (isShowChild && mChildDrawPosition == -1) {
    //            mChildDraw = mChildDraws.get(0);
    //            mChildDrawPosition = 0;
    //        }
    //        if (mItemCount != 0) {
    //            mDataLen = (mItemCount - 1) * mPointWidth;
    //            checkAndFixScrollX();
    //            setTranslateXFromScrollX(mScrollX);
    //        } else {
    //            //第一次设置adapter,默认滑动到最右端,空出一格空白,这个时候还没有回调onSizeChanged,所以改为onSizeChanged中初始化设置
    //            // setScrollX(0);
    //        }
    //        invalidate();
    //    }
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
     * 隐藏K线下方柱子
     */
    fun hideVolumeDraw() {
        volDraw = null
        invalidate()
    }

    /**
     * 给子区域添加画图方法
     *
     * @param childDraw IChartDraw
     */
    fun addChildDraw(childDraw: IChartDraw<Any>) {
        mChildDraws.add(childDraw)
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
        mMainDraw = mainDraw
        this.mainDraw = mMainDraw as MainDraw?
    }

    /**
     * 二分查找当前值的index
     *
     * @return
     */
    @JvmOverloads
    fun indexOfTranslateX(translateX: Float, start: Int = 0, end: Int = mItemCount - 1): Int {
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
     * 开始动画
     */
    fun startAnimation() {
        mAnimator?.start()
    }

    /**
     * 设置动画时间
     */
    fun setAnimationDuration(duration: Long) {
        mAnimator?.duration = duration
    }

    /**
     * 设置表格行数
     */
    fun setGridRows(gridRows: Int) {
        mGridRows = if (gridRows < 1) 1 else gridRows
    }

    /**
     * 设置表格列数
     */
    fun setGridColumns(gridColumns: Int) {
        mGridColumns = if (gridColumns < 1) 1 else gridColumns
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
    val topPadding: Float
        get() = mTopPadding.toFloat()

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    fun setTopPadding(topPadding: Int) {
        mTopPadding = topPadding
    }

    /**
     * 获取上方padding
     */
    val childPadding: Float
        get() = mChildPadding.toFloat()

    /**
     * 获取子试图上方padding
     */
    fun getmChildScaleYPadding(): Float {
        return mChildPadding.toFloat()
    }

    /**
     * 是否长按
     */
    override var isLongPress: Boolean
        get() = super.isLongPress
        set(isLongPress) {
            super.isLongPress = isLongPress
        }

    /**
     * 设置选择监听
     */
    fun setOnSelectedChangedListener(l: OnSelectedChangedListener?) {
        mOnSelectedChangedListener = l
    }

    fun onSelectedChanged(view: BaseKLineChartView?, point: Any?, index: Int) {
        mOnSelectedChangedListener?.onSelectedChanged(view, point, index)
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
        gridPaint.strokeWidth = width
        borderPaint.strokeWidth = width
    }

    /**
     * 设置表格线颜色
     */
    fun setGridLineColor(color: Int) {
        gridPaint.color = color
    }

    /**
     * 设置表格线颜色
     */
    fun setBorderLineColor(color: Int) {
        borderPaint.color = color
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
        mSelectedXLinePaint.color = color
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
        mSelectedYLinePaint.color = color
    }

    /**
     * 设置文字颜色
     */
    open fun setTextColor(color: Int) {
        textPaint.color = color
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    fun setMTextColor(color: Int) {
        mMaxMinPaint.color = color
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
        mSelectPointPaint.color = color
    }

    /**
     * 设置动态point 值显示背景
     */
    fun setLastLinePaintColor(color: Int) {
        mLastLinePaint.color = color
        mLastPaint.color = color
    }

    /**
     * 设置动态point 值显示文字
     */
    fun setLastTextPaintColor(color: Int) {
        mLastTextPaint.color = color
        mSelectTextPaint.color = color
    }

    /**
     * 设置开仓point
     */
    fun setOpenPaintBgColor(colorBg: Int) {
        mOpenTextBgPaintBg.color = colorBg
        mOpenPointBgPaint.color = colorBg
        mOpenArrowPaint.color = colorBg
    }

    /**
     * 设置SlTp背景
     */
    fun setSlTpBgColor(colorBg: Int) {
        mSlTpBg.color = colorBg
    }

    /**
     * 设置红绿色
     * */
    fun setRedGreen(red: Int, green: Int) {
        colorRed = red
        colorGreen = green
    }

    /**
     * 获取文字大小
     */
    /**
     * 设置文字大小
     */
    open var textSize: Float
        get() = textPaint.textSize
        set(textSize) {
            textPaint.textSize = textSize
            mLastTextPaint.textSize = textSize
            mSelectTextPaint.textSize = textSize
            mOpenTextPaint.textSize = textSize
            mSlTpText.textSize = textSize
        }

    /**
     * 设置每个点的宽度
     */
    fun setPointWidth(pointWidth: Float) {
        mPointWidth = pointWidth
    }

    fun getDisplayHeight(): Int {
        return mDisplayHeight + mTopPadding + mBottomPadding
    }


    /** 设置开仓价格*/
    open fun setOpenPrice(data: PointInfoBean?) {
        openData = data
    }

    /** 设置止盈止损*/
    open fun setSlTpData(data: SlTpInfoBean?) {
        sltpData = data
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

    fun max(num1: Float, num2: Float?): Float {
        return if (num2 == null) num1
        else kotlin.math.max(num1, num2)
    }

    fun min(num1: Float, num2: Float?): Float {
        return if (num2 == null) num1
        else kotlin.math.min(num1, num2)
    }


    /**
     * 设计图尺寸转换为实际尺寸
     */
    val Number?.pt: Int
        get() = if (isInEditMode) (pt1 * this.toSafeFloat()).toSafeInt() else pt2

    /**
     * 设计图尺寸转换为实际尺寸
     */
    val Number?.ptFloat: Float
        get() = if (isInEditMode) pt1 * this.toSafeFloat() else ptFloat2
}


class PointInfoBean(
    val price: Float,
    time: Long,
    val isBuy: Boolean = true
) {
    val time = when {
        time > 9_999_999_999 -> time / 1000
        else -> time
    }
}

class SlTpInfoBean(
    val isBuy: Boolean,
    val sl: SlTpDetailBean?,
    val tp: SlTpDetailBean?
)

class SlTpDetailBean(
    val price: Float
)