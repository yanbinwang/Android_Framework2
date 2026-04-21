package com.yanzhenjie.durban.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * 高效 Bitmap 绘制 Drawable
 * 作用：裁剪界面中专门用来显示图片的高性能组件
 */
class FastBitmapDrawable(b: Bitmap) : Drawable() {
    private var mWidth = 0
    private var mHeight = 0
    private var mAlpha = 255
    // 图片抗锯齿画笔
    private val mPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var mBitmap: Bitmap? = null

    init {
        setBitmap(b)
    }

    override fun draw(canvas: Canvas) {
        mBitmap?.takeIf { !it.isRecycled }?.let { bitmap ->
            canvas.drawBitmap(bitmap, null, bounds, mPaint)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mAlpha = alpha
        mPaint.setAlpha(alpha)
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.setColorFilter(cf)
    }

    override fun setFilterBitmap(filterBitmap: Boolean) {
        mPaint.isFilterBitmap = filterBitmap
    }

    override fun getIntrinsicWidth(): Int {
        return mWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mHeight
    }

    override fun getMinimumWidth(): Int {
        return mWidth
    }

    override fun getMinimumHeight(): Int {
        return mHeight
    }

    fun setBitmap(b: Bitmap?) {
        mBitmap = b
        mWidth = mBitmap?.width ?: 0
        mHeight = mBitmap?.height ?: 0
    }

    fun getBitmap(): Bitmap? {
        return mBitmap
    }

    override fun getAlpha(): Int {
        return mAlpha
    }

}