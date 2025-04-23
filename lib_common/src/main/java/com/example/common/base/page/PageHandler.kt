package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.exception.NoRouteFoundException
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.page.Extra.RESULT_CODE
import com.example.common.utils.manager.AppManager
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.toBundle
import java.io.Serializable

/**
 * 列表页调取方法
 */
fun XRecyclerView?.setState(length: Int = 0, resId: Int? = null, resText: Int? = null) {
    this ?: return
    finishRefreshing()
    //判断集合长度，有长度不展示emptyview只做提示
    if (length <= 0) empty.setEmptyState(resId, resText)
}

/**
 * 页面工具类
 * 1.接口提示
 * 2.遮罩层操作
 */
fun ViewGroup?.setEmptyState(resId: Int? = null, resText: Int? = null, index: Int = 1) {
    this ?: return
    val emptyLayout = if (this is EmptyLayout) this else getEmptyView(index)
    emptyLayout?.error(resId, resText)
}

/**
 * 详情页
 */
fun ViewGroup?.getEmptyView(index: Int = 1): EmptyLayout? {
    this ?: return null
    return if (childCount <= 1) {
        val empty = EmptyLayout(context).apply {
            onInflate()
            loading()
        }
        addView(empty)
        empty
    } else {
        getChildAt(index) as? EmptyLayout
    }
}

/**
 * 页面跳转的构建
 */
fun Activity.navigation(path: String, vararg params: Pair<String, Any?>?, activityResultValue: ActivityResultLauncher<Intent>) {
    //构建arouter跳转
    val postcard = ARouter.getInstance().build(path)
    //获取一下要跳转的页面及class
    val clazz = postcard.getPostcardClass(this) ?: return
    val intent = Intent(this, clazz)
    //检查目标页面是否已经在任务栈中，在的话直接拉起来
    if (AppManager.isExistActivity(clazz)) {
        //Activity 会调用 onNewIntent 方法来接收新的 Intent，并且它的生命周期方法调用顺序与普通启动 Activity 有所不同，
        //不会调用 onCreate 和 onStart 方法，而是调用 onRestart、onResume 等方法。
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    }
    //判断一下跳转参数
    var hasResultCode = false
    if (params.isNotEmpty()) {
        //过滤掉 null 值
        val nonNullParams = params.filterNotNull()
        hasResultCode = nonNullParams.find { it.first == RESULT_CODE } != null
        //排除 RESULT_CODE 参数，将其他参数添加到 Bundle 中
        val bundle = nonNullParams.filter { it.first != RESULT_CODE }.toBundle { this }
        intent.putExtras(bundle)
    }
    //检查 Activity 是否存活
    if (!isFinishing && !isDestroyed) {
        //跳转对应页面
        if (!hasResultCode) {
            startActivity(intent)
        } else {
            activityResultValue.launch(intent)
        }
    }
}

/**
 * 获取arouter构建的class文件
 */
fun Postcard.getPostcardClass(mContext: Context): Class<*>? {
    context = mContext
    return try {
        LogisticsCenter.completion(this)
        destination
    } catch (e: NoRouteFoundException) {
        e.printStackTrace()
        null
    }
}

fun Context.getPostcardClass(path: String): Class<*>? {
    return ARouter.getInstance().build(path).getPostcardClass(this)
}