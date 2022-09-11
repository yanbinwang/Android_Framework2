package com.example.common.utils.helper

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.view.bind
import com.example.common.R
import com.example.common.utils.setData
import com.google.android.material.tabs.TabLayout
import com.example.base.utils.function.view.adapter

/**
 * @description 菜单头工具类
 * tabGravity = GRAVITY_START
 * tabIndicator 不设置
 * @author yan
 */
object TabLayoutHelper {
    private var tabLayout: TabLayout? = null

    /**
     * 初始化->定制样式的导航栏
     */
    @JvmStatic
    fun <T : RecyclerView.Adapter<*>> initialize(pager: ViewPager2, adapter: T, tabLayout: TabLayout, tabTitle: MutableList<String>, isUserInput: Boolean = false) {
        pager.adapter(adapter, ViewPager2.ORIENTATION_HORIZONTAL, isUserInput)
        pager.bind(tabLayout) { tab, position -> tab.text = tabTitle[position] }
        //这个方法需要放在setupWithViewPager()后面
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.customView = getTabView(i, tabTitle)
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //设置选中图标样式
                val tabView = tab?.customView ?: return
                setData(true, tabView.findViewById(R.id.tv_title))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //设置未选中图标样式
                val tabView = tab?.customView ?: return
                setData(false, tabView.findViewById(R.id.tv_title))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    /**
     * 获取所有子view，替换为自己的布局
     */
    private fun getTabView(position: Int, tabTitle: MutableList<String>): View {
        val context = tabLayout?.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_page, null)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = tabTitle[position]
        //设置默认页面
        setData(position == 0, tvTitle)
        return view
    }

    /**
     * 设置数据
     */
    private fun setData(select: Boolean, tvTitle: TextView) {
        tvTitle.setData(
            colorRes = if (select) R.color.white else R.color.black,
            resid = if (select) R.drawable.shape_tab_selected_line else R.drawable.shape_tab_line
        )
    }

}