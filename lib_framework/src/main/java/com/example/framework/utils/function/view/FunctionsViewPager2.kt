package com.example.framework.utils.function.view

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orZero
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

//------------------------------------viewpager2扩展函数类------------------------------------
/**
 * ViewPager2获取内部RecyclerView
 */
fun ViewPager2?.getRecyclerView(): RecyclerView? {
    if (this == null) return null
    return try {
        (getChildAt(0) as? RecyclerView)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * ViewPager2隐藏fadingEdge
 */
fun ViewPager2?.hideFadingEdge() {
    if (this == null) return
    try {
        getRecyclerView()?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 降低ViewPager2灵敏度
 * @param multiplier 灵敏度倍数，默认为 3
 */
fun ViewPager2?.reduceSensitivity(multiplier: Int = 3) {
    try {
        val recyclerView = getRecyclerView()
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as? Int
        touchSlopField.set(recyclerView, touchSlop.orZero * multiplier)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 设置页面滑动监听
 */
fun ViewPager2?.setOnPageChangeListener(owner: LifecycleOwner? = getLifecycleOwner(), callback: ViewPager2.OnPageChangeCallback) {
    if (this == null) return
    registerOnPageChangeCallback(callback)
    owner.doOnDestroy {
        unregisterOnPageChangeCallback(callback)
    }
}

/**
 * 选中某页
 * class EvidencePageAdapter(private val fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
 *
 * private companion object {
 *      private const val PAGE_COUNT = 7       // 总页面数
 *      private const val POSITION_EXTRA = 0   // 未上传列表
 * }
 *
 * override fun getItemCount() = PAGE_COUNT
 *
 * override fun createFragment(position: Int): Fragment {
 *      return when (position) {
 *          POSITION_EXTRA -> EvidenceExtraFragment()
 *          else -> EvidenceListFragment().apply { arguments = Bundle().apply { putString(Extra.ID, (position - 1).toString()) } }
 *      }
 * }
 *
 * /**
 *  * 通过位置查找对应 Fragment
 *  * 当 FragmentStateAdapter 向 FragmentManager 添加 Fragment 时，官方会自动为每个位置的 Fragment 生成固定格式的 TAG："f" + position（比如位置 0 对应 TAG "f0"、位置 1 对应 TAG "f1"）
 * */
 * private fun findFragmentByPosition(position: Int): Fragment? {
 *      // 官方固定的 Fragment TAG 格式，不可修改
 *      val fragmentTag = "f$position"
 *      // 通过 FragmentManager 查找对应 TAG 的 Fragment
 *      return fragmentManager.findFragmentByTag(fragmentTag)
 * }
 *
 * /**
 *  * 获取未上传列表
 * */
 * fun getExtra(): EvidenceExtraFragment? {
 *      return findFragmentByPosition(POSITION_EXTRA) as? EvidenceExtraFragment
 * }
 *
 * }
 */
fun ViewPager2?.setCurrent(item: Int, smoothScroll: Boolean = true) {
    this ?: return
    val itemCount = adapter?.itemCount.orZero
    if (item in 0 until itemCount && item != currentItem) {
        try {
            setCurrentItem(item, smoothScroll)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
 * 设置适配器扩展
 */
fun ViewPager2?.adapter(adapter: RecyclerView.Adapter<*>, orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL, userInputEnabled: Boolean = true, pageLimit: Boolean = false) {
    if (this == null) return
    hideFadingEdge()
    setAdapter(adapter)
    setOrientation(orientation)
    /**
     * offscreenPageLimit默认值-1，系统会根据屏幕布局和可见区域自动计算可保留的页面数量
     * 如果页面宽度与屏幕宽度一致，则仅保留当前页面，如果页面宽度小于屏幕宽度（如横向滑动的分屏布局），则可能保留多个页面
     *
     * offscreenPageLimit = -1 的实际逻辑：
     * ViewPager2 会根据屏幕布局和页面尺寸动态计算可保留的页面数。通常情况下：
     * 单列布局（每个页面占满屏幕宽度）：系统会自动将 offscreenPageLimit 视为 1，即保留当前页面和相邻的一个页面（如果存在）。
     * 多列布局（页面宽度小于屏幕宽度）：可能保留更多页面以支持横向滚动。
     *
     * 1）目前已经不能被设为0，因为源码里规定如果要传值必须大于0，切默认配置是关闭，不会预加载，且fragment最好继承BaseLazyFragment，保证请求也滞后执行
     * 2）不开启的情况下就是只加载当前页，开启的情况下，有多少子页面就预加载多少，推荐fragment继承BaseLazyFragment，保证请求滞后执行，节省一部分内存
     * 3）不开启的情况下，如果使用EvenBus刷新子页面，需要获取Fragment的isAdded和对应参数来判断是否发起网络请求，开启的情况下，isAdded无需书写
     */
    val limitCount = adapter.itemCount - 1
    if (pageLimit) offscreenPageLimit = if (limitCount <= 0) 1 else limitCount
    //false 可以禁止用户通过手势左右滑动页面，但页面仍可通过代码控制
    isUserInputEnabled = userInputEnabled
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