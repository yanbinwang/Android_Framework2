package com.example.common.base.page

import java.util.*

/**
 * author:wyb
 * 页面参数类，跳转的参数，刷新页面页数操作
 */
class PageParams {
    private var map: MutableMap<String, Any> = HashMap()
//    private var currentCount: Int = 0
//    private var totalCount: Int = 0
//    private var index: Int = 1
//    private var hasNextPage: Boolean? = false

    fun append(key: String, value: Any?): PageParams {
        if (value != null) {
            map[key] = value
        }
        return this
    }

    fun setMap(map: MutableMap<String, Any>): PageParams {
        this.map = map
        return this
    }

    fun getParams():MutableMap<String, Any>{
        return map
    }

//刷新页面的逻辑，对页面page的处理可放在P层实现
//    //刷新清空
//    fun onRefresh() {
//        index = 1
//    }
//
//    fun onLoad(): Boolean {
//        return if (hasNextPage!!) {
//            ++index
//            true
//        } else {
//            false
//        }
//    }
//
//    //设置是否需要加载更多
//    fun hasNextPage(hasNextPage: Boolean?) {
//        this.hasNextPage = hasNextPage
//    }
//
//    //获取当前的数组长度
//    fun getIndex(): Int {
//        return index
//    }
//
//    //刷新清空
//    fun onRefresh() {
//        currentCount = 0
//        totalCount = 0
//    }
//
//    //是否需要加载更多
//    fun hasNextPage(): Boolean {
//        return currentCount < totalCount
//    }
//
//    //获取当前的数组长度
//    fun getCurrentCount(): Int {
//        return currentCount
//    }
//
//    //列加数组长度
//    fun setCurrentCount(currentCount: Int) {
//        this.currentCount += currentCount
//    }
//
//    //设置总数
//    fun setTotalCount(totalCount: Int) {
//        this.totalCount = totalCount
//    }

}
