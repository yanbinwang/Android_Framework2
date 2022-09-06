package com.example.common.base.bridge

import android.app.Activity

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
     * 弹出一个倒计时的dialog,默认1秒
     */
    fun showIntercept(second: Long = 1000)

    /**
     * 刷新动画dialog
     */
    fun showDialog(flag: Boolean = false)

    /**
     * 隐藏刷新球控件
     */
    fun hideDialog()

    /**
     * 路由跳转
     * params->页面参数类，跳转的参数，刷新页面页数操作
     */
    fun navigation(path: String, vararg params: Pair<String, Any?>?): Activity

}