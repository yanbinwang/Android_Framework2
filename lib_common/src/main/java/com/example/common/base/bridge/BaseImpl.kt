package com.example.common.base.bridge

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.example.common.R
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.visible

/**
 * Created by WangYanBin on 2020/6/11.
 * 定义基类可以调取的一些方法
 * 只开放给自写的base基类（common模块）
 */
internal interface BaseImpl {

    /**
     * 初始化状态栏
     * activity/fragment具备，其余不可重写
     */
    fun initImmersionBar(titleDark: Boolean = true, naviTrans: Boolean = true, navigationBarColor: Int = R.color.appNavigationBar) {}

    /**
     * 初始化控件
     * savedInstanceState->activity/fragment具备,其余为null
     */
    fun initView(savedInstanceState: Bundle?)

    /**
     * 初始化事件
     */
    fun initEvent()

    /**
     * 初始化数据
     */
    fun initData()

    /**
     * 控件不可操作
     */
    fun enabled(observer: LifecycleOwner, vararg views: View?, second: Long = -1L) {
        if (second <= 0) return
        views.forEach { it.disable() }
        TimerBuilder.schedule(observer, {
            views.forEach { it.enable() }
        }, second)
    }

    /**
     * 控件显示
     */
    fun visible(vararg views: View?) {
        views.forEach { it?.visible() }
    }

    /**
     * 控件隐藏（占位）
     */
    fun invisible(vararg views: View?) {
        views.forEach { it?.invisible() }
    }

    /**
     * 控件隐藏（不占位）
     */
    fun gone(vararg views: View?) {
        views.forEach { it?.gone() }
    }

}