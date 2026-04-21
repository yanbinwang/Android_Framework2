package com.yanzhenjie.durban.utils

/**
 * 三次方缓动动画工具类
 * 作用：裁剪界面图片缩放、旋转、回弹的平滑动画计算
 */
object CubicEasing {

    /**
     * 缓动动画：先快后慢
     */
    @JvmStatic
    fun easeOut(time: Float, start: Float, end: Float, duration: Float): Float {
        val t = time / duration - 1.0f
        return end * (t * t * t + 1.0f) + start
    }

    /**
     * 缓动动画：慢→快→慢
     */
    @JvmStatic
    fun easeInOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var t = time / (duration / 2.0f)
        return if (t < 1.0f) {
            end / 2.0f * t * t * t + start
        } else {
            t -= 2.0f
            end / 2.0f * (t * t * t + 2.0f) + start
        }
    }

}