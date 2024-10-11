package com.example.common.utils.builder

import android.os.Build
import android.util.SparseArray
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.common.BaseApplication
import com.example.framework.utils.builder.FragmentBuilder
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.bind
import com.example.framework.utils.function.view.size
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author yan
 * 项目实际使用中，ui是肯定不会按照安卓原生的导航栏来实现对应的效果的
 * 故而提出一个接口类，需要实现对应效果的地方去实现
 * 对应的样式属性，提出一个style（TabLayoutStyle）系统会有部分属性不响应，故而每次用到xml中都要配置部分属性
 * // app:tabPaddingStart="0dp"
 * // app:tabPaddingTop="0dp"
 * // app:tabPaddingEnd="0dp"
 * // app:tabPaddingBottom="0dp"
 * // app:tabMinWidth="0dp"
 * --------------------
 * app:tabPadding="0px"
 * app:tabMinWidth="0px"
 * --------------------
 *  <com.google.android.material.tabs.TabLayout
 *             android:id="@+id/tb_menu"
 *             statusBar_margin="@{true}"
 *             android:layout_width="match_parent"
 *             android:layout_height="44pt"
 *             android:background="@color/bgWhite"
 *             android:clipChildren="true"
 *             android:clipToPadding="false"
 *             android:theme="@style/TabLayoutStyle"
 *             app:tabIndicator="@drawable/layer_list_tab_line"
 *             app:tabMinWidth="0dp"
 *             app:tabMode="fixed"
 *             app:tabPaddingBottom="0dp"
 *             app:tabPaddingEnd="0dp"
 *             app:tabPaddingStart="0dp"
 *             app:tabPaddingTop="0dp" />
 */
abstract class TabLayoutBuilder<T, VDB : ViewDataBinding>(private val tab: TabLayout?, private var tabList: List<T>? = null) {
    private var builder: FragmentBuilder? = null
    private var mediator: TabLayoutMediator? = null
    private var listener: OnTabChangeListener? = null
    private val tabViews by lazy { SparseArray<VDB>() }
    protected val mContext get() = tab?.context ?: BaseApplication.instance.applicationContext
    protected val mCurrentIndex get() = tab?.selectedTabPosition.orZero

    /**
     * 无特殊绑定的自定义头
     */
    fun bind(list: List<T>? = null) {
        init(list)
        addOnTabSelectedListener()
    }

    /**
     * 注入管理器
     */
    fun bind(builder: FragmentBuilder, list: List<T>? = null) {
        this.builder = builder
        init(list)
        addOnTabSelectedListener()
    }

    /**
     * 注入viewpager2
     * userInputEnabled:是否左右滑动
     * pageLimit：是否预加载数据（懒加载为false）
     */
    fun bind(pager: ViewPager2?, adapter: RecyclerView.Adapter<*>, list: List<T>? = null, orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL, userInputEnabled: Boolean = true, pageLimit: Boolean = false) {
        pager?.adapter = null
        mediator?.detach()
        init(list)
        pager.adapter(adapter, orientation, userInputEnabled, pageLimit)
        mediator = pager.bind(tab)
        addOnTabSelectedListener()
    }

    private fun init(list: List<T>? = null) {
        tab?.removeAllTabs()
        tabViews.clear()
        if (null != list) tabList = list
        tabList?.forEach { _ -> tab?.addTab(tab.newTab()) }
    }

    /**
     * 这个方法需要放在setupWithViewPager()后面
     */
    private fun addOnTabSelectedListener() {
        for (i in 0 until tab?.tabCount.orZero) {
            tab?.getTabAt(i)?.apply {
                val mBinding = getBindView()
                if (tabViews[i] == null) tabViews.put(i, mBinding)
                customView = mBinding.root
                customView.size(WRAP_CONTENT, MATCH_PARENT)
                view.isLongClickable = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
                onBindView(mBinding, tabList.safeGet(i), i == 0, i)
            }
        }
        tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabBind(tab, true)
                listener?.onSelected(tab?.position.orZero)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                onTabBind(tab, false)
                listener?.onUnselected(tab?.position.orZero)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                listener?.onReselected(tab?.position.orZero)
            }

            private fun onTabBind(tab: TabLayout.Tab?, selected: Boolean) {
                tab?.customView ?: return
                tab.position.orZero.apply {
                    onBindView(tabViews[this], tabList.safeGet(this), selected, this)
                    builder?.selectTab(this)
                }
            }
        })
        //强制设置tab宽度
        val tabParent = tab?.getChildAt(0) as? ViewGroup
        for (i in 0 until tabParent?.childCount.orZero) {
            tabParent?.getChildAt(i)?.setPadding(0, 0, 0, 0)
            tabParent?.getChildAt(i).size(WRAP_CONTENT, MATCH_PARENT)
        }
    }

    /**
     * 回调方法，返回对应控件
     */
    protected abstract fun getBindView(): VDB

    /**
     * 设置数据
     */
    protected abstract fun onBindView(mBinding: VDB?, item: T?, selected: Boolean, index: Int)

    /**
     * 设置监听
     */
    fun setOnTabChangeListener(listener: OnTabChangeListener) {
        this.listener = listener
    }

    /**
     * 设置选中下标
     */
    fun setSelect(index: Int) {
        if (mCurrentIndex == index) return
        tab?.getTabAt(index)?.select()
    }

    /**
     * 监听
     */
    interface OnTabChangeListener {
        /**
         * tab被点2次（再次被选中时调用）
         * 列表加载完成，此时默认选中的是索引为0，回调会执行（onSelected不会执行）
         * 列表加载完成后，滑动到其他item，再次点击索引为0的Tab时，回调会执行
         * 之后索引为0的tab再次被选中，会回调onTabSelected
         */
        fun onReselected(position: Int)

        /**
         * tab进入选择状态
         * 列表加载完成后滑动到后面的 item，再次点击第一个 tab,此时onTabSelected不回调
         */
        fun onSelected(position: Int)

        /**
         * tab退出选择状态
         * 如当前选中索引为3的tab,你切换了索引为4的tab,此时 onTabUnselected回调索引为3
         */
        fun onUnselected(position: Int)

    }

}