package com.dataqin.common.widget.xrecyclerview.refresh.callback

import com.dataqin.common.widget.xrecyclerview.refresh.SwipeRefreshLayout

/**
 * 重新定义刷新接口
 */
interface OnXRefreshListener : SwipeRefreshLayout.OnRefreshListener {

    override fun onRefresh(index: Int) {
        onRefresh()
    }

    override fun onLoad(index: Int) {
        onLoad()
    }

    fun onRefresh()

    fun onLoad()

}