package com.example.common.base.page

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.example.base.utils.ToastUtil.mackToastSHORT
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.widget.empty.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * 页面工具类
 * 1.接口提示
 * 2.遮罩层操作
 */
object PageHandler {
    /**
     * 详情页调取方法
     */
    fun setEmptyState(container: ViewGroup, msg: String?) {
        setEmptyState(container, msg, -1, null)
    }

    fun setEmptyState(container: ViewGroup, msg: String?, imgRes: Int, emptyText: String?) {
        val emptyLayout = if (container is EmptyLayout) {
            container
        } else {
            container.context.getEmpty(container)
        }
        doResponse(msg)
        emptyLayout.visibility = View.VISIBLE
        if (!isNetworkAvailable()) {
            emptyLayout.showError()
        } else {
            emptyLayout.showEmpty(imgRes, emptyText)
        }
    }

    /**
     * 列表页调取方法
     */
    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int) {
        setListEmptyState(xRecyclerView, refresh, msg, length, -1, null)
    }

    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int, imgRes: Int, emptyText: String?) {
        val emptyLayout = xRecyclerView.context.getListEmpty(xRecyclerView)
        xRecyclerView.finishRefreshing()
        //区分此次刷新是否成功
        if (refresh) {
            emptyLayout.visibility = View.GONE
        } else {
            if (length > 0) {
                doResponse(msg)
                return
            }
            setEmptyState(xRecyclerView, msg, imgRes, emptyText)
        }
    }

//    /**
//     * 详情页
//     */
//    fun getEmpty(container: ViewGroup): EmptyLayout {
//        val emptyLayout: EmptyLayout?
//        if (container.childCount <= 1) {
//            emptyLayout = EmptyLayout(container.context)
//            emptyLayout.draw()
//            emptyLayout.showLoading()
//            container.addView(emptyLayout)
//        } else {
//            emptyLayout = container.getChildAt(1) as EmptyLayout
//        }
//        return emptyLayout
//    }
//
//    /**
//     * 列表页
//     */
//    fun getListEmpty(xRecyclerView: XRecyclerView): EmptyLayout {
//        return xRecyclerView.emptyView
//    }

    /**
     * 提示方法，根据接口返回的msg提示
     */
    fun doResponse(msg: String?) {
        var str = msg
        val context = BaseApplication.instance?.applicationContext!!
        if (TextUtils.isEmpty(str)) {
            str = context.getString(R.string.label_response_err)
        }
        mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_err) else str!!, context)
    }

}

/**
 * 详情页
 */
fun Context.getEmpty(container: ViewGroup): EmptyLayout {
    val emptyLayout: EmptyLayout?
    if (container.childCount <= 1) {
        emptyLayout = EmptyLayout(container.context)
        emptyLayout.draw()
        emptyLayout.showLoading()
        container.addView(emptyLayout)
    } else {
        emptyLayout = container.getChildAt(1) as EmptyLayout
    }
    return emptyLayout
}

/**
 * 列表页
 */
fun Context.getListEmpty(xRecyclerView: XRecyclerView): EmptyLayout {
    return xRecyclerView.emptyView
}