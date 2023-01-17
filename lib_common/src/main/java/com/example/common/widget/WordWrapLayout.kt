package com.example.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.example.common.R
import com.example.common.utils.function.pt

class WordWrapLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private var paddingStart: Int? = 10
    private var paddingTop: Int? = 5
    private var paddingEnd: Int? = 10
    private var paddingBottom: Int? = 5
    private var marginChild: Int? = 10

    init {
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WordWrapLayout)
            paddingStart = typedArray.getInt(R.styleable.WordWrapLayout_paddingChildStart, 10)
            paddingTop = typedArray.getInt(R.styleable.WordWrapLayout_paddingChildTop, 5)
            paddingEnd = typedArray.getInt(R.styleable.WordWrapLayout_paddingChildEnd, 10)
            paddingBottom = typedArray.getInt(R.styleable.WordWrapLayout_paddingChildBottom, 5)
            marginChild = typedArray.getInt(R.styleable.WordWrapLayout_marginChild, 10)//view左右间距
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //设置横纵坐标0,0开始，总行数为1
        var x = 0
        var y = 0
        var rows = 1
        //控件的实际宽度
        val actualWidth = MeasureSpec.getSize(widthMeasureSpec)
        //得到控件子view的总数
        for (index in 0 until childCount) {
            //给子view设置内部的padding
            val child = getChildAt(index)
            child.setPadding(paddingStart.pt, paddingTop.pt, paddingEnd.pt, paddingBottom.pt)
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val width = child.measuredWidth
            val height = child.measuredHeight
            //x坐标等于view本身的宽度加上设置的左右的margin
            x += width + marginChild.pt
            //如果x累加的长度大于了实际容器的长度
            if (x > actualWidth) {
                //x等于view本身的长度加上间距（清空之前累加的值，算作第二行的第一个）
                x = width
                //总行数+1
                rows++
            }
            //计算view纵坐标间距
            y = rows * (height + marginChild.pt)
        }
        //重新对view的显示长宽绘制，应等于计算出来的view的长宽的宽高加上margin和padding等操作的值
        setMeasuredDimension(actualWidth, y)
    }

    //返回控件的位置
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //得到控件的实际宽度（排除margin后）
        val actualWidth = r - l
        //设置横纵坐标0,0开始，总行数为1
        var x = 0
        var y: Int
        var rows = 1
        for (i in 0 until childCount) {
            //得到容器内的一个view的实际宽高
            val view = getChildAt(i)
            val width = view.measuredWidth
            val height = view.measuredHeight
            //x坐标等于view本身的宽度加上设置的左右的margin
            x += width + marginChild.pt
            //如果x累加的长度大于了实际容器的长度
            if (x > actualWidth) {
                //x等于view本身的长度加上间距（清空之前累加的值，算作第二行的第一个）
                x = width + marginChild.pt
                //总行数+1
                rows++
            }
            //计算view纵坐标间距
            y = (rows - 1) * (height + marginChild.pt)
            //重新对view的方向进行绘制
            view.layout(x - width - marginChild.pt, y, x - marginChild.pt, y + height)
        }
    }

}