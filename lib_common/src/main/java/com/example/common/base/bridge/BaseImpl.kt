package com.example.common.base.bridge

/**
 * Created by WangYanBin on 2020/6/11.
 */
interface BaseImpl {

    /**
     * 初始化控件绑定
     */
    fun initDataBinding()

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

}