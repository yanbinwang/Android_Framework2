package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.exception.NoRouteFoundException
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.service.InterceptorService
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.base.page.Extra.RESULT_CODE
import com.example.common.utils.manager.AppManager
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.toBundle

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
 * 1. makeCustomAnimation
 * 效果：借助自定义的动画资源，达成 Activity 切换时的过渡效果。
 * 用法：
 * // 从activityA跳转到activityB时使用自定义动画
 * Intent intent = new Intent(activityA, activityB);
 * ActivityOptions options = ActivityOptions.makeCustomAnimation(
 *     activityA,
 *     R.anim.slide_in_right,  // 进入动画
 *     R.anim.slide_out_left   // 退出动画
 * );
 * ActivityCompat.startActivity(activityA, intent, options.toBundle());
 * 实现效果：新 Activity 从右侧滑入，旧 Activity 向左侧滑出。
 *
 * 2. makeScaleUpAnimation
 * 效果：新 Activity 从特定位置开始，进行缩放和淡入操作。
 * 用法：
 * // 从坐标(startX, startY)处以初始尺寸(startWidth, startHeight)开始缩放
 * ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
 *     view,           // 动画起始的视图
 *     startX,         // X轴起始坐标
 *     startY,         // Y轴起始坐标
 *     startWidth,     // 初始宽度
 *     startHeight     // 初始高度
 * );
 * 实现效果：新 Activity 从指定点开始，逐渐放大到全屏
 *
 * 3. makeThumbnailScaleUpAnimation
 * 效果：以缩略图为基础，实现 Activity 的缩放过渡效果。
 * 用法：
 * // 共享缩略图的缩放动画
 * Bitmap thumbnail = getThumbnailBitmap(); // 获取缩略图
 * ActivityOptions options = ActivityOptions.makeThumbnailScaleUpAnimation(
 *     sourceView,     // 源视图
 *     thumbnail,      // 缩略图
 *     startX,         // 起始X坐标
 *     startY          // 起始Y坐标
 * );
 * 实现效果：新 Activity 从缩略图位置开始，逐步放大到全屏。
 *
 * 4. makeSceneTransitionAnimation
 * 效果：实现共享元素在不同 Activity 之间的平滑过渡。
 * 用法：
 * // 共享元素的场景过渡动画
 * Intent intent = new Intent(this, DetailActivity.class);
 * ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
 *     this,
 *     Pair.create(view1, "shared_element_name1"), // 共享元素1
 *     Pair.create(view2, "shared_element_name2")  // 共享元素2
 * );
 * ActivityCompat.startActivity(this, intent, options.toBundle());
 * 实现效果：共享元素在 Activity 切换时保持视觉连贯性，仿佛是同一个元素在移动或变换。
 *
 * 5. makeClipRevealAnimation
 * 效果：以圆形或矩形的方式，显示新 Activity。
 * 用法：
 * // 从指定位置开始的圆形显示动画
 * ActivityOptions options = ActivityOptions.makeClipRevealAnimation(
 *     targetView,     // 目标视图
 *     startX,         // 起始X坐标
 *     startY,         // 起始Y坐标
 *     width,          // 宽度
 *     height          // 高度
 * );
 * 实现效果：新 Activity 从指定点开始，像水波一样逐渐显示出来。
 *
 * 6. makeTaskLaunchBehind
 * 效果：在当前 Activity 的后面启动新的 Activity 任务。
 * 用法：
 * // 在当前Activity后面启动新任务
 * ActivityOptions options = ActivityOptions.makeTaskLaunchBehind();
 * startActivity(intent, options.toBundle());
 *
 * 7. setLaunchBounds
 * 效果：对 Activity 的启动区域进行限制。
 * 用法：
 * // 设置Activity的启动边界
 * ActivityOptions options = ActivityOptions.makeBasic();
 * options.setLaunchBounds(new Rect(left, top, right, bottom));
 * startActivity(intent, options.toBundle());
 */
fun Activity.navigation(path: String, vararg params: Pair<String, Any?>?, activityResultValue: ActivityResultLauncher<Intent>, options: ActivityOptionsCompat? = null) {
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