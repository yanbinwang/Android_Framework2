package com.example.reader.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.value.toSafeFloat
import com.example.reader.R
import com.example.reader.bean.EpubData
import com.example.reader.bean.EpubData.TYPE.IMG
import com.example.reader.bean.EpubData.TYPE.TEXT
import com.example.reader.bean.EpubData.TYPE.TITLE
import com.example.reader.utils.FileUtil
import com.example.reader.utils.ScreenUtil
import com.example.reader.utils.SpUtil

/**
 * 小说阅读器
 */
class PageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    val IS_TEST = false//是否进行单独测试
    private val TAG = "PageView"
    private var mIsShowContent = true//是否显示文本内容
    private var mListener: PageViewListener? = null
    protected val TYPE_TXT = 0//网络小说也属于 txt
    protected val TYPE_EPUB = 1
    protected var mPaint: Paint? = null
    protected var mTextSize = 0f//字体大小
    protected var mRowSpace = 0f//行距
    //纯文本绘制用
    protected var mContent = ""//文本内容
    protected var mPosition = 0//当前页第一个字的索引
    protected var mNextPosition = 0//下一页第一个字的索引
    //epub 绘制用
    protected var mEpubDataList = ArrayList<EpubData>()//epub 内容
    protected var mFirstPos = 0//第一位置索引，指向某个 EpubData
    protected var mSecondPos = 0//第二位置索引，指向 EpubData 内部字符串
    protected var mNextFirstPos = 0
    protected var mNextSecondPos = 0
    //当前页的索引（第几页，并不一定从 0 开始，只是作为 hashMap 的 key 而存在）
    protected var mPageIndex = 0
    protected var mFirstPosMap = HashMap<Int, Int>()
    protected var mSecondPosMap = HashMap<Int, Int>()
    //TYPE_TXT 为绘制普通文本（网络小说和本地 txt），TYPE_EPUB 为绘制 epub 文本（本地 epub）
    protected var mType = 0
    //翻页模式-》 NORMAL：普通翻页 REAL：仿真翻页
    protected var mTurnType = TURN_TYPE.NORMAL

    enum class TURN_TYPE {
        NORMAL, REAL
    }

    init {
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        mPaint?.color = resources.getColor(R.color.read_theme_0_text)

        mTextSize = SpUtil.getTextSize()
        mRowSpace = SpUtil.getRowSpace()
    }

    /**
     * 初始化，绘制纯文本
     */
    fun initDrawText(content: String, position: Int) {
        mContent = content
        mPosition = position
        mIsShowContent = true
        mPageIndex = 0
        mFirstPosMap.clear()
        mType = TYPE_TXT
        if (IS_TEST) {
            invalidate()
        }
    }

    /**
     * 初始化，绘制 epub
     */
    fun initDrawEpub(epubDataList: List<EpubData>, pos: Int, secondPos: Int) {
        mEpubDataList = epubDataList.toArrayList()
        mFirstPos = pos
        mSecondPos = secondPos
        mIsShowContent = true
        mPageIndex = 0
        mFirstPosMap.clear()
        mSecondPosMap.clear()
        mType = TYPE_EPUB
        if (IS_TEST) {
            invalidate()
        }
    }

    /**
     * 绘制前检查
     */
    protected fun checkBeforeDraw(): Boolean {
        if (!mIsShowContent) {
            return false
        }
        if (mType == TYPE_TXT && mContent.isEmpty()) {
            return false
        }
        return !(mType == TYPE_EPUB && mEpubDataList.isEmpty())
    }

    /**
     * 计算当前进度
     */
    protected fun calCurrProgress() {
        var f = 0f
        if (mType == TYPE_TXT) {
            f = mNextPosition.toSafeFloat() / mContent.length.toSafeFloat()
        } else if (mType == TYPE_EPUB) {
            f = calEpubProgress(mNextFirstPos, mNextSecondPos)
        }
        var progress: String
        if (f < 0.1f) {
            progress = (f * 100).toString()
            val end = 4.coerceAtMost(progress.length)
            progress = progress.substring(0, end) + "%"
        } else {
            progress = (f * 100).toString()
            val end = 5.coerceAtMost(progress.length)
            progress = progress.substring(0, end) + "%"
        }
        if (mListener != null) {
            mListener?.updateProgress(progress)
        }
    }

    protected fun drawText(canvas: Canvas?, textPaint: Paint) {
        textPaint.textSize = mTextSize
        drawText(canvas, textPaint, mTextSize + paddingTop)
        mFirstPosMap[mPageIndex] = mPosition
    }

    protected fun drawTextB(canvas: Canvas?, textPaint: Paint) {
        textPaint.textSize = mTextSize
        drawTextB(canvas, textPaint, mTextSize + paddingTop)
    }

    protected fun drawText(canvas: Canvas?, textPaint: Paint, currY: Float) {
        var posRecord = 0 // 记录当前页的头索引
        var content = "" // 绘制的内容
        if (mType == TYPE_TXT) {
            posRecord = mPosition
            content = mContent
        } else if (mType == TYPE_EPUB) {
            posRecord = mSecondPos
            content = mEpubDataList.safeGet(mFirstPos)?.data.orEmpty()
        }
        posRecord = drawTextImpl(canvas, textPaint, currY, content, posRecord)
        //更新相关变量
        if (mType == TYPE_TXT) {
            mNextPosition = posRecord
        } else if (mType == TYPE_EPUB) {
            if (posRecord == content.length) {
                mNextFirstPos = mFirstPos + 1
                mNextSecondPos = 0
            } else {
                mNextFirstPos = mFirstPos
                mNextSecondPos = posRecord
            }
        }
    }

    protected fun drawTextB(canvas: Canvas?, textPaint: Paint, currY: Float) {
        var posRecord = 0 //记录当前页的头索引
        var content = "" //绘制的内容
        if (mType == TYPE_TXT) {
            posRecord = mNextPosition
            content = mContent
        } else if (mType == TYPE_EPUB) {
            posRecord = mNextSecondPos
            content = mEpubDataList.safeGet(mNextFirstPos)?.data.orEmpty()
        }
        posRecord = drawTextImpl(canvas, textPaint, currY, content, posRecord)
    }

    /**
     * 真正进行文本绘制
     *
     * @param canvas 进行绘制的画布
     * @param currY 当前 Y 坐标
     * @param content 要绘制的文本内容
     * @param firstPos 要绘制的第一个字符的位置
     * @return
     */
    private fun drawTextImpl(canvas: Canvas?, textPaint: Paint, currY: Float, content: String?, firstPos: Int): Int {
        var mCurrY = currY
        var posRecord = firstPos
        val width = width.toFloat()
        val height = height.toFloat()
        val paddingBottom = paddingBottom
        val paddingStart = paddingStart
        val paddingEnd = paddingEnd
        var currX = paddingStart.toFloat()
        while (mCurrY < height - paddingBottom && posRecord < content?.length.orZero) {
            // 绘制下一行
            var add: Float // 为了左右两端对齐每个字需要增加的距离
            var num = 0 // 下一行的字数
            var textWidths = 0f // 下一行字体所占宽度
            var isNeed = false // 是否需要填充
            // 计算 add 和 num
            for (i in posRecord until content?.length.orZero) {
                val currS = content?.substring(i, i + 1)
                if (currS == "\n") {    // 换行
                    num++
                    break
                }
                val textWidth = getTextWidth(textPaint, currS)
                if (textWidths + textWidth >= width - paddingStart - paddingEnd) {  // 达到最大字数
                    isNeed = true
                    break
                }
                textWidths += textWidth
                num++
            }
            add = if (num <= 1) 0f else (width - paddingStart - paddingEnd - textWidths) / (num - 1)
            // 进行绘制
            for (i in 0 until num) {
                val currS = content?.substring(posRecord, posRecord + 1).orEmpty()
                if (currS == "\n") {
                    posRecord++
                    continue
                }
                canvas?.drawText(currS, currX, mCurrY, textPaint)
                currX += if (isNeed) {
                    getTextWidth(textPaint, currS) + add
                } else {
                    getTextWidth(textPaint, currS)
                }
                posRecord++
            }
            currX = paddingStart.toFloat()
            mCurrY += mTextSize + mRowSpace
        }
        return posRecord
    }

    protected fun drawEpub(canvas: Canvas, textPaint: Paint) {
        val width = width.toFloat()
        val height = height.toFloat()
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingStart = paddingStart
        val paddingEnd = paddingEnd
        var currY = when (mEpubDataList.safeGet(mFirstPos)?.type) {
            EpubData.TYPE.TEXT -> mTextSize + paddingTop
            EpubData.TYPE.TITLE -> mTextSize + mTextSize + paddingTop
            else -> 0f
        }
        //开始绘制
        var isFinished = false
        val tempFirstPos = mFirstPos
        val tempSecondPos = mSecondPos
        while (!isFinished) {
            val epubData = mEpubDataList.safeGet(mFirstPos)
            when (epubData?.type) {
                TEXT -> {
                    //普通文本绘制
                    textPaint.textSize = mTextSize
                    textPaint.textAlign = Paint.Align.LEFT
                    drawText(canvas, textPaint, currY)
                    isFinished = true
                }
                TITLE -> {
                    //绘制标题
                    val title = epubData.data
                    textPaint.textSize = mTextSize * 2//标题的字体更大
                    textPaint.textAlign = Paint.Align.CENTER//文字居中
                    while (currY <= height - paddingBottom && mSecondPos < title?.length.orZero) {
                        // 1. 计算能够绘制多少个字符
                        var num = 0
                        var currWidth = 0f
                        var i = mSecondPos
                        while (i < title?.length.orZero) {
                            val currS = title?.substring(i, i + 1)
                            val textWidth = getTextWidth(textPaint, currS)
                            if (currWidth + textWidth > width - paddingStart - paddingEnd) {
                                break
                            }
                            num++
                            currWidth += textWidth
                            i++
                        }
                        //2. 进行绘制
                        val currS = title?.substring(mSecondPos, mSecondPos + num).orEmpty()
                        canvas.drawText(currS, width / 2, currY, textPaint)
                        //3. 更新相关值
                        mSecondPos += num
                        currY += mTextSize + mTextSize + mRowSpace
                    }
                    //判断是否绘制完标题
                    if (mSecondPos < title?.length.orZero) {//没有绘制完
                        mNextFirstPos = mFirstPos
                        mNextSecondPos = mSecondPos
                        isFinished = true
                    } else {    // 绘制完成
                        if (currY >= height - paddingBottom || mFirstPos == mEpubDataList.size - 1 || mEpubDataList.safeGet(mFirstPos + 1)?.type === IMG) {
                            mNextFirstPos = mFirstPos + 1
                            mNextSecondPos = 0
                            isFinished = true
                        }
                        //还有位置，并且后面的是标题或文本，继续绘制
                        mFirstPos++
                        mSecondPos = 0
                    }
                }
                IMG -> {
                    //绘制图片
                    textPaint.textSize = mTextSize
                    textPaint.textAlign = Paint.Align.CENTER
                    val picPath = mEpubDataList.safeGet(mFirstPos)?.data
                    var bitmap = FileUtil.loadLocalPicture(picPath)
                    if (bitmap == null) {
                        val secondPath = mEpubDataList.safeGet(mFirstPos)?.secondData
                        bitmap = FileUtil.loadLocalPicture(secondPath)
                    }
                    if (bitmap != null) {
                        val src = Rect(0, 0, bitmap.width, bitmap.height)
                        val scale = bitmap.height.toFloat() / bitmap.width.toFloat()
                        val w = width.toInt() - paddingStart - paddingEnd
                        val h = (w * scale).toInt()
                        val dst = Rect(paddingStart, paddingTop, width.toInt() - paddingEnd, paddingTop + h)
                        canvas.drawBitmap(bitmap, src, dst, null)
                    } else {
                        canvas.drawText("图片加载失败", width / 2, height / 2, textPaint)
                    }
                    //更新变量
                    mNextFirstPos = mFirstPos + 1
                    mNextSecondPos = 0
                    isFinished = true
                }
                else -> null
            }
        }
        //恢复原值
        mFirstPos = tempFirstPos
        mSecondPos = tempSecondPos
        //更新 map
        mFirstPosMap[mPageIndex] = mFirstPos
        mSecondPosMap[mPageIndex] = mSecondPos
    }

    protected fun drawEpubB(canvas: Canvas, textPaint: Paint) {
        val width = width.toFloat()
        val height = height.toFloat()
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingStart = paddingStart
        val paddingEnd = paddingEnd
        var currY = when (mEpubDataList.safeGet(mNextFirstPos)?.type) {
            TEXT -> mTextSize + paddingTop
            TITLE -> mTextSize + mTextSize + paddingTop
            else -> 0f
        }
        //开始绘制
        var isFinished = false
        val tempFirstPos = mNextFirstPos
        val tempSecondPos = mNextSecondPos
        while (!isFinished) {
            val epubData = mEpubDataList[mNextFirstPos]
            when (epubData.type) {
                TEXT -> {
                    //普通文本绘制
                    textPaint.textSize = mTextSize
                    textPaint.textAlign = Paint.Align.LEFT
                    drawTextB(canvas, textPaint, currY)
                    isFinished = true
                }
                TITLE -> {
                    //绘制标题
                    val title = epubData.data
                    textPaint.textSize = mTextSize * 2//标题的字体更大
                    textPaint.textAlign = Paint.Align.CENTER//文字居中
                    while (currY <= height - paddingBottom && mNextSecondPos < title?.length.orZero) {
                        //1. 计算能够绘制多少个字符
                        var num = 0
                        var currWidth = 0f
                        var i = mNextSecondPos
                        while (i < title?.length.orZero) {
                            val currS = title?.substring(i, i + 1)
                            val textWidth = getTextWidth(textPaint, currS)
                            if (currWidth + textWidth > width - paddingStart - paddingEnd) {
                                break
                            }
                            num++
                            currWidth += textWidth
                            i++
                        }
                        //2. 进行绘制
                        val currS = title?.substring(mNextSecondPos, mNextSecondPos + num)
                        canvas.drawText(currS.orEmpty(), width / 2, currY, textPaint)
                        //3. 更新相关值
                        mNextSecondPos += num
                        currY += mTextSize + mTextSize + mRowSpace
                    }
                    //判断是否绘制完标题
                    if (mNextSecondPos < title?.length.orZero) {  //没有绘制完
                        isFinished = true
                    } else {    //绘制完成
                        if (currY >= height - paddingBottom || mNextFirstPos == mEpubDataList.size - 1 || mEpubDataList.safeGet(mNextFirstPos + 1)?.type === EpubData.TYPE.IMG) {
                            mNextFirstPos += 1
                            mNextSecondPos = 0
                            isFinished = true
                        }
                        //还有位置，并且后面的是标题或文本，继续绘制
                        mNextFirstPos++
                        mNextSecondPos = 0
                    }
                }
                IMG -> {
                    //绘制图片
                    textPaint.textSize = mTextSize
                    textPaint.textAlign = Paint.Align.CENTER
                    val picPath = mEpubDataList.safeGet(mNextFirstPos)?.data
                    var bitmap = FileUtil.loadLocalPicture(picPath)
                    if (bitmap == null) {
                        val secondPath = mEpubDataList.safeGet(mNextFirstPos)?.secondData
                        bitmap = FileUtil.loadLocalPicture(secondPath)
                    }
                    if (bitmap != null) {
                        val src = Rect(0, 0, bitmap.width, bitmap.height)
                        val scale = bitmap.height.toFloat() / bitmap.width.toFloat()
                        val w = width.toInt() - paddingStart - paddingEnd
                        val h = (w * scale).toInt()
                        val dst = Rect(paddingStart, paddingTop, width.toInt() - paddingEnd, paddingTop + h)
                        canvas.drawBitmap(bitmap, src, dst, null)
                    } else {
                        canvas.drawText("图片加载失败", width / 2, height / 2, textPaint)
                    }
                    isFinished = true
                }
                else -> null
            }
        }
        //恢复原值
        mNextFirstPos = tempFirstPos
        mNextSecondPos = tempSecondPos
    }

    /**
     * epub 根据 firstPos 和 secondPos 计算进度
     */
    private fun calEpubProgress(firstPos: Int, secondPos: Int): Float {
        if (firstPos == mEpubDataList.size) {
            return 1f
        }
        //计算数据量
        var curr = 0
        var sum = 0
        for (i in mEpubDataList.indices) {
            val epubData = mEpubDataList.safeGet(i)
            if (firstPos == i) {
                curr = sum + secondPos
            }
            when (epubData?.type) {
                IMG -> sum += 1
                TEXT, TITLE -> sum += epubData.data?.length.orZero
                else -> null
            }
        }
        return curr.toFloat() / sum.toFloat()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP -> {
                //根据离开时的位置进行不同操作
                val rawX = event.rawX
                val screenWidth = ScreenUtil.getScreenWidth()
                if (rawX <= 0.35f * screenWidth) {
                    //上一页
                    pre()
                } else if (rawX >= 0.65f * screenWidth) {
                    //下一页
                    next()
                } else {
                    //弹出或隐藏菜单
                    mListener?.showOrHideSettingBar()
                }
            }
            else -> {}
        }
        return true
    }

    /**
     * 绘制下一页
     */
    protected operator fun next(): Boolean {
        if (mType == TYPE_TXT) {
            mPosition = mNextPosition
            if (mPosition >= mContent.length) {//已经到达最后
                mListener?.next()//下一章节
                return false
            }
        } else if (mType == TYPE_EPUB) {
            mFirstPos = mNextFirstPos
            mSecondPos = mNextSecondPos
            if (mFirstPos == mEpubDataList.size) {
                mListener?.next()//下一章节
                return false
            }
        }
        mListener?.nextPage()
        mPageIndex++
        if (IS_TEST) {
            invalidate()
        }
        return true
    }

    /**
     * 绘制上一页
     */
    protected fun pre(): Boolean {
        mPageIndex--
        if (mType == TYPE_TXT) {
            if (mPosition == 0) {//已经是第一页
                mListener?.pre()//上一章节
                return false
            }
            if (mFirstPosMap.containsKey(mPageIndex)) {
                mPosition = mFirstPosMap[mPageIndex].orZero
            } else {
                //mPosition 更新为上一页的首字符位置
                updatePrePosTxt()
            }
        } else if (mType == TYPE_EPUB) {
            if (mFirstPos == 0 && mSecondPos == 0) {
                mListener?.pre()//上一章节
                return false
            }
            if (mFirstPosMap.containsKey(mPageIndex)) {
                mFirstPos = mFirstPosMap[mPageIndex].orZero
                mSecondPos = mSecondPosMap[mPageIndex].orZero
            } else {
                //计算 epub 的上一章节的位置索引
                updatePrePosEpub()
            }
        }
        mListener?.prePage()
        if (IS_TEST) {
            invalidate()
        }
        return true
    }

    /**
     * 获取字符串 str 利用 paint 绘制时的长度
     */
    private fun getTextWidth(paint: Paint?, str: String?): Float {
        var res = 0f
        if (!str.isNullOrEmpty()) {
            val widths = FloatArray(str.length)
            paint?.getTextWidths(str, widths)
            for (width in widths) {
                res += width
            }
        }
        return res
    }

    /**
     * 获取当前页第一个字符的位置
     */
    fun getPosition(): Int {
        return mPosition
    }

    /**
     * 设置第一个字符的位置
     */
    fun setPosition(mPosition: Int) {
        this.mPosition = mPosition
    }

    fun getFirstPos(): Int {
        return mFirstPos
    }

    fun getSecondPos(): Int {
        return mSecondPos
    }

    /**
     * 清除所有内容
     */
    fun clear() {
        mIsShowContent = false
        invalidate()
    }

    /**
     * 将 mPosition 更新为上一页的首字符位置
     */
    private fun updatePrePosTxt() {
        var currPos = mPosition - 1//当前页的字符位置
        val width = width.toFloat()
        val height = height.toFloat()
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingStart = paddingStart
        val paddingEnd = paddingEnd
        var currY = height - paddingBottom
        mPaint?.textSize = mTextSize
        while (currY >= mTextSize + paddingTop && currPos >= 0) {
            //绘制上一行
            var num = 0//上一行的字数
            var textWidths = 0f //上一行字体所占宽度
            for (i in currPos downTo 0) {
                val currS = mContent.substring(i, i + 1)
                if (currS == "\n") {//换行
                    num++
                    break
                }
                val textWidth = getTextWidth(mPaint, currS)
                if (textWidths + textWidth >= width - paddingStart - paddingEnd) {//达到最大字数
                    break
                }
                textWidths += textWidth
                num++
            }
            currPos -= num
            currY -= mTextSize + mRowSpace
        }
        //更新
        mPosition = if (currPos - 1 < 0) 0 else currPos - 1
    }

    /**
     * 将 mFirstPos, mSecondPos 更新为上一页的值
     */
    private fun updatePrePosEpub() {
        if (mSecondPos == 0) {//当前页是新的一页
            //上一数据是图片
            if (mEpubDataList.safeGet(mFirstPos - 1)?.type === EpubData.TYPE.IMG) {
                mFirstPos--
                return
            }
            //上一数据是标题或文本
            var finished = false
            var tempFirst = mFirstPos - 1
            var tempSecond: Int = mEpubDataList.safeGet(tempFirst)?.data?.length.orZero - 1
            var remainHeight = height - paddingTop - paddingBottom
            while (!finished) {
                val epubData = mEpubDataList.safeGet(tempFirst)
                when (epubData?.type) {
                    IMG -> {
                        mFirstPos = tempFirst + 1
                        mSecondPos = 0
                        finished = true
                    }
                    TITLE -> {
                        val textSize = mTextSize * 2
                        mPaint?.textSize = textSize
                        while (remainHeight >= textSize && tempSecond >= 0) {
                            var totalWidth = 0f
                            var num = 0
                            var i = tempSecond
                            while (i >= 0) {
                                val currS = mEpubDataList.safeGet(tempFirst)?.data?.substring(i, i + 1)
                                val textWidth = getTextWidth(mPaint, currS)
                                if (textWidth + totalWidth > width - paddingStart - paddingEnd) {
                                    break
                                }
                                totalWidth += textWidth
                                num++
                                i--
                            }
                            tempSecond -= num
                            remainHeight -= (textSize + mRowSpace).toInt()
                        }
                        //判断是否绘制完
                        if (tempSecond < 0) {  // 绘制完
                            tempFirst--
                            if (tempFirst < 0) {
                                mFirstPos = 0
                                mSecondPos = 0
                                finished = true
                                break
                            }
                            tempSecond = mEpubDataList.safeGet(tempFirst)?.data?.length.orZero - 1
                        } else {    //没有绘制完
                            mFirstPos = tempFirst
                            mSecondPos = tempSecond + 1
                            finished = true
                        }
                    }
                    TEXT -> {
                        mPaint?.textSize = mTextSize
                        while (remainHeight >= mTextSize && tempSecond >= 0) {
                            var totalWidth = 0f
                            var num = 0
                            var i = tempSecond
                            while (i >= 0) {
                                val currS =
                                    mEpubDataList.safeGet(tempFirst)?.data?.substring(i, i + 1)
                                if (currS == "\n") {
                                    num++
                                    break
                                }
                                val textWidth = getTextWidth(mPaint, currS)
                                if (textWidth + totalWidth > width - paddingStart - paddingEnd) {
                                    break
                                }
                                totalWidth += textWidth
                                num++
                                i--
                            }
                            tempSecond -= num
                            remainHeight -= (mTextSize + mRowSpace).toInt()
                        }
                        //判断是否绘制完
                        if (tempSecond < 0) {  //绘制完
                            tempFirst--
                            if (tempFirst < 0) {
                                mFirstPos = 0
                                mSecondPos = 0
                                finished = true
                                break
                            }
                            tempSecond = mEpubDataList.safeGet(tempFirst)?.data?.length.orZero - 1
                        } else {    //没有绘制完
                            mFirstPos = tempFirst
                            mSecondPos = tempSecond + 1
                            finished = true
                        }
                    }
                    else -> null
                }
            }
        } else {    //当前页不是新的
            var finished = false
            var tempFirst = mFirstPos
            var tempSecond = mSecondPos - 1
            var remainHeight = height - paddingTop - paddingBottom
            while (!finished) {
                val epubData = mEpubDataList.safeGet(tempFirst)
                when (epubData?.type) {
                    IMG -> {
                        mFirstPos = tempFirst + 1
                        mSecondPos = 0
                        finished = true
                    }
                    TITLE -> {
                        val textSize = mTextSize * 2
                        mPaint?.textSize = textSize
                        while (remainHeight >= textSize && tempSecond >= 0) {
                            var totalWidth = 0f
                            var num = 0
                            var i = tempSecond
                            while (i >= 0) {
                                val currS = mEpubDataList.safeGet(tempFirst)?.data?.substring(i, i + 1)
                                val textWidth = getTextWidth(mPaint, currS)
                                if (textWidth + totalWidth > width - paddingStart - paddingEnd) {
                                    break
                                }
                                totalWidth += textWidth
                                num++
                                i--
                            }
                            tempSecond -= num
                            remainHeight -= (textSize + mRowSpace).toInt()
                        }
                        // 判断是否绘制完
                        if (tempSecond < 0) {  // 绘制完
                            tempFirst--
                            if (tempFirst < 0) {
                                mFirstPos = 0
                                mSecondPos = 0
                                finished = true
                                break
                            }
                            tempSecond = mEpubDataList.safeGet(tempFirst)?.data?.length.orZero - 1
                        } else {    // 没有绘制完
                            mFirstPos = tempFirst
                            mSecondPos = tempSecond + 1
                            finished = true
                        }
                    }
                    TEXT -> {
                        mPaint?.textSize = mTextSize
                        while (remainHeight >= mTextSize && tempSecond >= 0) {
                            var totalWidth = 0f
                            var num = 0
                            var i = tempSecond
                            while (i >= 0) {
                                val currS =
                                    mEpubDataList.safeGet(tempFirst)?.data?.substring(i, i + 1)
                                if (currS == "\n") {
                                    num++
                                    break
                                }
                                val textWidth = getTextWidth(mPaint, currS)
                                if (textWidth + totalWidth > width - paddingStart - paddingEnd) {
                                    break
                                }
                                totalWidth += textWidth
                                num++
                                i--
                            }
                            tempSecond -= num
                            remainHeight -= (mTextSize + mRowSpace).toInt()
                        }
                        //判断是否绘制完
                        if (tempSecond < 0) {  //绘制完
                            tempFirst--
                            if (tempFirst < 0) {
                                mFirstPos = 0
                                mSecondPos = 0
                                finished = true
                                break
                            }
                            tempSecond = mEpubDataList.safeGet(tempFirst)?.data?.length.orZero - 1
                        } else {    //没有绘制完
                            mFirstPos = tempFirst
                            mSecondPos = tempSecond + 1
                            finished = true
                        }
                    }
                    else -> null
                }
            }
        }
    }

    /**
     * 设置文字颜色
     */
    fun setTextColor(color: Int) {
        mPaint?.color = color
    }

    /**
     * 根据进度跳转到指定位置（适合本地 txt 小说）
     */
    fun jumpWithProgress(progress: Float) {
        mPosition = (mContent.length * progress).toInt()
        mFirstPosMap.clear()
        invalidate()
    }

    /**
     * 设置翻页模式
     */
    fun setTurnType(mTurnType: TURN_TYPE?) {
        mTurnType ?: return
        this.mTurnType = mTurnType
    }

    /**
     * 回调监听
     */
    fun setPageViewListener(listener: PageViewListener) {
        mListener = listener
    }

    interface PageViewListener {
        /**
         * 通知主活动更新进度
         */
        fun updateProgress(progress: String)

        /**
         * 显示下一章节
         */
        fun next()

        /**
         * 显示上一章节
         */
        fun pre()

        /**
         * 下一页
         */
        fun nextPage()

        /**
         * 上一页
         */
        fun prePage()

        /**
         * 弹出或隐藏设置栏
         */
        fun showOrHideSettingBar()
    }

}