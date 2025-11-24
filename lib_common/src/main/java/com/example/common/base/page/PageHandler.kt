package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.base.page.Extra.BUNDLE_OPTIONS
import com.example.common.base.page.Extra.RESULT_CODE
import com.example.common.base.page.PageInterceptor.Companion.shouldIntercept
import com.example.common.utils.function.getCustomOption
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.value.toBundle
import com.therouter.TheRouter
import com.therouter.router.Navigator
import com.therouter.router.matchRouteMap


/**
 * 列表页调取方法
 */
fun XRecyclerView?.setState(length: Int = 0, imgRes: Int? = null, text: String? = null) {
    this ?: return
    finishRefreshing()
    // 判断集合长度，有长度不展示EmptyLayout只做提示
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
    // 构建router跳转
    val navigator = TheRouter.build(path)
    // createIntent内部会触发 PageInterceptor 的 process 方法,故而之前先set一个值,process内部做处理
    navigator.withBoolean(Extra.SKIP_INTERCEPT, true)
    val intent = navigator.createIntent(this)
    /**
     * 添加标记 : 检查目标页面是否已经在任务栈中，在的话直接拉起来
     * Activity 会调用 onNewIntent 方法来接收新的 Intent，并且它的生命周期方法调用顺序与普通启动 Activity 有所不同，
     * 不会调用 onCreate 和 onStart 方法，而是调用 onRestart、onResume 等方法。
     */
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    // 判断跳转参数
    var hasResultCode = false
    if (params.isNotEmpty()) {
        // 过滤掉 null 值
        val nonNullParams = params.filterNotNull()
        hasResultCode = nonNullParams.find { it.first == RESULT_CODE } != null
        // 排除 RESULT_CODE 参数，将其他参数添加到 Bundle 中
        val bundle = nonNullParams.filter { it.first != RESULT_CODE }.toBundle { this }
        intent.putExtras(bundle)
    }
    // 标记是否有动画配置
    if (null != options) {
        intent.putExtra(BUNDLE_OPTIONS, true)
    }
    // 获取一下拦截器
    navigator.navigateWithInterceptors({
        // 检查 Activity 是否存活
        if (!isFinishing && !isDestroyed) {
            // 跳转对应页面
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
 * 获取Router构建的class文件
 * Navigator 仅在 TheRouter.build(path) 后短期调用、用完即释放，不存在上下文（Context）被长期引用的场景
 */
fun Navigator.getDestinationClass(): Class<*>? {
    return try {
        // 使用 TheRouter.matchRouteMap() 查找 RouteItem
        val routeItem = matchRouteMap(url)
        // 从 RouteItem 中获取目标类的完整名称字符串
        val className = routeItem?.className ?: return null
        // 使用 Java 反射，将类名字符串转换为 Class 对象
        val targetClass = Class.forName(className)
        // 返回对应的类
        targetClass
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun String.getDestinationClass(): Class<*>? {
    return TheRouter.build(this).getDestinationClass()
}

/**
 * 获取Router构建的拦截器
 */
fun Navigator.navigateWithInterceptors(onContinue: () -> Unit, onInterrupt: (Throwable?) -> Unit) {
    try {
        // 匹配路由（复用 Navigator 自身的匹配逻辑）
        val routeItem = matchRouteMap(url)
        // 调用全局拦截器的 shouldIntercept，确保规则统一
        val isIntercepted = shouldIntercept(routeItem) { throwable ->
            // 异常回调（比如路由参数配置错误）
            throw throwable
        }
        // 根据拦截结果触发对应回调
        if (!isIntercepted) {
            onContinue()
        }
    } catch (e: Exception) {
        // 捕获其他意外异常（比如 matchRouteMap 失败）
        onInterrupt(e)
    }
}

/**
 * 透明动画
 */
fun Context?.getNoneOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_alpha_none, R.anim.set_alpha_none)
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
fun FragmentActivity?.getNonePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getNoneOptions().apply {
        schedule(this@getNonePreview, {
            finish()
        }, 500)
    }
}

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