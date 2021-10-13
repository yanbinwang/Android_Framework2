package com.example.common.base.page

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
/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.doResponse(){
    var strTemp = this
    val context = BaseApplication.instance?.applicationContext!!
    if (TextUtils.isEmpty(strTemp)) strTemp = context.getString(R.string.label_response_error)
    mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_error) else strTemp!!, context)
}

/**
 * 页面工具类
 * 1.接口提示
 * 2.遮罩层操作
 */
@JvmOverloads
fun ViewGroup.setState(msg: String?, imgRes: Int = -1, text: String? = null){
    val emptyLayout = if (this is EmptyLayout) this else getEmptyView()
    msg.doResponse()
    emptyLayout.apply {
        visibility = View.VISIBLE
        showError(imgRes, text)
    }
}

/**
 * 列表页调取方法
 */
@JvmOverloads
fun XRecyclerView.setState(msg: String?, length: Int = 0, imgRes: Int = -1, text: String? = null) {
    finishRefreshing()
    //判断集合长度，有长度不展示emptyview只做提示
    if (length > 0) msg.doResponse() else setState(msg, imgRes, text)
}

/**
 * 详情页
 */
fun ViewGroup.getEmptyView(): EmptyLayout {
    val emptyLayout: EmptyLayout?
    if (childCount <= 1) {
        emptyLayout = EmptyLayout(context)
        emptyLayout.apply {
            drawView()
            showLoading()
        }
        addView(emptyLayout)
    } else emptyLayout = getChildAt(1) as EmptyLayout
    return emptyLayout
}

/**
 * 列表页
 */
fun XRecyclerView.getEmptyView() = emptyView

//object PageHandler {
//    /**
//     * 提示方法，根据接口返回的msg提示
//     */
//    @JvmStatic
//    fun doResponse(msg: String?) {
//        var str = msg
//        val context = BaseApplication.instance?.applicationContext!!
//        if (TextUtils.isEmpty(str)) str = context.getString(R.string.label_response_error)
//        mackToastSHORT(if (!isNetworkAvailable()) context.getString(R.string.label_response_net_error) else str!!, context)
//    }
//
//    /**
//     * 详情页调取方法
//     */
//    @JvmOverloads
//    @JvmStatic
//    fun setState(container: ViewGroup, msg: String?, imgRes: Int = -1, text: String? = null) {
//        val emptyLayout = if (container is EmptyLayout) container else getEmptyView(container)
//        doResponse(msg)
//        emptyLayout.apply {
//            visibility = View.VISIBLE
//            showError(imgRes, text)
//        }
//    }
//
//    /**
//     * 列表页调取方法
//     */
//    @JvmOverloads
//    @JvmStatic
//    fun setState(xRecyclerView: XRecyclerView, msg: String?, length: Int = 0, imgRes: Int = -1, text: String? = null) {
//        xRecyclerView.finishRefreshing()
//        val emptyLayout = getEmptyView(xRecyclerView)
//        //判断集合长度，有长度不展示emptyview只做提示
//        if (length > 0) doResponse(msg) else setState(emptyLayout, msg, imgRes, text)
//    }
//
//    /**
//     * 详情页
//     */
//    @JvmStatic
//    fun getEmptyView(container: ViewGroup): EmptyLayout {
//        val emptyLayout: EmptyLayout?
//        if (container.childCount <= 1) {
//            emptyLayout = EmptyLayout(container.context)
//            emptyLayout.apply {
//                draw()
//                showLoading()
//            }
//            container.addView(emptyLayout)
//        } else emptyLayout = container.getChildAt(1) as EmptyLayout
//        return emptyLayout
//    }
//
//    /**
//     * 列表页
//     */
//    @JvmStatic
//    fun getEmptyView(xRecyclerView: XRecyclerView): EmptyLayout {
//        return xRecyclerView.emptyView
//    }
//
//}