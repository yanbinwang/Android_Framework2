package com.example.common.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.TextPaint
import android.text.style.ReplacementSpan
import com.example.base.utils.function.value.toSafeFloat

/**
 * first->id
 * second->文字大小
 * third->颜色
 */
class BackgroundImageSpan(private val triple: Triple<Int, Int, Int>) : ReplacementSpan(), ParcelableSpan {
    private var mWidth = -1

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        draw(canvas, mWidth, x, top, y, bottom, paint)
        canvas.drawText(text.toString(), start, end, x, y.toSafeFloat(), paint)
    }

    private fun draw(canvas: Canvas, width: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.textSize = triple.second.pt.toSafeFloat()
        paint.color = triple.third
        val mDrawable = drawable(triple.first)
        canvas.save()
        canvas.translate(x, top.toFloat())
        mDrawable?.setBounds(0, 0, width, bottom - top)
        mDrawable?.draw(canvas)
        canvas.restore()
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val size = paint.measureText(text, start, end)
        if (fm != null) paint.getFontMetricsInt(fm)
        mWidth = size.toInt()
        return mWidth
    }

    override fun updateDrawState(ds: TextPaint?) {
    }

    override fun getSpanTypeId(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(triple.first)
    }

    override fun describeContents(): Int {
        return 0
    }

}