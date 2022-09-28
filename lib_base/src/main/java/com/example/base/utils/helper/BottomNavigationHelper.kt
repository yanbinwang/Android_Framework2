package com.example.base.utils.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.inAnimation
import com.example.base.utils.function.view.vibrate
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *  Created by wangyanbin
 *  导航栏帮助类,支持viewpage2绑定，fragment绑定
 */
class BottomNavigationHelper(private val navigationView: BottomNavigationView, private val ids: ArrayList<Int>, private val anim: Boolean = true) {
    private var flipper: ViewPager2? = null
    private var helper: FrameLayoutHelper? = null
    private var pageType = PageType.FRAGMENT
    var onItemSelected: ((index: Int, isCurrent: Boolean?) -> Unit)? = null

    enum class PageType {
        FRAGMENT, VIEWPAGER2
    }

    fun bind(flipper: ViewPager2) {
        this.flipper = flipper
        this.pageType = PageType.VIEWPAGER2
    }

    fun bind(helper: FrameLayoutHelper) {
        this.helper = helper
        this.pageType = PageType.FRAGMENT
    }

    /**
     * 初始化
     */
    init {
        //去除长按的toast提示
        for (position in ids.indices) {
            (navigationView.getChildAt(0) as ViewGroup).getChildAt(position).findViewById<View>(ids[position]).setOnLongClickListener { true }
        }
        //最多配置5个
        navigationView.setOnItemSelectedListener { item ->
            //返回第一个符合条件的元素的下标，没有就返回-1
            val index = ids.indexOfFirst { it == item.itemId }
            val isPager = pageType == PageType.VIEWPAGER2
            val isCurrent = index == if (isPager) flipper?.currentItem else helper?.currentIndex
            if (!isCurrent) {
                if (isPager) flipper?.setCurrentItem(index, false) else helper?.selectTab(index)
            }
            onItemSelected?.invoke(index, isCurrent)
            if (anim) getItemView(index).getChildAt(0).apply {
                startAnimation(context.inAnimation())
                vibrate(50)
            }
            true
        }
    }

    /**
     * 选中下标
     */
    fun selectedItem(index: Int) = run { navigationView.selectedItemId = navigationView.menu.getItem(index)?.itemId ?: 0 }

    /**
     * 获取下标item
     */
    fun getItemView(index: Int) = (navigationView.getChildAt(0) as BottomNavigationMenuView).getChildAt(index) as BottomNavigationItemView

    /**
     * 添加角标
     * <?xml version="1.0" encoding="utf-8"?>
     * <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
     *      android:layout_width="match_parent"
     *      android:layout_height="match_parent"
     *      android:orientation="vertical">
     *
     * <TextView
     *      android:id="@+id/tv_msg_count"
     *      android:layout_width="15dp"
     *      android:layout_height="15dp"
     *      android:layout_gravity="center"
     *      android:layout_marginLeft="@dimen/dp_10"
     *      android:layout_marginTop="@dimen/dp_3"
     *      android:background="@drawable/bg_red_circle_10"
     *      android:gravity="center"
     *      android:textColor="@color/white"
     *      android:textSize="@dimen/sp_12"
     *      android:visibility="gone" />
     *
     * </LinearLayout>
     */
    fun setTips(resource: Int, index: Int = 0) {
        //获取整个的NavigationView
        val menuView = navigationView.getChildAt(0) as BottomNavigationMenuView
        //这里就是获取所添加的每一个Tab(或者叫menu)
        val tab = menuView.getChildAt(index) as BottomNavigationItemView
        //加载我们的角标View，新创建的一个布局
        val badge = LayoutInflater.from(navigationView.context).inflate(resource, menuView, false)
        //添加到Tab上
        tab.addView(badge)
    }

}