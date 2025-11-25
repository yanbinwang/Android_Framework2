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
 * 封装动画的通用逻辑（如时间控制、进度计算、状态管理），并提供一个抽象方法 computeRender 让子类实现具体的、与进度相关的绘制逻辑
 * Created by yan
 */
abstract class LoadingRenderer(context: Context) {
    var mHeight = dip2px(context, 56f)
    var mWidth = mHeight
    var mDuration = ANIMATION_DURATION
    val mBounds = Rect()
    private var mDrawable: LoadingDrawable? = null
    private var mCallback: Drawable.Callback? = null
    private var mRenderAnimator: ValueAnimator? = null
    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        computeRender(animation.getAnimatedValue() as Float)
        invalidateSelf()
    }

    companion object {
        private const val ANIMATION_DURATION = 1333L
    }

    init {
        setupAnimators()
    }

    private fun setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        mRenderAnimator?.repeatCount = ValueAnimator.INFINITE
        mRenderAnimator?.repeatMode = ValueAnimator.RESTART
        mRenderAnimator?.setDuration(mDuration)
        mRenderAnimator?.interpolator = LinearInterpolator()
        mRenderAnimator?.addUpdateListener(mAnimatorUpdateListener)
    }

    private fun invalidateSelf() {
        mCallback?.invalidateDrawable(mDrawable ?: return)
    }

    protected fun addRenderListener(animatorListener: Animator.AnimatorListener) {
        mRenderAnimator?.addListener(animatorListener)
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

    fun setDrawable(drawable: LoadingDrawable?) {
        this.mDrawable = drawable
    }

    fun setBounds(bounds: Rect) {
        mBounds.set(bounds)
    }

    fun setCallback(callback: Drawable.Callback) {
        this.mCallback = callback
    }

    open fun draw(canvas: Canvas?) {
        draw(canvas, mBounds)
    }

    @Deprecated("")
    open fun draw(canvas: Canvas?, bounds: Rect?) {
    }

    abstract fun computeRender(renderProgress: Float)

    abstract fun setAlpha(alpha: Int)

    abstract fun setColorFilter(cf: ColorFilter?)

    abstract fun reset()

}