package com.example.album.widget.square

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/**
 * 正方形 CardView
 * 根据屏幕方向自动把自身设置为正方形
 * 用于相册里的图片缩略图，保证图片显示为正方形、不拉伸、不变形
 */
class SquareCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
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