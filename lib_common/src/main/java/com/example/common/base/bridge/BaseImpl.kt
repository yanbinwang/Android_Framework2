package com.example.common.base.bridge

import android.view.View

/**
 * Created by WangYanBin on 2020/6/11.
 * 定义基类可以调取的一些方法
 */
interface BaseImpl {

    /**
     * 构建viewmodel用作数据交互/网络请求
     */
    fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM

    /**
     * 初始化状态栏
     */
    fun initImmersionBar(titleDark: Boolean = true, naviTrans: Boolean = false)

    /**
     * 初始化控件
     */
    fun initView()

    /**
     * 初始化事件
     */
    fun initEvent()

    /**
     * 初始化数据
     */
    fun initData()

    /**
     * 对象判空（批量）
     */
    fun isEmpty(vararg objs: Any?): Boolean

    /**
     * 控件不可操作
     */
    fun ENABLED(vararg views: View?, second: Long = 1000)

    /**
     * 控件显示
     */
    fun VISIBLE(vararg views: View?)

    /**
     * 控件隐藏（占位）
     */
    fun INVISIBLE(vararg views: View?)

    /**
     * 控件隐藏（不占位）
     */
    fun GONE(vararg views: View?)

}