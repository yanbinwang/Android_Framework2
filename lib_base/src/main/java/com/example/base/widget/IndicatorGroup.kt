package com.example.base.widget

import android.content.Context
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.toNewList
import com.example.base.utils.function.view.adapter
import com.example.base.utils.function.view.bind
import com.google.android.material.tabs.TabLayout

/**
 * @author yan
 * 项目实际使用中，ui是肯定不会按照安卓原生的导航栏来实现对应的效果的
 * 故而提出一个接口类，需要实现对应效果的地方去实现
 */
abstract class IndicatorGroup<V> {
    protected var context: Context? = null

    /**
     * tab->导航栏
     * pager->滑动布局
     * adapter->适配器
     * tabTitle->标题数据
     * isUserInput->是否允许滑动
     * onSelected->回调方法
     */
    fun <T : RecyclerView.Adapter<*>> initialize(tab: TabLayout, pager: ViewPager2, adapter: T, tabTitle: MutableList<V>, isUserInput: Boolean = false) {
        context = tab.context
        val tabList = tabTitle.toNewList { "" }
        pager.adapter(adapter, ViewPager2.ORIENTATION_HORIZONTAL, isUserInput)
        pager.bind(tab) { item, position -> item.text = tabList[position] }
        //这个方法需要放在setupWithViewPager()后面
        for (i in 0 until tab.tabCount) {
            tab.getTabAt(i)?.apply {
                customView = onCreateCustomView(tabTitle[i], i == 0)
                view.isLongClickable = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
            }
        }
        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //设置选中图标样式
                val tabView = tab?.customView ?: return
                onBindCustomView(tabView, tabTitle[tab.position], true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //设置未选中图标样式
                val tabView = tab?.customView ?: return
                onBindCustomView(tabView, tabTitle[tab.position], false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    /**
     * 回调方法，返回对应控件
     */
    protected abstract fun onCreateCustomView(item: V?, current: Boolean): View

    /**
     * 设置数据
     */
    protected abstract fun onBindCustomView(view: View, item: V?, current: Boolean)

}