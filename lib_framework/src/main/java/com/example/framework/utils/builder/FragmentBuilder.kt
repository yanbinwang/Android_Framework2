package com.example.framework.utils.builder

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.framework.utils.function.value.getSimpleName
import com.example.framework.utils.function.value.safeGet

/**
 *  Created by wangyanbin
 *  页面切换管理工具类
 *  manager->當前頁面的FragmentManager
 *  containerViewId->當前頁面FrameLayout佈局的id
 *  fragmentList->fragment的集合
 *  tab->没人选中的下标
 *
 *  //记录下标
 *  override fun recreate() {
 *  intent = Intent().apply { putExtra(Extras.TAB_INDEX, tabBottom.selectedTabPosition) }
 *  super.recreate()
 *  }
 *
 *  使用的页面需要重写：
 *  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
 *  }
 *
 *  @SuppressLint("MissingSuperCall")
 *  override fun onSaveInstanceState(outState: Bundle) {
 *  }
 *
 *  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
 *  super.onRestoreInstanceState(savedInstanceState)
 *  intent.getIntExtra(Extras.TAB_INDEX, 0).also { navigationBuilder.selectedItem(it) }
 *  }
 *
 */
class FragmentBuilder(private val manager: FragmentManager, private val containerViewId: Int) {
    private var arguments = false
    private var currentItem = 0
    private var clazzPair: List<Pair<Class<*>, String>>? = null
    private var clazzTriple: List<Triple<Class<*>, Pair<String, String>, String>>? = null
    private val list by lazy { ArrayList<Fragment>() }
    var onTabShow: ((tab: Int) -> Unit)? = null

    /**
     *  HomeFragment::class.java.getPair()
     *  first：class名
     *  second：tag值，不传默认为class名
     */
    fun bind(clazzPair: List<Pair<Class<*>, String>>) {
        this.list.clear()
        this.arguments = false
        this.clazzPair = clazzPair
        selectTab(0)
    }

    /**
     * EvidencePageFragment::class.java.getTriple(Extras.REQUEST_ID to id, "EviPager${id}")
     * first：class名
     * second：pair对象 （first，fragment透传的key second，透传的值）
     * third：内存中存储的tag
     */
    fun bindArguments(clazzTriple: List<Triple<Class<*>, Pair<String, String>, String>>) {
        this.list.clear()
        this.arguments = true
        this.clazzTriple = clazzTriple
        selectTab(0)
    }

    fun selectTab(tab: Int) {
        currentItem = tab
        val transaction = manager.beginTransaction()
        list.forEach { transaction.hide(it) }
        transaction.show(if (arguments) newInstanceArguments() else newInstance())
        transaction.commitAllowingStateLoss()
        onTabShow?.invoke(tab)
    }

    /**
     * 获取对应的fragment
     */
    fun <T : Fragment> getFragment(index: Int): T? {
        return list.safeGet(index) as? T
    }

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        return currentItem
    }

    private fun newInstance(): Fragment {
        clazzPair.safeGet(currentItem).let {
            val transaction = manager.beginTransaction()
            var fragment = manager.findFragmentByTag(it?.second)
            if (null == fragment) {
                fragment = it?.first?.newInstance() as Fragment
                transaction.add(containerViewId, fragment, it.second)
                transaction.commitAllowingStateLoss()
                list.add(fragment)
            }
            return fragment
        }
    }

    private fun newInstanceArguments(): Fragment {
        clazzTriple.safeGet(currentItem).let {
            val transaction = manager.beginTransaction()
            var fragment = manager.findFragmentByTag(it?.third)
            if (null == fragment) {
                fragment = it?.first?.newInstance() as Fragment
                val bundle = Bundle()
                bundle.putString(it.second.first, it.second.second)
                fragment.arguments = bundle
                transaction.add(containerViewId, fragment, it.third)
                transaction.commitAllowingStateLoss()
                list.add(fragment)
            }
            return fragment
        }
    }

}

/**
 * 默认返回自身和自身class名小写，也可指定
 */
fun Class<*>.getPair(name: String? = null): Pair<Class<*>, String> {
    return this to getSimpleName(name)
}

/**
 * 默认返回自身和自身class名小写以及请求的id
 */
fun Class<*>.getTriple(pair: Pair<String, String>, name: String? = null): Triple<Class<*>, Pair<String, String>, String> {
    return Triple(this, pair, getSimpleName(name))
}