package com.example.gallery.feature.album.widget.square

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 正方形 FrameLayout
 * 横竖屏自动适配，保持宽高相等 = 正方形
 * 用于相册网格中需要嵌套多层视图的条目
 */
class SquareFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    // 获取屏幕方向配置
    private val mConfig = resources.configuration

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val orientation = mConfig.orientation
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                super.onMeasure(widthMeasureSpec, widthMeasureSpec)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                super.onMeasure(heightMeasureSpec, heightMeasureSpec)
            }
            else -> {
                throw AssertionError("This should not be the case.")
            }
        }
    }

}