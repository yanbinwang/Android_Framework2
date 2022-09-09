package com.example.base.utils.function.view

import android.view.ViewGroup
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
 * 设置适配器扩展
 */
fun ViewPager2?.adapter(adapter: RecyclerView.Adapter<*>, isUserInput: Boolean = false) {
    if (this == null) return
    try {
        hideFadingEdge()
        getRecyclerView()?.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = adapter.itemCount //预加载数量
            isUserInputEnabled = isUserInput //禁止左右滑动
        }
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
 * 绑定vp和tab
 */
fun ViewPager2?.bind(tab: TabLayout?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(tab ?: return null, this ?: return null) { _, _ -> }.apply { attach() }
}

/**
 * 绑定vp和tab
 */
fun TabLayout?.bind(vp: ViewPager2?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(this ?: return null, vp ?: return null, listener).apply { attach() }
}

/**
 * 设置TabLayout的边距
 */
fun TabLayout?.paddingEdge(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    this ?: return
    val view = (getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ViewGroup
    view?.padding(start, top, end, bottom)
    view?.clipToPadding = false
}