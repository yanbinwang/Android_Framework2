package com.example.common.base.bridge

import android.app.Activity
import android.view.View
import com.example.common.base.page.PageParams

/**
 * Created by WangYanBin on 2020/6/8.
 * 控件操作
 */
interface BaseView {

    /**
     * 显示log
     */
    fun log(msg: String)

    /**
     * Toast显示
     */
    fun showToast(msg: String)

    /**
     * 显示刷新球动画
     */
    fun showDialog()

    /**
     * 不能点击关闭的dialog
     */
    fun showDialog(isClose: Boolean)

    /**
     * 隐藏刷新球控件
     */
    fun hideDialog()

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

    /**
     * 路由跳转
     */
    fun navigation(path: String): Activity

    /**
     * 路由跳转,带参数
     */
    fun navigation(path: String, params: PageParams): Activity

}