package com.example.common.base.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.exception.NoRouteFoundException
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.R
import com.example.common.config.Extras
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.resString
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import java.io.Serializable

/**
 * 提示方法，根据接口返回的msg提示
 */
fun String?.responseMsg(){
    val strTemp = this
    (if (!isNetworkAvailable()) resString(R.string.label_response_net_error) else { if(strTemp.isNullOrEmpty()) resString(R.string.label_response_error) else strTemp }).shortToast()
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
            onInflateView()
            showLoading()
        }
        addView(emptyLayout)
    } else emptyLayout = getChildAt(1) as EmptyLayout
    return emptyLayout
}

/**
 * 页面跳转的构建
 */
fun Activity.navigation(path: String, vararg params: Pair<String, Any?>?, activityResultValue: ActivityResultLauncher<Intent>? = null) {
    val postcard = ARouter.getInstance().build(path)
    var requestCode: Int? = null
    if (params.isNotEmpty()) {
        for (param in params) {
            val key = param?.first
            val value = param?.second
            val cls = value?.javaClass
            if (key == Extras.REQUEST_CODE) {
                requestCode = value as? Int
                continue
            }
            when {
                value is Parcelable -> postcard.withParcelable(key, value)
                value is Serializable -> postcard.withSerializable(key, value)
                cls == String::class.java -> postcard.withString(key, value as? String)
                cls == Int::class.javaPrimitiveType -> postcard.withInt(key, (value as? Int).orZero)
                cls == Long::class.javaPrimitiveType -> postcard.withLong(key, (value as? Long).orZero)
                cls == Boolean::class.javaPrimitiveType -> postcard.withBoolean(key, (value as? Boolean).orFalse)
                cls == Float::class.javaPrimitiveType -> postcard.withFloat(key, (value as? Float).orZero)
                cls == Double::class.javaPrimitiveType -> postcard.withDouble(key, (value as? Double).orZero)
                cls == CharArray::class.java -> postcard.withCharArray(key, value as? CharArray)
                cls == Bundle::class.java -> postcard.withBundle(key, value as? Bundle)
                else -> throw RuntimeException("不支持参数类型: ${cls?.simpleName}")
            }
        }
    }
    if (requestCode == null) {
        postcard.navigation()
    } else {
        postcard.context = this
        try {
            LogisticsCenter.completion(postcard)
            activityResultValue?.launch(Intent(this, postcard.destination))
        } catch (_: NoRouteFoundException) {
        }
    }
}