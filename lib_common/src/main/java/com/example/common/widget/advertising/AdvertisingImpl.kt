package com.example.common.widget.advertising

import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.common.R

/**
 *  Created by wangyanbin
 *  广告抽象类
 */
interface AdvertisingImpl {

    /**
     * @param uriList 图片的网络路径数组
     * @param ovalLayout 圆点容器 ,可为空
     * @param triple first：圆点选中时的背景ID second：圆点未选中时的背景ID third：圆点间距 （圆点容器可为空写0）
     * @param localAsset 是否是本地图片资源。默认否
     */
    fun start(uriList: ArrayList<String>, ovalLayout: LinearLayout? = null, triple: Triple<Int, Int, Int> = Triple(R.drawable.shape_advert_select, R.drawable.shape_advert_unselect, 10), localAsset: Boolean = false)

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