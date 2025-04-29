package com.example.framework.utils.builder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.forEach
import androidx.viewpager2.widget.ViewPager2
import com.example.framework.R
import com.example.framework.utils.PropertyAnimator.Companion.elasticityEnter
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.vibrate
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *  Created by wangyanbin
 *  导航栏帮助类,支持viewpage2绑定，fragment绑定
 *  builder.setOnItemSelectedListener { index, currentItem ->
 *  if (index == 2 && !isLogin()) {
 *  navigation(ARouterPath.LoginActivity)
 *  builder.selectedItem(currentItem)
 *  } else {
 *  if (!builder.isRepeat(index)) builder.selected(index)
 *  }
 *  }
 *
 *  <com.google.android.material.bottomnavigation.BottomNavigationView
 *  android:id="@+id/bnv_menu"
 *  android:layout_width="match_parent"
 *  android:layout_height="50pt"
 *  android:layout_gravity="bottom"
 *  android:background="@color/bgWhite"
 *  app:itemBackground="@null"
 *  app:itemIconSize="20pt"
 *  app:itemTextAppearanceActive="@style/BottomActiveText"
 *  app:itemTextAppearanceInactive="@style/BottomInactiveText"
 *  app:labelVisibilityMode="labeled"
 *  app:menu="@menu/menu_main_item" />
 *
 *  <!--没有选中的样式-->
 *  <style name="BottomInactiveText">
 *  <item name="android:textColor">@color/homeTextUnselected</item>
 *  <item name="android:textStyle">bold</item>
 *  <item name="android:textSize">@dimen/textSize10</item>
 *  </style>
 *
 *  <!--选中的样式-->
 *  <style name="BottomActiveText">
 *  <item name="android:textColor">@color/homeTextSelected</item>
 *  <item name="android:textStyle">bold</item>
 *  <item name="android:textSize">@dimen/textSize10</item>
 *  </style>
 */
//@SuppressLint("RestrictedApi")
//class NavigationBuilder(private val navigationView: BottomNavigationView?, private val ids: List<Int>, private val animation: Boolean = true) {
//    private var flipper: ViewPager2? = null
//    private var builder: FragmentBuilder? = null
//    private var onItemSelectedListener: ((index: Int) -> Unit)? = null
//    private val isPager get() = null != flipper
//    private val menuView get() = navigationView?.getChildAt(0) as? BottomNavigationMenuView
//
//    /**
//     * 初始化
//     */
//    init {
//        //去除长按的toast提示
//        for (position in ids.indices) {
//            menuView?.getChildAt(position)?.findViewById<View>(ids.safeGet(position).toSafeInt())?.setOnLongClickListener { true }
//        }
//        //最多配置5个tab，需要注意
//        navigationView?.setOnItemSelectedListener { item ->
//            //返回此次点击的下标
//            val index = ids.indexOfFirst { it == item.itemId }
//            //默认允许切换页面
//            selectTab(index)
//            //回调我们自己的监听，返回下标和前一次历史下标->-1就是没选过
//            onItemSelectedListener?.invoke(index)
//            true
//        }
//        //默认效果删除
//        navigationView?.itemIconTintList = null
//        navigationView?.itemTextColor = null
//    }
//
//    /**
//     * 绑定方法
//     */
//    fun bind(flipper: ViewPager2) {
//        this.flipper = flipper
//    }
//
//    fun bind(builder: FragmentBuilder) {
//        this.builder = builder
//    }
//
//    /**
//     * 只有禁止自动选择的模式/特许模式，才能调取
//     */
//    fun setSelect(index: Int, recreate: Boolean = false) {
//        //此时系统回收了页面，不管结果如何，立即切换
//        if (recreate) {
//            selectTab(index, true)
//        }
//        //新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器，监听器内做了页面的切换
//        //获取当前选中的Item ID
//        val currentItemId = navigationView?.selectedItemId
//        //要选中的Item ID
//        val toCurrentItemId = navigationView?.menu?.getItem(index)?.itemId.orZero
//        //开始调取item的切换，此时也会触发页面的selectTab（监听内，但监听内就会被return）
//        if (currentItemId != toCurrentItemId) {
//            selectItem(index)
//        }
//    }
//
//    /**
//     * 选中对应下标的item
//     * 仅当新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器
//     */
//    private fun selectItem(index: Int) {
//        val menu = navigationView?.menu
//        if (index < menu?.size().orZero) {
//            navigationView?.postDelayed({
//                navigationView.selectedItemId = menu?.getItem(index)?.itemId.orZero
//            }, 500)
//        }
////        navigationView?.post {
////            navigationView.selectedItemId = navigationView.menu.getItem(index)?.itemId.orZero
////        }
//    }
//
//    /**
//     * 选择对应下标的页面
//     */
//    private fun selectTab(tab: Int, recreate: Boolean = false) {
//        if (recreate) {
//            selectTabNow(tab, true)
//        } else {
//            if (getCurrentIndex() == tab || tab > ids.safeSize - 1 || tab < 0) return
//            selectTabNow(tab, false)
//        }
//    }
//
//    private fun selectTabNow(tab: Int, recreate: Boolean) {
//        //如果频繁点击相同的页面tab，不执行切换代码
////        if (!isRepeat(tab)) {
//            if (isPager) {
//                flipper?.setCurrentItem(tab, false)
//            } else {
//                builder?.selectTab(tab, recreate)
//            }
//            if (animation) {
//                getItemView(tab)?.getChildAt(0)?.apply {
//                    startAnimation(context.elasticityEnter())
//                    vibrate(50)
//                }
//            }
////        }
//    }
//
//    /**
//     * 获取下标item
//     */
//    fun getItemView(index: Int) = menuView?.getChildAt(index) as? BottomNavigationItemView
//
//    /**
//     * 获取当前选中的图片
//     */
//    fun getItemImage(index: Int = 0) = getItemView(index)?.findViewById(R.id.navigation_bar_item_icon_view) as? ImageView
//
//    /**
//     * 获取当前选中的下标
//     */
//    fun getCurrentIndex(): Int {
//        var currentIndex = 0
//        navigationView?.menu?.forEach { menuItem ->
//            if (menuItem.isChecked.orFalse)  {
//                currentIndex = ids.indexOfFirst { it == menuItem.itemId }
//            }
//        }
//        return currentIndex
//    }
//
////    /**
////     * 是否重复选择
////     */
////    fun isRepeat(index: Int): Boolean {
////        return index == (if (isPager) flipper?.currentItem else builder?.getCurrentIndex())
////    }
//
//    /**
//     * 添加角标
//     * <?xml version="1.0" encoding="utf-8"?>
//     * <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
//     *      android:layout_width="match_parent"
//     *      android:layout_height="match_parent"
//     *      android:orientation="vertical">
//     *
//     * <TextView
//     *      android:id="@+id/tv_msg_count"
//     *      android:layout_width="15dp"
//     *      android:layout_height="15dp"
//     *      android:layout_gravity="center"
//     *      android:layout_marginLeft="@dimen/dp_10"
//     *      android:layout_marginTop="@dimen/dp_3"
//     *      android:background="@drawable/bg_red_circle_10"
//     *      android:gravity="center"
//     *      android:textColor="@color/white"
//     *      android:textSize="@dimen/sp_12"
//     *      android:visibility="gone" />
//     *
//     * </LinearLayout>
//     */
//    fun addView(resource: Int, index: Int = 0): View {
//        //加载我们的角标View，新创建的一个布局
//        val badge = LayoutInflater.from(navigationView?.context).inflate(resource, menuView, false)
//        //添加到Tab上
//        getItemView(index)?.addView(badge)
//        //返回我们添加的view整体
//        return badge
//    }
//
//    /**
//     * 设置点击事件
//     */
//    fun setOnItemSelectedListener(onItemSelectedListener: ((index: Int) -> Unit)) {
//        this.onItemSelectedListener = onItemSelectedListener
//    }
//
//}
@SuppressLint("RestrictedApi")
class NavigationBuilder(private val navigationView: BottomNavigationView?, private val ids: List<Int>, private val animation: Boolean = true) {
    private var flipper: ViewPager2? = null
    private var builder: FragmentBuilder? = null
    private var listener: ((index: Int) -> Unit)? = null
    private val isPager get() = null != flipper
    private val menuView get() = navigationView?.getChildAt(0) as? BottomNavigationMenuView

    /**
     * 初始化
     */
    init {
        //去除长按的toast提示
        for (position in ids.indices) {
            menuView?.getChildAt(position)?.findViewById<View>(ids.safeGet(position).toSafeInt())?.setOnLongClickListener { true }
        }
        //最多配置5个tab，需要注意，每次点击都会触发回调
        navigationView?.setOnItemSelectedListener { item ->
            //返回此次点击的下标
            val index = ids.indexOfFirst { it == item.itemId }
            //默认允许切换页面
            selectTab(index)
            //回调我们自己的监听，返回下标和前一次历史下标->-1就是没选过
            listener?.invoke(index)
            true
        }
        //默认效果删除
        navigationView?.itemIconTintList = null
        navigationView?.itemTextColor = null
    }

    /**
     * 绑定方法
     */
    fun bind(flipper: ViewPager2, default: Int = 0) {
        this.flipper = flipper
        setSelect(default)
    }

    fun bind(builder: FragmentBuilder, default: Int = 0) {
        this.builder = builder
        setSelect(default)
    }

    /**
     * 只有禁止自动选择的模式/特许模式，才能调取
     */
    fun setSelect(index: Int, recreate: Boolean = false) {
        //此时系统回收了页面，不管结果如何，立即切换
        if (recreate) {
            selectTab(index, true)
        }
        //获取当前选中的Item ID
        val currentItemId = navigationView?.selectedItemId
        //要选中的Item ID
        val toCurrentItemId = navigationView?.menu?.getItem(index)?.itemId.orZero
        //开始调取item的切换，此时也会触发页面的selectTab（监听内，但监听内就会被return）
        if (currentItemId == toCurrentItemId) return
        //新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器
        selectItem(index)
    }

    /**
     * 选中对应下标的item
     * 仅当新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器
     */
    private fun selectItem(index: Int) {
        val menu = navigationView?.menu
        if (index < menu?.size().orZero) {
            navigationView?.postDelayed({
                navigationView.selectedItemId = menu?.getItem(index)?.itemId.orZero
            }, 500)
        }
    }

    /**
     * 选择对应下标的页面
     */
    private fun selectTab(tab: Int, recreate: Boolean = false) {
        if (recreate) {
            selectTabNow(tab, true)
        } else {
            if (getCurrentIndex() == tab || tab > ids.safeSize - 1 || tab < 0) return
            selectTabNow(tab, false)
        }
    }

    private fun selectTabNow(tab: Int, recreate: Boolean) {
        //如果频繁点击相同的页面tab，不执行切换代码
        if (isPager) {
            flipper?.setCurrentItem(tab, false)
        } else {
            builder?.selectTab(tab, recreate)
        }
        if (animation) {
            getItemView(tab)?.getChildAt(0)?.apply {
                startAnimation(context.elasticityEnter())
                vibrate(50)
            }
        }
    }

    /**
     * 获取下标item
     */
    fun getItemView(index: Int) = menuView?.getChildAt(index) as? BottomNavigationItemView

    /**
     * 获取当前选中的图片
     */
    fun getItemImage(index: Int = 0) = getItemView(index)?.findViewById(R.id.navigation_bar_item_icon_view) as? ImageView

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        var currentIndex = 0
        navigationView?.menu?.forEach { menuItem ->
            if (menuItem.isChecked.orFalse)  {
                currentIndex = ids.indexOfFirst { it == menuItem.itemId }
            }
        }
        return currentIndex
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
    fun addView(resource: Int, index: Int = 0): View {
        //加载我们的角标View，新创建的一个布局
        val badge = LayoutInflater.from(navigationView?.context).inflate(resource, menuView, false)
        //添加到Tab上
        getItemView(index)?.addView(badge)
        //返回我们添加的view整体
        return badge
    }

    /**
     * 设置点击事件
     */
    fun setOnItemSelectedListener(listener: ((index: Int) -> Unit)) {
        this.listener = listener
    }

}