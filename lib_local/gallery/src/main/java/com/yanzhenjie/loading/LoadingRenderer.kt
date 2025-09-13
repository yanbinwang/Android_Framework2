package com.yanzhenjie.loading

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import com.example.framework.utils.function.value.orFalse
import com.yanzhenjie.loading.Utils.dip2px

/**
 * <p>Base Renderer.</p>
 * Created by yanzhenjie on 17-3-27.
 */
abstract class LoadingRenderer(context: Context) {
    /**
     * Whenever [LoadingDrawable] boundary changes mBounds will be updated.
     * More details you can see [LoadingDrawable.onBoundsChange]
     */
    protected val mBounds = Rect()
    protected var mDuration = 0L
    protected var mWidth = 0f
    protected var mHeight = 0f

    private var mCallback: Drawable.Callback? = null
    private var mRenderAnimator: ValueAnimator? = null
    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        computeRender(animation.getAnimatedValue() as Float)
        invalidateSelf()
    }

    init {
        mWidth = dip2px(context, 56f).also { mHeight = it }
        mDuration = ANIMATION_DURATION
        setupAnimators()
    }

    companion object {
        private const val ANIMATION_DURATION = 1333L
    }

    protected open fun draw(canvas: Canvas) {
        draw(canvas, mBounds)
    }

    @Deprecated("Use draw(Canvas) instead, bounds are now managed by mBounds", ReplaceWith("draw(canvas)"))
    protected open fun draw(canvas: Canvas, bounds: Rect) {
    }

    protected abstract fun computeRender(renderProgress: Float)

    protected abstract fun setAlpha(alpha: Int)

    protected abstract fun setColorFilter(cf: ColorFilter?)

    protected abstract fun reset()

    protected fun addRenderListener(animatorListener: Animator.AnimatorListener?) {
        mRenderAnimator?.addListener(animatorListener)
    }

    private fun setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        mRenderAnimator?.repeatCount = ValueAnimator.INFINITE
        mRenderAnimator?.repeatMode = ValueAnimator.RESTART
        mRenderAnimator?.duration = mDuration
        mRenderAnimator?.interpolator = LinearInterpolator()
        mRenderAnimator?.addUpdateListener(mAnimatorUpdateListener)
    }

    private fun invalidateSelf() {
        // 将 null 显式转换为 "平台类型 Drawable!"（Java 类型，空安全由开发者负责）
        val nullDrawable = null as Drawable
        // 传递平台类型参数，Kotlin 不会报错
        mCallback?.invalidateDrawable(nullDrawable)
    }

    fun start() {
        reset()
        mRenderAnimator?.addUpdateListener(mAnimatorUpdateListener)
        mRenderAnimator?.repeatCount = ValueAnimator.INFINITE
        mRenderAnimator?.setDuration(mDuration)
        mRenderAnimator?.start()
    }

    fun stop() {
        mRenderAnimator?.removeUpdateListener(mAnimatorUpdateListener)
        mRenderAnimator?.repeatCount = 0
        mRenderAnimator?.setDuration(0)
        mRenderAnimator?.end()
    }

    fun isRunning(): Boolean {
        return mRenderAnimator?.isRunning.orFalse
    }

    fun setCallback(callback: Drawable.Callback) {
        this.mCallback = callback
    }

    fun setBounds(bounds: Rect) {
        mBounds.set(bounds)
    }

}