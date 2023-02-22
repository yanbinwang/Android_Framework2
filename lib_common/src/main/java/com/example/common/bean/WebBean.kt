package com.example.common.bean

import java.io.Serializable

/**
 * @description 全局页面跳转
 * @author yan
 */
data class WebBean(private val title: String = "", private val url: String = "", private val isLight: Boolean = true, private val isTitleRequired: Boolean = true) : WebBundle() {

    override fun getLight() = isLight

    override fun getTitleRequired() = isTitleRequired

    override fun getTitle() = title

    override fun getUrl() = url

}

abstract class WebBundle : Serializable {

    /**
     * 黑白电池
     */
    abstract fun getLight(): Boolean

    /**
     * 是否需要标题头
     */
    abstract fun getTitleRequired(): Boolean

    /**
     * 获取页面标题
     */
    abstract fun getTitle(): String

    /**
     *获取页面地址
     */
    abstract fun getUrl(): String

}