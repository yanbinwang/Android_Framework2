package com.example.common.base.page

import com.example.framework.utils.function.value.orZero

/**
 *  Created by wangyanbin
 *  项目中分页统一外层套该类(合并部分接口带有data部分带有list)
 */
data class Page<T>(
    var total: Int? = null,//总记录数
    var list: MutableList<T>? = null
)

/**
 * 提供两种转换思路，嵌套一层得到我们想要的page对象
 */
class PageList<T>(private val list: List<T>) : List<T> by list {
    private var total = 0

    fun setTotal(total: Int?) {
        this.total = total.orZero
    }

    fun getPage(): Page<T> {
        return Page<T>().also {
            it.list = list as? MutableList<T>
            it.total = total
        }
    }
}

fun <T> List<T>?.getPage(total: Int?): Page<T>? {
    if (isNullOrEmpty()) return null
    return Page<T>().also {
        it.list = this as? MutableList<T>
        it.total = total
    }
}

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面工具类
 * override fun onRefresh(refreshLayout: RefreshLayout) {
 * viewModel.onRefresh { viewModel.getEvidenceList() }
 * }
 *
 * override fun onLoadMore(refreshLayout: RefreshLayout) {
 * viewModel.onLoad { if (it) binding.xrvEvidence.finishRefreshing() else viewModel.getEvidenceList() }
 * }
 *
 * fun getEvidenceList() {
 *  launch({EvidenceSubscribe.getEvidenceListApi(hashMapOf(
 *              "evidenceType" to evidenceType,
 *              "type" to "1",
 *              "current" to page,
 *"             limit" to Constants.PAGE_LIMIT).params())
 *  }, {
 *      setTotalCount(it?.total)
 *      evidenceData.postValue(it)//先回调赋值刷新适配器
 *  }, {
 *      onError()
 *      recyclerView?.setState(currentCount)
 *  }, isShowDialog = false)
 * }
 *
 *  postValue完成后，回调的订阅里赋值一下
 *  binding.adapter.notify(it.list, viewModel.hasRefresh) { viewModel.emptyView?.empty() } or binding.adapter.notify<ViewModel>(it.list, viewModel)
 */
class Paging {
    var hasRefresh = false//是否刷新
    var currentCount = 0//当前页面列表数据总数
    var totalCount = 0//服务器列表数据总数
    var page = 1//当前页数

    /**
     * 刷新清空
     */
    inline fun onRefresh(crossinline listener: () -> Unit = {}) {
        hasRefresh = true
        page = 1
        currentCount = 0
        totalCount = 0
        listener.invoke()
    }

    /**
     * 加载更多
     */
    inline fun onLoad(crossinline listener: (noMore: Boolean) -> Unit = {}) {
        if (hasNextPage()) {
            hasRefresh = false
            ++page
            listener(false)
        } else {
            listener(true)
        }
    }

    /**
     * 此次请求失败
     */
    fun onError() {
        page--
        if (page <= 0) page = 1
    }

    /**
     * 是否需要加载更多
     */
    fun hasNextPage(): Boolean {
        return currentCount < totalCount
    }

}