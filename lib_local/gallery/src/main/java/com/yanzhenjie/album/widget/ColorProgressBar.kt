package com.yanzhenjie.album.widget

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.gallery.R

/**
 * 可自定义颜色的 ProgressBar
 */
class ColorProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ProgressBar(context, attrs, defStyleAttr) {

    init {
        // 强制使用项目自带的无锯齿圆形加载图
        val loadingDrawable = ContextCompat.getDrawable(context, R.drawable.layer_list_loading)
        indeterminateDrawable = loadingDrawable?.mutate()
    }

    /**
     * 给加载条设置颜色
     */
    fun setColorFilter(@ColorInt color: Int) {
        // 获取系统自带的旋转动画条 / mutate() 让这个 Drawable 独立，不影响其他地方的 ProgressBar
        val drawable = indeterminateDrawable.mutate()
        // 着色
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        // 设置回去
        indeterminateDrawable = drawable
    }

}