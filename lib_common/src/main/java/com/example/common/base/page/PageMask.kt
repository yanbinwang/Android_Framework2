package com.example.common.base.page

import android.text.TextUtils
import android.view.View
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.widget.empty.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.ToastUtil.mackToastSHORT

/**
 * 遮罩层操作
 */
object PageMask {

    @JvmStatic
    fun doResponse(msg: String?) {
        var str = msg
        val context = BaseApplication.getInstance().applicationContext
        if (TextUtils.isEmpty(str)) {
            str = context.getString(R.string.label_response_err)
        }
        mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_err) else str!!, context)
    }

    @JvmStatic
    fun setEmptyState(emptyLayout: EmptyLayout, msg: String?) {
        setEmptyState(emptyLayout, msg, -1, null)
    }

    @JvmStatic
    fun setEmptyState(emptyLayout: EmptyLayout, msg: String?, imgRes: Int, emptyText: String?) {
        doResponse(msg)
        emptyLayout.visibility = View.VISIBLE
        if (!isNetworkAvailable()) {
            emptyLayout.showError()
        } else {
            emptyLayout.showEmpty(imgRes, emptyText)
        }
    }

    @JvmStatic
    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int) {
        setListEmptyState(xRecyclerView, refresh, msg, length, -1, null)
    }

    @JvmStatic
    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int, imgRes: Int, emptyText: String?) {
        val emptyLayout = xRecyclerView.emptyView
        xRecyclerView.setRefreshing(false)
        //区分此次刷新是否成功
        if (refresh) {
            emptyLayout.visibility = View.GONE
        } else {
            if (length > 0) {
                doResponse(msg)
                return
            }
            setEmptyState(emptyLayout, msg, imgRes, emptyText)
        }
    }

}