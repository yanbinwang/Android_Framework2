package com.example.framework.utils.builder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.forEach
import androidx.viewpager2.widget.ViewPager2
import com.example.framework.R
import com.example.framework.utils.enterAnimation
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
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
 */
@SuppressLint("RestrictedApi")
class NavigationBuilder(private val navigationView: BottomNavigationView?, private val ids: List<Int>, private val animation: Boolean = true) {
    private var currentItem = 0
    private var flipper: ViewPager2? = null
    private var builder: FragmentBuilder? = null
    private var onItemSelectedListener: ((index: Int, currentItem: Int) -> Unit)? = null
    private val isPager get() = null != flipper
    private val menuView get() = navigationView?.getChildAt(0) as? BottomNavigationMenuView
    var enableSelected = true//默认页面切换是在点击后就能触发的

    /**
     * 初始化
     */
    init {
        //去除长按的toast提示
        ids.indices.forEachIndexed { index, i ->
            menuView?.getChildAt(index)?.findViewById<View>(i)?.setOnLongClickListener { true }
        }
        //最多配置5个tab，需要注意
        navigationView?.setOnItemSelectedListener { item ->
            //返回此次点击的下标
            val index = ids.indexOfFirst { it == item.itemId }
            //默认允许切换页面
            if (enableSelected) selectTab(index)
            //回调我们自己的监听，返回下标和前一次历史下标
            onItemSelectedListener?.invoke(index, currentItem)
            true
        }
    }

    /**
     * 绑定方法
     */
    fun bind(flipper: ViewPager2) {
        this.flipper = flipper
    }

    fun bind(builder: FragmentBuilder) {
        this.builder = builder
    }

    /**
     * 只有禁止自动选择的模式才能调取
     */
    fun selected(index: Int) {
        if (!enableSelected) {
            selectedItem(index)
            selectTab(index)
        }
    }

    /**
     * 选中对应下标的item
     */
    fun selectedItem(index: Int) {
        navigationView?.post {
            navigationView.selectedItemId = navigationView.menu.getItem(index)?.itemId.orZero
        }
    }

    /**
     * 选择对应下标的页面
     */
    fun selectTab(index: Int) {
        if (index == -1) return
        currentItem = index
        //如果频繁点击相同的页面tab，不执行切换代码
        if (!isRepeat(index)) {
            if (isPager) {
                flipper?.setCurrentItem(index, false)
            } else {
                builder?.selectTab(index)
            }
            if (animation) {
                getItemView(index)?.getChildAt(0)?.apply {
                    startAnimation(context.enterAnimation())
                    vibrate(50)
                }
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
        navigationView?.menu?.forEach { menuItem ->
            if (menuItem.isChecked.orFalse) return ids.indexOfFirst { it == menuItem.itemId }
        }
        return 0
    }

    /**
     * 是否重复选择
     */
    fun isRepeat(index: Int): Boolean {
        return index == (if (isPager) flipper?.currentItem else builder?.getCurrentIndex())
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
    fun setOnItemSelectedListener(onItemSelectedListener: ((index: Int, currentItem: Int) -> Unit)) {
        this.onItemSelectedListener = onItemSelectedListener
    }

}