package com.example.common.base.bridge

import android.app.Activity

/**
 * Created by WangYanBin on 2020/6/8.
 * 控件操作
 */
interface BaseView {

    /**
     * 构建viewmodel用作数据交互/网络请求
     */
    fun <VM : BaseViewModel> createViewModel(vmClass: Class<VM>): VM

    /**
     * 显示log
     */
    fun log(msg: String)

    /**
     * 刷新动画dialog
     * 如果设置了second，flag可改为true
     */
    fun showDialog(flag: Boolean = false, second: Long = -1L, block: () -> Unit = {})

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