package com.example.glide.callback

import android.os.Looper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.framework.utils.WeakHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by WangYanBin on 2020/7/31.
 * 图片下载监听
 */
//abstract class GlideRequestListener<R> : RequestListener<R> {
//    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
//
//    init {
//        weakHandler.post {
//            onLoadStart()
//        }
//    }
//
//    /**
//     * 加载成功
//     */
//    override fun onResourceReady(resource: R & Any, model: Any, target: Target<R>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
//        handleLoadResult(resource)
//        /**
//         * true->表示已经处理好资源，不让 Glide 继续默认流程
//         * false->让 Glide 继续默认的显示流程
//         */
//        return false
//    }
//
//    /**
//     * 加载失败
//     */
//    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>, isFirstResource: Boolean): Boolean {
//        e?.printStackTrace()
//        handleLoadResult(null)
//        return false
//    }
//
//    private fun handleLoadResult(resource: R?) {
//        weakHandler.post {
//            onLoadFinished(resource)
//        }
//    }
//
//    /**
//     * 开始加载
//     */
//    protected abstract fun onLoadStart()
//
//    /**
//     * 完成加载，成功的情况下R是必定有值的
//     */
//    protected abstract fun onLoadFinished(resource: R?)
//
//}
abstract class GlideRequestListener<R> : RequestListener<R> {
    // 协程作用域
    private val scope by lazy { CoroutineScope(Main.immediate) }

    init {
        scope.launch {
            onLoadStart()
        }
    }

    /**
     * 加载成功
     */
    override fun onResourceReady(resource: R & Any, model: Any, target: Target<R>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
        handleLoadResult(resource)
        /**
         * true->表示已经处理好资源，不让 Glide 继续默认流程
         * false->让 Glide 继续默认的显示流程
         */
        return false
    }

    /**
     * 加载失败
     */
    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<R>, isFirstResource: Boolean): Boolean {
        e?.printStackTrace()
        handleLoadResult(null)
        return false
    }

    private fun handleLoadResult(resource: R?) {
        scope.launch {
            onLoadFinished(resource)
        }
    }

    /**
     * 开始加载（在主线程调用）
     */
    protected abstract fun onLoadStart()

    /**
     * 完成加载（在主线程调用）
     */
    protected abstract fun onLoadFinished(resource: R?)

}