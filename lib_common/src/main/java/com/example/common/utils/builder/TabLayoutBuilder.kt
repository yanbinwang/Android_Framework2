package com.example.common.utils.builder

import android.content.Context
import android.os.Build
import android.util.SparseArray
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.builder.FrameLayoutBuilder
import com.example.base.utils.function.value.orZero
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.function.view.adapter
import com.example.base.utils.function.view.bind
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author yan
 * 项目实际使用中，ui是肯定不会按照安卓原生的导航栏来实现对应的效果的
 * 故而提出一个接口类，需要实现对应效果的地方去实现
 */
abstract class TabLayoutBuilder<T, VDB : ViewDataBinding>(private val tab: TabLayout, private var tabList: List<T>? = null) {
    private var builder: FrameLayoutBuilder? = null
    private var mediator: TabLayoutMediator? = null
    private val tabViews by lazy { SparseArray<VDB>() }
    protected val context: Context get() = tab.context
    val currentIndex get() = tab.selectedTabPosition

    /**
     * 注入管理器
     */
    fun bind(builder: FrameLayoutBuilder, list: List<T>? = null) {
        this.builder = builder
        init(list)
        addOnTabSelectedListener()
    }

    /**
     * 注入viewpager2
     */
    fun bind(pager: ViewPager2, adapter: RecyclerView.Adapter<*>, isUserInput: Boolean = false, list: List<T>? = null) {
        pager.adapter = null
        mediator?.detach()
        init(list)
        pager.adapter(adapter, ViewPager2.ORIENTATION_HORIZONTAL, isUserInput)
        mediator = pager.bind(tab)
        addOnTabSelectedListener()
    }

    private fun init(list: List<T>? = null) {
        tab.removeAllTabs()
        tabViews.clear()
        if (null != list) tabList = list
        tabList?.forEach { _ -> tab.addTab(tab.newTab()) }
    }

    /**
     * 这个方法需要放在setupWithViewPager()后面
     */
    private fun addOnTabSelectedListener() {
        for (i in 0 until tab.tabCount) {
            tab.getTabAt(i)?.apply {
                val binding = getBindView()
                if (tabViews[i] == null) tabViews.put(i, binding)
                customView = binding.root
                view.isLongClickable = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
                onBindView(binding, tabList.safeGet(i), i == 0)
            }
        }
        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabBind(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                onTabBind(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            private fun onTabBind(tab: TabLayout.Tab?, selected: Boolean) {
                tab?.customView ?: return
                tab.position.orZero.apply {
                    onBindView(tabViews[this], tabList.safeGet(this), selected)
                    builder?.selectTab(this)
                }
            }
        })
    }

    /**
     * 回调方法，返回对应控件
     */
    protected abstract fun getBindView(): VDB

    /**
     * 设置数据
     */
    protected abstract fun onBindView(binding: VDB?, item: T?, selected: Boolean)

}