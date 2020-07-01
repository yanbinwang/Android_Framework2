package com.example.common.base.page

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面页数操作
 */
class Page {
    private var index: Int = 1
    private var hasNextPage: Boolean? = false

    //刷新清空
    fun onRefresh() {
        index = 1
    }

    //设置是否需要加载更多
    fun hasNextPage(hasNextPage: Boolean?) {
        this.hasNextPage = hasNextPage
    }

    //获取当前的数组长度
    fun getIndex(): Int {
        return index
    }

    //加载更多
    fun onLoad(): Boolean {
        return if (hasNextPage!!) {
            ++index
            true
        } else {
            false
        }
    }

//    //刷新清空
//    fun onRefresh() {
//        currentCount = 0
//        totalCount = 0
//    }
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