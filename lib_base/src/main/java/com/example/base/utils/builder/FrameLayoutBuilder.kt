package com.example.base.utils.builder

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
 *  使用的页面需要重写：
 *  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
 *  }
 *
 *  @SuppressLint("MissingSuperCall")
 *  override fun onSaveInstanceState(outState: Bundle) {
 *  }
 *
 *  //记录下标
 *  override fun recreate() {
 *  intent = Intent().apply { putExtra("tab", tabBottom.selectedTabPosition) }
 *  super.recreate()
 *  }
 *
 *  //api是个key，没传或者获取失败都让页面切换到上次选中的地方
 *  override fun onNewIntent(intent: Intent?) {
 *  super.onNewIntent(intent)
 *  if (intent == null) return
 *  val api = intent.getStringExtra("api")
 *  if (api.isNullOrEmpty()) {
 *  //跳转页面
 *  intent.getIntExtra("tab", -1).also {
 *  selectTab(it)
 *  }
 *  }
 */
class FrameLayoutBuilder(private val manager: FragmentManager, private val containerViewId: Int, private val clazzPair: List<Pair<Class<*>, String>>) {
    private val list = ArrayList<Fragment>()
    var currentIndex = -1
    var onTabShow: ((tab: Int) -> Unit)? = null

    init {
        list.clear()
        selectTab(0)
    }

    fun selectTab(tab: Int) {
        if (currentIndex == tab) return
        currentIndex = tab
        val transaction = manager.beginTransaction()
        list.forEach { transaction.hide(it) }
        transaction.show(newInstance(clazzPair.safeGet(tab)))
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

}