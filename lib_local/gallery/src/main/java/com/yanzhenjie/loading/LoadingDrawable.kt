package com.yanzhenjie.loading

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toSafeInt

/**
 * 自定义 Drawable，专门用于实现 加载动画
 * Created by yan
 */
class LoadingDrawable(private val mLoadingRender: LoadingRenderer?) : Drawable(), Animatable {

    init {
        mLoadingRender?.setDrawable(this)
        mLoadingRender?.setCallback(object : Callback {
            override fun invalidateDrawable(who: Drawable) {
                invalidateSelf()
            }

            override fun scheduleDrawable(who: Drawable, what: Runnable, millisecond: Long) {
                scheduleSelf(what, millisecond)
            }

            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                unscheduleSelf(what)
            }
        })
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mLoadingRender?.setBounds(bounds)
    }

    override fun draw(canvas: Canvas) {
        if (!getBounds().isEmpty) {
            mLoadingRender?.draw(canvas)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mLoadingRender?.setAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mLoadingRender?.setColorFilter(colorFilter)
    }

    override fun isRunning(): Boolean {
        return mLoadingRender?.isRunning().orFalse
    }

    override fun start() {
        mLoadingRender?.start()
    }

    override fun stop() {
        mLoadingRender?.stop()
    }

    override fun getIntrinsicHeight(): Int {
        return mLoadingRender?.mHeight.toSafeInt()
    }

    override fun getIntrinsicWidth(): Int {
        return mLoadingRender?.mWidth.toSafeInt()
    }

}