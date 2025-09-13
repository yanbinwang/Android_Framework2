package com.yanzhenjie.loading

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.example.framework.utils.function.value.toSafeInt

/**
 * <p>Animation Drawable.</p>
 * Created by yanzhenjie on 17-3-27.
 */
class LoadingDrawable(private val loadingRender: LoadingRenderer) : Drawable(), Animatable {
    private val callback = object : Callback {
        override fun invalidateDrawable(who: Drawable) {
            invalidateSelf()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            scheduleSelf(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            unscheduleSelf(what)
        }
    }

    init {
        loadingRender.setCallback(callback)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        loadingRender.setBounds(bounds)
    }

    override fun draw(canvas: Canvas) {
        if (!bounds.isEmpty) {
            loadingRender.draw(canvas)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        loadingRender.setAlpha(alpha)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        loadingRender.setColorFilter(colorFilter)
    }

    override fun isRunning(): Boolean {
        return loadingRender.isRunning()
    }

    override fun start() {
        loadingRender.start()
    }

    override fun stop() {
        loadingRender.stop()
    }

    override fun getIntrinsicHeight(): Int {
        return loadingRender.mHeight.toSafeInt()
    }

    override fun getIntrinsicWidth(): Int {
        return loadingRender.mWidth.toSafeInt()
    }

}