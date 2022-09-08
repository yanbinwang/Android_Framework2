package com.example.common.base.page

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.example.base.utils.ToastUtil.mackToastSHORT
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseMsg(){
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
fun ViewGroup.setState(imgRes: Int = -1, text: String? = null){
    val emptyLayout = if (this is EmptyLayout) this else getEmptyView()
    emptyLayout.apply {
        visibility = View.VISIBLE
        showError(imgRes, text)
    }
}

/**
 * 列表页调取方法
 */
@JvmOverloads
fun XRecyclerView.setState(length: Int = 0, imgRes: Int = -1, text: String? = null) {
    finishRefreshing()
    //判断集合长度，有长度不展示emptyview只做提示
    if (length <= 0) empty?.setState(imgRes, text)
}

/**
 * 详情页
 */
fun ViewGroup.getEmptyView(): EmptyLayout {
    val emptyLayout: EmptyLayout?
    if (childCount <= 1) {
        emptyLayout = EmptyLayout(context)
        emptyLayout.apply {
            onDrawView()
            showLoading()
        }
        addView(emptyLayout)
    } else emptyLayout = getChildAt(1) as EmptyLayout
    return emptyLayout
}