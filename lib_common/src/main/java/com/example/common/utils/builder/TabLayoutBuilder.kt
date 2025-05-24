package com.example.common.utils.builder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.SparseArray
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.common.BaseApplication
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.builder.FragmentBuilder
import com.example.framework.utils.function.value.isMainThread
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.bind
import com.example.framework.utils.function.view.clearClick
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.size
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.concurrent.ConcurrentHashMap

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
 *             app:tabIndicatorColor="@color/bgOrange"
 *             app:tabIndicatorHeight="3pt"
 *             app:tabBackground="@android:color/transparent"
 *             app:tabRippleColor="@android:color/transparent"
 *             app:tabMinWidth="0dp"
 *             app:tabMode="fixed"
 *             app:tabPadding="0px"
 *             app:tabPaddingBottom="0dp"
 *             app:tabPaddingEnd="0dp"
 *             app:tabPaddingStart="0dp"
 *             app:tabPaddingTop="0dp" />
 *
 *             <com.google.android.material.tabs.TabLayout
 *                 android:id="@+id/tb_indicator"
 *                 android:layout_width="match_parent"
 *                 android:layout_height="50pt"
 *                 android:layout_gravity="bottom"
 *                 android:background="@color/bgDefault"
 *                 android:clipChildren="true"
 *                 android:clipToPadding="false"
 *                 android:theme="@style/TabLayoutStyle"
 *                 app:tabBackground="@android:color/transparent"
 *                 app:tabRippleColor="@android:color/transparent"
 *                 app:tabIndicator="@null"
 *                 app:tabMinWidth="0dp"
 *                 app:tabMode="fixed"
 *                 app:tabPadding="0px"
 *                 app:tabPaddingBottom="0dp"
 *                 app:tabPaddingEnd="0dp"
 *                 app:tabPaddingStart="0dp"
 *                 app:tabPaddingTop="0dp" />
 *
 *                 override fun initEvent() {
 *         super.initEvent()
 *         indicator.setOnTabChangeListener(object : TabLayoutBuilder.OnTabChangeListener {
 *             override fun onReselected(position: Int) {
 *                 onSelected(position, true)
 *             }
 *
 *             override fun onSelected(position: Int) {
 *                 onSelected(position, false)
 *             }
 *
 *             override fun onUnselected(position: Int) {
 *             }
 *         })
 *     }
 *
 *     private fun onSelected(index: Int, isReselected: Boolean) {
 *         //如果是重复点击的，或者与上一次相等的情况，不予以操作
 *         val unable = isReselected || index == currentItem
 *         if (!unable) {
 *             if (index == 2 && !isLogin()) {
 *                 navigation(ARouterPath.LoginActivity)
 *                 //秒切频率太快，commit还未来得及切换，倒计时1s切回上个选项卡
 *                 schedule({
 *                     indicator.setSelect(currentItem)
 *                 })
 *             } else {
 *                 currentItem = index
 *             }
 *         }
 *     }
 */
//abstract class TabLayoutBuilder<T, VDB : ViewDataBinding>(private val tab: TabLayout?, private var tabList: List<T>? = null) {
//    private var bindMode = -1//绑定模式 -> -1：正常 / 0：FragmentManager / 1：ViewPager2
//    private var builder: FragmentBuilder? = null
//    private var mediator: TabLayoutMediator? = null
//    private var listener: OnTabChangeListener? = null
//    private var clickActions = ConcurrentHashMap<Int, (() -> Unit)>()
//    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
//    private val tabViews by lazy { SparseArray<VDB>() }
//    private val mContext get() = tab?.context ?: BaseApplication.instance.applicationContext//整体上下文
//    private val mCurrentItem get() = tab?.selectedTabPosition.orZero//当前选中下标
//    private val mTabCount get() = tab?.tabCount.orZero//当前需要管理的总长度
//    private val tabListener = object : TabLayout.OnTabSelectedListener {
//        override fun onTabSelected(tab: TabLayout.Tab?) {
//            //处理选中事件
//            //可以在这里更新页面内容或者改变选中标签的样式
//            onTabBind(tab, true)
//            listener?.onSelected(tab?.position.orZero)
//        }
//
//        override fun onTabUnselected(tab: TabLayout.Tab?) {
//            //处理取消选中事件
//            onTabBind(tab, false)
//            listener?.onUnselected(tab?.position.orZero)
//        }
//
//        override fun onTabReselected(tab: TabLayout.Tab?) {
//            //处理再次选中同一个标签的事件
//            //可以在这里执行相应的操作
//            listener?.onReselected(tab?.position.orZero)
//        }
//
//        private fun onTabBind(tab: TabLayout.Tab?, selected: Boolean) {
//            tab?.customView ?: return
//            tab.position.orZero.apply {
//                //子tab状态回调
//                onBindView(tabViews[this], tabList.safeGet(this), selected, this)
//                //下标对应的fragment显示,只有manager需要手动切，viewpager2在绑定时就已经实现了切换
//                if (selected && 0 == bindMode) builder?.selectTab(this)
//            }
//        }
//    }
//
//    /**
//     * 无特殊绑定的自定义头
//     */
//    fun build(list: List<T>? = null, default: Int = 0) {
//        bindMode = -1
//        initView(list)
//        initEvent(default)
//    }
//
//    /**
//     * 注入管理器
//     */
//    fun bind(fragmentBuilder: FragmentBuilder, list: List<T>? = null, default: Int = 0) {
//        bindMode = 0
//        builder = fragmentBuilder
//        initView(list)
//        initEvent(default)
//    }
//
//    /**
//     * 注入viewpager2
//     * userInputEnabled:是否左右滑动
//     * pageLimit：是否预加载数据（懒加载为false）
//     */
//    fun bind(pager: ViewPager2?, adapter: RecyclerView.Adapter<*>, list: List<T>? = null, orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL, userInputEnabled: Boolean = true, pageLimit: Boolean = true, default: Int = 0) {
//        bindMode = 1
//        pager?.adapter = null
//        mediator?.detach()
//        initView(list)
//        pager.adapter(adapter, orientation, userInputEnabled, pageLimit)
//        mediator = pager.bind(tab)
//        initEvent(default)
//    }
//
//    private fun initView(list: List<T>? = null) {
//        tab?.removeAllTabs()
//        tabViews.clear()
//        if (null != list) tabList = list
//        tabList?.forEach { _ -> tab?.addTab(tab.newTab()) }
//    }
//
//    /**
//     * 这个方法需要放在setupWithViewPager()后面
//     */
//    private fun initEvent(default: Int = 0) {
//        for (i in 0 until mTabCount) {
//            tab?.getTabAt(i)?.apply {
//                val bindView = getBindView()
//                if (tabViews[i] == null) tabViews.put(i, bindView)
//                customView = bindView.root
//                customView.size(WRAP_CONTENT, MATCH_PARENT)
//                view.isLongClickable = false
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
//                onBindView(bindView, tabList.safeGet(i), i == 0, i)
//            }
//        }
//        tab?.addOnTabSelectedListener(tabListener)
//        //强制设置tab宽度
//        val tabParent = tab?.getChildAt(0) as? ViewGroup
//        for (i in 0 until tabParent?.childCount.orZero) {
//            tabParent?.getChildAt(i)?.setPadding(0, 0, 0, 0)
//            tabParent?.getChildAt(i).size(WRAP_CONTENT, MATCH_PARENT)
//        }
//        //如果设置了默认选择下标则做一个指定
//        setSelect(default)
//    }
//
//    /**
//     * 回调方法，返回对应控件
//     */
//    protected abstract fun getBindView(): VDB
//
//    /**
//     * 设置数据
//     */
//    protected abstract fun onBindView(mBinding: VDB?, item: T?, selected: Boolean, index: Int)
//
//    /**
//     * 获取上下文
//     */
//    fun getContext(): Context {
//        return mContext
//    }
//
//    /**
//     * 获取总长度
//     */
//    fun getTabCount(): Int {
//        return mTabCount
//    }
//
//    /**
//     * 获取当前选中的下标
//     */
//    fun getCurrentIndex(): Int {
//        return mCurrentItem
//    }
//
//    /**
//     * 设置选中下标
//     * 当调用select()方法选中一个不同的tab时，会触发addOnTabSelectedListener的回调；如果选中的是当前已经选中的tab，则不会触发
//     */
//    fun setSelect(index: Int, recreate: Boolean = false) {
//        if (recreate) {
//            builder?.selectTab(index, true)
//            selectTabNow(index)
//        } else {
//            if (mCurrentItem == index || index > mTabCount - 1 || index < 0) return
//            selectTabNow(index)
//        }
//    }
//
//    private fun selectTabNow(index: Int) {
//        tab?.postDelayed({
//            tab.getTabAt(index)?.select()
//        }, 500)
//    }
//
//    /**
//     * 对应下标需求对应不同的点击，改为自定义
//     */
//    fun addClickAllowed(vararg params: Pair<Int, (() -> Unit)>) {
//        clickActions = ConcurrentHashMap(params.toMap())
//        for (i in 0 until mTabCount) {
//            val mTab = tab?.getTabAt(i)
//            mTab?.customView.let {
//                it?.isClickable = true
//                it?.click {
//                    weakHandler.post {
//                        val data = clickActions[i]
//                        if (data != null) {
//                            data.invoke()
//                        } else {
//                            mTab?.select()
//                            for (j in 0 until mTabCount) {
//                                onBindView(tabViews[j], tabList.safeGet(j), j == i, j)
//                            }
//                            if (0 == bindMode) builder?.selectTab(i)
//                        }
//                    }
//                }
//            }
//        }
//        tab?.removeOnTabSelectedListener(tabListener)
//        // 移除之前的拦截器
//        tab?.setOnTouchListener(null)
//    }
//
//    /**
//     * 还原对应下标的点击
//     * addClickAllowed后可还原
//     */
//    fun allowedReset(index: Int) {
//        val clickAction = clickActions[index]
//        if (clickAction != null) {
//            val mTab = tab?.getTabAt(index)
//            mTab?.customView.click {
//                weakHandler.post {
//                    mTab?.select()
//                    for (j in 0 until mTabCount) {
//                        onBindView(tabViews[j], tabList.safeGet(j), j == index, j)
//                    }
//                    if (0 == bindMode) builder?.selectTab(index)
//                }
//            }
//        }
//    }
//
//    /**
//     * 整个TabLayout的操作，拦截所有，改为自己的点击
//     * true拦截 false不拦截
//     */
//    fun setClickable(isClickable: Boolean, listener: (() -> Unit)? = {}) {
//        for (i in 0 until mTabCount) {
//            tab?.getTabAt(i)?.customView.let {
//                it?.isClickable = isClickable
//                if (isClickable) {
//                    it?.click {
//                        listener?.invoke()
//                    }
//                } else {
//                    it?.clearClick()
//                }
//            }
//        }
//        if (isClickable) {
//            tab?.removeOnTabSelectedListener(tabListener)
//            // 移除之前的拦截器
//            tab?.setOnTouchListener(null)
//        } else {
//            tab?.addOnTabSelectedListener(tabListener)
//            // 设置触摸拦截器
//            tab?.setOnTouchListener { _, _ -> true }
//        }
//    }
//
//    /**
//     * 设置监听
//     */
//    fun setOnTabChangeListener(listener: OnTabChangeListener) {
//        this.listener = listener
//    }
//
//    /**
//     * 监听
//     */
//    interface OnTabChangeListener {
//        /**
//         * tab被点2次（再次被选中时调用）
//         * 列表加载完成，此时默认选中的是索引为0，回调会执行（onSelected不会执行）
//         * 列表加载完成后，滑动到其他item，再次点击索引为0的Tab时，回调会执行
//         * 之后索引为0的tab再次被选中，会回调onTabSelected
//         */
//        fun onReselected(position: Int)
//
//        /**
//         * tab进入选择状态
//         * 列表加载完成后滑动到后面的 item，再次点击第一个 tab,此时onTabSelected不回调
//         */
//        fun onSelected(position: Int)
//
//        /**
//         * tab退出选择状态
//         * 如当前选中索引为3的tab,你切换了索引为4的tab,此时 onTabUnselected回调索引为3
//         */
//        fun onUnselected(position: Int)
//
//    }
//
//}
@SuppressLint("ClickableViewAccessibility")
abstract class TabLayoutBuilder<T, VDB : ViewDataBinding>(private val tab: TabLayout?, private var tabList: List<T>? = null) {
    private var bindMode = -1//绑定模式 -> -1：正常 / 0：FragmentManager / 1：ViewPager2
    private var builder: FragmentBuilder? = null
    private var mediator: TabLayoutMediator? = null
    private var listener: OnTabChangeListener? = null
    private var clickActions = ConcurrentHashMap<Int, (() -> Unit)>()
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val tabViews by lazy { SparseArray<VDB>() }
    private val mContext get() = tab?.context ?: BaseApplication.instance.applicationContext//整体上下文
    private val mCurrentItem get() = tab?.selectedTabPosition.orZero//当前选中下标
    private val mTabCount get() = tab?.tabCount.orZero//当前需要管理的总长度
    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            //处理选中事件
            //可以在这里更新页面内容或者改变选中标签的样式
            onTabBind(tab, true)
            listener?.onSelected(tab?.position.orZero)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            //处理取消选中事件
            onTabBind(tab, false)
            listener?.onUnselected(tab?.position.orZero)
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            //处理再次选中同一个标签的事件
            //可以在这里执行相应的操作
            listener?.onReselected(tab?.position.orZero)
        }

        private fun onTabBind(tab: TabLayout.Tab?, selected: Boolean) {
            tab?.customView ?: return
            tab.position.orZero.apply {
                //子tab状态回调
                onBindView(tabViews[this], tabList.safeGet(this), selected, this)
                //下标对应的fragment显示,只有manager需要手动切，viewpager2在绑定时就已经实现了切换
                if (selected && 0 == bindMode) builder?.selectTab(this)
            }
        }
    }

    /**
     * 无特殊绑定的自定义头
     */
    fun build(list: List<T>? = null, default: Int = 0) {
        bindMode = -1
        initView(list)
        initEvent(default)
    }

    /**
     * 注入管理器
     */
    fun bind(fragmentBuilder: FragmentBuilder, list: List<T>? = null, default: Int = 0) {
        bindMode = 0
        builder = fragmentBuilder
        initView(list)
        initEvent(default)
    }

    /**
     * 注入viewpager2
     * userInputEnabled:是否左右滑动
     * pageLimit：是否预加载数据（懒加载为false）
     */
    fun bind(pager: ViewPager2?, adapter: RecyclerView.Adapter<*>, list: List<T>? = null, orientation: Int = ViewPager2.ORIENTATION_HORIZONTAL, userInputEnabled: Boolean = true, pageLimit: Boolean = true, default: Int = 0) {
        bindMode = 1
        pager?.adapter = null
        mediator?.detach()
        initView(list)
        pager.adapter(adapter, orientation, userInputEnabled, pageLimit)
        mediator = pager.bind(tab)
        initEvent(default)
    }

    private fun initView(list: List<T>? = null) {
        tab?.removeAllTabs()
        tabViews.clear()
        if (null != list) tabList = list
        tabList?.forEach { _ -> tab?.addTab(tab.newTab()) }
    }

    /**
     * 这个方法需要放在setupWithViewPager()后面
     */
    private fun initEvent(default: Int = 0) {
        for (i in 0 until mTabCount) {
            tab?.getTabAt(i)?.apply {
                val bindView = getBindView()
                if (tabViews[i] == null) tabViews.put(i, bindView)
                customView = bindView.root
                customView.size(WRAP_CONTENT, MATCH_PARENT)
                view.isLongClickable = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
                onBindView(bindView, tabList.safeGet(i), i == 0, i)
            }
        }
        tab?.addOnTabSelectedListener(tabListener)
        //强制设置tab宽度
        val tabParent = tab?.getChildAt(0) as? ViewGroup
        for (i in 0 until tabParent?.childCount.orZero) {
            tabParent?.getChildAt(i)?.setPadding(0, 0, 0, 0)
            tabParent?.getChildAt(i).size(WRAP_CONTENT, MATCH_PARENT)
        }
        //如果设置了默认选择下标则做一个指定
        setSelect(default)
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
     * 获取上下文
     */
    fun getContext(): Context {
        return mContext
    }

    /**
     * 获取总长度
     */
    fun getTabCount(): Int {
        return mTabCount
    }

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        return mCurrentItem
    }

    /**
     * 设置选中下标
     * 当调用select()方法选中一个不同的tab时，会触发addOnTabSelectedListener的回调；如果选中的是当前已经选中的tab，则不会触发
     */
    fun setSelect(index: Int, recreate: Boolean = false) {
        if (recreate) {
            builder?.selectTab(index, true)
            selectTabNow(index)
        } else {
            if (mCurrentItem == index || index > mTabCount - 1 || index < 0) return
            selectTabNow(index)
        }
    }

    /**
     * view.post {} 的工作原理
     * 当你调用 view.post(Runnable) 时：
     * 如果当前线程是 UI 线程，Runnable 会立即执行
     * 如果当前线程不是 UI 线程，Runnable 会被发送到 UI 线程的消息队列中执行
     */
    private fun selectTabNow(index: Int) {
        tab?.post {
            // 当代码执行到这里时，TabLayout 已经完成初始化
            tab.getTabAt(index)?.select()
        }
    }

    /**
     * 对应下标需求对应不同的点击，改为自定义
     */
    fun addClickAllowed(vararg params: Pair<Int, (() -> Unit)>) {
        clickActions = ConcurrentHashMap(params.toMap())
        for (i in 0 until mTabCount) {
            val mTab = tab?.getTabAt(i)
            mTab?.customView.let {
                it?.isClickable = true
                it?.click {
                    if (isMainThread) {
                        clickAllowedAction(mTab, i)
                    } else {
                        weakHandler.post {
                            clickAllowedAction(mTab, i)
                        }
                    }
                }
            }
        }
        tab?.removeOnTabSelectedListener(tabListener)
        // 移除之前的拦截器
        tab?.setOnTouchListener(null)
    }

    private val clickAllowedAction = { mTab: TabLayout.Tab?, i: Int ->
        val action = clickActions[i]
        if (action != null) {
            action.invoke()
        } else {
            allowedResetAction(mTab, i)
        }
    }

    /**
     * 还原对应下标的点击
     * addClickAllowed后可还原
     */
    fun allowedReset(index: Int) {
        val action = clickActions[index]
        if (action != null) {
            val mTab = tab?.getTabAt(index)
            mTab?.customView.click {
                if (isMainThread) {
                    allowedResetAction(mTab, index)
                } else {
                    weakHandler.post {
                        allowedResetAction(mTab, index)
                    }
                }
            }
        }
    }

    private val allowedResetAction = { mTab: TabLayout.Tab?, i: Int ->
        mTab?.select()
        for (j in 0 until mTabCount) {
            onBindView(tabViews[j], tabList.safeGet(j), j == i, j)
        }
        if (0 == bindMode) builder?.selectTab(i)
    }

    /**
     * 整个TabLayout的操作，拦截所有，改为自己的点击
     * true拦截 false不拦截
     */
    fun setClickable(isClickable: Boolean, listener: (() -> Unit)? = {}) {
        for (i in 0 until mTabCount) {
            tab?.getTabAt(i)?.customView.let {
                it?.isClickable = isClickable
                if (isClickable) {
                    it?.click {
                        listener?.invoke()
                    }
                } else {
                    it?.clearClick()
                }
            }
        }
        if (isClickable) {
            tab?.removeOnTabSelectedListener(tabListener)
            // 移除之前的拦截器
            tab?.setOnTouchListener(null)
        } else {
            tab?.addOnTabSelectedListener(tabListener)
            // 设置触摸拦截器
            tab?.setOnTouchListener { _, _ -> true }
        }
    }

    /**
     * 设置监听
     */
    fun setOnTabChangeListener(listener: OnTabChangeListener) {
        this.listener = listener
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