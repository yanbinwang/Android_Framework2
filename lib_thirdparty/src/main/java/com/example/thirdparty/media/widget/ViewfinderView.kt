package com.example.thirdparty.media.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.toSafeFloat

/**
 * Created by wangyanbin
 * 相机外层边框，通过代码绘制
 */
@SuppressLint("DrawAllocation")
class ViewfinderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val screenRate = 20.pt//四个边角的长度
    private val cornerWidth = 1.pt //四个边角的粗细
    private val paint = Paint()//画笔对象

    override fun onDraw(canvas: Canvas) {
        val frame = Rect(0, 0, width, height)
        // 画笔颜色
        val maskColor = Color.GREEN
        paint.color = maskColor
        // 画扫描框边上的角，总共8个部分
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

    fun onShutter() {
        setBackgroundColor(color(android.R.color.black))
        if (animation != null) {
            if (animation.hasStarted() && !animation.hasEnded()) {
                return
            }
        }
        val anim = AlphaAnimation(1f, 0f)
        anim.fillAfter = false
        anim.duration = 500
        anim.interpolator = AccelerateInterpolator()
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                setBackgroundColor(color(android.R.color.transparent))
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        startAnimation(anim)
    }

}