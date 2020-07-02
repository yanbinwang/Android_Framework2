package com.example.common.base.page

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面页数操作
 */
class PageList {
    private var index: Int = 1//当前页数
    private var hasRefresh: Boolean? = false//是否刷新
    private var hasNextPage: Boolean? = false//是否有下一页

    //刷新清空
    fun onRefresh() {
        hasRefresh = true
        index = 1
    }

    //设置是否需要加载更多
    fun hasNextPage(hasNextPage: Boolean?) {
        this.hasNextPage = hasNextPage
    }

    fun hasRefresh(): Boolean {
        return hasRefresh!!
    }

    //获取当前的数组长度
    fun getIndex(): Int {
        return index
    }

    //加载更多
    fun onLoad(): Boolean {
        return if (hasNextPage!!) {
            hasRefresh = false
            ++index
            true
        } else {
            false
        }
    }

}