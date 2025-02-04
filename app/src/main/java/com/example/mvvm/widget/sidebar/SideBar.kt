package com.example.mvvm.widget.sidebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R

/**
 * SideBar类就是ListView右侧的字母索引View，我们需要使用setTextView(TextView mTextDialog)
 * 来设置用来显示当前按下的字母的TextView,以及使用setOnTouchingLetterChangedListener方法来设置
 * 回调接口，在回调方法onTouchingLetterChanged(String s)中来处理不同的操作
 */
class SideBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var choose = -1 // 选中
    private var mTextDialog: TextView? = null
    private var onTouchingLetterChangedListener: OnTouchingLetterChangedListener? = null//触摸事件
    private val paint by lazy { Paint() }
    private var b = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#") // 26个字母

    /**
     * 重写这个方法
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //获取焦点改变背景颜色.
        val height = height//获取对应高度
        val width = width//获取对应宽度
        val singleHeight = height / b.size//获取每一个字母的高度
        for (i in b.indices) {
            paint.color = Color.rgb(33, 65, 98)
            // paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.DEFAULT_BOLD)
            paint.isAntiAlias = true
//            paint.textSize = 40f
            paint.textSize = 15.ptFloat
            // 选中的状态
            if (i == choose) {
                paint.color = Color.parseColor("#3399ff")
                paint.isFakeBoldText = true
            }
            // x坐标等于中间-字符串宽度的一半.
            val xPos = width / 2 - paint.measureText(b[i]) / 2
            val yPos: Float = (singleHeight * i + singleHeight).toFloat()
            canvas.drawText(b[i], xPos, yPos, paint)
            paint.reset()//重置画笔
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        val y = event?.y.toSafeFloat()//点击y坐标
        val oldChoose = choose
        val listener = onTouchingLetterChangedListener
        val c = (y / height * b.size).toSafeInt() // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.
        when (action) {
            MotionEvent.ACTION_UP -> {
                setBackgroundDrawable(ColorDrawable(0x00000000))
                choose = -1 //
                invalidate()
                mTextDialog.invisible()
            }
            else -> {
                setBackgroundResource(R.drawable.shape_sidebar)
                if (oldChoose != c) {
                    if (c >= 0 && c < b.size) {
                        listener?.onTouchingLetterChanged(b[c])
                        mTextDialog?.text = b[c]
                        mTextDialog.visible()
                        choose = c
                        invalidate()
                    }
                }
            }
        }
        return true
    }

    /**
     * 为SideBar设置显示字母的TextView
     */
    fun setTextView(mTextDialog: TextView?) {
        this.mTextDialog = mTextDialog
    }

    /**
     * 向外公开的方法
     */
    fun setOnTouchingLetterChangedListener(onTouchingLetterChangedListener: OnTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener
    }

    /**
     * 接口回调
     */
    interface OnTouchingLetterChangedListener {
        fun onTouchingLetterChanged(s: String?)
    }

}