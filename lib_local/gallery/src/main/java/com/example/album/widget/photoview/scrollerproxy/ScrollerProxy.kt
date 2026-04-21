package com.example.album.widget.photoview.scrollerproxy

/**
 * 滚动计算器代理类
 * 作用：对不同 Android 版本的滚动效果（惯性滑动、回弹）做兼容
 */
abstract class ScrollerProxy {

    /**
     * 计算滚动偏移（系统滚动时调用）
     */
    abstract fun computeScrollOffset(): Boolean

    /**
     * 执行惯性滚动（Fling）
     */
    abstract fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int, maxY: Int, overX: Int, overY: Int)

    /**
     * 强制结束滚动
     */
    abstract fun forceFinished(finished: Boolean)

    /**
     * 滚动是否已经结束
     */
    abstract fun isFinished(): Boolean

    /**
     * 获取当前滚动到的 X 坐标
     */
    abstract fun getCurrX(): Int

    /**
     * 获取当前滚动到的 Y 坐标
     */
    abstract fun getCurrY(): Int

}