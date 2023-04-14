package com.example.common.base.page

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
 *  launch({
 *      EvidenceSubscribe.getEvidenceListApi(hashMapOf(
 *              "evidenceType" to evidenceType,
 *              "type" to "1",
 *              "current" to paging.page,
 *"             limit" to Paging.pageLimit).params())
 *  }, {
 *      paging.currentCount = it?.total.orZero
 *      reset(it?.hasNextPage)
 *      evidenceData.postValue(it)
 *  }, {
 *      recyclerView?.setState(paging.currentCount.orZero)
 *  }, isShowDialog = false)}
 */
class Paging {
    var hasRefresh = false//是否刷新
    var page = 1//当前页数
    var currentCount = 0//当前页面列表数据总数
    var totalCount = 0//服务器列表数据总数

    //是否需要加载更多
    fun hasNextPage(): Boolean {
        return currentCount < totalCount
    }

    //刷新清空
    fun onRefresh(onConvert: () -> Unit = {}) {
        hasRefresh = true
        page = 1
        onConvert.invoke()
    }

    //加载更多
    fun onLoad(onConvert: (noMore: Boolean) -> Unit = {}) {
        if (hasNextPage()) {
            hasRefresh = false
            ++page
            onConvert(false)
        } else onConvert(true)
    }

}