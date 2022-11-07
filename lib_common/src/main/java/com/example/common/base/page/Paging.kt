package com.example.common.base.page

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面页数操作
 */
class Paging {
    var hasRefresh = false//是否刷新
    var totalCount = 0//服务器数组总数
    var currentCount = 0//当前页面数组数
    var page = 1//当前页数
    val pageLimit = "10"

    //是否需要加载更多
    fun hasNextPage(): Boolean {
        return currentCount < totalCount
    }

    //刷新清空
    fun onRefresh() {
        hasRefresh = true
        page = 1
    }

    //加载更多
    fun onLoad(): Boolean {
        return if (hasNextPage()) {
            hasRefresh = false
            ++page
            true
        } else false
    }

}