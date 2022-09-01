package com.example.common.base.bridge

import android.view.View
import com.example.common.base.proxy.SimpleTextWatcher

/**
 * Created by WangYanBin on 2020/6/11.
 * 定义基类可以调取的一些方法
 */
interface BaseImpl {

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
     * 批量注入输入监听
     */
    fun onTextChanged(simpleTextWatcher: SimpleTextWatcher?, vararg views: View?)

    /**
     * 批量注入点击事件
     */
    fun onClick(onClickListener: View.OnClickListener?, vararg views: View?)

    /**
     * 控件不可操作
     */
    fun ENABLED(second: Long = 1000, vararg views: View?)

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