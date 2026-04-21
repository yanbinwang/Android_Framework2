package com.example.gallery.album.widget.photoview.scrollerproxy

import android.content.Context
import android.widget.OverScroller

/**
 * Android 2.3 ~ 4.0 版本使用的滚动计算器
 * 直接使用系统 OverScroller 实现惯性滑动、回弹效果
 */
class GingerScroller(context: Context) : ScrollerProxy() {
    // 系统自带的滚动计算器（支持越界回弹、惯性滑动）
    private val mScroller = OverScroller(context)

    override fun computeScrollOffset(): Boolean {
        return mScroller.computeScrollOffset()
    }

    override fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int, maxY: Int, overX: Int, overY: Int) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY)
    }

    override fun forceFinished(finished: Boolean) {
        mScroller.forceFinished(finished)
    }

    override fun isFinished(): Boolean {
        return mScroller.isFinished
    }

    override fun getCurrX(): Int {
        return mScroller.currX
    }

    override fun getCurrY(): Int {
        return mScroller.currY
    }

}