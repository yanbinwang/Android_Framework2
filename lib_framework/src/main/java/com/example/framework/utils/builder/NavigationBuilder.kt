package com.example.framework.utils.builder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.forEach
import androidx.core.view.size
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.framework.R
import com.example.framework.utils.PropertyAnimator.Companion.elasticityEnter
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.vibrate
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

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
 *
 *  方案一：通过 MenuItem 直接修改（最简单）
 * 如果你只是想修改菜单项的文本，可以直接通过 BottomNavigationView 的 menu 对象来操作：
 *
 * kotlin
 * val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
 *
 * // 获取指定位置的菜单项（例如修改第2个菜单项，下标从0开始）
 * val menuItem = bottomNavigationView.menu.getItem(1)
 * menuItem.title = "新的文字" // 设置新的标题文字
 *
 * 方案二：自定义 BottomNavigationView 样式
 * 如果你需要更灵活地控制文字样式（如字体、大小、颜色等），可以通过自定义样式实现：
 *
 * kotlin
 * // 先定义一个自定义样式
 * <style name="CustomBottomNavigationText" parent="Widget.Design.BottomNavigationView">
 *     <item name="android:textSize">12sp</item>
 *     <item name="android:textColor">@color/custom_color_state_list</item>
 *     <!-- 其他样式属性 -->
 * </style>
 *
 * // 在布局文件中应用样式
 * <com.google.android.material.bottomnavigation.BottomNavigationView
 *     android:id="@+id/bottom_navigation"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:itemTextAppearanceActive="@style/CustomBottomNavigationText"
 *     app:itemTextAppearanceInactive="@style/CustomBottomNavigationText"
 *     app:menu="@menu/bottom_nav_menu" />
 *
 * // 然后在代码中修改文字
 * bottomNavigationView.menu.getItem(1).title = "自定义文字"
 *
 * 方案三：使用反射获取子 View 修改（高级方法）
 * 如果你需要完全自定义某个菜单项的视图，可以通过反射获取内部视图：
 *
 * kotlin
 * // 获取 BottomNavigationMenuView
 * val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
 *
 * // 获取指定位置的菜单项视图（例如第2个菜单项）
 * if (index >= 0 && index < menuView.childCount) {
 *     val itemView = menuView.getChildAt(index) as BottomNavigationItemView
 *
 *     // 修改文字（注意：这种方式依赖于内部实现，可能在不同版本的Material Design库中变化）
 *     val smallLabel = itemView.findViewById<TextView>(com.google.android.material.R.id.smallLabel)
 *     val largeLabel = itemView.findViewById<TextView>(com.google.android.material.R.id.largeLabel)
 *
 *     smallLabel.text = "新的小文字"
 *     largeLabel.text = "新的大文字"
 * }
 *
 * 方案四：使用 BottomNavigationView 的 Tab 配置（如果结合 ViewPager）
 * 如果你使用 BottomNavigationView 结合 ViewPager2，可以通过 TabLayoutMediator 来配置：
 *
 * kotlin
 * // 假设你已经设置了 ViewPager2 和 BottomNavigationView
 * TabLayoutMediator(bottomNavigationView, viewPager) { tab, position ->
 *     // 根据位置设置不同的文字
 *     tab.text = when(position) {
 *         0 -> "首页"
 *         1 -> "新的文字" // 自定义第二个标签的文字
 *         2 -> "发现"
 *         else -> "其他"
 *     }
 * }.attach()
 */
@SuppressLint("RestrictedApi")
class NavigationBuilder(private val observer: LifecycleOwner, private val navigationView: BottomNavigationView?, private val ids: List<Int>, private val animation: Boolean = true) {
    private var bindMode = 0 // 绑定模式 -> 0：FragmentManager / 1：ViewPager2
    private var hasAction: Boolean = false // 是否重写点击
    private var flipper: ViewPager2? = null
    private var builder: FragmentBuilder? = null
    private var commitJob: Job? = null // 切换协程
    private var listener: ((index: Int) -> Unit)? = null
    private var clickActions = ConcurrentHashMap<Int, (() -> Unit)>()
    private val menuView get() = navigationView?.getChildAt(0) as? BottomNavigationMenuView

    /**
     * 初始化
     */
    init {
        observer.doOnDestroy {
            commitJob?.cancel()
        }
        // 去除长按的toast提示
        for (position in ids.indices) {
            menuView?.getChildAt(position)?.findViewById<View>(ids.safeGet(position).orZero)?.setOnLongClickListener { true }
        }
        // 最多配置5个tab，需要注意，每次点击都会触发回调(true：允许，false：拦截)
        navigationView?.setOnItemSelectedListener { item ->
            // 返回此次点击的下标
            val index = ids.indexOfFirst { it == item.itemId }
            if (hasAction && clickActions.safeSize > 0) {
                val action = clickActions[index]
                if (action != null) {
                    action.invoke()
                    false
                } else {
                    onItemSelected(index)
                    true
                }
            } else {
                onItemSelected(index)
                true
            }
        }
        // 默认效果删除
        navigationView?.itemIconTintList = null
        navigationView?.itemTextColor = null
    }

    private fun onItemSelected(index: Int) {
        // 默认允许切换页面
        selectTab(index)
        // 回调我们自己的监听，返回下标和前一次历史下标->-1就是没选过
        listener?.invoke(index)
    }

    /**
     * 绑定方法
     */
    fun bind(builder: FragmentBuilder, default: Int = 0) {
        this.bindMode = 0
        this.builder = builder
        setSelect(default)
    }

    fun bind(flipper: ViewPager2, default: Int = 0) {
        this.bindMode = 1
        this.flipper = flipper
        setSelect(default)
    }

    /**
     * 只有禁止自动选择的模式/特许模式，才能调取
     */
    fun setSelect(index: Int, recreate: Boolean = false) {
        // 此时系统回收了页面，不管结果如何，立即切换
        if (recreate) {
            selectTab(index, true)
        }
        // 获取当前选中的Item ID
        val currentItemId = navigationView?.selectedItemId
        // 要选中的Item ID
        val toCurrentItemId = navigationView?.menu?.getItem(index)?.itemId.orZero
        // 开始调取item的切换，此时也会触发页面的selectTab（监听内，但监听内就会被return）
        if (currentItemId == toCurrentItemId) return
        // 新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器
        selectItem(index)
    }

    /**
     * 选中对应下标的item
     * 仅当新选中的 itemId 与当前选中的 itemId 不同时，才会触发监听器
     */
    private fun selectItem(index: Int) {
        val menu = navigationView?.menu
        if (index < menu?.size.orZero) {
            commitJob?.cancel()
            commitJob = observer.lifecycleScope.launch(Main.immediate) {
                navigationView?.selectedItemId = menu?.getItem(index)?.itemId.orZero
            }
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
        // 如果频繁点击相同的页面tab，不执行切换代码
        if (bindMode == 1) {
            flipper?.setCurrentItem(tab, false)
        } else {
            builder?.commit(tab, recreate)
        }
        if (animation) {
            getItemView(tab)?.getChildAt(0)?.apply {
                startAnimation(context.elasticityEnter())
                vibrate(50)
            }
        }
    }

    /**
     * 对应下标需求对应不同的点击，改为自定义
     */
    fun addClickAllowed(vararg params: Pair<Int, (() -> Unit)>) {
        hasAction = true
        clickActions = ConcurrentHashMap(params.toMap())
    }

    /**
     * 还原对应下标的点击
     * addClickAllowed后可还原
     */
    fun allowedReset(index: Int) {
        hasAction = true
        clickActions.remove(index)
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
        // 加载我们的角标View，新创建的一个布局
        val badge = LayoutInflater.from(navigationView?.context).inflate(resource, menuView, false)
        // 添加到Tab上
        getItemView(index)?.addView(badge)
        // 返回我们添加的view整体
        return badge
    }

    /**
     * 设置点击事件
     */
    fun setOnItemSelectedListener(listener: ((index: Int) -> Unit)) {
        this.listener = listener
    }

}