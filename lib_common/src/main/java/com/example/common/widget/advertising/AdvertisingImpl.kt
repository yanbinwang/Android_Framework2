package com.example.common.widget.advertising

import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2

/**
 *  Created by wangyanbin
 *  广告抽象类
 */
interface AdvertisingImpl {

    /**
     * @param uriList 图片的网络路径数组
     */
    fun start(uriList: ArrayList<String>)

    /**
     * @param radius 图片圆角
     * @param localAsset 是否是本地图片资源,默认否
     * @param ovalList 圆点资源
     * @param ovalLayout 圆点容器,可为空
     * @param scroll 是否自动滚动,默认是
     */
    fun setConfiguration(radius: Int = 0, localAsset: Boolean = false, scroll: Boolean = true, ovalList: Triple<Drawable, Drawable, Int>? = null, ovalLayout: LinearLayout? = null)

    /**
     * 设置方向
     */
    fun setOrientation(orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL)

    /**
     * 设置边距
     */
    fun setPageTransformer(marginPx: Int)

    /**
     * 设置背景色/状态栏深浅
     * @param barList 图片背景/状态栏深浅
     * @param onPageScrolled 滑动渐变背景
     */
    fun setWindowBar(barList: ArrayList<Pair<Boolean, Int>>, onPageScrolled: ((data: Pair<Boolean, Int>) -> Unit))

}