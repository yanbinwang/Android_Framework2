package com.example.common.utils.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
    private var weakActivity: WeakReference<AppCompatActivity>? = null
    var onTabClickListener: OnTabClickListener? = null

    @JvmOverloads
    @JvmStatic
    fun initialize(activity: AppCompatActivity, containerViewId: Int, fragmentList: ArrayList<Fragment>, tabNum: Int = 0, isDark: Boolean = true) {
        this.weakActivity = WeakReference(activity)
        this.containerViewId = containerViewId
        this.fragmentList = fragmentList
        //默认选中下标以及导航栏颜色
        StatusBarBuilder(activity).setTransparent(isDark)
        showFragment(tabNum, true)
    }

    @JvmOverloads
    @JvmStatic
    fun showFragment(tabNum: Int, load: Boolean = false) {
        if (fragmentList.size > tabNum) {
            //commit只能提交一次，所以每次都需要重新实例化
            val fragmentManager = weakActivity?.get()?.supportFragmentManager!!
            val fragmentTransaction = fragmentManager.beginTransaction()
            if (load) {
                for (i in fragmentList.indices) {
                    fragmentTransaction.add(containerViewId, fragmentList[i])
                }
            }
            //全部隱藏后显示指定的页面
            for (fragment in fragmentList) {
                fragmentTransaction.hide(fragment)
            }
            fragmentTransaction.show(fragmentList[tabNum])
            fragmentTransaction.commitAllowingStateLoss()
            onTabClickListener?.onTabClickListener(tabNum)
        }
    }

    interface OnTabClickListener {

        fun onTabClickListener(tabNum: Int)

    }

}