package com.example.gallery.feature.album.widget.square

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * 正方形布局（根据屏幕方向自动适配）
 * 竖屏：宽 = 高
 * 横屏：高 = 宽
 * 专门用于相册图片网格列表（保证每个格子都是正方形）
 */
class SquareRelativeLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {
    // 获取屏幕方向配置
    private val mConfig = resources.configuration

    /**
     * 测量宽高，强制变成正方形
     * 竖屏：高 = 宽
     * 横屏：宽 = 高
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
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

}