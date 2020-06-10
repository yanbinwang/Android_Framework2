package com.example.common.base.bridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * author: wyb
 * 基础方法
 */
interface BaseImpl {

    /**
     * 初始化绑定
     */
//    fun initDataBinding()

    fun initDataBinding(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?

    /**
     * 初始化数据模型
     */
    fun initViewModel()

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
     * 赋值-文案
     */
    fun setText(res: Int, str: String?)

    /**
     * 赋值-颜色
     */
    fun setTextColor(res: Int, color: Int)

    /**
     * 虚拟键盘开启
     */
    fun openDecor(view: View?)

    /**
     * 虚拟键盘关闭
     */
    fun closeDecor()

    /**
     * 让一个view获得焦点
     */
    fun setViewFocus(view: View?)

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
