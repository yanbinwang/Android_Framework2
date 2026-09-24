package com.example.gallery.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import com.example.framework.utils.function.drawable
import com.example.gallery.R

/**
 * 可自定义颜色的 ProgressBar
 */
class ColorProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ProgressBar(context, attrs, defStyleAttr) {

    init {
        // 强制使用项目自带的无锯齿圆形加载图
        val loadingDrawable = context.drawable(R.drawable.layer_list_loading)
        indeterminateDrawable = loadingDrawable
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 页面销毁兜底，停止动画释放资源
        isIndeterminate = false
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView === this) {
            when (visibility) {
                VISIBLE -> isIndeterminate = true
                GONE, INVISIBLE -> isIndeterminate = false
            }
        }
    }

    /**
     * 给加载条设置颜色
     */
    fun setColorFilter(@ColorInt color: Int) {
        // 获取系统自带的旋转动画条 / mutate() 让这个 Drawable 独立，不影响其他地方的 ProgressBar / 着色后设置回去
        indeterminateDrawable = indeterminateDrawable?.mutate()?.apply {
            setTint(color)
        }
    }

}