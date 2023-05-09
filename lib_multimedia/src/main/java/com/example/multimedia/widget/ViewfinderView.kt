package com.example.multimedia.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.common.utils.function.pt

/**
 * Created by wangyanbin
 * 相机外层边框，通过代码绘制
 */
@SuppressLint("DrawAllocation")
class ViewfinderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var screenRate = 20.pt//四个边角的长度
    private val cornerWidth = 1.pt //四个边角的粗细
    private val paint by lazy { Paint() } //画笔对象

    override fun onDraw(canvas: Canvas?) {
        val frame = Rect(0, 0, width, height)
        //画笔颜色
        val maskColor = Color.GREEN
        paint.color = maskColor
        //画扫描框边上的角，总共8个部分
        paint.color = Color.YELLOW
        canvas?.apply {
            drawRect(frame.left.toFloat(), frame.top.toFloat(), (frame.left + screenRate).toFloat(), (frame.top + cornerWidth).toFloat(), paint)
            drawRect(frame.left.toFloat(), frame.top.toFloat(), (frame.left + cornerWidth).toFloat(), (frame.top + screenRate).toFloat(), paint)
            drawRect((frame.right - screenRate).toFloat(), frame.top.toFloat(), frame.right.toFloat(), (frame.top + cornerWidth).toFloat(), paint)
            drawRect((frame.right - cornerWidth).toFloat(), frame.top.toFloat(), frame.right.toFloat(), (frame.top + screenRate).toFloat(), paint)
            drawRect(frame.left.toFloat(), (frame.bottom - cornerWidth).toFloat(), (frame.left + screenRate).toFloat(), frame.bottom.toFloat(), paint)
            drawRect(frame.left.toFloat(), (frame.bottom - screenRate).toFloat(), (frame.left + cornerWidth).toFloat(), frame.bottom.toFloat(), paint)
            drawRect((frame.right - screenRate).toFloat(), (frame.bottom - cornerWidth).toFloat(), frame.right.toFloat(), frame.bottom.toFloat(), paint)
            drawRect((frame.right - cornerWidth).toFloat(), (frame.bottom - screenRate).toFloat(), frame.right.toFloat(), frame.bottom.toFloat(), paint)
        }
    }
}