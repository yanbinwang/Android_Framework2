package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.example.base.utils.function.dip2px

/**
 * @description
 * @author
 */
class WordWrapLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    private val PADDING_HORIZONTAL by lazy { context.dip2px(10f) } //水平方向padding
    private val PADDING_VERTICAL by lazy { context.dip2px(5f) }//垂直方向padding
    private val MARGIN_CHILD by lazy { context.dip2px(10f) }//view左右间距

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //设置横纵坐标0,0开始，总行数为1
        var x = 0
        var y = 0
        var rows = 1
        //控件的实际宽度
        val actualWidth = MeasureSpec.getSize(widthMeasureSpec)
        //得到控件子view的总数
        val childCount = childCount
        for (index in 0 until childCount) {
            //给子view设置内部的padding
            val child = getChildAt(index)
            child.setPadding(
                PADDING_HORIZONTAL,
                PADDING_VERTICAL,
                PADDING_HORIZONTAL,
                PADDING_VERTICAL
            )
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val width = child.measuredWidth
            val height = child.measuredHeight
            //x坐标等于view本身的宽度加上设置的左右的margin
            x += width + MARGIN_CHILD
            //如果x累加的长度大于了实际容器的长度
            if (x > actualWidth) {
                //x等于view本身的长度加上间距（清空之前累加的值，算作第二行的第一个）
                x = width
                //总行数+1
                rows++
            }
            //计算view纵坐标间距
            y = rows * (height + MARGIN_CHILD)
        }
        //重新对view的显示长宽绘制，应等于计算出来的view的长宽的宽高加上margin和padding等操作的值
        setMeasuredDimension(actualWidth, y)
    }

    //返回控件的位置
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //得到容器内所有的子view的数量
        val childCount = childCount
        //得到控件的实际宽度（排除margin后）
        val actualWidth: Int = r - l
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
            x += width + MARGIN_CHILD
            //如果x累加的长度大于了实际容器的长度
            if (x > actualWidth) {
                //x等于view本身的长度加上间距（清空之前累加的值，算作第二行的第一个）
                x = width + MARGIN_CHILD
                //总行数+1
                rows++
            }
            //计算view纵坐标间距
            y = (rows - 1) * (height + MARGIN_CHILD)
            //重新对view的方向进行绘制
            view.layout(x - width - MARGIN_CHILD, y, x - MARGIN_CHILD, y + height)
        }
    }

}