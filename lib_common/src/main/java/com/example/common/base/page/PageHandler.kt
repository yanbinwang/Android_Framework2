package com.example.common.base.page

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.widget.empty.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.base.utils.ToastUtil.mackToastSHORT

/**
 * 页面工具类
 * 1.接口提示
 * 2.遮罩层操作
 */
object PageHandler {
    private var emptyLayout: EmptyLayout? = null
    private var xRecyclerView: XRecyclerView? = null

    /**
     * 初始化方法
     * 详情页
     */
    @JvmStatic
    fun initialize(container: ViewGroup) {
        this.emptyLayout = EmptyLayout(container.context)
        if (container.childCount <= 1) {
            emptyLayout?.draw()
            emptyLayout?.showLoading()
            container.addView(emptyLayout)
        }
    }

    /**
     * 初始化方法
     * 列表页
     */
    @JvmStatic
    fun initialize(xRecyclerView: XRecyclerView) {
        this.xRecyclerView = xRecyclerView
        this.emptyLayout = xRecyclerView.emptyView
    }

    /**
     * 详情页调取方法
     */
    @JvmStatic
    fun setEmptyState(msg: String?) {
        setEmptyState(msg, -1, null)
    }

    @JvmStatic
    fun setEmptyState(msg: String?, imgRes: Int, emptyText: String?) {
        doResponse(msg)
        emptyLayout?.visibility = View.VISIBLE
        if (!isNetworkAvailable()) {
            showError()
        } else {
            showEmpty(imgRes, emptyText)
        }
    }

    /**
     * 列表页调取方法
     */
    @JvmStatic
    fun setListEmptyState(refresh: Boolean, msg: String?, length: Int) {
        setListEmptyState(refresh, msg, length, -1, null)
    }

    @JvmStatic
    fun setListEmptyState(refresh: Boolean, msg: String?, length: Int, imgRes: Int, emptyText: String?) {
        xRecyclerView?.finishRefreshing()
        //区分此次刷新是否成功
        if (refresh) {
            emptyLayout?.visibility = View.GONE
        } else {
            if (length > 0) {
                doResponse(msg)
                return
            }
            setEmptyState(msg, imgRes, emptyText)
        }
    }

    /**
     * 提示方法，根据接口返回的msg提示
     */
    @JvmStatic
    fun doResponse(msg: String?) {
        var str = msg
        val context = BaseApplication.instance?.applicationContext!!
        if (TextUtils.isEmpty(str)) {
            str = context.getString(R.string.label_response_err)
        }
        mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_err) else str!!, context)
    }

    @JvmStatic
    fun hideEmpty() {
        emptyLayout?.visibility = View.GONE
    }

    @JvmStatic
    fun showLoading() {
        emptyLayout?.showLoading()
    }

    @JvmStatic
    fun showEmpty(resId: Int = -1, emptyText: String? = "") {
        emptyLayout?.showEmpty(resId, emptyText)
    }

    @JvmStatic
    fun showError() {
        emptyLayout?.showError()
    }

//    @JvmStatic
//    fun doResponse(msg: String?) {
//        var str = msg
//        val context = BaseApplication.instance?.applicationContext!!
//        if (TextUtils.isEmpty(str)) {
//            str = context.getString(R.string.label_response_err)
//        }
//        mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_err) else str!!, context)
//    }
//
//    @JvmStatic
//    fun setEmptyState(emptyLayout: EmptyLayout, msg: String?) {
//        setEmptyState(emptyLayout, msg, -1, null)
//    }
//
//    @JvmStatic
//    fun setEmptyState(emptyLayout: EmptyLayout, msg: String?, imgRes: Int, emptyText: String?) {
//        doResponse(msg)
//        emptyLayout.visibility = View.VISIBLE
//        if (!isNetworkAvailable()) {
//            emptyLayout.showError()
//        } else {
//            emptyLayout.showEmpty(imgRes, emptyText)
//        }
//    }
//
//    @JvmStatic
//    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int) {
//        setListEmptyState(xRecyclerView, refresh, msg, length, -1, null)
//    }
//
//    @JvmStatic
//    fun setListEmptyState(xRecyclerView: XRecyclerView, refresh: Boolean, msg: String?, length: Int, imgRes: Int, emptyText: String?) {
//        val emptyLayout = xRecyclerView.emptyView
//        xRecyclerView.finishRefreshing()
//        //区分此次刷新是否成功
//        if (refresh) {
//            emptyLayout.visibility = View.GONE
//        } else {
//            if (length > 0) {
//                doResponse(msg)
//                return
//            }
//            setEmptyState(emptyLayout, msg, imgRes, emptyText)
//        }
//    }

}