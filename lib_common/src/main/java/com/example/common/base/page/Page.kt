package com.example.common.base.page

/**
 *  Created by wangyanbin
 *  项目中分页统一外层套该类(合并部分接口带有data部分带有list)
 */
data class Page<T>(
    var total: Int? = null,//总记录数
    var list: MutableList<T>? = null
)

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面工具类
 * override fun onRefresh(refreshLayout: RefreshLayout) {
 * viewModel.paging.onRefresh { viewModel.getEvidenceList() }
 * }
 *
 * override fun onLoadMore(refreshLayout: RefreshLayout) {
 * viewModel.paging.onLoad { if (it) binding.xrvEvidence.finishRefreshing() else viewModel.getEvidenceList() }
 * }
 *
 * fun getEvidenceList() {
 *  launch({EvidenceSubscribe.getEvidenceListApi(hashMapOf(
 *              "evidenceType" to evidenceType,
 *              "type" to "1",
 *              "current" to paging.page,
 *"             limit" to Constants.PAGE_LIMIT).params())
 *  }, {
 *      paging.totalCount = it?.total.orZero
 *      evidenceData.postValue(it)//先回调赋值刷新适配器
 *      reset(it?.hasNextPage.orFalse)
 *  }, {
 *      recyclerView?.setState(paging.currentCount.orZero)
 *  }, isShowDialog = false)
 * }
 *
 *  postValue完成后，回调的订阅里赋值一下
 *  binding.adapter.notify(it.list, viewModel.paging.hasRefresh) { viewModel.emptyView?.empty() } or binding.adapter.notify<ViewModel>(it.list, viewModel)
 *  viewModel.paging.currentCount = binding.adapter.size()
 */
class Paging {
    var hasRefresh = false//是否刷新
    var page = 1//当前页数
    var currentCount = 0//当前页面列表数据总数
    var totalCount = 0//服务器列表数据总数

    //刷新清空
    fun onRefresh(listener: () -> Unit = {}) {
        hasRefresh = true
        page = 1
        currentCount = 0
        totalCount = 0
        listener.invoke()
    }

    //加载更多
    fun onLoad(listener: (noMore: Boolean) -> Unit = {}) {
        if (hasNextPage()) {
            hasRefresh = false
            ++page
            listener(false)
        } else listener(true)
    }

    //是否需要加载更多
    private fun hasNextPage(): Boolean {
        return currentCount < totalCount
    }

}