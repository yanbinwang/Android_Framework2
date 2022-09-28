package com.example.base.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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
class FrameLayoutHelper(private val manager: FragmentManager, private val containerViewId: Int, private val list: ArrayList<Fragment>, tab: Int = 0) {
    var onTabShow: ((tabNum: Int) -> Unit)? = null
    var currentIndex = tab

    init {
        val transaction = manager.beginTransaction()
        list.forEach { transaction.add(containerViewId, it) }
        selectTab(tab)
    }

    fun selectTab(tab: Int) {
        currentIndex = tab
        manager.beginTransaction().apply {
            //全部隱藏后显示指定的页面
            list.forEach { hide(it) }
            show(list[tab])
            commitAllowingStateLoss()
            onTabShow?.invoke(tab)
        }
    }

}