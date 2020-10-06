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
     * 刷新动画dialog
     */
    fun showDialog(flag: Boolean = false)

    /**
     * 隐藏刷新球控件
     */
    fun hideDialog()

    /**
     * 路由跳转
     */
    fun navigation(path: String, params: PageParams ?= null): Activity

}