package com.example.common.bean

import com.example.framework.utils.function.value.orFalse
import java.io.Serializable

/**
 * @description 全局页面跳转
 * @author yan
 */
data class WebBean(private val title: String = "", private val url: String = "", private val light: Boolean = true, private val titleRequired: Boolean = true) : WebBundle() {

//    companion object {
//        /**
//         * navigation(ARouterPath.WebActivity,Extra.BUNDLE_BEAN to WebBean.aboutAs())
//         */
//        fun aboutAs(): WebBean {
//            return WebBean()
//        }
//    }

    override fun getLight() = light

    override fun getTitleRequired() = titleRequired

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

    /**
     * 如果需要标题头，并且未传默认标题
     */
    fun isTitleRequired(): Boolean {
        return getTitleRequired().orFalse && getTitle().isEmpty()
    }

}