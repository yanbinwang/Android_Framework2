package com.example.base.utils.function.view

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

//------------------------------------viewpager2扩展函数类------------------------------------
/**
 * ViewPager2隐藏fadingEdge
 */
fun ViewPager2?.hideFadingEdge() {
    if (this == null) return
    try {
        getRecyclerView()?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    } catch (ignore: Exception) {
    }
}

/**
 * ViewPager2获取内部RecyclerView
 */
fun ViewPager2?.getRecyclerView(): RecyclerView? {
    if (this == null) return null
    return try {
        (getChildAt(0) as RecyclerView)
    } catch (ignore: Exception) {
        null
    }
}

/**
 * 设置适配器扩展
 */
fun ViewPager2?.adapter(adapter: RecyclerView.Adapter<*>, orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL, isUserInput: Boolean = false, offscreenPage: Boolean = false) {
    if (this == null) return
    hideFadingEdge()
    setAdapter(adapter)
    setOrientation(orientation)
    if(offscreenPage) offscreenPageLimit = adapter.itemCount  - 1//预加载数量
    isUserInputEnabled = isUserInput //禁止左右滑动
}

/**
 * ViewPager2向后翻页
 */
fun ViewPager2?.nextPage(isSmooth: Boolean = true) {
    if (this == null) return
    adapter?.let { adapter ->
        if (adapter.itemCount == 0) return
        setCurrentItem(currentItem + 1, isSmooth)
    }
}

/**
 * ViewPager2向前翻页
 */
fun ViewPager2?.prevPage(isSmooth: Boolean = true) {
    if (this == null) return
    adapter?.let { adapter ->
        if (adapter.itemCount == 0) return
        setCurrentItem(currentItem - 1, isSmooth)
    }
}

/**
 * 降低ViewPager2灵敏度
 */
fun ViewPager2?.desensitization() {
    try {
        val recyclerView = getRecyclerView()
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * 3)
    } catch (ignore: java.lang.Exception) {
    }
}

/**
 * 绑定vp和tab
 */
fun ViewPager2?.bind(tab: TabLayout?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(tab ?: return null, this ?: return null, true, listener).apply { attach() }
}

/**
 * 绑定vp和tab
 */
fun TabLayout?.bind(vp: ViewPager2?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(this ?: return null, vp ?: return null, listener).apply { attach() }
}