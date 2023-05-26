package com.example.common.base.page

/**
 *  Created by wangyanbin
 *  项目中分页统一外层套该类(合并部分接口带有data部分带有list)
 */
data class Page<T>(
    var total: Int? = null,//总记录数
    var hasNextPage: Boolean? = null,//是否有下一页（是否有更多数据）
    var list: MutableList<T>? = null
)

/**
 * Created by WangYanBin on 2020/7/1.
 * 应用于刷新页面工具类
 * override fun onRefresh(refreshLayout: RefreshLayout) {
 * paging.onRefresh { viewModel.getEvidenceList(paging) }
 * }
 *
 * override fun onLoadMore(refreshLayout: RefreshLayout) {
 * paging.onLoad { if (it) binding.xrvEvidence.finishRefreshing() else viewModel.getEvidenceList(paging) }
 * }
 *
 *fun getEvidenceList(paging: Paging) {
 *  launch({EvidenceSubscribe.getEvidenceListApi(hashMapOf(
 *              "evidenceType" to evidenceType,
 *              "type" to "1",
 *              "current" to paging.page,
 *"             limit" to Constants.PAGE_LIMIT).params())
 *  }, {
 *      paging.totalCount = it?.total.orZero
 *      reset(it?.hasNextPage.orFalse)
 *      evidenceData.postValue(it)
 *  }, {
 *      recyclerView?.setState(paging.currentCount.orZero)
 *  }, isShowDialog = false)}
 *
 *  postValue完成后，回调的订阅里赋值一下
 *  paging.currentCount = binding.adapter.size()
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