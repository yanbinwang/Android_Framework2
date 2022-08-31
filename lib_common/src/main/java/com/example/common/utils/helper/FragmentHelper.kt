package com.example.common.utils.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.common.utils.builder.StatusBarBuilder
import java.lang.ref.WeakReference

/**
 *  Created by wangyanbin
 *  页面切换管理工具类
 *  每个页面传入activity，切换的根视图id，内存区分的标题tag，对应的fragment
 */
object FragmentHelper {
    private var containerViewId = 0
    private var fragmentList = ArrayList<Fragment>()
    private var fragmentManager: FragmentManager? = null
    var onTabShow: ((tabNum: Int) -> Unit)? = null

    @JvmOverloads
    @JvmStatic
    fun initialize(activity: AppCompatActivity, containerViewId: Int, fragmentList: ArrayList<Fragment>, tabNum: Int = 0, dark: Boolean = true) {
        StatusBarBuilder(activity.window).setTransparent(dark)
        val weakActivity = WeakReference(activity)
        this.fragmentManager = weakActivity.get()?.supportFragmentManager
        this.containerViewId = containerViewId
        this.fragmentList = fragmentList
        showFragment(tabNum, true)
    }

    @JvmOverloads
    @JvmStatic
    fun showFragment(tabNum: Int, load: Boolean = false) {
        if (fragmentList.size > tabNum) {
            //commit只能提交一次，所以每次都需要重新实例化
            val fragmentTransaction = fragmentManager?.beginTransaction()
            if (load) {
                for (i in fragmentList.indices) {
                    fragmentTransaction?.add(containerViewId, fragmentList[i])
                }
            }
            //全部隱藏后显示指定的页面
            for (fragment in fragmentList) {
                fragmentTransaction?.hide(fragment)
            }
            fragmentTransaction?.show(fragmentList[tabNum])
            fragmentTransaction?.commitAllowingStateLoss()
            onTabShow?.invoke(tabNum)
        }
    }

}