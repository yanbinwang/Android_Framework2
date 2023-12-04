package com.example.thirdparty.media.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.toSafeFloat

/**
 * Created by wangyanbin
 * 相机外层边框，通过代码绘制
 */
@SuppressLint("DrawAllocation")
class ViewfinderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var screenRate = 20.pt//四个边角的长度
    private val cornerWidth = 1.pt //四个边角的粗细
    private val paint by lazy { Paint() } //画笔对象

    override fun onDraw(canvas: Canvas) {
        val frame = Rect(0, 0, width, height)
        //画笔颜色
        val maskColor = Color.GREEN
        paint.color = maskColor
        //画扫描框边上的角，总共8个部分
        paint.color = Color.YELLOW
        canvas.apply {
            drawRect(frame.left.toSafeFloat(), frame.top.toSafeFloat(), (frame.left + screenRate).toSafeFloat(), (frame.top + cornerWidth).toSafeFloat(), paint)
            drawRect(frame.left.toSafeFloat(), frame.top.toSafeFloat(), (frame.left + cornerWidth).toSafeFloat(), (frame.top + screenRate).toSafeFloat(), paint)
            drawRect((frame.right - screenRate).toSafeFloat(), frame.top.toSafeFloat(), frame.right.toSafeFloat(), (frame.top + cornerWidth).toSafeFloat(), paint)
            drawRect((frame.right - cornerWidth).toSafeFloat(), frame.top.toSafeFloat(), frame.right.toSafeFloat(), (frame.top + screenRate).toSafeFloat(), paint)
            drawRect(frame.left.toSafeFloat(), (frame.bottom - cornerWidth).toSafeFloat(), (frame.left + screenRate).toSafeFloat(), frame.bottom.toSafeFloat(), paint)
            drawRect(frame.left.toSafeFloat(), (frame.bottom - screenRate).toSafeFloat(), (frame.left + cornerWidth).toSafeFloat(), frame.bottom.toSafeFloat(), paint)
            drawRect((frame.right - screenRate).toSafeFloat(), (frame.bottom - cornerWidth).toSafeFloat(), frame.right.toSafeFloat(), frame.bottom.toSafeFloat(), paint)
            drawRect((frame.right - cornerWidth).toSafeFloat(), (frame.bottom - screenRate).toSafeFloat(), frame.right.toSafeFloat(), frame.bottom.toSafeFloat(), paint)
        }
    }
}