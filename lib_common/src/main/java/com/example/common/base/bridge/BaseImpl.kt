package com.example.common.base.bridge

import android.os.Bundle
import android.view.View
import com.example.common.R

/**
 * Created by WangYanBin on 2020/6/11.
 * 定义基类可以调取的一些方法
 */
interface BaseImpl {

    /**
     * 构建viewmodel用作数据交互/网络请求
     */
    fun <VM : BaseViewModel> VM.create(): VM?

    /**
     * 初始化状态栏
     */
    fun initImmersionBar(titleDark: Boolean = true, naviTrans: Boolean = true, navigationBarColor: Int = R.color.appPrimaryDark)

    /**
     * 初始化控件
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
    fun enabled(vararg views: View?, second: Long = 1000)

    /**
     * 控件显示
     */
    fun visible(vararg views: View?)

    /**
     * 控件隐藏（占位）
     */
    fun invisible(vararg views: View?)

    /**
     * 控件隐藏（不占位）
     */
    fun gone(vararg views: View?)

}