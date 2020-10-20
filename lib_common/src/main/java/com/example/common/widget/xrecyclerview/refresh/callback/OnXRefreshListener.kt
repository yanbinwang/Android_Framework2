package com.example.common.widget.xrecyclerview.refresh.callback

import com.example.common.widget.xrecyclerview.refresh.SwipeRefreshLayout

/**
 * 重新定义刷新接口
 */
abstract class OnXRefreshListener : SwipeRefreshLayout.OnRefreshListener {

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    final override fun onRefresh(index: Int) {
        onRefresh()
    }

    final override fun onLoad(index: Int) {
        onLoad()
    }
    // </editor-fold>

    open fun onRefresh() {}

    open fun onLoad() {}

}