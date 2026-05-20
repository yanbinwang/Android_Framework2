package com.example.glide.callback

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 直接使用 CoroutineScope().launch：
 * 在非生命周期绑定的场景（如工具类、抽象类），使用轻量级的临时协程作用域（无父 Job）是安全的，因为：
 * 协程执行完毕后自动释放资源，无内存泄漏风险。
 * Glide 的回调是单次触发（非长耗时任务），无需复杂的作用域管理
 */
abstract class GlideRequestListener<R> : RequestListener<R> {
    // 协程作用域
    private val scope by lazy { CoroutineScope(SupervisorJob() + Main.immediate) }

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