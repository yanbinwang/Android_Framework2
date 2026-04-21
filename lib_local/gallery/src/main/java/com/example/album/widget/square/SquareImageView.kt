package com.example.album.widget.square

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * 正方形 ImageView
 * 根据屏幕方向自动把自身设置为正方形
 * 用于相册里的图片缩略图，保证图片显示为正方形、不拉伸、不变形
 */
class SquareImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 获取屏幕方向配置
    private val mConfig = resources.configuration

    /**
     * 测量时强制宽高相等
     * 竖屏：高度 = 宽度
     * 横屏：宽度 = 高度
     */
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