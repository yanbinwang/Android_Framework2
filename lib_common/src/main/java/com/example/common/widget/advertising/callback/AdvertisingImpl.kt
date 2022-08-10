package com.example.common.widget.advertising.callback

import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2

/**
 *  Created by wangyanbin
 *  广告抽象类
 */
interface AdvertisingImpl {

    /**
     * @param uriList 图片的网络路径数组 ,为空时 加载 adsId
     * @param ovalLayout 圆点容器 ,可为空
     * @param focusedId 圆点选中时的背景ID,圆点容器可为空写0
     * @param normalId 圆点正常时的背景ID,圆点容器为空写0
     * @param margin 左右边距，可为空写0
     * @param local 是否是本地图片资源。默认否
     */
    fun start(uriList: List<String>, ovalLayout: LinearLayout? = null, focusedId: Int, normalId: Int, margin: Int, local: Boolean)

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

    /**
     * 设置监听
     */
    fun setOnAdvertisingClickListener(onAdvertisingClickListener: OnAdvertisingClickListener)

}