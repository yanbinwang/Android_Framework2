package com.yanzhenjie.loading

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * 提供一个开箱即用的、带有动画效果的 “加载中” 视图
 * Created by yan
 */
class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mLoadingDrawable: LoadingDrawable? = null
    private var mLoadingRenderer: LevelLoadingRenderer? = null

    init {
        mLoadingRenderer = LevelLoadingRenderer(context)
        mLoadingDrawable = LoadingDrawable(mLoadingRenderer)
        setImageDrawable(mLoadingDrawable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    private fun startAnimation() {
        mLoadingDrawable?.start()
    }

    private fun stopAnimation() {
        mLoadingDrawable?.stop()
    }

    fun setCircleColors(r1: Int, r2: Int, r3: Int) {
        mLoadingRenderer?.setCircleColors(r1, r2, r3)
    }

}