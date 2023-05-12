package com.example.mvvm.utils.span

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Spannable
import android.text.Spanned
import android.text.style.ReplacementSpan
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import com.example.common.BaseApplication
import com.example.common.utils.function.pt
import com.example.mvvm.R

/**
 * description 一段文字中如果某行需要变为图片，则可以继承RankSpanInterface，
 * ‘在Cheezeebit交易，訂單賺取高達%1$s的訂單獎勵 ★’--》最后一个特殊星星符号就会被替换为对应的图片，每个大小会不一样，故而单独写成一个span
 * creator yan
 */
@SuppressLint("UseCompatLoadingForDrawables")
class RankSpan : ReplacementSpan() {

    private var mSize = 0
    private val bitmap = ResourcesCompat.getDrawable(BaseApplication.instance.resources, R.mipmap.ic_rank, null)?.toBitmapOrNull(60, 60)

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        mSize = paint.measureText(text, start, end).toInt()
        //mSize就是span的宽度，span有多宽，开发者可以在这里随便定义规则
        //可以根据传入起始截至位置获得截取文字的宽度，最后加上左右两个圆角的半径得到span宽度
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (bitmap == null) return
        val size = mSize * 0.8f
        val left = mSize * 0.1f
        paint.isAntiAlias = true //设置画笔的锯齿效果
        val center = (paint.descent() + paint.ascent()) / 2f
//        val oval = RectF(x + left, y + center - size / 2, x + mSize - left, y + center + size / 2)
        val oval = RectF(x + left, y + center - size / 2 - 5.pt, x + 24.pt - left, y + center + 22.pt / 2)
        canvas.drawBitmap(bitmap, null, oval, paint)
    }
}

interface RankSpanInterface {

    fun Spannable.setRankSpan(): Spannable {
        if (!contains("★")) return this
        var index = -1
        for (i in 1..10) {
            index = indexOf("★", index + 1)
            if (index in indices) {
                setSpan(RankSpan(), index, index + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            } else {
                break
            }
        }
        return this
    }

}