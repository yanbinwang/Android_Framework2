package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.exception.NoRouteFoundException
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.service.InterceptorService
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.base.page.Extra.BUNDLE_OPTIONS
import com.example.common.base.page.Extra.RESULT_CODE
import com.example.common.utils.function.getCustomOption
import com.example.common.utils.manager.AppManager
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.value.toBundle

/**
 * 列表页调取方法
 */
fun XRecyclerView?.setState(length: Int = 0, imgRes: Int? = null, text: String? = null) {
    this ?: return
    finishRefreshing()
    //判断集合长度，有长度不展示emptyview只做提示
    if (length <= 0) empty.setEmptyState(imgRes, text)
}

/**
 * 页面工具类
 * 1.接口提示
 * 2.遮罩层操作
 */
fun ViewGroup?.setEmptyState(resId: Int? = null, text: String? = null, index: Int = 1) {
    this ?: return
    val emptyLayout = if (this is EmptyLayout) this else getEmptyView(index)
    emptyLayout?.error(resId, text)
}

/**
 * 详情页
 */
fun ViewGroup?.getEmptyView(index: Int = 1): EmptyLayout? {
    this ?: return null
    return if (childCount <= 1) {
        val empty = EmptyLayout(context).apply {
            onInflate()
//            loading()
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
fun Activity.navigation(path: String, vararg params: Pair<String, Any?>?, activityResultValue: ActivityResultLauncher<Intent>, options: ActivityOptionsCompat? = null) {
    //构建arouter跳转
    val postcard = ARouter.getInstance().build(path)
    //获取一下要跳转的页面及class
    val clazz = postcard.getPostcardClass(this) ?: return
    val intent = Intent(this, clazz)
    //检查目标页面是否已经在任务栈中，在的话直接拉起来
    if (AppManager.isActivityAlive(clazz)) {
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
    //标记是否有动画配置
    if (null != options) {
        intent.putExtra(BUNDLE_OPTIONS, true)
    }
    //获取一下拦截器
    postcard.navigateWithInterceptors({
        //检查 Activity 是否存活
        if (!isFinishing && !isDestroyed) {
            //跳转对应页面
            if (!hasResultCode) {
                startActivity(intent, options?.toBundle())
            } else {
                activityResultValue.launch(intent, options)
            }
        }
    }, {
        it?.printStackTrace()
    })
}

/**
 * 获取arouter构建的class文件
 * Postcard 仅在 ARouter.build(path) 后短期调用、用完即释放，不存在上下文（Context）被长期引用的场景
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

fun String.getPostcardClass(): Class<*>? {
    val context = BaseApplication.instance.applicationContext
    return context.getPostcardClass(this)
}

/**
 * 获取arouter构建的拦截器
 */
fun Postcard.navigateWithInterceptors(onContinue: () -> Unit, onInterrupt: (Throwable?) -> Unit) {
    val interceptorService = ARouter.getInstance().navigation(InterceptorService::class.java)
    interceptorService.doInterceptions(this, object : InterceptorCallback {
        override fun onContinue(postcard: Postcard) {
            onContinue()
        }

        override fun onInterrupt(exception: Throwable?) {
            onInterrupt(exception)
        }
    })
}

/**
 * 默认透明动画配置
 */
fun Context?.getFadeOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_alpha_in, R.anim.set_alpha_out)
}

/**
 * 默认方向动画配置
 */
fun Context?.getSlideOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_translate_bottom_in, R.anim.set_translate_bottom_out)
}

/**
 * 页面如果在栈底,跳转拉起新页面的时候采用当前配置,过渡掉系统动画
 */
fun FragmentActivity?.getFadePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getFadeOptions().apply {
        schedule(this@getFadePreview, {
            finish()
        }, 500)
    }
}

fun FragmentActivity?.getSlidePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getSlideOptions().apply {
        schedule(this@getSlidePreview, {
            finish()
        }, 500)
    }
}