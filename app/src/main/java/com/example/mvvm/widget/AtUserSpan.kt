package com.example.mvvm.widget

import android.R.attr
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan


/**
 * @description 插入的字符串
 * @author yan
 */
//class AtUserSpan(private val drawable: Drawable) : DynamicDrawableSpan() {
//    var name: String? = null
//    var uid: String? = null
//
//    override fun getDrawable(): Drawable {
//        return drawable
//    }
//
//}
class AtUserSpan(private val drawable: Drawable) : DynamicDrawableSpan() {
    var name: String? = null
    var uid: String? = null

    override fun getDrawable(): Drawable {
        return drawable
    }

//    override fun draw(
//        canvas: Canvas,
//        text: CharSequence?,
//        start: Int,
//        end: Int,
//        x: Float,
//        top: Int,
//        y: Int,
//        bottom: Int,
//        paint: Paint
//    ) {
//        val b: Drawable = getDrawable()
//        canvas.save()
//        val transY = (bottom - b.bounds.bottom) / 2 //transY就是Span绘制的高度
//        canvas.translate(attr.x.toFloat(), transY.toFloat())
//        b.draw(canvas)
//        canvas.restore()
//    }

}