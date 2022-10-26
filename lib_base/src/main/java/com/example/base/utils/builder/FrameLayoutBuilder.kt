package com.example.base.utils.builder

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.base.utils.function.value.safeGet

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
 *  intent = Intent().apply { putExtra("tab", tabBottom.selectedTabPosition) }
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
 *  intent.getIntExtra(Extras.TAB_INDEX, -1).also { navigationBuilder.selectedItem(it) }
 *  }
 *
 */
class FrameLayoutBuilder(private val manager: FragmentManager, private val containerViewId: Int) {
    private var arguments = false
    private var clazzPair: List<Pair<Class<*>, String>>? = null
    private var clazzTriple: List<Triple<Class<*>, Pair<String, String>, String>>? = null
    private val list = ArrayList<Fragment>()
    var currentItem = 0
    var onTabShow: ((tab: Int) -> Unit)? = null

    init {
        list.clear()
    }

    fun bind(clazzPair: List<Pair<Class<*>, String>>) {
        this.arguments = false
        this.clazzPair = clazzPair
        selectTab(0)
    }

    fun bindArguments(clazzTriple: List<Triple<Class<*>, Pair<String, String>, String>>) {
        this.arguments = true
        this.clazzTriple = clazzTriple
        selectTab(0)
    }

    fun selectTab(tab: Int) {
        currentItem = tab
        val transaction = manager.beginTransaction()
        list.forEach { transaction.hide(it) }
        transaction.show(
            if (arguments) newInstanceArguments(clazzTriple.safeGet(tab)) else newInstance(
                clazzPair.safeGet(tab)
            )
        )
        transaction.commitAllowingStateLoss()
        onTabShow?.invoke(tab)
    }

    private fun newInstance(pair: Pair<Class<*>, String>?): Fragment {
        val transaction = manager.beginTransaction()
        var fragment = manager.findFragmentByTag(pair?.second)
        if (null == fragment) {
            fragment = pair?.first?.newInstance() as Fragment
            transaction.add(containerViewId, fragment, pair.second)
            transaction.commitAllowingStateLoss()
            list.add(fragment)
        }
        return fragment
    }

    private fun newInstanceArguments(triple: Triple<Class<*>, Pair<String, String>, String>?): Fragment {
        val transaction = manager.beginTransaction()
        var fragment = manager.findFragmentByTag(triple?.third)
        if (null == fragment) {
            fragment = triple?.first?.newInstance() as Fragment
            val bundle = Bundle()
            bundle.putString(triple.second.first, triple.second.second)
            fragment.arguments = bundle
            transaction.add(containerViewId, fragment, triple.third)
            transaction.commitAllowingStateLoss()
            list.add(fragment)
        }
        return fragment
    }

}