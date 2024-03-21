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
     * @param radius 图片圆角
     * @param localAsset 是否是本地图片资源。默认否
     * @param ovalList 圆点资源
     * @param ovalLayout 圆点容器 ,可为空
     */
    fun start(uriList: ArrayList<String>, radius: Int = 0, localAsset: Boolean = false, ovalList: Triple<Drawable, Drawable, Int>? = null, ovalLayout: LinearLayout? = null)

    /**
     * 设置自动滚动
     */
    fun setAutoScroll(scroll: Boolean = true)

    /**
     * 设置方向
     */
    fun setOrientation(orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL)

    /**
     * 设置边距
     */
    fun setPageTransformer(marginPx: Int)

}