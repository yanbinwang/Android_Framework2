package com.example.framework.utils.builder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.framework.utils.function.scaleShown
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.vibrate
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *  Created by wangyanbin
 *  导航栏帮助类,支持viewpage2绑定，fragment绑定
 */
class NavigationBuilder(private val navigationView: BottomNavigationView, private val ids: List<Int>, private val animation: Boolean = true) {
    var flipper: ViewPager2? = null
    var builder: FragmentBuilder? = null
    var onItemSelected: ((index: Int, isCurrent: Boolean?) -> Unit)? = null

    /**
     * 初始化
     */
    init {
        //去除长按的toast提示
        for (position in ids.indices) {
            (navigationView.getChildAt(0) as? ViewGroup)?.getChildAt(position)?.findViewById<View>(ids.safeGet(position).toSafeInt())?.setOnLongClickListener { true }
        }
        //最多配置5个tab，需要注意
        navigationView.setOnItemSelectedListener { item ->
            //返回第一个符合条件的元素的下标，没有就返回-1
            val index = ids.indexOfFirst { it == item.itemId }
            //如果频繁点击相同的页面tab，不执行切换代码，只做结果返回
            val isPager = null != flipper
            val isCurrent = index == if (isPager) flipper?.currentItem else builder?.getCurrentIndex()
            if (!isCurrent) {
                if (isPager) flipper?.setCurrentItem(index, false) else builder?.selectTab(index)
                if (animation) getItemView(index)?.getChildAt(0)?.apply {
                    startAnimation(context.scaleShown())
                    vibrate(50)
                }
            }
            onItemSelected?.invoke(index, isCurrent)
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
    fun getItemView(index: Int) = (navigationView.getChildAt(0) as? BottomNavigationMenuView)?.getChildAt(index) as? BottomNavigationItemView

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        val menu = navigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.isChecked) return ids.indexOfFirst { it == menuItem.itemId }
        }
        return 0
    }

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
        val menuView = navigationView.getChildAt(0) as? BottomNavigationMenuView
        //这里就是获取所添加的每一个Tab(或者叫menu)
        val tab = menuView?.getChildAt(index) as? BottomNavigationItemView
        //加载我们的角标View，新创建的一个布局
        val badge = LayoutInflater.from(navigationView.context).inflate(resource, menuView, false)
        //添加到Tab上
        tab?.addView(badge)
    }

}