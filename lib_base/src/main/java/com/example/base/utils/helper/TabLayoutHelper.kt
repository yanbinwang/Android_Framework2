package com.example.base.utils.helper

import android.content.Context
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.safeGet
import com.example.base.utils.function.view.adapter
import com.example.base.utils.function.view.bind
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author yan
 * 项目实际使用中，ui是肯定不会按照安卓原生的导航栏来实现对应的效果的
 * 故而提出一个接口类，需要实现对应效果的地方去实现
 */
abstract class TabLayoutHelper<T>(private val tab: TabLayout, private var tabList: List<T>? = null) {
    protected var context: Context = tab.context
    private var helper: FrameLayoutHelper? = null
    private var mediator: TabLayoutMediator? = null

    /**
     * 注入管理器
     */
    fun bind(helper: FrameLayoutHelper, tabList: MutableList<T>? = null) {
        this.helper = helper
        initialize(tabList)
        addOnTabSelectedListener()
    }

    /**
     * 注入viewpager2
     */
    fun bind(pager: ViewPager2, adapter: RecyclerView.Adapter<*>, isUserInput: Boolean = false, tabList: MutableList<T>? = null) {
        mediator?.detach()
        initialize(tabList)
        pager.adapter = null
        pager.adapter(adapter, ViewPager2.ORIENTATION_HORIZONTAL, isUserInput)
        mediator = pager.bind(tab)
        addOnTabSelectedListener()
    }

    private fun initialize(list: MutableList<T>? = null) {
        if (null != list) tabList = list
        tab.removeAllTabs()
        tabList?.forEach { _ -> tab.addTab(TabLayout.Tab()) }
    }

    /**
     * 这个方法需要放在setupWithViewPager()后面
     */
    private fun addOnTabSelectedListener() {
        for (i in 0 until tab.tabCount) {
            tab.getTabAt(i)?.apply {
                customView = onCreateCustomView(tabList.safeGet(i), i == 0)
                view.isLongClickable = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
            }
        }
        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //设置选中图标样式
                val tabView = tab?.customView ?: return
                onBindCustomView(tabView, tabList.safeGet(tab.position), true)
                helper?.selectTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //设置未选中图标样式
                val tabView = tab?.customView ?: return
                onBindCustomView(tabView, tabList.safeGet(tab.position), false)
                helper?.selectTab(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    /**
     * 回调方法，返回对应控件
     */
    protected abstract fun onCreateCustomView(item: T?, current: Boolean): View

    /**
     * 设置数据
     */
    protected abstract fun onBindCustomView(view: View, item: T?, current: Boolean)

}